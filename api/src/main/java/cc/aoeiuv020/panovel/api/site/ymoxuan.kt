package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern
import cc.aoeiuv020.panovel.api.reverseRemoveDuplication

/**
 * Created by AoEiuV020 on 2018.06.03-13:56:04.
 */
class Ymoxuan : DslJsoupNovelContext() {init {
    // 换了域名http://www.yanmoxuan.net/， 然后502,
    hide = true
    site {
        name = "衍墨轩"
        baseUrl = "https://www.yanmoxuan.org"
        logo = "https://www.ymxxs.com/image/logo.gif"
    }
    search {
        get {
            // https://www.ymoxuan.com/search.htm?keyword=%E9%83%BD%E5%B8%82
            url = "/search.htm"
            data {
                "keyword" to it
            }
        }
        document {
            items("body > section > div.left > section > ul > li:not(:nth-child(1)):not(:nth-last-child(1))") {
                name("> span.n2 > a")
                author("> span.a2 > a")
            }
        }
    }
    // https://www.ymoxuan.com/text_321.html
    bookIdRegex = "((/text_)|(/\\d+/))(\\d+)"
    bookIdIndex = 3
    detailPageTemplate = "/text_%s.html"
    detail {
        document {
            novel {
                name("body > section > div.left > article.info > header > h1")
                /*
                <i><a href="/author/骷髅精灵/" title="骷髅精灵 作品大全">骷髅精灵</a></i>
                 */
                // 以防万一像起点那样有的作者不给链接，这里用i不用a,
                author("body > section.container > div.left > article.info > p.detail.pt20 > i:nth-child(1)")
            }
            image("body > section.container > div.left > article.info > div.cover > img")
            update("body > section.container > div.left > article.info > p:nth-child(4) > i", format = "yyyy-MM-dd HH:mm")
            /*
            <p class="desc ">双月当空，无限可能的英魂世界<br>
            孤寂黑暗，神秘古怪的嬉命小丑<br>
            百城联邦，三大帝国，异族横行，魂兽霸幽<br>
            这是一个英雄辈出的年代，人类卧薪尝胆重掌地球主权，孕育着进军高纬度的野望！<br>
            重点是……二年级的废柴学长王同学，如何使用嬉命轮盘，撬动整个世界，伙伴们，请注意，学长来了！！！
            </p>
             */
            introduction("body > section.container > div.left > article.info > p.desc", block = ownLinesString())
        }
    }
    // https://www.ymoxuan.com/book/0/321/index.html
    chapterDivision = 1000
    chaptersPageTemplate = "/book/%d/%s/index.html"
    chapters {
        document {
            items("body > section > article > ul > li > a")
        }.reverseRemoveDuplication()
    }
    // https://www.ymoxuan.com/book/0/321/56473.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items("#content")
        }
    }
}
}

