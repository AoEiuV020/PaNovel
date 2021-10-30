package cc.aoeiuv020.panovel.find.qidiantu.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cc.aoeiuv020.base.jar.ioExecutorService
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.report.Reporter
import cc.aoeiuv020.panovel.util.noCover
import cc.aoeiuv020.regex.pick
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class QidiantuListAdapter : RecyclerView.Adapter<QidiantuListAdapter.ViewHolder>() {
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
                .inflate(R.layout.item_qidiantu_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(item)
        }
        holder.bind(item)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvType: TextView = itemView.findViewById(R.id.tvType)
        private val tvWords: TextView = itemView.findViewById(R.id.tvWords)
        private val tvRatio: TextView = itemView.findViewById(R.id.tvRatio)

        @SuppressLint("SetTextI18n")
        fun bind(item: Item) {
            showImage(item)
            tvName.text = item.name
            tvAuthor.text = item.author + " • " + item.count
            tvDate.text = item.dateAdded
            tvType.text = item.type
            tvWords.text = item.words
            val ratio = if (item.collection.isBlank() && item.firstOrder.isBlank()) {
                item.ratio
            } else {
                "${item.collection}/${item.firstOrder}= ${item.ratio}"
            }
            tvRatio.text = ratio
        }

        private fun showImage(item: Item) {
            ivImage.setTag(R.id.tag_image_item, item)
            val ctx = ivImage.context
            if (!item.image.isNullOrBlank()) {
                Glide.with(ctx.applicationContext)
                    .load(item.image)
                    .apply(RequestOptions().apply {
                        placeholder(R.mipmap.no_cover)
                        error(R.mipmap.no_cover)
                    })
                    .into(ivImage)
                return
            }
            val url = item.url
            val name = item.name
            ivImage.setImageResource(R.mipmap.no_cover)
            ivImage.context.doAsync({ e ->
                val message = "刷新小说《${name}》失败，"
                Reporter.post(message, e)
            }, ioExecutorService) {
                val bookId = url.pick("http.*/info/(\\d*)").first()
                val site = "起点中文"
                val novelManager = DataManager.query(site, item.author, item.name, bookId)
                novelManager.requestDetail(false)
                val imageUrl = novelManager.novel.image
                item.image = imageUrl
                item.name = novelManager.novel.name
                uiThread { ctx ->
                    val tag = ivImage.getTag(R.id.tag_image_item)
                    if (tag != item) {
                        return@uiThread
                    }
                    tvName.text = item.name
                    if (imageUrl == noCover) {
                        ivImage.setImageResource(R.mipmap.no_cover)
                    } else {
                        Glide.with(ctx.applicationContext)
                            .load(novelManager.getImage(imageUrl))
                            .apply(RequestOptions().apply {
                                placeholder(R.mipmap.no_cover)
                                error(R.mipmap.no_cover)
                            })
                            .into(ivImage)
                    }
                }
            }

            ivImage.setImageResource(R.mipmap.no_cover)

        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: Item)
    }
}