package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext

/**
 * Created by AoEiuV020 on 2018.06.03-11:12:05.
 */
class Mianhuatang : DslJsoupNovelContext() {init {
    site {
        name = "棉花糖小说"
        baseUrl = "https://www.mianhuatang2.com"
        logo = "http://www.mianhuatang520.com/css/logo.png"
    }
    search {
        // http://www.mianhuatang520.com/search.aspx?bookname=%B6%BC%CA%D0
        get {
            url = "/search.aspx"
            charset = "GBK"
            data {
                "bookname" to it
            }
        }
        document {
            items("#newscontent > div.l > ul > li") {
                name("> span.s2 > a")
                author(" > span.s4")
            }
        }
    }
    // http://www.mianhuatang520.com/xs/8326615/
    detailPageTemplate = "/xs/%s/"
    detail {
        document {
            novel {
                name("#info > h1")
                author("#info > div:nth-child(2)", block = pickString("作\\s*者：(\\S*)"))
            }
            image("#fmimg > img")
            /*
            <div id="intro">
                <p>
                    一念成沧海，一念化桑田。一念斩千魔，一念诛万仙。　　唯我念……永恒　　这是耳根继《仙逆》《求魔》《我欲封天》后，创作的第四部长篇小说《一念永恒》
                </p>
                <p>
                </p>
            </div>
             */
            introduction("#intro > p:not(:nth-last-child(1))")
        }
    }
    chapters {
        document {
            /*
            <a href="http://www.mianhuatang520.com/xs/8326615/82245484.htm">第573章 另有安排</a>
             */
            items("#list > dl > dd > a")
        }
    }
    // http://www.mianhuatang520.com/xs/8326615/81992178.htm
    contentPageTemplate = "/xs/%s.htm"
    content {
        document {
            items("#zjneirong")
        }.dropLastWhile { line ->
            line == "本站小说txt下载无须注册，即下即看！"
                    || line == "本站域名变为 www.mianhuatang2.com"
        }
    }
}
}

