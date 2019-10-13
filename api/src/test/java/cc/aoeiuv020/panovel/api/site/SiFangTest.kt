package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2019.10.13-13:50:00.
 */
class SiFangTest : BaseNovelContextText(SiFang::class) {
    @Test
    fun search() {
        search("逆风岁月", "君尽欢", "20")
    }

    @Test
    fun detail() {
        detail("20", "20", "逆风岁月", "君尽欢",
                "http://www.sifangbook.com/uploads/20180724/4cf6966367c66aef17091409b493c895.jpg",
                "当曾经的谎言破碎在手中，原本一心想逃离家族的俞南思不得不停下脚步。为夺回母亲该有的一切，她甚至不惜踏上违背初心的路。而赫北书，无论曾经现在，虚无的皮囊，内心下依然隐藏着恶魔，肆无忌惮。\n" +
                        "开始的二人，命途相似，做的却都不是真实的自...",
                "2018-12-24 23:49:32")
    }

    @Test
    fun chapters() {
        chapters("20", "001：关于俞家", "712", null,
                "140：圆满结局", "5543", "2018-12-24 23:49:32",
                140)
    }

    @Test
    fun content() {
        content("712",
                "三月，是雨最肆意的时节。",
                "想到这儿，俞南思不屑地笑了笑，这种鬼话父亲也信，真是傻透了。",
                56)
    }

}