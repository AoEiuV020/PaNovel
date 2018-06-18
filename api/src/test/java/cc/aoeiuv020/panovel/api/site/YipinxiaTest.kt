package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.03-20:07:30.
 */
class YipinxiaTest : BaseNovelContextText(Yipinxia::class) {
    @Test
    fun search() {
        search("都市")
        search("时空娱乐邪神", "时空", "4")
        search("直播之死亡设计师", "无心恋爱", "2")
    }

    @Test
    fun detail() {
        detail("5542", "5542", "万界建道门", "觅食之野猪",
                "http://www.yipinxia.net/files/article/image/5/5542/5542s.jpg",
                "自从平凡的陈凡有了一个系统后，他的一切都变了。\n" +
                        "什么？有人要和我比武？那个张三丰，你去教他做人。\n" +
                        "什么？有人要和我比兵器？那个谁，你去把我的诛仙剑拿来。\n" +
                        "什么？有人要和我比兵法？那个诸葛亮，你去和他比划两下。\n" +
                        "这是一个人有了系统后，...）",
                null)
    }

    @Test
    fun chapters() {
        chapters("5542", "第一章 系统，我要求退货", "5542/1227954", null,
                "第334章 虞子期", "5542/1228337", null,
                334)
    }

    @Test
    fun content() {
        content("5542/1228337",
                "大司命看着空空如也的对面愣了好久，才愤怒的咆哮道，“好个卑鄙无耻的小贼，居然骗我会横贯八方然后逃走！”",
                "“啊。”三人走后不久，虞子期也是惨叫一声，随后便再次一动不动起来，云中君冷冷的看了天明等人逃跑的方向一眼，不知在想什么。",
                52)
        content("2/1554",
                "![img](http://www.yipinxia.net/files/article/attachment/0/2/1554/1439.gif)",
                "![img](http://www.yipinxia.net/files/article/attachment/0/2/1554/1439.gif)",
                1)
    }

}