package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.ownTextListSplitWhitespace
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.regex.matches
import cc.aoeiuv020.regex.pick
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by AoEiuV020 on 2018.06.03-18:49:36.
 */
class Jdxs520 : DslJsoupNovelContext() {init {
    site {
        name = "经典小说520"
        baseUrl = "http://www.jdxs5200.net"
        logo = "http://www.jdxs5200.net/jdxs/images/logo.png"
    }
    search {
        get {
            // http://www.jdxs5200.com/modules/article/search.php?searchtype=articlename&searchkey=%B6%BC%CA%D0&page=1
            charset = "GBK"
            url = "/modules/article/search.php"
            data {
                "searchtype" to "articlename"
                "searchkey" to it
                // 加上&page=1可以避开搜索时间间隔的限制，
                // 也可以通过不加载cookies避开搜索时间间隔的限制，
                "page" to "1"
            }
        }
        document {
            if (URL(root.ownerDocument().location()).path.startsWith("/book_")) {
                single {
                    name("#info > h1")
                    author("#info > span")
                }
            } else {
                items("body > div.warpper > div.o_all > div.o_content > div > table > tbody > tr:not(:nth-child(1))") {
                    name("> td:nth-child(1) > a")
                    author("> td:nth-child(3)")
                }
            }
        }
    }
    // http://www.jdxs5200.com/book_77903/
    bookIdRegex = "/book_(\\d+)"
    detailPageTemplate = "/book_%s/"
    detail {
        document {
            novel {
                name("#info > h1")
                author("#info > span")
            }
            image("#fmimg > img")
            /*
            <div id="intro">
                本书已更新<b class="txt-lg txt-orange">202万</b>字，念念不忘，总裁乘胜追妻最新章节：<a class="link-lastchapter" href="46358897.html" title="第148章 重新开始">第148章 重新开始</a>(2018-06-02 21:52)
                <br>&nbsp;&nbsp;初见，她在下，他在上，他的口中叫着别人的名字。<br>
&nbsp;&nbsp;再见，她衣裳凌乱，披头散发，被人屈辱按在地上，狼狈不堪……<br>
&nbsp;&nbsp;他是人人敬畏的传奇人物，霍家太子爷。<br>
&nbsp;&nbsp;顺手救下她，冷漠送她四个字“咎由自取！”<br>
&nbsp;&nbsp;狼狈的她，却露出一抹明媚的笑，声音清脆“姐夫……谢谢啊！”
                念念不忘，总裁乘胜追妻是作者七爷精心创作的玄幻小说大作，经典小说网实时同步更新念念不忘，总裁乘胜追妻最新章节无弹窗广告版。书友所发表的念念不忘，总裁乘胜追妻评论，并不代表经典小说网赞同或者支持念念不忘，总裁乘胜追妻读者的观点。
                <br>七爷的其他作品：<a href="/book_130135/" target="_blank">爱不言衷</a>、<a href="/book_124032/" target="_blank">怦然心动， 娇妻心间宠</a>、<a href="/book_40728/" target="_blank">冷血总裁放过我</a>
                <br>您要是觉得《念念不忘，总裁乘胜追妻》还不错，请点击右上角的分享按钮分享到你的朋友圈吧！
            </div>
             */
            introduction("#intro") {
                it.textNodes()
                        .dropWhile { !it.wholeText.trim().matches("\\(\\S+ \\S+\\)") }
                        .also {
                            update = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).parse(it.first().wholeText.trim().pick("\\((\\S+ \\S+)\\)").first())
                        }
                        .drop(1)
                        .dropLastWhile { !it.wholeText.trim().endsWith("的其他作品：") }
                        .dropLast(1)
                        .flatMap { it.ownTextListSplitWhitespace() }
                        .dropLast(1)
                        .joinToString("\n")
            }
        }
    }
    chapters {
        document {
            items("body > div:nth-child(4) > div:nth-child(1) > ul > li > a")
            lastUpdate("#intro", format = "yyyy-MM-dd HH:mm", block = pickString("\\((\\S+ \\S+)\\)"))
        }
    }
    // http://www.jdxs5200.com/book_77903/46358897.html
    bookIdWithChapterIdRegex = "/book_(\\d+/\\d+)"
    contentPageTemplate = "/book_%s.html"
    content {
        document {
            items("#htmlContent") {
                it.textNodes().drop(1)
                        .flatMap { it.ownTextListSplitWhitespace() }
            }
        }
    }
}
}

