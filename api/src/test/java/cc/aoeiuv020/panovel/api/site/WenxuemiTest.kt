package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.03-19:57:36.
 */
class WenxuemiTest : BaseNovelContextText(Wenxuemi::class) {
    @Test
    fun search() {
        search("都市")
        search("大道朝天", "猫腻", "24/24163")
    }

    @Test
    fun detail() {
        detail("30/30818", "30/30818", "长生种", "月中阴",
                "https://www.wenxuemi6.com/files/article/image/30/30818/30818s.jpg",
                "这是一个疯狂氪金（挂逼），在异界为所欲为的故事。生死看淡，不服就干！……ps：老月已经完本《法师奥义》《永恒武道》皆是精品，大家有兴趣的可以去看一看。老月出品，必属精品！书友企鹅群：189099589",
                "2020-03-22 23:11:15")
    }

    @Test
    fun chapters() {
        chapters("30/30818", "第一章 你可还有童子身？", "30/30818/15644464", null,
                "新书《以力服人》已经发布", "30/30818/20073343", "2020-03-22 23:11:15",
                1126)
    }

    @Test
    fun content() {
        content("30/30818/15644464",
                "姓名：雷道（十八岁）",
                "张青龙也没有看那些银子，而是沉声道：“道哥儿真要练武也行，但这童子功却有些特殊。嗯，我得先问道哥儿一句，道哥儿，你可还有童子身？”",
                73)
    }

}