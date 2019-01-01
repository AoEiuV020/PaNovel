package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.03-15:47:22.
 */
class GgdownTest : BaseNovelContextText(Ggdown::class) {
    @Test
    fun search() {
        search("都市")
        search("与天同兽", "雾矢翊", "46272")
        search("黑风城战记", "耳雅", "19447")
    }

    @Test
    fun detail() {
        detail("19447", "19447", "黑风城战记", "耳雅",
                "http://www.ggdown.com/image/19/19447/19447s.jpg",
                "《黑风城战记》是《龙图案卷集》的续篇，由十个战役组成，地点是西北要塞黑风城，同时也有破案情节贯穿于战役中~~\n" +
                        "恶帝城的建立打破了西北的平静，正邪之战一触即发~~\n" +
                        "案件结合战役，龙图原班人马继续他们的传奇经历~~",
                "2018-2-12 10:20:33")
    }

    @Test
    fun chapters() {
        chapters("19447", "第1章 【黑风城】", "19/19447/5585742", null,
                "第236章 【老帘子】", "19/19447/22928333", null,
                236)
    }

    @Test
    fun content() {
        content("19/19447/5585742",
                "欢迎您的光临，任何搜索引擎搜索“读零零小说网”即可快速进入本站，所有章节显示为同一页面时，是因为你的浏览器缓存未更新。",
                "王重阳无语：“你简直丧心病狂，我全真门下怎么有你这么不成器的弟子。”",
                48)
    }

}