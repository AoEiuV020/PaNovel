package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.textListSplitWhitespace
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext

/**
 * Created by AoEiuV020 on 2018.06.09-19:08:51.
 */
class Zzdxsw : DslJsoupNovelContext() {init {
    // 网速过慢，
    enabled = false
    site {
        name = "猪猪岛小说网"
        baseUrl = "http://www.zzdxsw.org"
        logo = "http://www.zzdxsw.org/images/logo.png"
    }
    search {
        get {
            // http://www.zzdxsw.org/plus/search.php?kwtype=0&searchtype=&q=%B6%BC%CA%D0
            url = "/plus/search.php"
            charset = "GBK"
            data {
                "kwtype" to "0"
                "searchtype" to ""
                "q" to it
            }
        }
        document {
            items("#main > ul > li") {
                name("> h2 > a")
                author("> div.words > p.state > a:nth-child(1)")
            }
        }
    }
    // http://www.zzdxsw.org/chaojibingwangzaidushi1/
    // http://m.zzdxsw.org/yinhunjiaoqi_laogong_biewanwo/
    // 不能匹配到主机名前面的双斜杆//，
    bookIdRegex = "(//[^/]+)?/([^/]+)"
    bookIdIndex = 1
    detailPageTemplate = "/%s/"
    detail {
        document {
            novel {
                name("body > div:nth-child(6) > div > div.Left > div.j_box > div.title > h1")
                author("body > div:nth-child(6) > div > div.Left > div.j_box > div.info > ul > li:nth-child(1) > a")
            }
            image("body > div:nth-child(6) > div > div.Left > div.p_box > div.pic > a > img")
            update("body > div:nth-child(6) > div > div.Left > div.j_box > div.words", format = "yyyy-MM-dd HH:mm", block = pickString("\\((\\d+-\\d+-\\d+ \\d+:\\d+)\\)"))
            /*
<p>简介：<br>陈辰是个逆天的宅男，是的！绝对逆天！
    在春梦的意Y中死去，如此彪悍的行为连地藏王菩萨都不敢收他！
    再加上他乃是九世处男，情债累累，月老都说了，不把这些情债还清，你丫就别想投胎！
    好吧！带着地藏王菩萨赠送的超级泡*妞*笔记本，陈辰华丽丽的穿越了！
    既然重新来过，老爸就不能只是个科级小干部！玛尼？正*部*级？那是必须的！
    既然重新来过，达官贵人的女儿绝对不能放过，超级大财团的贵妇董事长也不能错过！可是，尼玛为什么连外国公主，国际大明星都会爱上我？
    简单的说，这是一个彪悍的少年肆虐都市花丛的拉风故事！
    小萝莉、美御姐、贵女王、俏人妻——一个都不能少！</p>
             */
            introduction("body > div:nth-child(6) > div > div.Left > div.j_box > div.words > p") {
                it.textListSplitWhitespace().drop(1).joinToString("\n")
            }
        }
    }
    chapters {
        document {
            items("body > div:nth-child(9) > div.Con.jj_pl > div.list_box > ul > li > a")
            lastUpdate("body > div:nth-child(6) > div > div.Left > div.j_box > div.words", format = "yyyy-MM-dd HH:mm", block = pickString("\\((\\d+-\\d+-\\d+ \\d+:\\d+)\\)"))
        }
    }
    // http://www.zzdxsw.org/dushizhizongyihuacong/84310.html
    bookIdWithChapterIdRegex = "(//[^/]+)?/([^./]+/\\d+)"
    bookIdWithChapterIdIndex = 1
    contentPageTemplate = "/%s.html"
    content {
        document {
            items("div.box_box")
        }
    }
}
}

