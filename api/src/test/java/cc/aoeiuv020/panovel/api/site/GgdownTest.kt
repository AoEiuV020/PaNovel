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
                "https://www.ggdownxs.com/image/19/19447/19447s.jpg",
                "恶帝城的建立打破了西北的平静，正邪之战一触即发~~\n" +
                        "案件结合战役，龙图原班人马继续他们的传奇经历~~\n",
                "2019-05-15 00:00:00")
    }

    @Test
    fun chapters() {
        chapters("19447", "第1章 【黑风城】", "19/19447/5585742", null,
                "第254章 【番外火凤篇】01 红叶红蝶", "19/19447/33518674", null,
                191)
    }

    @Test
    fun content() {
        content("19/19447/5585742",
                "黑风山位于黑风城以南，山顶常年积雪、山腰植被茂密、山脚溪流交错，一山四季。",
                "go~",
                100)
    }

}