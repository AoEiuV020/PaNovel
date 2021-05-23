package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.anull.notNull
import cc.aoeiuv020.regex.pick
import org.junit.Test

/**
 * Created by AoEiuV020 on 2021.05.15-22:58:40.
 */
class BiqugezhhTest : BaseNovelContextText(Biqugezhh::class) {
    @Test
    fun search() {
        search("都市")
        search("柯学捡尸人", "仙舟", "80451829")
    }

    @Test
    fun detail() {
        detail(
            "80451829", "80451829", "柯学捡尸人", "仙舟",
            "https://www.biquzhh.com/files/article/image/80451/80451829/80451829s.jpg",
            "大佬A苦口婆心：“你才18，人生还很长。”大佬B语重心长：“这天赋，不走正道可惜了。”大佬C忧心忡忡：“离开组织吧，你不该站在黑暗里。”---------------\n" +
                    "江夏“嗯”“是”“您说得对”应付三连，收下被发的第N张“你本来应该是个好人”卡，心累的叹了一口气。他明明是个心理健康、成绩优异、有理想有抱负的大好青年。……可是为什么所有人都在劝他改邪归正。",
            "2021-05-15 10:10:05"
        )
    }

    @Test
    fun chapters() {
        chapters(
            "80451829", "第1章 你不要多想", "80451_80451829/612135739", null,
            "第334章 贝尔摩德的价值", "80451_80451829/672485251", "2018-06-09 00:00:00",
            334
        )
    }

    @Test
    fun content() {
        content(
            "80451_80451829/672485251",
            "看好乌佐的琴酒可能会感到不快。但具体问题也要具体分析。",
            "于是延迟了几秒后，他语气生硬地说：“工藤已经死了。”",
            46
        )
    }

    @Test
    fun bookId() {
        val bookUrlList = listOf(
            "https://www.biquzhh.com/80451_80451829/",
            "http://www.zanghaihuatxt.com/book/goto/id/80451829"
        )
        val site = Biqugezhh()
        bookUrlList.all {
            it.pick(site.bookIdRegex.notNull())[site.bookIdIndex] == "80451829"
        }
    }
}