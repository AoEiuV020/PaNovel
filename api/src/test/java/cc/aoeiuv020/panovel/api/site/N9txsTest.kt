package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2020.03.28-16:05:24.
 */
class N9txsTest : BaseNovelContextText(N9txs::class) {
    @Test
    fun search() {
        search("都市")
        search("诡校危道", "微尘轻轻", "171852")
        search("最强神医混都市", "九歌", "43776")
    }

    @Test
    fun detail() {
        detail("43776", "43776", "最强神医混都市", "九歌",
                "https://img.9txs.org/43776/526786.jpg",
                "★精华简介★\n" +
                        "当过兵王，又是神医，兼职修真，一路混迹都市，一路美女狂收。我的桃花运，运不断，做个帅哥太累了！",
                "2020-03-28 15:08:01")
    }

    @Test
    fun chapters() {
        chapters(
            "171852", "第1章 六年前的信", "171852/315691", null,
            "第628章 那一刻的惊鸿", "171852/1342970", "2020-12-31 10:05:00",
            628
        )
        chapters(
            "43776", "第1章 小哥，帮帮我", "43776/3466718", null,
            "第5884章 这该死的缘分", "43776/4291998", "2021-10-11 23:10:00",
            5884
        )
    }

    @Test
    fun content() {
        content(
            "171852/684157",
            "九桃小说 老域名(9txs.com)被墙，请您牢记本站最新域名(9txs.org)",
            "您可以在百度里搜索“诡校危道 九桃小说(9txs.org)”查找最新章节！",
            76
        )
    }
}