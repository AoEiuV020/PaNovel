package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.03-22:26:59.
 */
class GulizwTest : BaseNovelContextText(Gulizw::class) {
    @Test
    fun search() {
        search("都市")
        search("一念永恒", "耳根", "94719")
        search("我是至尊", "风凌天下", "5141")
    }

    @Test
    fun detail() {
        detail("94719", "94719", "一念永恒", "耳根",
                "http://www.gulizw.com/files/article/image/94/94719/94719s.jpg",
                "一念成沧海，一念化桑田。一念斩千魔，一念诛万仙。唯我念……永恒",
                "2018-02-09 00:00:00")
    }

    @Test
    fun chapters() {
        chapters("52771", "第1章 黄山真君和九洲一号群", "52771/20455308", null,
                "第1743章 你就不能给我正常的晋升一回吗？", "52771/37939770", null,
                1824)
        chapters("94719", "外传1 柯父。", "94719/28723558", null,
                "第1314章 你的选择（终）", "94719/37928328", null,
                1529)
    }

    @Test
    fun content() {
        content("94719/37928328",
                "画面在这一刻，成为了永恒，渐渐模糊，直至消散。",
                "六月一号，不见不散！",
                21)
    }

}