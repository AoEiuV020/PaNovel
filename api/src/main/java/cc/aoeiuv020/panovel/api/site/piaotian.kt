@file:Suppress("UnnecessaryVariable")

package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.href
import cc.aoeiuv020.base.jar.ownTextListSplitWhitespace
import cc.aoeiuv020.base.jar.path
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.firstTwoIntPattern
import cc.aoeiuv020.regex.pick
import java.net.URL

/**
 *
 * Created by AoEiuV020 on 2017.10.02-16:03:02.
 */
class Piaotian : DslJsoupNovelContext() {init {
    // 被墙了，但是墙外正常，
    site {
        name = "飘天文学"
        baseUrl = "https://www.ptwxz.com"
        logo = "https://www.ptwxz.com/css/logo.gif"
    }
    search {
        get {
            // https://www.piaotian.com/modules/article/search.php?searchtype=articlename&searchkey=%B6%BC%CA%D0&page=1
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
            // 搜索结果可能直接跳到详情页，
            if (URL(root.ownerDocument().location()).path.startsWith("/bookinfo")) {
                single {
                    val tbody1 = element("#content > table > tbody")
                    val tbody2 = element("tr:nth-child(1) > td > table > tbody", parent = tbody1)
                    val pattern = "" +
                            "(\\S*)\\s" +
                            "类\\s*别：(\\S*)\\s" +
                            "作\\s*者：(\\S*)\\s" +
                            "管\\s*理\\s*员：(\\S*)\\s" +
                            "全文长度：(\\S*)\\s" +
                            "最后更新：(\\S*)\\s" +
                            "文章状态：(\\S*)\\s" +
                            "授权级别：(\\S*)\\s" +
                            "首发状态：(\\S*)\\s" +
                            "收\\s*藏\\s*数：(\\S*)\\s" +
                            "总推荐数：(\\S*)\\s" +
                            "本月推荐：(\\S*)\\s" +
                            "收到鲜花：(\\S*)" +
                            ""
                    val list = tbody2.text().pick(pattern)
                    name = list[0]
                    author = list[2]
                }
            } else {
                /*
                <a href="https://www.piaotian.com/bookinfo/9/9312.html">都市最强装逼系统</a>
                 */
                items("#content > table.grid > tbody > tr:not(:nth-child(1))") {
                    name("td:nth-child(1) > a")
                    author("td:nth-child(3)")
                }
            }
        }
    }
    bookIdRegex = firstTwoIntPattern
    detailPageTemplate = "/bookinfo/%s.html"
    detail {
        document {
            val tbody1 = element("#content > table > tbody")
            val tbody2 = element("tr:nth-child(1) > td > table > tbody", parent = tbody1)
            val pattern = "" +
                    "(\\S*)\\s" +
                    "类\\s*别：(\\S*)\\s" +
                    "作\\s*者：(\\S*)\\s" +
                    "管\\s*理\\s*员：(\\S*)\\s" +
                    "全文长度：(\\S*)\\s" +
                    "最后更新：(\\S*)\\s" +
                    "文章状态：(\\S*)\\s" +
                    "授权级别：(\\S*)\\s" +
                    "首发状态：(\\S*)\\s" +
                    "收\\s*藏\\s*数：(\\S*)\\s" +
                    "总推荐数：(\\S*)\\s" +
                    "本月推荐：(\\S*)\\s" +
                    "收到鲜花：(\\S*)" +
                    ""
            val list = tbody2.text().pick(pattern)
            novel {
                name = list[0]
                author = list[2]
            }
            val td = element("tr:nth-child(4) > td > table > tbody > tr > td:nth-child(2)", parent = tbody1)
            image("a > img", parent = td)
            introduction("div", parent = td, block = ownLinesString())
            update(
                "> tr:nth-child(1) > td > table > tbody > tr:nth-child(3) > td:nth-child(1)",
                parent = tbody1,
                format = "最后更新：yyyy-MM-dd"
            )
        }
    }
    chaptersPageTemplate = "/html/%s/index.html"
    chapters {
        /* https://www.piaotian.com/html/4/4316/index.html
        <ul>
            <li><a href="javascript:window.external.addFavorite('http://www.piaotian.com/bookinfo/4/4316.html','飘天文学-从前有座灵剑山在线阅读')">添加到IE收藏夹</a></li>
            <li><a href="javascript:window.open('http://shuqian.qq.com/post?from=3&amp;title='+encodeURIComponent(document.title)+'&amp;uri='+encodeURIComponent(document.location.href)+'&amp;jumpback=2&amp;noui=1','favit','width=930,height=470,left=50,top=50,toolbar=no,menubar=no,location=no,scrollbars=yes,status=yes,resizable=yes');void(0)">收藏到QQ书签</a></li>
            <li><a href="javascript:window.open('http://cang.baidu.com/do/add?it='+encodeURIComponent(document.title.substring(0,76))+'&amp;iu='+encodeURIComponent(location.href)+'&amp;fr=ien#nw=1','_blank','scrollbars=no,width=600,height=450,left=75,top=20,status=no,resizable=yes'); void 0">+到百度搜藏</a></li>
            <li><a href="#" onclick="window.open('http://myweb.cn.yahoo.com/popadd.html?url='+encodeURIComponent(document.location.href)+'&amp;title='+encodeURIComponent(document.title), 'Yahoo','scrollbars=yes,width=780,height=550,left=80,top=80,status=yes,resizable=yes');">+到雅虎收藏</a></li>
        </ul>
         */
        /* https://www.piaotian.com/html/8/8955/index.html
        <ul>
            <li><a href="5841514.html">新书感言</a></li>
            <li>&nbsp;</li>
            <li>&nbsp;</li>
            <li>&nbsp;</li>
        </ul>
         */
        document {
            items("div.mainbody > div.centent > ul > li > a") {
                name = root.text()
                require(root.href() != "#")
                extra = root.path().let {
                    require(it.isNotBlank())
                    findBookIdWithChapterId(it)
                }
            }
        }
    }
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/html/%s.html"
    content {
        document {
            items("html > body") {
                it.ownTextListSplitWhitespace()
            }
        }
    }
}
}

