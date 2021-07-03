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
    fun chapters2() {
        chapters(
            "18/18907", "第一章 末世", "18/18907/3799362:0", null,
            "第二百八十章 条件", "18/18907/4365973:1", "2021-07-02 08:47:44",
            280
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
    fun content2() {
        content(
            "18/18907/4365973:1",
            "“恭喜，赵老，你身体里已经没有癌细胞，你痊愈了。”洪磊放下手中的检查报告，笑着跟赵毅说道。",
            "而且谢丽波娃怀疑树根的生长速度是被陨石逼出来的，这样一来就麻烦了，不用陨石勾引它的话，它就会朝着四面八方的方向生长，迟早会到达基地那里去，如果像现在这样勾引了的话，未来搞不好就是拿人命去填，她没有办法，只能把这里的巨树的异常情况向上面汇报，上面收到她的消息，让她继续这样用陨石勾引树根，他们要研究一下接下来该怎么做比较好。",
            36
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