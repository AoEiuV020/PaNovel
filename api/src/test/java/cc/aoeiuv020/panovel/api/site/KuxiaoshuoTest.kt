package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.09-18:47:20.
 */
class KuxiaoshuoTest : BaseNovelContextText(Kuxiaoshuo::class) {
    @Test
    fun search() {
        search("都市")
        search("圣墟", "辰东", "2_2715")
        search("天神诀", "太一生水", "1_1947")
    }

    @Test
    fun detail() {
        detail("2_2715", "2_2715", "圣墟", "辰东",
                "https://r.m.xxbiqudu.com/cover/aHR0cHM6Ly9yLm0ueHhiaXF1ZHUuY29tL2ZpbGVzL2FydGljbGUvaW1hZ2UvMi8yNzE1LzI3MTVzLmpwZw==",
                "在破败中崛起，在寂灭中复苏。\n" +
                        "沧海成尘，雷电枯竭，那一缕幽雾又一次临近大地，世间的枷锁被打开了，一个全新的世界就此揭开神秘的一角……",
                "2018-06-09 00:00:00")
        detail("1_1947", "1_1947", "天神诀", "太一生水",
                "https://r.m.xxbiqudu.com/cover/aHR0cHM6Ly9yLm0ueHhiaXF1ZHUuY29tL2ZpbGVzL2FydGljbGUvaW1hZ2UvMS8xOTQ3LzE5NDdzLmpwZw==",
                "苍穹星域，武魂万千。\n" +
                        "有飞龙在天，可一日千里。有霸气无双，可斗转星移。\n" +
                        "杨青玄身怀十大至强武魂之一“天下有敌”，从此十荡十决，千军辟易！\n" +
                        "（新书上架，望大家多推荐、多宣传、多收藏）",
                "2018-06-09 00:00:00")
    }

    @Test
    fun chapters() {
        chapters("2_2715", "第一章 沙漠中的彼岸花", "2_2715/17380548", null,
                "第1088章 阳间命运的十字路口", "2_2715/151851897", "2018-06-09 00:00:00",
            1104)
        chapters("1_1947", "楔子：身世之谜，时空灰烬", "1_1947/16176821", null,
                "第1770章 求情", "1_1947/160124928", "2018-06-09 00:00:00",
                1776)
    }

    @Test
    fun content() {
        content("2_2715/17380548",
                "大漠孤烟直，长河落日圆。",
                "有异常之事吗？楚风加快脚步，赶到山脚下，临近牧民的栖居地。",
                59)
        content("1_1947/151847211",
                "杨青玄两鬓上淌下汗水，被众人盯着，如芒在背。",
                "玄天机身穿黑袍，五官俊美，淡然微笑道：“大力魔牛王，你还有闲情在这玩耍吗？" +
                        "忘川断流，天河水位失衡，那东西怕是要出世了吧。”",
                46)
    }

}