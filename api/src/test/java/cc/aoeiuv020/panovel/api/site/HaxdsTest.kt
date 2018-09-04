package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.07-14:56:42.
 */
class HaxdsTest : BaseNovelContextText(Haxds::class) {
    @Test
    fun search() {
        search("都市")
        search("一剑飞仙", "流浪的蛤蟆", "72169")
        search("和空姐同居的日子", "三十", "67")
    }

    @Test
    fun detail() {
        detail("72169", "72169", "一剑飞仙", "流浪的蛤蟆",
                "http://www.haxwx11.com/article/image/72/72169/72169s.jpg",
                "x.千锤百炼烈火雷霆，十年换得一剑光寒。潜牙伏爪百般忍受，只为今朝一飞冲霄！" +
                        "各位书友要是觉得《一剑飞仙》还不错的话请不要忘记向您qq群和微博里的朋友推荐哦！",
                "2018-06-01 00:00:00")
        detail("67", "67", "和空姐同居的日子", "三十",
                "http://www.haxwx11.com/article/image/0/67/67s.jpg",
                "只想写一个开心快乐的爱情故事，只想写一个普通人在社会生存的故事，" +
                        "只想写一个可以让大家看了之后微微一笑的故事，多谢大家给予的支持，请留言指教！谢谢！",
                "2016-05-24 00:00:00")
    }

    @Test
    fun chapters() {
        chapters("72169", "一、天行健，君子以自强不息", "72/72169/11460020", null,
                "八百八十六、见习探员", "72/72169/51051473", null,
                1043)
        chapters("67", "第一章 相遇", "0/67/7347", null,
                "关于《和我同居的女人》", "0/67/7406", null,
                73)
    }

    @Test
    fun content() {
        content("72/72169/11460020",
                "; 人越是长大，就越是认清这个世界，曾有过的憧憬和幻想，会被现实一一击破。",
                "但是大气污染极为严重的城市天空，怎么可能看到如此繁盛的星空",
                31)
        content("0/67/7406",
                "关于更新",
                "三十2005年8月30日凌晨",
                17)
    }

}