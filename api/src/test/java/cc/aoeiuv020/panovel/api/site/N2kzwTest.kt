package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.regex.compilePattern
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.08-18:49:05.
 */
class N2kzwTest : BaseNovelContextText(N2kzw::class) {
    @Test
    fun search() {
        search("都市")
        search("一世之尊", "爱潜水的乌贼", "0/852")
    }

    @Test
    fun detail() {
        detail("3/3427", "3/3427", "遮天", "辰东",
                "http://www.2kzw.com/files/article/image/3/3427/3427s.jpg",
                "冰冷与黑暗并存的宇宙深处，九具庞大的龙尸拉着一口青铜古棺，亘古长存。\n" +
                        "这是太空探测器在枯寂的宇宙中捕捉到的一幅极其震撼的画面。\n" +
                        "九龙拉棺，究竟是回到了上古，还是来到了星空的彼岸？\n" +
                        "一个浩大的仙侠世界，光怪陆离，神秘无尽。热血似火山沸腾，激情若瀚海汹涌，欲望如深渊无止境……\n" +
                        "登天路，踏歌行，弹指遮天。",
                null)
        detail("0/852", "0/852", "一世之尊", "爱潜水的乌贼",
                "http://www.2kzw.com/files/article/image/0/852/852s.jpg",
                "我这一生，不问前尘，不求来世，只轰轰烈烈，快意恩仇，败尽各族英杰，傲笑六道神魔！",
                null)
    }

    @Test
    fun chapters() {
        chapters("3/3427", "第0001章 星空中的青铜巨棺", "3/3427/5363475", null,
                "第1821章 遮天", "3/3427/5467373", null,
                1821)
        chapters("0/852", "第0001章 机心", "0/852/3064897", null,
                "第1397章 机关算尽太聪明（大结局）", "0/852/3155459", null,
                1397)
    }

    @Test
    fun content() {
        content("3/3427/5363475",
                "第001章 星空中的青铜巨棺",
                "“呼叫地球……”",
                50)
        content("0/852/3155459",
                "第四十九章 机关算尽太聪明（大结局）",
                "==========================================================",
                99)
    }

    @Test
    fun pickNameTest() {
        // 这网站所有名字都是/遮天(精校版)/官道无疆(校对版)/
        val pickName = { e: String ->
            e.replace(compilePattern("\\(\\S+版\\)$").toRegex(), "")
        }
        assertEquals("遮天", pickName("遮天(精校版)"))
    }

}