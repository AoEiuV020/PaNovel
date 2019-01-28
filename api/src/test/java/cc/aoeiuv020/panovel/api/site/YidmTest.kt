package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.regex.pick
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.10.05-00:38:40.
 */
class YidmTest : BaseNovelContextText(Yidm::class) {
    init {
        // 下载不到版权受限的小说了，这个源就没什么意义了，
        enabled = false
    }
    @Test
    fun search() {
        search("都市")
        search("OVERLORD不死者之王")
    }

    @Test
    fun detail() {
        detail("1517", "1517", "OVERLORD不死者之王", "丸山くがね",
                "https://covercdn2.yidm.com/1/1517/1517l.jpg",
                "一款席卷游戏界的网路游戏「YGGDRASIL」，" +
                        "有一天突然毫无预警地停止一切服务——原本应该是如此。" +
                        "但是不知为何它却成了一款即使过了结束时间，玩家角色依然不会登出的游戏。" +
                        "NPC开始拥有自己的思想。<br />\r\n" +
                        "现实世界当中一名喜欢电玩的普通青年，" +
                        "似乎和整个公会一起穿越到异世界，变成拥有骷髅外表的最强魔法师「飞鼠」。" +
                        "他率领的公会「安兹．乌尔．恭」将展开前所未有的奇幻传说！",
                "2018-09-03 22:51:00")
    }

    @Test
    fun chapters() {
        chapters("1517", "第一卷 不死者之王", "1517/49048/", null,
                "插图", "1517/84886/87227", "2018-09-03 22:51:00",
                136)
    }

    @Test
    fun content() {
        content("1517/49048/49049",
                "OVERLORD1 不死者之王",
                "于是──",
                93)
        content("1517/49048/",
                "第一卷 不死者之王",
                "第一卷 不死者之王",
                1)
        content("1517/49048/49050",
                "![img](jar:${cacheDir.resolve("迷糊轻小说")
                        .resolve("1517")
                        .resolve("49048")
                        .toURI()}!/img/82173.jpg)",
                "飞鼠告诉自己，暂时把这个问题抛在脑后，先将目前的当务之急依序处理完毕之后再来伤脑筋吧。",
                701)
        content("1517/49048/73186",
                "![img](jar:${cacheDir.resolve("迷糊轻小说")
                        .resolve("1517")
                        .resolve("49048")
                        .toURI()}!/img/82169.jpg)",
                "![img](jar:${cacheDir.resolve("迷糊轻小说")
                        .resolve("1517")
                        .resolve("49048")
                        .toURI()}!/img/82184.jpg)",
                16)
    }

    @Test
    fun img() {
        val wpub = File.createTempFile("pre", ".wpub")
        val rootUrl = URL("jar:${wpub.toURI()}!/")
        val line = "{\$FULLPATH\$}img/26508.jpg"
        try {
            val img = line.pick("\\{\\\$FULLPATH\\\$\\}(img\\S*)")
                    .first()
                    .let { "![img](${URL(rootUrl, it)})" }
            assertEquals("![img](${rootUrl.toString() + "img/26508.jpg"})", img)
        } finally {
            wpub.delete()
        }
    }
}
