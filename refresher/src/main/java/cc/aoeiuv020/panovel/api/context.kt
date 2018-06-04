package cc.aoeiuv020.panovel.api

import cc.aoeiuv020.panovel.api.site.*

/**
 * Created by AoEiuV020 on 2018.06.04-19:10:18.
 */
@Suppress("RemoveExplicitTypeArguments")
val contexts: List<NovelContext> by lazy {
    listOf(
            Piaotian(), Biquge(), Liudatxt(), Qidian(),
            Sfacg(), Snwx(), Syxs(),
            Yssm(), Qlwx(),
            Byzw(), Fenghuaju(), Yllxs(),
            Mianhuatang(), Gxwztv(), Ymoxuan(),
            Qingkan(), Ggdown(), Biqugebook(),
            Guanshuwang(), Jdxs520(), Lread(),
            Wenxuemi(), Yipinxia(), N360dxs(),
            N7dsw(), Aileleba(), Gulizw(),
            N73xs()
    )
}
private val nameMap by lazy {
    contexts.associateBy { it.site.name }
}

fun getNovelContextByName(name: String): NovelContext {
    return nameMap[name] ?: throw IllegalArgumentException("网站不支持: $name")
}
