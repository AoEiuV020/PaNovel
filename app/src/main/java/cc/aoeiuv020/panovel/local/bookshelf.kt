package cc.aoeiuv020.panovel.local

import android.util.Base64


/**
 * 书架，
 * Created by AoEiuV020 on 2017.10.04-19:23:01.
 */

object Bookshelf : LocalSource {
    fun contains(novelLocal: NovelLocal): Boolean = fileExists(novelLocal.bookId)

    fun add(novelLocal: NovelLocal) = fileSave(novelLocal.bookId, novelLocal)

    fun remove(novelLocal: NovelLocal) = fileRemove(novelLocal.bookId)

    fun list(): List<NovelLocal> = fileList()

    fun get(novelLocal: NovelLocal): NovelLocal? = fileLoad(novelLocal.bookId)
}

val NovelLocal.bookId get() = md5Base64(novelItem.toString() + requester.toString())

fun md5Base64(s: String): String {
    val digest = java.security.MessageDigest.getInstance("MD5")
    digest.update(s.toByteArray())
    val messageDigest = digest.digest()
    return Base64.encodeToString(messageDigest, Base64.NO_PADDING or Base64.URL_SAFE or Base64.NO_WRAP)
}