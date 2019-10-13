package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.05-18:55:47.
 */
class WukongTest : BaseNovelContextText(Wukong::class) {
    @Test
    fun search() {
        search("都市")
        search("我的宝藏男神藏不住了", "鱼酱二千", "54740")
        search("不朽凡人", "鹅是老五", "11")
    }

    @Test
    fun detail() {
        detail("885", "885", "神藏", "打眼",
                "https://www.wukong.la/files/article/image/0/885/885s.jpg",
                "一念之间，沧海桑田\n" +
                        "打眼带你进入古玩的世界！！！",
                "2018-06-05 12:32:00")
        detail("11", "11", "不朽凡人", "鹅是老五",
                "https://www.wukong.la/files/article/image/0/11/11s.jpg",
                "我，只有凡根，一介凡人！我，叫莫无忌！我，要不朽！",
                "2017-12-31 12:44:00")
    }

    @Test
    fun chapters() {
        chapters("885", "第一章 少年和胖子", "885/386850", null,
                "第一千一百六十三章 筑基期", "885/23979393", "2018-06-05 12:32:00",
                1163)
        chapters("11", "楔子", "11/541", null,
                "我本一凡人（后记）", "11/19981466", "2017-12-31 12:44:00",
                1248)
    }

    @Test
    fun content() {
        content("885/386850",
                "金陵，地处华夏东部地区，长江下游，濒江近海。",
                "这个身形有些肥硕，横向发展的身体，使得那山路小径显得愈发的狭窄起来，不过肥胖",
                26)
        content("11/19981466",
                "每一本书完本后，内心总是空虚的，不朽凡人完本后，我一样的不想动，依然是一种茫然，或者是不知道应该去做什么了。",
                "最后如果有什么事情，我会在公众微信号中第一时间发布出来。我的公众微信号是eslw26，或者搜索公众号‘鹅是老五’。",
                23)
    }

}