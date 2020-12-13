package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.03-13:57:34.
 */
class YmoxuanTest : BaseNovelContextText(Ymoxuan::class) {
    @Test
    fun search() {
        search("都市")
        search("斗战狂潮", "骷髅精灵", "31577")
        search("大主宰", "天蚕土豆", "21825")
    }

    @Test
    fun detail() {
        detail("31577", "31577", "斗战狂潮", "骷髅精灵",
                "http://r.m.ymxxs.com/jieqi/cover/31/31577/31577s.jpg",
                "双月当空，无限可能的英魂世界\n" +
                        "孤寂黑暗，神秘古怪的嬉命小丑\n" +
                        "百城联邦，三大帝国，异族横行，魂兽霸幽\n" +
                        "这是一个英雄辈出的年代，人类卧薪尝胆重掌地球主权，孕育着进军高纬度的野望！\n" +
                        "重点是……二年级的废柴学长王同学，如何使用嬉命轮盘，撬动整个世界，伙伴们，请注意，学长来了！！！",
                "2018-06-03 00:04:00")
    }

    @Test
    fun chapters() {
        chapters("31577", "第一章 嬉命小丑", "31/31577/29700048", null,
                "12月1日，英雄联盟：我的时代！", "31/31577/172511130", null,
                1245)
    }

    @Test
    fun content() {
        content("31/31577/29700048",
                "欲望是进步的动力，也孕育了毁灭。",
                "小丑的脸一下子跨了下来，“什么小狗，我是嬉命小丑辛巴，戏弄命运，无所不能，小孩，你摊上大事儿了！”",
                28)
    }

}