package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.03-17:18:16.
 */
class BiqugebookTest : BaseNovelContextText(Biqugebook::class) {
    @Test
    fun search() {
        search("都市")
        search("都市修仙高手", "雪无泪", "76572")
        search("我女儿是鬼差", "森刀无伤", "94492")
    }

    @Test
    fun detail() {
        detail("94492", "94492", "我女儿是鬼差", "森刀无伤",
                "http://www.biqugebook.com/files/article/image/94/94492/94492s.jpg",
                "超级鬼修徐乐穿越到平行世界，成了一枚渣男……\n" +
                        "名下多了负几十万的存款，以及，一个破碎的小家庭。\n" +
                        "孩子妈是集团高管，年薪百万，美貌如花，然而在法律上，两人没有任何关系……\n" +
                        "女儿六岁，目前在读小学，长得好看，人又乖巧，无可挑剔，可是……\n" +
                        "她却是个鬼差！！！\n" +
                        "熊孩子不好好读书，成天跟游魂野鬼打交道，时不时还会往家里领！\n" +
                        "最重要的是，她还天真的以为徐乐什么都不知道！\n" +
                        "好吧，你开心就好……\n" +
                        "（新书上传打滚求收藏求推荐票……）",
                "2018-05-31 17:50:00")
    }

    @Test
    fun chapters() {
        chapters("94492", "第一章 渣男！", "94492/33000962", null,
                "第128章 邪恶又烧脑的台词", "94492/34336305", "2018-05-31 17:50:00",
                130)
    }

    @Test
    fun content() {
        content("94492/33000962",
                "“粑粑，贝贝好看嘛？”",
                "穿着一件破烂外衣，身材有些伛偻，看不出男女，脸上布满血污，小腹处有一个贯穿",
                40)
    }

}