package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext

class N69shu : DslJsoupNovelContext() {init {
    site {
        name = "69书吧"
        baseUrl = "https://www.69shu.com"
        logo =
            "http://tiebapic.baidu.com/forum/w%3D580/sign=9e34182a39dda3cc0be4b82831e93905/84431926cffc1e175f800d845d90f603728de9b0.jpg"
    }
    search {
        post {
            // https://www.69shu.com/modules/article/search.php?searchkey=%B6%BC%CA%D0&searchtype=all
            charset = "GBK"
            url = "/modules/article/search.php"
            data {
                "searchkey" to it
                "searchtype" to "all"
                "page" to "1"
            }
        }
        document {
            single("^/txt/") {
                name("div.booknav2 > h1 > a")
                author("div.booknav2 > p:nth-child(2) > a")
            }
            items("div.newbox > ul > li") {
                // 这有点迷，浏览器解析的是div > h3 > a，但jsoup解析出来是div > a > h3 > a
                name("> div.newnav h3 > a")
                author("> div.newnav > div.labelbox > label:nth-child(1)")
            }
        }
    }
    // https://www.69shu.com/txt/35934.htm
    bookIdRegex = "/txt/(\\d+)"
    detailPageTemplate = "/txt/%s.htm"
    detail {
        document {
            novel {
                name("div.booknav2 > h1 > a")
                author("div.booknav2 > p:nth-child(2) > a")
            }
            image("div.bookimg2 > img")
            update("div.booknav2 > p:nth-child(5)", format = "更新：yyyy-MM-dd")
            introduction("div.navtxt > p:nth-child(1)")
        }
    }
    // https://www.69shu.com/35934/
    chaptersPageTemplate = "/%s/"
    chapters {
        document {
            items("#catalog > ul > li > a")
        }
    }
    // https://www.69shu.com/txt/35934/26013675
    contentPageTemplate = "/txt/%s"
    content {
        document {
            items("div.txtnav", block = ownLinesSplitWhitespace())
        }
    }
}
}

