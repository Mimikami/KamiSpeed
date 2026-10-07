#include "elf_util.h"

#include <dlfcn.h>
#include <elf.h>
#include <link.h>
#include <stdio.h>
#include <string.h>
#include <sys/mman.h>
#include <unistd.h>

#if defined(__LP64__)
#define ELF_R_SYM(i)  ELF64_R_SYM(i)
#define ELF_R_TYPE(i) ELF64_R_TYPE(i)
typedef Elf64_Sym  elf_sym_t;
typedef Elf64_Rela elf_rela_t;
typedef Elf64_Rel  elf_rel_t;
typedef Elf64_Dyn  elf_dyn_t;
#else
#define ELF_R_SYM(i)  ELF32_R_SYM(i)
#define ELF_R_TYPE(i) ELF32_R_TYPE(i)
typedef Elf32_Sym  elf_sym_t;
typedef Elf32_Rela elf_rela_t;
typedef Elf32_Rel  elf_rel_t;
typedef Elf32_Dyn  elf_dyn_t;
#endif

#if defined(__aarch64__)
#define R_JUMP_SLOT R_AARCH64_JUMP_SLOT
#define R_GLOB_DAT  R_AARCH64_GLOB_DAT
#elif defined(__arm__)
#define R_JUMP_SLOT R_ARM_JUMP_SLOT
#define R_GLOB_DAT  R_ARM_GLOB_DAT
#elif defined(__x86_64__)
#define R_JUMP_SLOT R_X86_64_JUMP_SLOT
#define R_GLOB_DAT  R_X86_64_GLOB_DAT
#elif defined(__i386__)
#define R_JUMP_SLOT R_386_JMP_SLOT
#define R_GLOB_DAT  R_386_GLOB_DAT
#else
#define R_JUMP_SLOT 0
#define R_GLOB_DAT 0
#endif

typedef struct {
    hook_sym_t *syms;
    int         nsyms;
    const char *only_path; /* NULL = 全部模块 */
    int         patched;
    /* 当前模块信息：写槽前校验地址落在 PT_LOAD 段内，防止误解析动态段后写坏内存 */
    const ElfW(Phdr) *phdr;
    int         phnum;
    uintptr_t   base;
} hook_ctx_t;

/* ------------------------------------------------------------------ */

static int in_load_segment(hook_ctx_t *ctx, uintptr_t addr) {
    if (ctx->phdr == NULL) return 1;
    for (int i = 0; i < ctx->phnum; i++) {
        if (ctx->phdr[i].p_type != PT_LOAD) continue;
        uintptr_t start = ctx->base + ctx->phdr[i].p_vaddr;
        uintptr_t end   = start + ctx->phdr[i].p_memsz;
        if (addr >= start && addr < end) return 1;
    }
    return 0;
}

static int patch_slot(uintptr_t slot_addr, void *new_func, void **old_func) {
    void **slot = (void **) slot_addr;
    if (slot == NULL || new_func == NULL) return 0;
    if (*slot == new_func) return 0;          /* 已替换，跳过 */

    if (old_func != NULL && *old_func == NULL) {
        *old_func = *slot;
    }

    long page_size = sysconf(_SC_PAGESIZE);
    if (page_size <= 0) page_size = 4096;
    uintptr_t p_mask = (uintptr_t) page_size - 1;
    uintptr_t start = slot_addr & ~p_mask;
    uintptr_t end   = (slot_addr + sizeof(void *) + p_mask) & ~p_mask;

    if (mprotect((void *) start, end - start, PROT_READ | PROT_WRITE) != 0) {
        return 0;
    }
    *slot = new_func;
    __builtin___clear_cache((char *) slot_addr, (char *) slot_addr + sizeof(void *));
    /* 保持可写：延迟绑定运行时仍可能写入该页 */
    mprotect((void *) start, end - start, PROT_READ | PROT_WRITE);
    return 1;
}

