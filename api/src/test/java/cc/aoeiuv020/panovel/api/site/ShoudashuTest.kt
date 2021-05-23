package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2021.05.15-22:58:40.
 */
class ShoudashuTest : BaseNovelContextText(Shoudashu::class) {
    @Test
    fun search() {
        search("都市")
        search("都市最强狂神萧羽秦媛媛", "半只凉鞋", "277/277083")
    }

    @Test
    fun detail() {
        detail(
            "277/277083", "277/277083", "都市最强狂神萧羽秦媛媛", "半只凉鞋",
            "https://www.shoudashu.com/files/article/image//277/277083/277083s.jpg",
            "萧羽活了五千年，死不了，也老不成，这搞得他心情有点差，所以还请没事别招惹。要是惹怒了被暴打一顿，他还会狠狠地告诉你：“我的世界，没有能与不能，只有想与不想。”",
            null
        )
    }

    @Test
    fun chapters() {
        chapters(
            "277/277083", "第一章 大亨之殇", "277/277083/66921365", null,
            "第四百二十六章 你怎么敢", "277/277083/70018272", null,
            425
        )
    }

    @Test
    fun content() {
        content(
            "277/277083/66921365",
            "炎夏，魔都。",
            "萧羽感受到的怨气，就是由她的身上散发出来的。",
            91
        )
    }

}