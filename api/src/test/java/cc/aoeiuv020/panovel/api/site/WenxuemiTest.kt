package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.03-19:57:36.
 */
class WenxuemiTest : BaseNovelContextText(Wenxuemi::class) {
    @Test
    fun search() {
        search("都市")
        search("牧神记", "宅猪", "22/22768")
        search("大道朝天", "猫腻", "24/24163")
    }

    @Test
    fun detail() {
        detail("24/24163", "24/24163", "大道朝天", "猫腻",
                "https://www.wenxuemi6.com/files/article/image/24/24163/24163s.jpg",
                "我就是剑。\n" +
                        "千里杀一人，十步不愿行。\n" +
                        "千里杀一人，十步不得行。\n" +
                        "千里杀一人，十步？不行！\n" +
                        "我就是剑，剑就是我。\n" +
                        "大道朝天，各执一剑。\n" +
                        "（欢迎加入猫腻大道朝天官方群，群号码：311875513）",
                "2018-06-03 12:05:14")
    }

    @Test
    fun chapters() {
        chapters("24/24163", "说在前面", "24/24163/11718668", null,
                "第三十四章寻剑", "24/24163/13415137", "2018-06-03 12:05:14",
                237)
    }

    @Test
    fun content() {
        content("24/24163/13415137",
                "（昨天的章节名先不继续用，以后有合适的章节再重复用。）",
                "就在她准备说些什么的时候，德瑟瑟忽然睁大眼睛，问道：“你们知道那件事了吧？”",
                107)
    }

}