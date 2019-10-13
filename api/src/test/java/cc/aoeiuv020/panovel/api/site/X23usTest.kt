package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.08-18:03:24.
 */
class X23usTest : BaseNovelContextText(X23us::class) {
    @Test
    fun search() {
        search("都市")
        search("帝霸", "厌笔萧生", "64889")
    }

    @Test
    fun detail() {
        detail("64889", "64889", "帝霸", "厌笔萧生",
                "https://www.x23us.com/files/article/image/64/64889/64889s.jpg",
                "天若逆我，我必封之，神若挡我，我必屠之——站在万族之巅的李七夜立下豪言！\n" +
                        "这是属于一个平凡小子崛起的故事，一个牧童走向万族之巅的征程。\n" +
                        "在这里充满神话与奇迹，天魔建起古国，石人筑就天城，鬼族铺成仙路，魅灵修补神府……",
                "2018-06-08 00:00:00")
    }

    @Test
    fun chapters() {
        chapters("64889", "契子（读者必看 非常重要）", "64/64889/26487455", null,
                "第3800章三才剑法", "64/64889/33839747", "2019-10-11 00:00:00",
                3842)
    }

    @Test
    fun content() {
        content("64/64889/26487455",
                "契子",
                "一只不甘命运被左右的乌鸦，对抗着天地最可怕的存在，左右着千万年中的一个又一个大时代的变迁！",
                24)
    }

}