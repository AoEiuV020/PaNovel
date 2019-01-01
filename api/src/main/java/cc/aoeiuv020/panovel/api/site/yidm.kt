package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.gson.toBean
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelDetail
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.regex.pick
import com.google.gson.JsonObject
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore

class Yidm : DslJsoupNovelContext() {init {
    // 轻小说网站默认不开启，
    enabled = false
    site {
        name = "迷糊轻小说"
        baseUrl = "http://www.yidm.com"
        logo = "http://static.yidm.com/img/index/logo.png"
    }
    header {
        // 模拟迷糊轻小说app，
        // 还缺个PHPSESSID，保持和cookie里一致有点麻烦，
        userAgent = "RN(0.52.0) Yidmos Yidm(V3) Android"
        // app里是小写的，
        "accept" to "application/json"
    }
    search {
        get {
            url = "//openapi.yidm.com/article/search.php"
            data {
                "searchKey" to it
                "page" to "1"
                "pageSize" to "20"
            }
        }
        response { json ->
            json.toBean<JsonObject>()
                    .get("data")
                    .asJsonArray
                    .map {
                        val a = it.asJsonObject
                        NovelItem(site.name,
                                a["articlename"].asString,
                                a["author"].asString,
                                a["articleid"].asString
                        )
                    }
        }
    }
    // http://www.yidm.com/article/info/1/1517.html
    // 1517
    // http://openapi.yidm.com/article/getArticleInfo.php?aid=1517
    detailPageTemplate = "/article/info/%s/%s.html"
    detailDivision = 1000
    detail { aid ->
        get {
            url = "//openapi.yidm.com/article/getArticleInfo.php?aid=%s".notNull().format(aid)
        }
        response { json ->
            json.toBean<JsonObject>()
                    .get("data")
                    .asJsonObject
                    .get("detail")
                    .asJsonObject
                    .let { a ->
                        NovelDetail(NovelItem(site.name,
                                a["articlename"].asString,
                                a["author"].asString,
                                a["articleid"].asString),
                                a["cover"].asString + (a["size"].asJsonObject.get("large").asString),
                                Date(a["lasttimestamp"].asLong * 1000),
                                a["intro"].asString,
                                a["articleid"].asString
                        )
                    }
        }
    }
    chapters { aid ->
        get {
            url = "//openapi.yidm.com/article/getArticleInfo.php?aid=%s".notNull().format(aid)
        }
        response { json ->
            var lastUpdate: Date?
            json.toBean<JsonObject>()
                    .get("data")
                    .asJsonObject
                    .also {
                        lastUpdate = Date(it.get("detail").asJsonObject
                                .get("lasttimestamp")
                                .asLong * 1000)
                    }
                    .get("volumes")
                    .asJsonArray
                    .flatMap {
                        val v = it.asJsonObject
                        val vid = v.get("vid").asString
                        val chapters: MutableList<NovelChapter> = v.get("chapters")
                                .asJsonArray
                                .mapTo(mutableListOf()) {
                                    val c = it.asJsonObject
                                    val cid = c.get("cid").asString
                                    NovelChapter(
                                            c.get("chapter").asString,
                                            "$aid/$vid/$cid"
                                    )
                                }
                        // 卷名单独成一章，
                        val vChapter = NovelChapter(
                                v.get("volume").asString,
                                "$aid/$vid/"
                        )
                        chapters.add(0, vChapter)
                        chapters
                    }.also { it.lastOrNull()?.update = lastUpdate }
        }
    }
    // http://www.yidm.com/article/html/1/1512/48685.html
    // http://openapi.yidm.com/article/downVolumeWpub.php?aid=1517&vid=49048
    contentPageTemplate = "//www.yidm.com/article/html/%s/%s/%s.html"
    getNovelContentUrl { extra ->
        val (aid, _, cid) = extra.split('/')
        if (cid.isBlank()) {
            getNovelDetailUrl(aid)
        } else {
            contentPageTemplate.notNull().format(aid.toInt() / 1000, aid, cid)
        }
    }
    content { extra ->
        val (aid, vid, cid) = extra.split('/')
        get {
            url = "http://openapi.yidm.com/article/downVolumeWpub.php?aid=$aid&vid=$vid"
        }
        val cacheFile = mCacheDir?.resolve(aid)?.also { it.mkdirs() }?.resolve(vid)
        if (cacheFile != null) {
            try {
                return@content readFromWpub(cacheFile, cid)
            } catch (_: Exception) {
                // 无论如何读取失败都继续往下走正常下载，
                // 包括章节不存在的情况，
            }
        }

        return@content keyLocker.runInAcquire("$aid/$vid") {
            // 二次验证，拿到锁时可能之前的线程已经下载好了，避免重复下载，
            if (cacheFile != null) {
                try {
                    readFromWpub(cacheFile, cid)
                } catch (_: Exception) {
                    // 无论如何读取失败都继续往下走正常下载，
                    // 包括章节不存在的情况，
                    inputStream {
                        downloadWpub(it, aid, vid).also { tmpFile ->
                            tmpFile.renameTo(cacheFile)
                        }
                    }
                    readFromWpub(cacheFile, cid)
                }
            } else {
                val wpub = inputStream {
                    downloadWpub(it, aid, vid)
                }
                readFromWpub(wpub, cid)
            }
        }
    }
}

