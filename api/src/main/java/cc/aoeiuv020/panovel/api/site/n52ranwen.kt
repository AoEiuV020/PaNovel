package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.divide
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern

/**
 * Created by AoEiuV020 on 2018.06.08-19:34:21.
 */
class N52ranwen : DslJsoupNovelContext() {init {
    // 这网站连接超时了，
    // 是被墙了，
    enabled = false
    site {
        name = "燃文小说"
        baseUrl = "https://www.52ranwen.cc"
        logo = "https://raw.githubusercontent.com/AoEiuV020/PaNovel/static/logo/52ranwen.png"
    }
    search {
        get {
            // 电脑版是百度搜索，
            // http://m.52ranwen.cc/modules/article/search.php?searchtype=articlename&searchkey=%E9%83%BD%E5%B8%82&page=1
            url = "//m.52ranwen.cc/modules/article/search.php"
            data {
                "searchtype" to "articlename"
                "searchkey" to it
                // 加上&page=1可以避开搜索时间间隔的限制，
                // 也可以通过不加载cookies避开搜索时间间隔的限制，
                "page" to "1"
            }
        }
        document {
            items("#jieqi_page_contents > div") {
                extra("> a")
                name("> a > div.row_text > h4")
                /*
                <p class="gray">徐奇峰 | 都市逍遥狂少
	<br>    逍遥狂少身份神秘，功夫逆天，艳福顶级，纵意花都翻手为云覆手为雨。各路恶..</p>
                 */
                author("> a > div.row_text > p") {
                    it.ownText().divide('|').first.trim()
                }
            }
        }
    }
    // http://www.52ranwen.cc/book/80534.html
    // http://www.52ranwen.cc/book/80/80534/
    // http://www.52ranwen.cc/book/80/80534/20611237.html
    // http://m.52ranwen.cc/book/80534.html
    // http://m.52ranwen.cc/book/80/80534/
    // http://m.52ranwen.cc/book/80/80534/20611246.html
    bookIdRegex = "/book/(\\d+/)?(\\d+)(\\.html|/\\d+)"
    bookIdIndex = 1
    detailPageTemplate = "/book/%s.html"
    detail {
        document {
            novel {
                name("body > div.container > div > div:nth-child(3) > div.book-main > div.book-head > div.title > h1 > a")
                author("body > div.container > div > div:nth-child(3) > div.book-main > div.book-head > div.title > span", block = pickString("作\\s*者：(\\S*)"))
            }
            image("body > div.container > div > div:nth-child(3) > div.book-main > div.book-body > div.img > a > img")
            update("body > div.container > div > div:nth-child(3) > div.book-main > div.book-bottom > div.hd > em", format = "更新时间：yyyy-MM-dd HH:mm")
            introduction("body > div.container > div > div:nth-child(3) > div.book-main > div.book-body > div.content > div.intro", block = ownLinesString())
        }
    }
    // http://www.52ranwen.cc/book/80/80534/
    chapterDivision = 1000
    chaptersPageTemplate = "/book/%d/%s/"
    chapters {
        document {
            items("body > div.directory.area > div.booklist > dl > dd > a")
        }
    }
    // http://www.52ranwen.cc/book/80/80534/20611258.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items("#content", block = ownLines())
        }
    }
}
}
