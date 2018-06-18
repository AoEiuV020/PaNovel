package cc.aoeiuv020.reader.simple

import android.content.Context
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.reader.Image
import kotlinx.android.synthetic.main.simple_image_item.view.*
import org.jetbrains.anko.dip

/**
 * Created by AoEiuV020 on 2018.06.11-11:05:03.
 */
internal class ImageViewHolder(
        itemView: View,
        private val prAdapter: PageRecyclerAdapter
) : PageRecyclerAdapter.ViewHolder(itemView) {
    private val ctx: Context = itemView.context
    private val ivImage = itemView.ivImage

    fun setImage(image: Image) {
        ivImage.apply {
            post {
                layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                    setMargins((prAdapter.mLeftSpacing.toFloat() / 100 * itemView.width).toInt(),
                            topMargin,
                            (prAdapter.mRightSpacing.toFloat() / 100 * itemView.width).toInt(),
                            ctx.dip(prAdapter.mParagraphSpacing))
                }
            }

        }
        prAdapter.reader.requester.requestImage(image, ivImage)
    }
}