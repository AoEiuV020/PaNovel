package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.03-20:25:10.
 */
class N360dxsTest : BaseNovelContextText(N360dxs::class) {
    @Test
    fun search() {
        search("都市")
        search("OVERLORD不死者之王", "丸山くがね", "http://famitongwenku.360dxs.com/book_1592.html")
        search("为美好的世界献上祝福！(给予这个绝美的世界以祝福！)", "晓なつめ", "http://jiaochuanwenku.360dxs.com/book_1657.html")
    }

    @Test
    fun detail() {
        detail("//jiaochuanwenku.360dxs.com/book_1657.html", "//jiaochuanwenku.360dxs.com/book_1657.html",
                "为美好的世界献上祝福！(给予这个绝美的世界以祝福！)", "晓なつめ",
                "http://jiaochuanwenku.360dxs.com/static/books/logo/1657s.jpg",
                "喜爱游戏的家里蹲少年佐藤和真的人生突然闭幕……但是他的眼前出现自称女神的美少女。" +
                        "转生到异世界的和真就此为了满足食衣住而努力工作！" +
                        "原本只想安稳度日的和真，却因为带去的女神接二连三引发问题，甚至被魔王军盯上了!?",
                "2018-06-03 06:51:00")
    }

    @Test
    fun chapters() {
        chapters("//jiaochuanwenku.360dxs.com/book_1657.html",
                "人物简介", "http://jiaochuanwenku.360dxs.com/chapter_56212.html", null,
                "g店特典 假面店主的掏耳膝枕", "http://jiaochuanwenku.360dxs.com/chapter_86695.html", "2018-06-03 06:51:00",
                239)
    }

    @Test
    fun content() {
        content("http://jiaochuanwenku.360dxs.com/chapter_86695.html",
                "网译版",
                "——结果面具用肥皂抹了一下之后，就拿下来了。",
                44)
        content("http://famitongwenku.360dxs.com/chapter_89626.html",
                "![img](http://famitongwenku.360dxs.com/static/books/chapter/1592/89626/106143.jpg)",
                "![img](http://famitongwenku.360dxs.com/static/books/chapter/1592/89626/106155.jpg)",
                13)
    }

}