package cc.aoeiuv020.panovel.api

/**
 * Created by AoEiuV020 on 2018.06.04-19:10:18.
 */
@Suppress("RemoveExplicitTypeArguments")
val contexts: List<NovelContext> by lazy {
    NovelContext.getAllSite()
}
private val nameMap by lazy {
    contexts.associateBy { it.site.name }
}

fun getNovelContextByName(name: String): NovelContext {
    return nameMap[name] ?: throw IllegalArgumentException("网站不支持: $name")
}
