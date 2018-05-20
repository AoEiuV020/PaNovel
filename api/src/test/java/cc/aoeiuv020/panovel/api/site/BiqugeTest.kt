package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.NovelDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 *
 * Created by AoEiuV020 on 2017.10.08-21:52:04.
 */
class BiqugeTest {
    init {
        System.setProperty("org.slf4j.simpleLogger.log.Biquge", "trace")
    }

    private lateinit var context: Biquge
    @Before
    fun setUp() {
        context = Biquge()
    }

    @Test
    fun searchNovelName() {
        context.searchNovelName("最大权限").let {
            it.forEach { novelItem ->
                println(novelItem)
            }
            assertTrue(it.any { novelItem ->
                novelItem.name == "最大权限"
            })
        }
    }

    @Test
    fun getNovelDetail() {
        val assertBlock: (NovelDetail) -> Unit = {
            assertEquals("最大权限", it.novel.name)
            assertEquals("肥鱼马甲", it.novel.author)
            assertEquals("制作游戏成功的林陨意外猝死，穿越到自己制作的游戏世界，结果拥有了这个世界的最大权限！\n" +
                    "最大权限书友群：305908807", it.introduction)
            println(it.bigImg)
            println(it.update)
        }
        context.getNovelDetail("http://www.biquge.cn/book/18156/").let(assertBlock)
        context.getNovelDetail("http://www.biquge.cn/book/18156/").let(assertBlock)
    }

    @Test
    fun getNovelItem() {
        context.getNovelItem("https://m.biqubao.com/book/18156/").let {
            assertEquals("最大权限", it.name)
            assertEquals("肥鱼马甲", it.author)
        }
    }

    @Test
    fun getNovelChaptersAsc() {
        context.getNovelChaptersAsc("http://www.biqubao.com/book/18156/").let { list ->
            list.forEach {
                println(it)
            }
            assertEquals("序章", list.first().name)
        }
    }

    @Test
    fun getNovelText() {
        context.getNovelText("http://www.biqubao.com/book/18156/8791124.html").textList.let {
            assertEquals(38, it.size)
            assertEquals("中年男子下意识的接住魂晶，这东西能够让卡片使更好的修炼魂力，可以说是硬通货，属于最高等的金钱。", it.first())
            assertEquals("“没什么，绿色级别吗，这不是问题。”林陨摇了摇头，接着拿出一张卡片，直接激发，同时林陨的气息也迅速增强，瞬间魂力翻倍，直接突破一千，迈过绿色级别的门槛。", it.last())
        }
    }
}