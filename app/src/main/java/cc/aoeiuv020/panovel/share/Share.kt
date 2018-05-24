package cc.aoeiuv020.panovel.share

import android.content.Context
import android.view.View
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.entity.BookList
import cc.aoeiuv020.panovel.local.BookListData
import cc.aoeiuv020.panovel.local.toBean
import cc.aoeiuv020.panovel.local.toJson
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.dialog_shared.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse
import org.jetbrains.anko.yesButton

/**
 *
 * Created by AoEiuV020 on 2018.03.07-19:14:09.
 */
object Share {
    private val paste = PasteUbuntu()

    fun check(url: String): Boolean {
        return paste.check(url)
    }

    fun shareBookList(bookList: BookList, shareExpiration: Expiration): String {
        TODO("要从bookList查到小说再上传，")
        return paste.upload(PasteUbuntu.PasteUbuntuData(bookList.toJson(), expiration = shareExpiration))
    }

    fun receiveBookList(url: String): BookListData {
        TODO("要兼容旧版，")
        val text = paste.download(url)
        return text.toBean()
    }

    fun alert(context: Context, url: String, qrCode: String) {
        val layout = View.inflate(context, R.layout.dialog_shared, null)
        layout.tvUrl.apply {
            text = url
            setTextIsSelectable(true)
            setOnClickListener {
                context.browse(url)
            }
        }
        Glide.with(context).load(qrCode).into(layout.ivQrCode)
        context.alert {
            titleResource = R.string.share
            customView = layout
            yesButton { }
        }.show()
    }
}