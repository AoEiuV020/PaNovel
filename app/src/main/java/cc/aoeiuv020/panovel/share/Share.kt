package cc.aoeiuv020.panovel.share

import cc.aoeiuv020.panovel.local.BookListData
import cc.aoeiuv020.panovel.local.toBean
import cc.aoeiuv020.panovel.local.toJson

/**
 *
 * Created by AoEiuV020 on 2018.03.07-19:14:09.
 */
object Share {
    private val paste = PasteUbuntu()

    fun check(url: String): Boolean {
        return paste.check(url)
    }

    fun shareBookList(bookList: BookListData): String {
        return paste.upload(PasteUbuntu.PasteUbuntuData(bookList.toJson()))
    }

    fun receiveBookList(url: String): BookListData {
        val text = paste.download(url)
        return text.toBean()
    }
}