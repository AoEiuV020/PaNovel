package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.regex.pick
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.03-13:19:06.
 */
class GxwztvTest : BaseNovelContextText(Gxwztv::class) {
    @Test
    fun regex() {
        val regex = "((/ba)|(/\\d+/))(\\d+)"
        val index = 3
        listOf("https://www.gxwztv.com/ba9814.shtml",
                "https://www.gxwztv.com/9/9814/",
                "https://www.gxwztv.com/9/9814/166515215.html"
        ).forEach {
            assertEquals("9814", it.pick(regex)[index])
        }
    }

    @Test
    fun search() {
        search("都市")
        search("帝霸", "厌笔萧生", "9814")
        search("腹黑老公溺宠：老婆不准躲", "望月存雅", "55886")
    }

    @Test
    fun detail() {
        detail("9814", "9814", "帝霸", "厌笔萧生",
                "http://r.gxwztv.com/files/article/image/9/9814/9814s.jpg",
                "千万年前，李七夜栽下一株翠竹。" +
                        "八百万年前，李七夜养了一条鲤鱼。" +
                        "五百万年前，李七夜收养一个小女孩。" +
                        "今天，李七夜一觉醒来，翠竹修练成神灵，鲤鱼化作金龙，小女孩成为九界女帝。" +
                        "这是一个养成的故事，一个不死的人族小子养成了妖神、养成了仙兽、养成了女帝的故事",
                "2018-06-03 09:18:00")
    }

    @Test
    fun chapters() {
        chapters("9814", "帝霸", "9/9814/2206102", null,
                "第3186章将离去", "9/9814/166515215", null,
                3318)
    }

    @Test
    fun content() {
        content("9/9814/166515215",
                "李七夜说要离开，就要离开，当他把兵器打磨好之后，就启程前往不渡海。",
                "大揭秘！！想知道长生老人的生死之谜吗？想知道长生老人到底残留了什么后手吗？" +
                        "来这里！！关注微信公众号“萧府军团”，查看历史消息，或输入“生死”即可阅览相关信息！！",
                57)
    }

}