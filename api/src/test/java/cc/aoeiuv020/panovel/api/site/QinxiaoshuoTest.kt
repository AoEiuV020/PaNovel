package cc.aoeiuv020.panovel.api.site

import org.junit.Test
import java.net.URLEncoder

/**
 * Created by AoEiuV020 on 2018.09.04-03:47:10.
 */
class QinxiaoshuoTest : BaseNovelContextText(Qinxiaoshuo::class) {
    @Test
    fun search() {
        search("异世界")
        search("都市")
        search("OVERLORD", "丸山くがね", "OVERLORD")
        // 全名太长，搜索失败，
        search("为美好的世界献上祝福！(给予这个绝美的世界以祝福！)", "晓なつめ", URLEncoder.encode("为美好的世界献上祝福！(给予这个绝美的世界以祝福！)", "utf-8").toLowerCase())
    }

    @Test
    fun detail() {
        detail(URLEncoder.encode("为美好的世界献上祝福！(给予这个绝美的世界以祝福！)", "utf-8").toLowerCase(), URLEncoder.encode("为美好的世界献上祝福！(给予这个绝美的世界以祝福！)", "utf-8").toLowerCase(),
                "为美好的世界献上祝福！(给予这个绝美的世界以祝福！)", "晓なつめ",
                "http://static.qinxiaoshuo.com:4000/bookimg/1609.jpg",
                "喜爱游戏的家里蹲少年佐藤和真的人生突然闭幕……但是他的眼前出现自称女神的美少女。转生到异世界的和真就此为了满足食衣住而努力工作！原本只想安稳度日的和真，却因为带去的女神接二连三引发问题，甚至被魔王军盯上了!?",
                "2019-08-19 23:00:00")
        detail("OVERLORD", "OVERLORD",
                "OVERLORD", "丸山くがね",
                "http://static.qinxiaoshuo.com:4000/bookimg/1545.jpg",
                "一款席卷游戏界的网络游戏「YGGDRASIL」，有一天突然毫无预警地停止一切服务——原本应该是如此。但是不知为何它却成了一款即使过了结束时间，玩家角色依然不会登出的游戏。NPC开始拥有自己的思想。\n" +
                        "现实世界当中一名喜欢电玩的普通青年，似乎和整个公会一起穿越到异世界，变成拥有骷髅外表的最强魔法师「飞鼠」。他率领的公会「安兹．乌尔．恭」将展开前所未有的奇幻传说！",
                "2019-12-23 10:27:42")
    }

    @Test
    fun chapters() {
        chapters(URLEncoder.encode("为美好的世界献上祝福！(给予这个绝美的世界以祝福！)", "utf-8").toLowerCase(),
                "转载信息", "0/1609/5d77d1cb56fec85e5b100448", null,
                "后记", "0/1609/5d77d1da56fec85e5b100552", "2019-08-19 23:00:00",
                267)
        chapters("OVERLORD",
                "prologue", "0/1545/5d77d0d856fec85e5b0ffccc", null,
                "作者杂感", "0/1545/5ea3edf4e5337d4bcf7e81b5", "2020-04-25 15:59:00",
                142)
    }

    @Test
    fun content() {
        content("0/1545/5d77d0d856fec85e5b0ffccc",
                "第一卷 不死者之王 prologue",
                "于是────",
                56)
        content("0/1609/5d77d1cb56fec85e5b100452",
                "第一卷 啊啊，没用的女神大人 插画",
                "![img](http://static.qinxiaoshuo.com:4000/novel_img/1609/5d77d1cb56fec85e5b100452/23.jpg)",
                24)
    }
}