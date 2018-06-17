package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.linesString
import net.sf.jazzlib.ZipFile
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.URL

/**
 * Created by AoEiuV020 on 2018.06.16-17:52:10.
 */
class EpubParserTest : ParserTest(EpubParser::class) {

    @Test
    fun url() {
        val file = getFile("/home/aoeiuv/tmp/panovel/epub/打工吧！魔王大人17.epub") ?: return
        listOf(
                "file:/home/aoeiuv/tmp/panovel/epub/打工吧！魔王大人17.epub",
                "file:///home/aoeiuv/tmp/panovel/epub/打工吧！魔王大人17.epub",
                "file://localhost/home/aoeiuv/tmp/panovel/epub/打工吧！魔王大人17.epub"
        ).forEach {
            // file协议三种数量的斜杆/都能支持，
            // 默认URL.toString是一个斜杆/的，
            assertEquals(file.toURI().toURL(), URL(it))
        }
        val coverUrl = URL("jar:${file.toURI()}!/cover1.jpeg")
        assertEquals("jar:file:/home/aoeiuv/tmp/panovel/epub/打工吧！魔王大人17.epub!/cover1.jpeg", coverUrl.toString())
        assertNull(coverUrl.authority)
        assertEquals("", coverUrl.host)
        assertEquals("file:/home/aoeiuv/tmp/panovel/epub/打工吧！魔王大人17.epub!/cover1.jpeg", coverUrl.path)
        assertEquals(coverUrl.path, coverUrl.file)
        // 如果文件不存在，openString会抛FileNotFoundException，
        coverUrl.openStream().use { input ->
            // 图片第一个字符，
            assertEquals(255, input.read())
        }
    }

    @Test
    fun epublibTest() {
        val file = getFile("/home/aoeiuv/tmp/panovel/epub/打工吧！魔王大人17.epub") ?: return
        val zipFile = ZipFile(file)
        val book = EpubReader().readEpub(zipFile)

        book.coverImage.let {
            println(it.href)
        }

        assertEquals("[和ヶ原聡司].打工吧！魔王大人17", book.title)

        val author = book.metadata.authors.first().run { "$firstname $lastname" }

        assertEquals("和ヶ原 聡司", author)

        val intro = book.metadata.descriptions.joinToString("\n") {
            Jsoup.parse(it).body()
                    .linesString()
        }
        assertEquals("魔王大人，在正式职员录用考试中落选！\n" +
                "之后由于木崎的调动命令，职员们乱成一团！！\n" +
                "众望所归的广播剧也预定在2017年6月7日发售！！！\n" +
                "魔王在正式职员录用考试中落选，十分不甘，比以往更加努力工作，展现出神级职员的一面。然而，失去了从异世界安特·伊苏拉流落日本以来一直努力的目标，他的外表与内心都十分消沉。\n" +
                "并且，遭到袭击、保持在可爱小鸡的柔弱状态的卡米奥，还有“大魔王的遗产”中最后一件“星际宝石”的所在地，必须解决的问题堆积如山。\n" +
                "其中，有人在代代木周边目击到被认为握有卡米奥遇袭关键的存在。与天祢共赴现场的魔王撞见了鳄鱼般的恶魔。好不容易确保的、需要照料的三坪房间VILLA\n" +
                "ROSA笹塚201号房间，无论在工作中还是私人事务中，魔王的心都无暇休息……\n" +
                "麦丹劳一侧，由于店长木崎接到调动命令，以惠美和千穗为首，职员们广受动摇。木崎为商量未来事项而面见魔王，惠美和千穗察觉魔王已经没有留在日本的理由，开始感到不安。是回到安特·伊苏拉，还是留在日本继续工作，魔王开始为今后的事业该如何是好而烦恼，他做出的选择是——\n" +
                "为您献上工作成分很多的庶民派FANTASY第17卷！", intro)
        val contents = book.contents
        assertEquals(13, contents.size)
    }
}