static int match_and_patch(uintptr_t base, elf_sym_t *symtab, const char *strtab,
                           uintptr_t r_offset, unsigned long r_info, hook_ctx_t *ctx) {
    unsigned int type = (unsigned int) ELF_R_TYPE(r_info);
    if (type != (unsigned int) R_JUMP_SLOT && type != (unsigned int) R_GLOB_DAT) return 0;

    unsigned int idx = (unsigned int) ELF_R_SYM(r_info);
    if (idx == 0 || symtab == NULL || strtab == NULL) return 0;

    const char *name = strtab + symtab[idx].st_name;
    if (name == NULL) return 0;

    int hits = 0;
    for (int k = 0; k < ctx->nsyms; k++) {
        hook_sym_t *h = &ctx->syms[k];
        if (h->name == NULL || h->new_func == NULL) continue;
        if (strcmp(name, h->name) != 0) continue;
        uintptr_t slot = base + r_offset;
        if (!in_load_segment(ctx, slot)) continue;   /* 越界偏移，绝不写 */
        if (patch_slot(slot, h->new_func, &h->old_func)) {
            h->hits++;
            hits++;
        }
    }
    return hits;
}

static void scan_dynamic(struct dl_phdr_info *info, uintptr_t base, hook_ctx_t *ctx) {
    elf_dyn_t *dyn = NULL;
    for (int i = 0; i < info->dlpi_phnum; i++) {
        if (info->dlpi_phdr[i].p_type == PT_DYNAMIC) {
            dyn = (elf_dyn_t *) (base + (uintptr_t) info->dlpi_phdr[i].p_vaddr);
            break;
        }
    }
    if (dyn == NULL) return;

    elf_sym_t *symtab = NULL;
    const char *strtab = NULL;
    void   *jmprel   = NULL;
    size_t  pltrelsz = 0;
    long    pltrel   = DT_RELA;
    void   *rela     = NULL;
    size_t  relasz   = 0;
    void   *rel      = NULL;
    size_t  relsz    = 0;

    for (elf_dyn_t *d = dyn; d->d_tag != DT_NULL; d++) {
        switch (d->d_tag) {
            case DT_SYMTAB:  symtab   = (elf_sym_t *) (base + (uintptr_t) d->d_un.d_ptr); break;
            case DT_STRTAB:  strtab   = (const char *) (base + (uintptr_t) d->d_un.d_ptr); break;
            case DT_JMPREL:  jmprel   = (void *) (base + (uintptr_t) d->d_un.d_ptr); break;
            case DT_PLTRELSZ: pltrelsz = (size_t) d->d_un.d_val; break;
            case DT_PLTREL:  pltrel   = (long) d->d_un.d_val; break;
            case DT_RELA:    rela     = (void *) (base + (uintptr_t) d->d_un.d_ptr); break;
            case DT_RELASZ:  relasz   = (size_t) d->d_un.d_val; break;
            case DT_REL:     rel      = (void *) (base + (uintptr_t) d->d_un.d_ptr); break;
            case DT_RELSZ:   relsz    = (size_t) d->d_un.d_val; break;
            default: break;
        }
    }

    /* 1) .rel.plt / .rela.plt —— 函数调用的跳转槽 */
    if (jmprel != NULL && symtab != NULL && strtab != NULL && pltrelsz > 0) {
        if (pltrel == DT_RELA) {
            elf_rela_t *r = (elf_rela_t *) jmprel;
            size_t n = pltrelsz / sizeof(elf_rela_t);
            for (size_t i = 0; i < n; i++) {
                ctx->patched += match_and_patch(base, symtab, strtab,
                                                (uintptr_t) r[i].r_offset, r[i].r_info, ctx);
            }
        } else {
            elf_rel_t *r = (elf_rel_t *) jmprel;
            size_t n = pltrelsz / sizeof(elf_rel_t);
            for (size_t i = 0; i < n; i++) {
                ctx->patched += match_and_patch(base, symtab, strtab,
                                                (uintptr_t) r[i].r_offset, r[i].r_info, ctx);
            }
        }
    }

    /* 2) .rela.dyn / .rel.dyn —— GLOB_DAT 全局函数指针 */
    if (symtab != NULL && strtab != NULL) {
        if (rela != NULL && relasz > 0) {
            elf_rela_t *r = (elf_rela_t *) rela;
            size_t n = relasz / sizeof(elf_rela_t);
            for (size_t i = 0; i < n; i++) {
                ctx->patched += match_and_patch(base, symtab, strtab,
                                                (uintptr_t) r[i].r_offset, r[i].r_info, ctx);
            }
        }
        if (rel != NULL && relsz > 0) {
            elf_rel_t *r = (elf_rel_t *) rel;
            size_t n = relsz / sizeof(elf_rel_t);
            for (size_t i = 0; i < n; i++) {
                ctx->patched += match_and_patch(base, symtab, strtab,
                                                (uintptr_t) r[i].r_offset, r[i].r_info, ctx);
            }
        }
    }
}

