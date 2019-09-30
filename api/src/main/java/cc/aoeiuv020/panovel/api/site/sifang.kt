package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.title
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.regex.pick

class SiFang: DslJsoupNovelContext(){init{
    site{
        name = "四方中文网"
        baseUrl = "http://www.sifangbook.com/"
        logo = "http://www.sifangbook.com/static/index/Img/logo.jpg"
    }

    search {
        get {
            url = "/booklibrary/index"
            data {
                "keywords" to it
            }
        }
        document {
            items("#BooklibraryIndex > div.BooklibraryList > ul > li"){
                name("> span > a")
                author("> a > dl > dd.nickname")
            }

        }
    }
    detailPageTemplate = "/booklibrary/show/id/%s/"
    detail {
        document {
            novel {
                name("#BooklibraryShow_Left > div.BookContent > div > div.title > span")
                author("#BooklibraryShow_Left > div.BookContent > div > div.title > em"){
                    it.text().removeSuffix(" 著")
                }
            }
            image("#BooklibraryShow_Left > div.BookContent > div > div.pic > img")
            introduction("#BooklibraryShow_Left > div.BookContent > div > div.jianjie")
            update("#BooklibraryShow_Left > div.BookContent > div > div.title > i",format = "yyyy-MM-dd HH:mm:ss"){
                it.title().pick("更新时间：.*").first()
            }
        }
    }
    chaptersPageTemplate="/booklibrary/readdir/id/%s/"
    chapters {
        document {
            items("#BooklibraryShow_Left > div.BookDir > div.chapter_list > ul > li:nth-child(1) > a")
            lastUpdate("#BooklibraryShow_Left > div.BookContent > div > div.title > i",format = "yyyy-MM-dd HH:mm:ss"){
                it.title().pick("更新时间：.*").first()
            }
        }
    }
    content {
        document {
            items("#ChapterContent")
        }
    }
}
}