    private val keyLocker = KeyLocker()

    private fun downloadWpub(input: InputStream, aid: String, vid: String): File {
        // 直接上锁，禁止多线程下载同一卷，
        // mCacheDir不必须，如果没有，就放在系统临时目录，事后不删除，
        val tmpFile = File.createTempFile(aid, vid, mCacheDir)
        tmpFile.outputStream().use { output ->
            input.copyTo(output)
        }
        return tmpFile
    }

    init {
        initJar()
    }

    private fun initJar() {
        // 禁用默认缓存连接，对任意URLConnection实例使用都可以，
        // 否则jar打开的文件不会被关闭，从而导致文件被覆盖了依然能读到旧文件，
        // 多少会影响性能，
        URL("jar:file:/fake.jar!/fake.file").openConnection().defaultUseCaches = false
    }

    private fun readFromWpub(wpub: File, cid: String): List<String> {
        if (cid.isBlank()) {
            return listOf(readChapterName(wpub))
        }
        val rootUrl = URL("jar:${wpub.toURI()}!/")
        val cUrl = URL(rootUrl, cid)
        return cUrl.openStream().bufferedReader().useLines { sequence ->
            sequence.mapNotNull { line ->
                // {$FULLPATH$}img/26508.jpg
                // "![img]($it)"
                try {
                    line.pick("\\{\\\$FULLPATH\\\$\\}(img\\S*)")
                            .first()
                            .let { "![img](${URL(rootUrl, it)})" }
                } catch (_: Exception) {
                    // 该行不是图片，
                    line.trim().takeIf { it.isNotEmpty() }
                }
            }.toList()
        }
    }

    private fun readChapterName(wpub: File): String {
        val rootUrl = URL("jar:${wpub.toURI()}!/")
        val iUrl = URL(rootUrl, "index.opf")
        return iUrl.openStream().reader().readText()
                .toBean<JsonObject>()
                .get("volumeName")
                .asString
    }

    @Suppress("MemberVisibilityCanBePrivate")
    class KeyLocker {
        // 没有删除不再被使用的entry, 可能浪费内存，
        private val semaphoreMap = ConcurrentHashMap<String, Semaphore>()

        fun acquire(key: String) {
            val semaphore = semaphoreMap.getOrPut(key) { Semaphore(1, true) }
            semaphore.acquireUninterruptibly()
        }

        fun release(key: String) {
            // key不存在就什么都不做，
            semaphoreMap[key]?.release()
        }

        fun <T> runInAcquire(key: String, block: () -> T): T = try {
            acquire(key)
            block()
        } finally {
            // 放在finally以防万一io异常时也要释放锁，
            release(key)
        }
    }
}