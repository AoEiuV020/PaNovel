package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.href
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.regex.pick

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
    // curl 'http://www.mianhuatang2.com/ashx/zj.ashx' -H 'Cookie: ASP.NET_SessionId=szjiqf0uhqlp5tuoca3iogf1; cuid=www.mianhuatang2.com_2019-10-13_11:32:50:378_761; userbook=8287754%EF%BC%83%E4%B8%87%E5%8F%A4%E7%A5%9E%E5%B8%9D%EF%BD%9C8326615%EF%BC%83%E6%BC%AB%E5%A8%81%E4%B8%96%E7%95%8C%E7%9A%84%E6%9C%AF%E5%A3%AB; LookNum=4' -H 'Origin: http://www.mianhuatang2.com' -H 'Accept-Encoding: gzip, deflate' -H 'Accept-Language: zh-CN,zh;q=0.9,zh-TW;q=0.8,en;q=0.7' -H 'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3835.0 Safari/537.36' -H 'Content-Type: application/x-www-form-urlencoded; charset=UTF-8' -H 'Accept: */*' -H 'Referer: http://www.mianhuatang2.com/xs/8287754/' -H 'X-Requested-With: XMLHttpRequest' -H 'Connection: keep-alive' --data 'action=GetZj&xsid=175943' --compressed --insecure
    chapters {
        val xsid = responseBody(connect(getNovelChapterUrl(it)))
                .string()
                .pick("var xsid=(\\d+);")
                .first()

        post {
            url = "/ashx/zj.ashx"
            charset = "GBK"
            data {
                "action" to "GetZj"
                "xsid" to xsid
            }
        }
        document {
            /*
            <a href="http://www.mianhuatang520.com/xs/8326615/82245484.htm">第573章 另有安排</a>
             */
            items("dd > a") {
                name = root.text()
                val cid = root.href().removeSuffix(".htm")
                extra = "$it/$cid"
            }
        }
    }
    // https://www.mianhuatang2.com/xs/8326615/82763935.htm
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

