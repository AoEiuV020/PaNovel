package cc.aoeiuv020.panovel.api

import cc.aoeiuv020.panovel.data.NovelProvider
import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.util.noCover
import java.net.URL
import java.util.*

/**
 * Created by AoEiuV020 on 2018.06.13-13:27:02.
 */
class ApiNovelProvider(
        private val novel: Novel,
        private val context: NovelContext
) : NovelProvider {
    override fun getDetailUrl(): String {
        return context.getNovelDetailUrl(novel.detail)
    }

    override fun updateNovelDetail() {
        val novelDetail = context.getNovelDetail(novel.detail)
        novel.name = novelDetail.novel.name
        novel.author = novelDetail.novel.author
        novel.detail = novelDetail.novel.extra
        novel.image = novelDetail.image ?: noCover
        novel.introduction = novelDetail.introduction
        novel.chapters = novelDetail.extra
        // detail页面的更新时间不保存，
        // 万一只有详情页有时间，章节列表页没有，收到更新通知时会通知详情页的时间，可能一直没变过，
    }

    override fun requestNovelChapters(): List<NovelChapter> {
        val list = context.getNovelChaptersAsc(novel.nChapters)
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
                // 无更新时可能receiveUpdateTime为0？貌似不会，
                receiveUpdateTime = checkUpdateTime
            }
        }
        return list
    }

    override fun getNovelContent(chapter: NovelChapter, listener: ((Long, Long) -> Unit)?): List<String> {
        return context.getNovelContent(chapter.extra, listener)
    }

    override fun getContentUrl(chapter: NovelChapter): String {
        return context.getNovelContentUrl(chapter.extra)
    }

    override fun cleanData() {
        context.cleanData()
    }

    override fun cleanCache() {
        context.cleanCache()
    }

    override fun getImage(extra: String): URL {
        return context.getImage(extra)
    }
}