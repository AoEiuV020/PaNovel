package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.base.jar.pick
import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext
import cc.aoeiuv020.panovel.api.firstIntPattern
import cc.aoeiuv020.panovel.api.firstThreeIntPattern

/**
 *
 * Created by AoEiuV020 on 2018.03.06-18:10:46.
 */
class Sfacg : DslJsoupNovelContext() { init {
    site {
        name = "SF轻小说"
        baseUrl = "http://book.sfacg.com"
        logo = "http://rs.sfacg.com/images/sflogo.gif"
    }
    search {
        get {
            url = "http://s.sfacg.com/?Key=${utf8(it)}&S=1&SS=0"
        }
        document {
            /*
            <ul style="width:100%">
                <li class="Conjunction">
                    <img src="http://rs.sfacg.com/web/novel/images/NovelCover/Small/2016/07/5fe29c4c-d774-4e12-b4e3-578814f51fec.jpg" id="SearchResultList1___ResultList_Cover_0" border="0" alt="吸血萝莉在都市" width="80" height="100"></li>
                <li><strong class="F14PX"><a href="http://book.sfacg.com/Novel/44856" id="SearchResultList1___ResultList_LinkInfo_0" class="orange_link2">吸血萝莉在都市</a></strong>
                    <br> 综合信息： 那一片宁静/2018/5/9 20:11:26
                    <br> 　一个高中毕业的公子哥因通宵玩电脑而猝死，醒来时却发现自己成了萝莉...... 　　但是这个萝莉有什么不可告人的秘密？结局将会如何？ 　　本萝莉又软又萌，喜欢软妹子的读者老爷千万别错过。 　　－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－顺便带着点小傲娇哟～～ PS：不会上架的QWQ
                </li>
            </ul>
             */
            items("#form1 > table.comic_cover.Height_px22.font_gray.space10px > tbody > tr > td > ul") {
                name("li > strong > a")
                author("> li:nth-child(2)", block = pickString("综合信息： ([^/]*)/"))
            }
        }
    }
    detailTemplate = "/Novel/%s/"
    detail {
        document {
            val div = element("div.wrap > div.d-summary > div.summary-content")
            novel {
                name("> h1 > span.text", parent = div)
                author("> div.count-info.clearfix > div.author-info > div.author-name > span", div)
            }
            image("#hasTicket > div.left-part > div > div.pic > a > img")
            introduction("> p", parent = div)
            // 排版有两种，重要的是更新时间的位置不同，
            // http://book.sfacg.com/Novel/123589/
            update("> div.count-info.clearfix > div.count-detail span:nth-child(4)", parent = div,
                    format = "yyyy/MM/dd HH:mm:ss", block = pickString("更新：(.*)"))
            if (update == null) {
                // http://book.sfacg.com/Novel/114367/
                update("body > div.container > div.d-banner > div > div > div.summary-content" +
                        " > div.count-info.clearfix > div.count-detail > div:nth-child(2) > span",
                        format = "yyyy/MM/dd HH:mm:ss", block = pickString("更新：(.*)"))
            }
        }
    }
    chapterTemplate = "/Novel/%s/MainIndex/"
    chapters {
        /*
        <li>
            <a href="/vip/c/1892767/" title="第四十一章" class="">
                <span class="icn_vip">VIP</span> 第四十一章
            </a>
        </li>
         */
        document {
            items("div.story-catalog > div.catalog-list > ul > li > a") {
                // vip章节这里包含一个小标签，写着VIP,
                name = root.ownText()
                // vip章节和普通章节规则不一致，统一拿全路径，
                extra = root.path()
            }
        }
    }
    getNovelContentUrl {
        // vip章节和普通章节规则不一致，不统一处理，
        try {
            // http://book.sfacg.com/Novel/123589/204084/1887037/
            val bookId = it.pick(firstThreeIntPattern).first()
            "/Novel/$bookId/"
        } catch (e: Exception) {
            // http://book.sfacg.com/vip/c/1725750/
            val bookId = it.pick(firstIntPattern).first()
            "/vip/c/$bookId/"
        }
    }
    content {
        document {
            // vip章节仅有的一行没有包在p里，
            // 普通章节有"#ChapterBody > p",
            items("#ChapterBody")
        }
    }
}
}

