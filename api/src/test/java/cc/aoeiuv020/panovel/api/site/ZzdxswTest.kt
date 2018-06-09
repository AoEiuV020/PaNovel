package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.09-19:10:45.
 */
class ZzdxswTest : BaseNovelContextText(Zzdxsw::class) {
    @Test
    fun regex() {
        val r = "(//[^/]+)?/([^/]+)"
        val index = 1
        listOf("http://www.zzdxsw.org/dushizhizongyihuacong/84310.html",
                "/dushizhizongyihuacong/",
                "http://www.zzdxsw.org/dushizhizongyihuacong/")
                .forEach {
                    assertEquals("dushizhizongyihuacong", it.pick(r)[index])
                }
    }

    @Test
    fun search() {
        search("都市")
        search("诡秘之主", "爱潜水的乌贼", "guimizhizhu")
        search("武器大师", "独悠", "wuqidashi")
    }

    @Test
    fun detail() {
        detail("guimizhizhu", "guimizhizhu", "诡秘之主", "爱潜水的乌贼",
                "http://www.zzdxsw.org/uploads/allimg/c180607/152S6360035250-13437.jpg",
                "蒸汽与机械的浪潮中，谁能触及非凡？" +
                        "历史和黑暗的迷雾里，又是谁在耳语？" +
                        "我从诡秘中醒来，睁眼看见这个世界：\n" +
                        "枪械，大炮，巨舰，飞空艇，差分机；" +
                        "魔药，占卜，诅咒，倒吊人，封印物……" +
                        "光明依旧照耀，神秘从未远离，这是一段“愚者”的传说。",
                "2018-06-09 16:24:00")
        detail("wuqidashi", "wuqidashi", "武器大师", "独悠",
                "http://www.zzdxsw.org/uploads/allimg/c150913/144215DH4Z60-1bc.jpg",
                "万族林立，亿城争锋，高手辈出。倚天剑、屠龙刀，争奇斗艳。" +
                        "孔雀翎、霸王枪，各领风骚。" +
                        "地球上最年轻的铸剑大师，异界重生，一鼎九阳神炉，一卷神器图谱，让他如鱼得水，踏临武道巅峰。" +
                        "强者如林，我主沉浮！※本书游戏版权已高价售出，保证完本，绝不太监" +
                        "※※独悠出品，本本精品※",
                "2018-01-02 07:26:00")
    }

    @Test
    fun chapters() {
        chapters("guimizhizhu", "第一章 绯红", "guimizhizhu/9183633", null,
                "第三十九章 有趣的技巧", "guimizhizhu/9192303", "2018-06-09 16:24:00",
                42)
        chapters("wuqidashi", "第1章 九阳神炉", "wuqidashi/1818947", null,
                "第2280章 后记", "wuqidashi/8281229", "2018-01-02 07:26:00",
                2282)
    }

    @Test
    fun content() {
        content("guimizhizhu/9183633",
                "痛！",
                "清晰倒映的镜子如实呈现，一个狰狞的伤口盘踞在他的太阳穴位置，边缘是烧灼的痕迹，周围沾满了血污，而内里有灰白色的脑浆在缓缓蠕动。",
                69)
        content("wuqidashi/8281229",
                "“……”",
                "（全书完）",
                102)
    }

}