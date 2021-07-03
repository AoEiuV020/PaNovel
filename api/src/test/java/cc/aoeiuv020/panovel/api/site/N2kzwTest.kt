package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.regex.compilePattern
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.08-18:49:05.
 */
class N2kzwTest : BaseNovelContextText(N2kzw::class) {
    @Test
    fun search() {
        search("都市")
        search("一世之尊", "爱潜水的乌贼", "56/56353")
    }

    @Test
    fun detail() {
        detail(
            "56/56353", "56/56353", "一世之尊", "爱潜水的乌贼",
            "https://www.2kzw.cc/images/56/56353.jpg",
            "我这一生，不问前尘，不求来世，只轰轰烈烈，快意恩仇，败尽各族英杰，傲笑六道神魔！",
            "2017-10-26 10:07:22"
        )
    }

    @Test
    fun chapters() {
        chapters(
            "56/56353", "第一章 机心", "56/56353/4271498", null,
            "番外（十） 奇遇", "56/56353/4281553", "2021-06-08 03:53:37",
            1422
        )
    }

    @Test
    fun content() {
        content(
            "56/56353/4280886",
            "【最新播报】明天就是515，起点周年庆，福利最多的一天。除了礼包书包，这次的515红包狂翻肯定要看，红包哪有不抢的道理，定好闹钟昂~",
            "ps.5.15「起点」下红包雨了！中午12点开始每个小时抢一轮，一大波515红包就看运气了。你们都去抢，抢来的起点币继续来订阅我的章节啊！",
            55
        )
    }

    @Test
    fun pickNameTest() {
        // 这网站所有名字都是/遮天(精校版)/官道无疆(校对版)/
        val pickName = { e: String ->
            e.replace(compilePattern("\\(\\S+版\\)$").toRegex(), "")
        }
        assertEquals("遮天", pickName("遮天(精校版)"))
    }

}