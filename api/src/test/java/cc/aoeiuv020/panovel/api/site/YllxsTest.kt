package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.regex.pick
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat

/**
 * Created by AoEiuV020 on 2018.06.02-21:08:03.
 */
class YllxsTest : BaseNovelContextText(Yllxs::class) {
    @Test
    fun regex() {
        val regex = "(/xiaoshuo/\\d*)?/(\\d+)"
        val index = 1
        "http://www.166xs.com/116732.html".pick(regex)[index].let {
            assertEquals("116732", it)
        }
        "http://www.166xs.com/xiaoshuo/116/116732/".pick(regex)[index].let {
            assertEquals("116732", it)
        }
        "http://www.166xs.com/xiaoshuo/90/90745/".pick(regex)[index].let {
            assertEquals("90745", it)
        }
        "http://www.166xs.com/xiaoshuo/90/90745/19961399.html".pick(regex)[index].let {
            assertEquals("90745", it)
        }
        "http://www.166xs.com/xiaoshuo/121/121623/34377467.html".pick(regex)[index].let {
            assertEquals("121623", it)
        }
    }

    @Test
    fun date() {
        val date = SimpleDateFormat("yyyy-MM-dd").parse("2018-03-20")
        assertEquals(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2018-03-20 00:00:00"), date)
    }

    @Test
    fun string() {
        "%.3s".format("1234", "fdsa").let {
            assertEquals("123", it)
        }
    }

    @Test
    fun search() {
        search("都市")
        search("超品相师", "西域刀客", "116732")
        search("点道为止", "梦入神机", "121623")
    }

    @Test
    fun detail() {
        detail("121623", "121623", "点道为止", "梦入神机",
                "http://www.166xs.com/files/article/image/121/121623/121623s.jpg",
                "功夫究竟是什么？\n" +
                        "花架子还是杀人技？\n" +
                        "三千年冷兵器战争和无数民间私斗酝酿出来的把式，究竟是不是骗局？\n" +
                        "国术流开创者，功夫小说第一人梦入神机，在本书中为您揭秘。\n" +
                        "止戈为武，点到为止。\n" +
                        "“你若无敌，将会如何？”\n" +
                        "“得饶人处且饶人。”",
                "2018-06-02 00:00:00")
    }

    @Test
    fun chapters() {
        chapters("121623", "第一章 庄稼把式 一锄一翻皆功夫", "121/121623/34377467", null,
                "第249章 继承烦恼 取舍去留会选择", "121/121623/37797899", null,
                249)
        chapters("1", "第一章 罗峰", "0/1/2", null,
                "完本免费看啦", "0/1/21700169", null,
                1492)
    }

    @Test
    fun content() {
        content("121/121623/37797899",
                "“这其中有什么分别么？”刘石问。",
                "倒是这男子沉得住气，看了苏劫一眼，点点头：“老爸，你既然有高手保护，那我就放心了。”",
                64)
    }

}
