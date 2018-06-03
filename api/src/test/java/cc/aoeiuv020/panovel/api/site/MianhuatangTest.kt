package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.03-11:13:24.
 */
class MianhuatangTest : BaseNovelContextText(Mianhuatang::class) {
    @Test
    fun search() {
        search("都市")
        search("平天策", "无罪", "8327366")
        search("漫威世界的术士", "火之高兴", "8326615")
    }

    @Test
    fun detail() {
        detail("8326615", "8326615", "漫威世界的术士", "火之高兴",
                "http://images2.mianhuatang520.com/images2/20170924/201709241855035253.jpg",
                "恶魔是我的奴仆，邪能是我的力量，暗影与烈焰伴我左右，我是一名术士。" +
                        "一名术士行走在漫威的世界里。（没系统，靠自己吧少年。）" +
                        "欢迎加入漫威世界的术士书友群，群号码：177873494",
                null)
    }

    @Test
    fun chapters() {
        chapters("8326615", "关于更新", "8326615/81992178", null,
                "第573章 另有安排", "8326615/82245484", null,
                588)
    }

    @Test
    fun content() {
        content("8326615/81992178",
                "各位尊敬的读者，我知道很多人都对我的更新很不满，在此我先道歉。",
                "所以还请各位读者多多体谅，要是觉得我的小说还入的了法眼，还请大家多多支持。",
                4)
    }
}