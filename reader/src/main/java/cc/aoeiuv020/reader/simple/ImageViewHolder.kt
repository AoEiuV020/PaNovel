package cc.aoeiuv020.reader.simple

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.reader.INovelReader
import cc.aoeiuv020.reader.Image
import cc.aoeiuv020.reader.R
import kotlinx.android.synthetic.main.simple_image_item.view.*
import org.jetbrains.anko.dip

/**
 * Created by AoEiuV020 on 2018.06.11-11:05:03.
 */
internal class ImageViewHolder(
        itemView: View,
        private val prAdapter: PageRecyclerAdapter
) : PageRecyclerAdapter.ViewHolder(itemView) {
    companion object {
        fun create(ctx: Context, parent: ViewGroup, prAdapter: PageRecyclerAdapter): ImageViewHolder {
            val view = LayoutInflater.from(ctx).inflate(R.layout.simple_image_item, parent, false)
            return ImageViewHolder(view, prAdapter)
        }
    }

    private val ctx: Context = itemView.context
    private val tvPage = itemView.tvPage
    private val ivImage = itemView.ivImage

    init {
        ivImage.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (itemView.height != v.height) {
                itemView.layoutParams = itemView.layoutParams.apply {
                    height = v.height
                }
            }
        }
    }

    fun setImage(reader: INovelReader, index: Int, image: Image) {
        tvPage.text = (index + 1).toString()
        tvPage.setTextColor(reader.config.textColor)
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