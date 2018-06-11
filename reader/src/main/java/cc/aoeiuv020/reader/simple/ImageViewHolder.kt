package cc.aoeiuv020.reader.simple

import android.content.Context
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.simple_image_item.view.*

/**
 * Created by AoEiuV020 on 2018.06.11-11:05:03.
 */
internal class ImageViewHolder(
        itemView: View,
        @Suppress("UNUSED_PARAMETER") prAdapter: PageRecyclerAdapter
) : PageRecyclerAdapter.ViewHolder(itemView) {
    private val ctx: Context = itemView.context
    private val ivImage = itemView.ivImage

    fun setImage(src: String) {
        Glide.with(ctx)
                .load(src)
                .apply(RequestOptions().apply {
                })
                .into(ivImage)
    }
}