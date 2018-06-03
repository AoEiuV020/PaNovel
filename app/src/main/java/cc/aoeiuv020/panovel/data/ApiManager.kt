package cc.aoeiuv020.panovel.data

import android.content.Context
import cc.aoeiuv020.panovel.api.NovelChapter
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.api.site.*
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.util.notNullOrReport
import java.net.URL
import java.util.*

/**
 * Created by AoEiuV020 on 2018.05.23-15:55:11.
 */
class ApiManager(ctx: Context) {
    init {
        NovelContext.cache(ctx.cacheDir.resolve("api"))
        NovelContext.files(ctx.filesDir.resolve("api"))
    }

    // TODO: 动漫之家还是维护，等开放了，测试后再添加，
    @Suppress("RemoveExplicitTypeArguments")
    val contexts: List<NovelContext> by lazy {
        listOf(
                Piaotian(), Biquge(), Liudatxt(), Qidian(),
                Sfacg(), Snwx(), Syxs(),
                Yssm(), Qlwx(),
                Byzw(), Fenghuaju(), Yllxs(),
                Mianhuatang(), Gxwztv(), Ymoxuan(),
                Qingkan(), Ggdown(), Biqugebook(),
                Guanshuwang(), Jdxs520(), Lread()
        )
    }
    // 缓存host对应网站上下文的映射，
    private val hostMap: MutableMap<String, NovelContext> by lazy {
        contexts.associateByTo(mutableMapOf()) { URL(it.site.baseUrl).host }
    }
    private val nameMap by lazy {
        contexts.associateBy { it.site.name }
    }

    @Suppress("unused")
    fun getNovelContextByUrl(url: String): NovelContext {
        return hostMap[URL(url).host]
                ?: contexts.firstOrNull { it.check(url) }
                ?: throw IllegalArgumentException("网址不支持: $url")
    }

    fun getNovelContextByName(name: String): NovelContext {
        return nameMap[name] ?: throw IllegalArgumentException("网站不支持: $name")
    }

    fun context(novel: Novel): NovelContext {
        return getNovelContextByName(novel.site)
    }

    fun updateNovelDetail(novel: Novel) {
        val novelDetail = context(novel).getNovelDetail(novel.detail)
        novel.name = novelDetail.novel.name
        novel.author = novelDetail.novel.author
        novel.detail = novelDetail.novel.extra
        novel.image = novelDetail.image
        novel.introduction = novelDetail.introduction
        novel.chapters = novelDetail.extra
        if (novelDetail.update != null) {
            novel.updateTime = novelDetail.update.notNullOrReport()
        }
    }

    fun requestNovelChapters(novel: Novel): List<NovelChapter> {
        val list = context(novel).getNovelChaptersAsc(novel.nChapters)
        novel.apply {
            // 是否真的有更新，
            val hasNew = list.size > chaptersCount
            chaptersCount = list.size
            // 多余的警告，反馈了，
            // https://youtrack.jetbrains.com/issue/KT-24557
            @Suppress("RemoveRedundantCallsOfConversionMethods")
            lastChapterName = list.lastOrNull()?.name.toString()
            if (readAtChapterIndex == 0) {
                // 阅读至第一章代表没阅读过，保存第一章的章节名，
                @Suppress("RemoveRedundantCallsOfConversionMethods")
                readAtChapterName = list.firstOrNull()?.name.toString()
            }
            // 如果有更新时间，就存起来，
            list.lastOrNull()?.update?.let {
                updateTime = it
            }
            checkUpdateTime = Date()
            if (hasNew) {
                receiveUpdateTime = checkUpdateTime
            }
        }
        return list
    }

    fun getDetailUrl(novel: Novel): String {
        return context(novel).getNovelDetailUrl(novel.detail)
    }

    fun search(context: NovelContext, name: String): List<NovelItem> {
        return context.searchNovelName(name)
    }

    fun getNovelFromUrl(context: NovelContext, url: String): NovelItem {
        return context.getNovelItem(url)
    }

    fun removeCookies(context: NovelContext) {
        context.removeCookies()
    }

    fun getNovelContent(novel: Novel, chapter: NovelChapter): List<String> {
        return context(novel).getNovelContent(chapter.extra)
    }

    fun getContentUrl(novel: Novel, chapter: NovelChapter): String {
        return context(novel).getNovelContentUrl(chapter.extra)
    }

    fun cleanCache() {
        NovelContext.cleanCache()
    }
}