package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstThreeIntPattern

/**
 * Created by AoEiuV020 on 2018.06.03-13:18:07.
 */
class Gxwztv : DslJsoupNovelContext() {init {
    site {
        name = "梧州中文台"
        baseUrl = "https://www.gxwztv.com/"
        logo = "https://www.gxwztv.com/images/logo.png"
    }
    search {
        get {
            // https://www.gxwztv.com/search.htm?keyword=%E9%83%BD%E5%B8%82
            url = "/search.htm"
            data {
                "keyword" to it
            }
        }
        document {
            items("#novel-list > ul > li:not(:nth-child(1)):not(:nth-last-child(1))") {
                name(" > div.col-xs-3 > a")
                author(" > div:nth-child(4)")
            }
        }
    }
    // https://www.gxwztv.com/ba9814.shtml
    bookIdRegex = "((/ba)|(/\\d+/))(\\d+)"
    bookIdIndex = 3
    detailPageTemplate = "/ba%s.shtml"
    detail {
        document {
            novel {
                /*
                <h1>帝霸　<small>作者：厌笔萧生</small></h1>
                 */
                name("#btop-info > div > article > div > div.col-xs-8 > ul > li:nth-child(1) > h1") {
                    it.ownText().removeSuffix("　")
                }
                author("#btop-info > div > article > div > div.col-xs-8 > ul > li:nth-child(1) > h1 > small",
                        block = pickString("作者：(\\S*)"))
            }
            image("#btop-info > div > article > div > div.col-xs-2 > img")
            update("#btop-info > div > article > div > div.col-xs-8 > ul > li:nth-child(4)", format = "yyyy-MM-dd HH:mm", block = pickString("更新时间：(.*)"))
            /*
            <p style="" id="all">千万年前，李七夜栽下一株翠竹。
            八百万年前，李七夜养了一条鲤鱼。五百万年前，李七夜收养一个小女孩。
            今天，李七夜一觉醒来，翠竹修练成神灵，鲤鱼化作金龙，小女孩成为九界女帝。
            这是一个养成的故事，一个不死的人族小子养成了妖神、养成了仙兽、养成了女帝的故事
            <a class="unfold" href="javascript:$('#shot,#all').toggle();">[收起]</a>
            </p>
             */
            introduction("#all") {
                it.ownTextList().joinToString("\n")
            }
        }
    }
    // https://www.gxwztv.com/9/9814/
    chapterDivision = 1000
    chaptersPageTemplate = "/%d/%s/"
    chapters {
        document {
            items("#chapters-list > li > a")
        }
    }
    // https://www.gxwztv.com/9/9814/166515215.html
    bookIdWithChapterIdRegex = firstThreeIntPattern
    contentPageTemplate = "/%s.html"
    content {
        document {
            items("#txtContent")
        }
    }
}
}

