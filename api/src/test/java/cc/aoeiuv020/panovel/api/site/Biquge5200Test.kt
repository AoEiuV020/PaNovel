package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2021.05.15-22:58:40.
 */
class Biquge5200Test : BaseNovelContextText(Biquge5200::class) {
    @Test
    fun search() {
        search("都市")
        search("柯学捡尸人", "仙舟", "143123")
    }

    @Test
    fun detail() {
        detail(
            "143123", "143123", "柯学捡尸人", "仙舟",
            "http://r.m.biquge5200.net/cover/aHR0cDovL2Jvb2tjb3Zlci55dWV3ZW4uY29tL3FkYmltZy8zNDk1NzMvMTAyNTA4OTMwNy8xODA=",
            "大佬A苦口婆心：\n" +
                    "“你才18，人生还很长。”\n" +
                    "大佬B语重心长：\n" +
                    "“这天赋，不走正道可惜了。”\n" +
                    "大佬C忧心忡忡：\n" +
                    "“离开组织吧，你不该站在黑暗里。”\n" +
                    "---------------\n" +
                    "江夏“嗯”“是”“您说得对”应付三连，收下被发的第N张“你本来应该是个好人”卡，心累的叹了一口气。\n" +
                    "他明明是个心理健康、成绩优异、有理想有抱负的大好青年。\n" +
                    "……可是为什么所有人都在劝他改邪归正。",
            "2021-07-01 00:00:00"
        )
    }

    @Test
    fun chapters() {
        chapters(
            "143123", "本书相关", "143_143123/176075793", null,
            "第389章 高傲的名侦探（合章）", "143_143123/178948142", "2021-07-01 00:00:00",
            387
        )
    }

    @Test
    fun content() {
        content(
            "143_143123/178948142",
            "柯学捡尸人正文卷第389章高傲的名侦探江夏走神归走神，该听的也都听进了耳朵里。",
            "7017k",
            115
        )
    }
}