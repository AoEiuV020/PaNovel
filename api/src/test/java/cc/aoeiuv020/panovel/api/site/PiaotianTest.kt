package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 *
 * Created by AoEiuV020 on 2017.10.02-16:07:05.
 */
class PiaotianTest : BaseNovelContextText(Piaotian::class) {
    @Test
    fun search() {
        search("都市")
        search("斗破苍穹")
        search("从前有座灵剑山")
        search("从前")
    }

    @Test
    fun detail() {
        detail("8/8605", "8/8605", "剑灵同居日记", "国王陛下",
                "https://www.piaotian.com/files/article/image/8/8605/8605s.jpg",
                "“天外神剑剑灵，应呼唤而苏醒，我问你，你就是我的坐骑么？”\n一个无敌的随身剑灵与天才美少女（们）的同居故事。",
                "2018-05-21 17:30:00")
    }

    @Test
    fun chapters() {
        chapters("4/4316", "序幕：天外飞仙+第一章：客栈柴房温暖如春", "4/4316/2216316", null,
                "第七十七章：再见", "4/4316/4260402", null,
                852)
        chapters("8/8912",
                "第001章 狂暴系统", "8/8912/5786830", null,
                "第3568章 大道无门？（补一）", "8/8912/6442359", null,
                3564)
    }

    @Test
    fun content() {
        content("8/8605/5582838",
                "6月1日凌晨0点，本书正式上架。",
                "请各位绅士们量力而行，不必强求逆天。",
                21)
    }
}
