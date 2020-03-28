package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2020.03.28-22:33:42.
 */
class N168kanshuTest : BaseNovelContextText(N168kanshu::class) {
    @Test
    fun search() {
        search("都市")
        search("女总裁的近身高手", "不吃老鼠的猫", "10/10273")
        search("英雄？我早就不当了", "无聊看看天", "98/98881")
    }

    @Test
    fun detail() {
        detail("98/98881", "98/98881", "英雄？我早就不当了", "无聊看看天",
                "https://www.168kanshu.com/files/article/image/98/98881/98881s.jpg",
                "《英雄？我早就不当了》简介：\n" +
                        "伴随着各种各样的危机与绝望而来的，是人类第一个真正意义上的超级英雄。\n" +
                        "在他的带动下，不断涌现出了大量的超级英雄。但是他在持续战斗了五年之后，却消失在了人们的视线之中。\n" +
                        "您要是觉得《\n" +
                        "英雄？我早就不当了\n" +
                        "》还不错的话请不要忘记向您QQ群和微博微信里的朋友推荐哦！\n" +
                        "https://www.168kanshu.com/xs/98/98881/",
                null)
    }

    @Test
    fun chapters() {
        chapters("98/98881", "第一章", "98/98881/46980584", null,
                "深海降临（38）", "98/98881/75724228", null,
                1557)
    }

    @Test
    fun content() {
        content("98/98881/75724228",
                "一秒记住【168看书】手机访问：m.168kanshu.com",
                "【提示】：如果觉得此文不错，请推荐给更多小伙伴吧！分享也是一种享受。",
                267)
    }
}