package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.06-15:28:17.
 */
class DajiaduTest : BaseNovelContextText(Dajiadu::class) {
    @Test
    fun search() {
        // 这网站搜索可能随机失败，刷新又好，
        search("都市")
        search("修真聊天群", "圣骑士的传说", "24/24478")
    }

    @Test
    fun detail() {
        detail(
            "24/24478", "24/24478", "修真聊天群", "圣骑士的传说",
            "https://www.dajiadu8.com/files/article/image/24/24478/24478s.jpg",
            "某天，宋书航意外加入了一个仙侠中二病资深患者的交流群，里面的群友们都以‘道友’相称，群名片都是各种府主、洞主、真人、天师。连群主走失的宠物犬都称为大妖犬离家出走。整天聊的是炼丹、闯秘境、炼功经验啥的。\n" +
                    "突然有一天，潜水良久的他突然发现……群里每一个群员，竟然全部是修真者，能移山倒海、长生千年的那种！\n" +
                    "啊啊啊啊，世界观在一夜间彻底崩碎啦！\n" +
                    "(本站郑重提醒：本故事纯属虚构，如有雷同，纯属巧合，切勿模仿。)",
            null
        )
    }

    @Test
    fun chapters() {
        chapters(
            "24/24478", "书友群", "24/24478/7005434", null,
            "新书上传啦，《万界点名册》", "24/24478/14761299", null,
            3229
        )
    }

    @Test
    fun content() {
        content("23/23752/11507981",
                "矫若惊龙，漂若浮云，宣纸上的“咏箸”宛若一条蛟龙破纸而出，搅动一室浩然正气和灵气，震惊了场中众人。",
                "bq",
                62)
    }

}