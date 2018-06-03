package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.03-21:27:57.
 */
class N7dswTest : BaseNovelContextText(N7dsw::class) {
    @Test
    fun search() {
        search("都市")
        search("江湖我独行", "心之弈剑", "0/133")
        search("重生之神级败家子", "辰机唐红豆", "1/1133")
    }

    @Test
    fun detail() {
        detail("0/654", "0/654", "魔天记", "忘语",
                "https://www.7dsw.com/files/article/image/0/654/654s.jpg",
                "一名在无数凶徒中长大的亡命少年，一个人与魔并立的时代，一个可以役使厉鬼、妖灵的大千世界……",
                null)
    }

    @Test
    fun chapters() {
        chapters("0/654", "凡人感言", "0/654/178709", null,
                "忘语新书《玄界之门》", "0/654/10148254", null,
                1606)
    }

    @Test
    fun content() {
        content("0/654/10148254",
                "序章",
                "忘语新书《玄界之门》已经在正式上传连载了，还望喜欢《凡人修仙传》和《魔天记》的道友们，去关注和收藏一下哦。）(未完待续。)",
                8)
    }

}