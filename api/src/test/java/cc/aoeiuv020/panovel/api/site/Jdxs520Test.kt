package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.03-18:50:03.
 */
class Jdxs520Test : BaseNovelContextText(Jdxs520::class) {
    @Test
    fun search() {
        // 这网站可能搜索结果可能随机出现乱码开头，刷新又好，
        search("都市")
        search("念念不忘，总裁乘胜追妻", "七爷", "77903")
        search("圣墟", "辰东", "45887")
    }

    @Test
    fun detail() {
        detail("77903", "77903", "念念不忘，总裁乘胜追妻", "七爷",
                "http://www.jdxs5200.net/files/article/image/77/77903/77903s.jpg",
                "初见，她在下，他在上，他的口中叫着别人的名字。\n" +
                        "再见，她衣裳凌乱，披头散发，被人屈辱按在地上，狼狈不堪……\n" +
                        "他是人人敬畏的传奇人物，霍家太子爷。\n" +
                        "顺手救下她，冷漠送她四个字“咎由自取！”\n" +
                        "狼狈的她，却露出一抹明媚的笑，声音清脆“姐夫……谢谢啊！”",
                "2018-06-02 21:52:00")
    }

    @Test
    fun chapters() {
        chapters("77903", "第1章 错位替身", "77903/45183134", null,
                "第148章 重新开始", "77903/46358897", "2018-06-02 21:52:00",
                148)
    }

    @Test
    fun content() {
        content("77903/45183134",
                "十二月的南城很冷很冷。",
                "从此，她们之间就没有了交集。",
                45)
    }

}