package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.05.18-16:51:57.
 */
class YssmTest : BaseNovelContextText(Yssm::class) {
    @Test
    fun search() {
        search("都市")
        search("都市之超品狂兵", "痞子老妖", "298/298400")
        search("斗破苍穹之最穿越系统", "优言", "273/273061")
        search("恐怖广播", "纯洁滴小龙", "116/116459")
    }

    @Test
    fun detail() {
        // https://www.yssm.org/uctxt/37/37777/
        detail("37/37777", "37/37777", "全能高手都市护花", "一朵小白",
                "https://www.snwx8.com/modules/article/images/nocover.jpg",
                "会医术，会读心，厨艺精，好身手…想提升某项能力？那就动动手指头选择…\n" +
                        "一个倒了十八年霉的悲催孩子，在悲催地与恶少结仇后，终于人品大爆发，得到了一个来自两百年后的系统，有逆天的宿主升级功能，还附带兑换系统…\n" +
                        "&nb",
                "2015-3-22 21:28:31")
    }

    @Test
    fun chapters() {
        // https://www.yssm.org/uctxt/294/294596/
        chapters("294/294596",
                "第二十三章 窃听风云 上", "294/294596/1514862", null,
                "最终章", "294/294596/1514872", null,
                11)
        // https://www.yssm.org/uctxt/57/57794/
        chapters("57/57794",
                "第1章 驱逐之人", "57/57794/278290", null,
                "第3743章 另外的‘仙’缘", "57/57794/850773", null,
                3746)
    }

    @Test
    fun content() {
        // https://www.yssm.org/uctxt/57/57794/850773.html
        content("57/57794/850773",
                "昆仑一界，一座座仙宫沐浴在星辰光芒之下。",
                "破而后立！在败给李叶后，以这种强大无比的姿态再一次从死亡的深渊中爬了出来！甚至比原本的他更加恐怖。",
                66)
    }
}
