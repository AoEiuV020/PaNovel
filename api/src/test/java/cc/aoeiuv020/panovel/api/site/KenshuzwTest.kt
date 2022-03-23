package cc.aoeiuv020.panovel.api.site

import org.junit.Test

class KenshuzwTest : BaseNovelContextText(Kenshuzw::class) {
    @Test
    fun search() {
        search("都市")
        search("桃运神医混都市", "一念", "44805")
        search("柯学捡尸人", "仙舟", "185033")
    }

    @Test
    fun detail() {
        detail(
            "44805", "44805", "桃运神医混都市", "一念",
            "http://api.kenshuzw.com/44/44805/44805s.jpg",
            "实习医生叶正豪，意外的得到一本古书上的玄术与医道传承，自此开始了不一样的人生，他银针渡人，术法渡鬼，成就济世仁心，医道问卜、风水勘舆无所不精。且看主角如何弘扬华",
            "2021-10-10 00:00:00"
        )
        detail(
            "185033", "185033", "柯学捡尸人", "仙舟",
            "http://api.kenshuzw.com/185/185033/185033s.jpg",
            "大佬A苦口婆心：\n“你才18，人生还很长。”\n大佬B语重心长：\n“这天赋，不走正道可惜了。”\n大佬C忧心忡忡：\n“离开组织吧，你不该站在黑暗里。”\n………………\n江夏“嗯”、“是”、“您说得对”应付三连，收下被发的第N张“你本来应该是个好人”卡，心累的叹了一口气。\n.\n他明明是个心理健康、成绩优异、有理想有抱负的大好青年。\n……可是为什么所有人都在劝他改邪归正。\n===================\n马甲流，半无敌，轻松日常~",
            "2021-10-13 00:00:00"
        )
    }

    @Test
    fun chapters() {
        chapters(
            "44805", "第1章 实习生", "44805/24882082", null,
            "第4361章 比试", "44805/90423077", "2021-10-10 00:00:00",
            4344
        )
        chapters(
            "185033", "第1章 你不要多想", "185033/84973589", null,
            "第995章 服部：可恶的人贩子！ 求月票(ﾉ∇︎〃 )", "185033/93248712", "2022-03-23 00:00:00",
            1036
        )
    }

    @Test
    fun content() {
        content(
            "44805/90423077",
            "“老三，好久没有回来，不知道你府上有没有新来人，就是那种能打的？”二龙皇笑呵呵的说：“我这位战将收来，还不知道具体战力如何呢？”",
            "现在他只需要发出指令，这海妖就会听从他的指令而来。",
            32
        )
        content(
            "185033/90477829",
            "走廊的阴影中。",
            "7017k",
            59
        )
    }

}