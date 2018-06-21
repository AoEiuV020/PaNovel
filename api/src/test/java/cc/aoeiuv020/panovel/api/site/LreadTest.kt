package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.03-19:36:18.
 */
class LreadTest : BaseNovelContextText(Lread::class) {
    @Test
    fun search() {
        search("都市")
        search("道君", "跃千愁", "53677")
        search("飞剑问道", "我吃西红柿", "88917")
    }

    @Test
    fun detail() {
        detail("88917", "88917", "飞剑问道", "我吃西红柿",
                "https://www.lread.net/files/article/image/88/88917/88917s.jpg",
                "在这个世界，有狐仙、河神、水怪、大妖，也有求长生的修行者。\n" +
                        "修行者们，\n" +
                        "开法眼，可看妖魔鬼怪。\n" +
                        "炼一口飞剑，可千里杀敌。\n" +
                        "千里眼、顺风耳，更可探查四方。\n" +
                        "……\n" +
                        "秦府二公子‘秦云’，便是一位修行者……",
                "2018-06-03 00:15:00")
    }

    @Test
    fun chapters() {
        chapters("88917", "第一章 归来", "88917/32771268", null,
                "第十一篇 第八章 魔神世界的帝君", "88917/40963324", "2018-06-03 00:15:00",
                679)
    }

    @Test
    fun content() {
        content("88917/40963324",
                "“死！”",
                "那模糊庞大身影沉默了片刻，才道：“在蛮祖教布置的一切，全部毁掉，不留任何痕迹。不能让神霄道人他们查出来！至于你们四个，放弃蛮祖教，想办法逃命吧。”",
                64)
    }

}