package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext

/**
 * Created by AoEiuV020 on 2018.06.03-14:54:37.
 */
class Qingkan : DslJsoupNovelContext() {init {
    site {
        name = "请看小说"
        baseUrl = "https://www.qingkan9.com"
        logo = "https://www.qingkan9.com/static/image/logo.png"
    }
    search {
        get {
            // https://www.qingkan9.com/novel.php?action=search&searchtype=novelname&searchkey=%B6%BC%CA%D0
            url = "/novel.php"
            charset = "GBK"
            data {
                "action" to "search"
                "searchtype" to "novelname"
                "searchkey" to it
            }
        }
        document {
            // 最后一个div没用，类名也不同，
            items("#right_cont > div.ss_box") {
                name("> h3 > span.sp_name > a")
                /*
                <span class="sp_name">
                <a class="sp_bookname" href="https://www.qingkan9.com/book/dushiyoushenwang/info.html" target="_blank">都市有神王</a>
                 / 芍子
                 </span>
                 */
                author("> h3 > span.sp_name") {
                    // ownText，不取出其中的a,
                    it.ownText().pick("/ (\\S*)").first()
                }
            }
        }
    }
    // https://www.qingkan9.com/book/zhengxu/
    bookIdRegex = "/book/([^/]+)"
    detailPageTemplate = "/book/%s/"
    detail {
        document {
            novel {
                name("#bookname > span > h1")
                author("#aboutbook") {
                    it.textNodes().first().text().pick("作者：(\\S+)").first()
                }
            }
            image = null
            /*
<div id="aboutbook">
作者：何途&nbsp;&nbsp;&nbsp;&nbsp;类别：玄幻奇幻<br>
&nbsp;&nbsp;&nbsp;&nbsp;芸芸众生，众生皆苦。大千世界，三界六道。神、佛、魔、人皆处于轮回之中，
为生死苦、老病苦、爱别离苦、愿长久苦，苦于沉沦浮世苦海，于轮回中穿行不息，因而神要凌驾众生、佛欲超脱六道、魔妄执掌玄黄、人求争渡苦海。
而这是一个从轮回中跳脱的故事，三千世界，四极寰宇，我早已入局落子，你呢？<br>
&nbsp;&nbsp;&nbsp;&nbsp;各位书友要是觉得《争虚》还不错的话请不要忘记向您QQ群和微博里的朋友推荐哦！争虚最新章节,争虚无弹窗,争虚全文阅读.---------------------何途
</div>
             */
            introduction("#aboutbook") {
                // 不知道jsoup算的，（作者：何途）算一个，（类别：玄幻奇幻）算一个，
                it.textNodes().drop(2).dropLast(1).joinToString {
                    it.ownLinesString()
                }
            }
        }
    }
    chapters {
        document {
            items("#box > div.zjbox > ul > li:nth-child(1) > a")
        }
    }
    // https://www.qingkan9.com/book/zhengxu/57330157.html
    bookIdWithChapterIdRegex = "/book/([^/]+/\\d+)"
    contentPageTemplate = "/book/%s.html"
    content {
        document {
            items("#content", block = ownLines())
        }
    }
}
}

