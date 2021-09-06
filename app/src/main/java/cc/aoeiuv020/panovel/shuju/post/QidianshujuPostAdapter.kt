package cc.aoeiuv020.panovel.shuju.post

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.shuju.list.QidianshujuListActivity
import java.util.concurrent.TimeUnit

/**
 * Created by AoEiuV020 on 2021.09.06-23:09:42.
 */
class QidianshujuPostAdapter : RecyclerView.Adapter<QidianshujuPostAdapter.ViewHolder>() {
    private val data = mutableListOf<Post>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<Post>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_qidianshuju_post, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvNum: TextView = itemView.findViewById(R.id.tvNum)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        fun bind(item: Post) {
            itemView.setOnClickListener {
                QidianshujuListActivity.start(it.context, item.url)
            }
            tvTitle.text = item.title
            tvNum.text = item.num
            tvDate.text = item.date?.let { date ->
                DateUtils.getRelativeTimeSpanString(
                    date.time,
                    System.currentTimeMillis(),
                    TimeUnit.SECONDS.toMillis(1)
                )
            } ?: ""
        }
    }
}