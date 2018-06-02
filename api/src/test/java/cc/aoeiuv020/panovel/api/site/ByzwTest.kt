package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.02-19:48:12.
 */
class ByzwTest : BaseNovelContextText(Byzw::class) {
    @Test
    fun search() {
        search("都市")
        search("道君", "跃千愁", "28675")
        search("蛊真人", "蛊真人", "7900")
    }

    @Test
    fun detail() {
        detail("7900", "7900", "蛊真人", "蛊真人",
                "https://www.zwdu.com/files/article/image/7/7900/7900s.jpg",
                "人是万物之灵，蛊是天地真精。\n" +
                        "三观不正，魔头重生。\n" +
                        "昔日旧梦，同名新作。\n" +
                        "一个穿越者不断重生的故事。\n" +
                        "一个养蛊、炼蛊、用蛊的奇特世界。\n" +
                        "春秋蝉、月光蛊、酒虫、一气金光虫、青丝蛊、希望蛊……\n" +
                        "还有一个纵情纵横的绝世大魔头！",
                "2018-04-26 21:42:32")
    }

    @Test
    fun chapters() {
        chapters("7900", "序：不是走向成功，就是走向毁灭", "7900/234025", null,
                "今天无更", "7900/13073827", "2018-06-02 19:01:52",
                2038)
    }

    @Test
    fun content() {
        content("7900/234025",
                "在过去的几个月内，我经历了生活赐予的痛苦，并且身心都在其中挣扎。",
                "看更新最快的武动乾坤最新章节Www.81zw.Coｍ",
                60)
    }

}