package com.kamispeed.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kamispeed.app.databinding.ItemAppBinding

class AppListAdapter(
    private var items: List<AppEntry>,
    private val onClick: (AppEntry) -> Unit,
    private val onLongClick: (AppEntry) -> Unit = {},
) : RecyclerView.Adapter<AppListAdapter.Holder>() {

    class Holder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    fun submit(list: List<AppEntry>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.binding.ivIcon.setImageDrawable(item.icon)
        holder.binding.tvName.text = item.label
        val override = com.kamispeed.container.VirtualCore.INSTANCE.preferredActivity(item.packageName)
        holder.binding.tvPkg.text = when {
            override != null -> "${item.packageName}\n启动页：${override.substringAfterLast('.')}（长按可改）"
            item.system -> "${item.packageName}  ·  系统"
            else -> item.packageName
        }
        holder.binding.root.setOnClickListener { onClick(item) }
        holder.binding.root.setOnLongClickListener { onLongClick(item); true }
        holder.binding.btnRun.setOnClickListener { onClick(item) }
        holder.binding.btnRun.setOnLongClickListener { onLongClick(item); true }
    }
}
