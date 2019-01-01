package cc.aoeiuv020.panovel.data

import android.content.Context
import cc.aoeiuv020.panovel.api.ApiNovelProvider
import cc.aoeiuv020.panovel.api.NovelContext
import cc.aoeiuv020.panovel.api.NovelItem
import cc.aoeiuv020.panovel.data.entity.Novel
import java.net.URL

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
        NovelContext.getAllSite()
    }
    // 缓存host对应网站上下文的映射，
    private val hostMap: MutableMap<String, NovelContext> by lazy {
        contexts.associateByTo(mutableMapOf()) { URL(it.site.baseUrl).host }
    }
    private val nameMap by lazy {
        contexts.associateBy { it.site.name }
    }

    @Suppress("unused")
    fun siteNameContains(name: String) = nameMap.containsKey(name)

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


    fun search(context: NovelContext, name: String): List<NovelItem> {
        return context.searchNovelName(name)
    }

    fun getNovelFromUrl(context: NovelContext, url: String): NovelItem {
        return context.getNovelItem(url)
    }

    fun removeCookies(context: NovelContext) {
        context.removeCookies()
    }

    fun cleanCache() {
        NovelContext.cleanCache()
    }

    fun getNovelProvider(novel: Novel): NovelProvider {
        return ApiNovelProvider(novel, context(novel))
    }
}