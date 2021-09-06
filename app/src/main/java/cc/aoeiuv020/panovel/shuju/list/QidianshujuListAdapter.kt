package cc.aoeiuv020.panovel.shuju.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cc.aoeiuv020.panovel.R

/**
 * Created by AoEiuV020 on 2021.09.06-23:09:42.
 */
class QidianshujuListAdapter : RecyclerView.Adapter<QidianshujuListAdapter.ViewHolder>() {
    private val data = mutableListOf<Item>()
    private var onItemClickListener: OnItemClickListener? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<Item>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return data.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_qidianshuju_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(item.url)
        }
        holder.bind(item)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        private val tvLevel: TextView = itemView.findViewById(R.id.tvLevel)
        private val tvType: TextView = itemView.findViewById(R.id.tvType)
        private val tvWords: TextView = itemView.findViewById(R.id.tvWords)
        private val tvRatio: TextView = itemView.findViewById(R.id.tvRatio)
        fun bind(item: Item) {
            ivImage.setImageResource(R.mipmap.no_cover)
            tvName.text = item.name
            tvAuthor.text = item.author
            tvLevel.text = item.level
            tvType.text = item.type
            tvWords.text = item.words
            val ratio = if (item.collection.isBlank() && item.firstOrder.isBlank()) {
                item.ratio
            } else {
                "${item.collection}/${item.firstOrder}= ${item.ratio}"
            }
            tvRatio.text = ratio
        }
    }

    interface OnItemClickListener {
        fun onItemClick(url: String)
    }
}