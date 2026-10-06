package com.kamispeed.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kamispeed.app.databinding.ActivityAddAppBinding
import com.kamispeed.app.databinding.ItemAppBinding

/**
 * 添加应用到主面板快捷启动列表。
 * 顶部搜索框按名称/包名过滤；点击条目切换收藏状态。
 */
class AddAppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAppBinding
    private lateinit var adapter: AddAppAdapter

    private var all: List<AppEntry> = emptyList()
    private var favorites: MutableSet<String> = HashSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AddAppAdapter(emptyList(), favorites) { entry -> toggle(entry) }
        binding.rvApps.layoutManager = LinearLayoutManager(this)
        binding.rvApps.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = applyFilter(s?.toString())
            override fun afterTextChanged(s: android.text.Editable?) = Unit
        })

        load()
    }

    private fun load() {
        binding.pbLoading.visibility = View.VISIBLE
        Thread {
            val list = AppRepository.load(this)
            val favs = HashSet(FavoritesStore.load(this))
            runOnUiThread {
                binding.pbLoading.visibility = View.GONE
                all = list
                favorites = favs
                applyFilter(binding.etSearch.text?.toString())
            }
        }.start()
    }

    private fun applyFilter(query: String?) {
        val q = query?.trim()?.lowercase()
        val filtered = if (q.isNullOrEmpty()) {
            all
        } else {
            all.filter { it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
        }
        binding.tvCount.text = "全部应用 (${filtered.size})"
        adapter.submit(filtered)
    }

    private fun toggle(entry: AppEntry) {
        if (favorites.contains(entry.packageName)) {
            FavoritesStore.remove(this, entry.packageName)
            favorites.remove(entry.packageName)
            Toast.makeText(this, "已从主面板移除：${entry.label}", Toast.LENGTH_SHORT).show()
        } else {
            FavoritesStore.add(this, entry.packageName)
            favorites.add(entry.packageName)
            Toast.makeText(this, "已添加到主面板：${entry.label}", Toast.LENGTH_SHORT).show()
        }
        adapter.submit(adapter.current)
    }

    override fun finish() {
        setResult(RESULT_OK)
        super.finish()
    }
}

class AddAppAdapter(
    var current: List<AppEntry>,
    private val favorites: MutableSet<String>,
    private val onToggle: (AppEntry) -> Unit,
) : RecyclerView.Adapter<AddAppAdapter.Holder>() {

    class Holder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    fun submit(list: List<AppEntry>) {
        current = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int = current.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = current[position]
        val added = favorites.contains(item.packageName)
        holder.binding.ivIcon.setImageDrawable(item.icon)
        holder.binding.tvName.text = item.label
        holder.binding.tvPkg.text = item.packageName
        holder.binding.btnRun.text = if (added) "已添加" else "添加"
        holder.binding.btnRun.setTextColor(
            if (added) 0xFF8A97A8.toInt() else 0xFF37D67A.toInt()
        )
        holder.binding.root.setOnClickListener { onToggle(item) }
        holder.binding.btnRun.setOnClickListener { onToggle(item) }
    }
}
