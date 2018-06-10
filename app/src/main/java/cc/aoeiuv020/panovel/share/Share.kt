package cc.aoeiuv020.panovel.share

import android.content.Context
import android.view.View
import cc.aoeiuv020.base.jar.toBean
import cc.aoeiuv020.base.jar.toJson
import cc.aoeiuv020.base.jar.type
import cc.aoeiuv020.panovel.App
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.data.DataManager
import cc.aoeiuv020.panovel.data.entity.BookList
import cc.aoeiuv020.panovel.data.entity.NovelMinimal
import cc.aoeiuv020.panovel.util.safelyShow
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
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
    /**
     * 对不同版本的分享结果进行支持，
     * 当前第二版，
     * 第一版的没有版本号，
     */
    private const val VERSION: Int = 2

    fun check(url: String): Boolean {
        return paste.check(url)
    }

    fun shareBookList(bookList: BookList, shareExpiration: Expiration): String {
        val novelList = DataManager.getNovelFromBookList(bookList.nId).map {
            NovelMinimal(it)
        }
        val bookListBean = BookListBean(bookList.name, novelList, VERSION)
        return paste.upload(PasteUbuntu.PasteUbuntuData(bookListBean.toJson(App.gson), expiration = shareExpiration))
    }

    /**
     * @return 返回导入的书单中的小说数量，
     */
    fun receiveBookList(url: String): Int {
        val text = paste.download(url)
        val bookListJson = text.toBean<JsonObject>(App.gson)
        val version = bookListJson.get("version")?.asJsonPrimitive?.asInt
        val bookListBean: BookListBean = when (version) {
            2 -> {
                App.gson.fromJson(bookListJson, type<BookListBean>())
            }
            else -> {
                // 旧版version为null,
                val oldBookListBean: OldBookListBean = App.gson.fromJson(bookListJson, type<OldBookListBean>())
                BookListBean(oldBookListBean.name, oldBookListBean.list.map {
                    // 旧版的extra为完整地址，直接拿来，就算写进数据库了，刷新详情页后也会被新版的bookId覆盖，
                    NovelMinimal(site = it.site, author = it.author, name = it.name, detail = it.requester.extra)
                }, VERSION)
            }
        }
        DataManager.importBookList(bookListBean.name, bookListBean.list)
        return bookListBean.list.size
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
        }.safelyShow()
    }
}