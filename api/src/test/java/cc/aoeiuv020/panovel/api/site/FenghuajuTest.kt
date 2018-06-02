package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.02-20:18:20.
 */
class FenghuajuTest : BaseNovelContextText(Fenghuaju::class) {
    @Test
    fun search() {
        search("都市")
        search("一念永恒", "耳根", "4_4282")
        search("我真是大明星", "尝谕", "3_3426")
    }

    @Test
    fun detail() {
        detail("18_18819", "18_18819", "天道图书馆", "横扫天涯",
                "http://www.fenghuaju.cc/image/18/18819/18819s.jpg",
                "张悬穿越异界，成了一名光荣的教师，脑海中多出了一个神秘的图书馆。\n" +
                        "只要他看过的东西，无论人还是物，都能自动形成书籍，记录下对方各种各样的缺点，于是，他牛大了！\n" +
                        "“昊天大帝，你怎么不喜欢穿内裤啊？堂堂大帝，能不能注意点形象？”\n" +
                        "“玲珑仙子，你如果晚上再失眠，可以找我嘛，我这个人唱安眠曲很有一套的！”\n" +
                        "“还有你，乾坤魔君，能不能少吃点大葱，想把老子熏死吗？”\n" +
                        "……\n" +
                        "这是一个师道传承，培养、指点世界最强者的牛逼拉风故事。",
                "2018-06-02 19:38:40")
    }

    @Test
    fun chapters() {
        chapters("18_18819", "第一章 骗子", "18_18819/2", null,
                "第一千二百五十章 张悬传授的绝招上", "18_18819/1338", "2018-06-02 19:38:40",
                1334)
    }

    @Test
    fun content() {
        content("18_18819/1338",
                "“亲自考核？”",
                ":",
                114)
    }
}