static int skip_module(const char *path) {
    if (path == NULL || path[0] == '\0') return 1; /* 主可执行体，跳过 */
    if (strstr(path, "libspeedhack.so") != NULL) return 1;
    if (strstr(path, "libc.so") != NULL) return 1;      /* 定义方，无 GOT 可改 */
    if (strstr(path, "libdl.so") != NULL) return 1;
    if (strstr(path, "ld-android.so") != NULL) return 1;
    if (strstr(path, "linker") != NULL) return 1;
    /* ★ vivo Android 16 真机加固：只钩应用自己 /data 下的游戏库。
     * 系统框架库（/system /apex /vendor /system_ext /product）里有
     * RELR 紧凑重定位、IRELATIVE、厂商私有布局，误解析动态段会把函数
     * 指针写进错误地址 → 内存污染 → 无关线程（binder）随机 SIGSEGV。 */
    if (strstr(path, "/system/") != NULL) return 1;
    if (strstr(path, "/apex/") != NULL) return 1;
    if (strstr(path, "/vendor/") != NULL) return 1;
    if (strstr(path, "/system_ext/") != NULL) return 1;
    if (strstr(path, "/product/") != NULL) return 1;
    return 0;
}

static int hook_cb(struct dl_phdr_info *info, size_t size, void *data) {
    (void) size;
    hook_ctx_t *ctx = (hook_ctx_t *) data;
    const char *path = info->dlpi_name ? info->dlpi_name : "";

    if (ctx->only_path != NULL) {
        if (strcmp(path, ctx->only_path) != 0) return 0;
    } else if (skip_module(path)) {
        return 0;
    }

    ctx->phdr  = info->dlpi_phdr;
    ctx->phnum = info->dlpi_phnum;
    ctx->base  = (uintptr_t) info->dlpi_addr;
    scan_dynamic(info, (uintptr_t) info->dlpi_addr, ctx);
    return 0;
}

int elf_hook_all(hook_sym_t *syms, int nsyms) {
    hook_ctx_t ctx;
    ctx.syms = syms;
    ctx.nsyms = nsyms;
    ctx.only_path = NULL;
    ctx.patched = 0;
    dl_iterate_phdr(hook_cb, &ctx);
    return ctx.patched;
}

int elf_hook_one(const char *module_path, hook_sym_t *syms, int nsyms) {
    if (module_path == NULL) return 0;
    hook_ctx_t ctx;
    ctx.syms = syms;
    ctx.nsyms = nsyms;
    ctx.only_path = module_path;
    ctx.patched = 0;
    dl_iterate_phdr(hook_cb, &ctx);
    return ctx.patched;
}

void elf_basename(const char *path, char *out, size_t out_len) {
    if (out == NULL || out_len == 0) return;
    out[0] = '\0';
    if (path == NULL) return;
    const char *p = strrchr(path, '/');
    p = (p != NULL) ? p + 1 : path;
    const char *bang = strrchr(p, '!');
    if (bang != NULL) p = bang + 1;
    snprintf(out, out_len, "%s", p);
}
