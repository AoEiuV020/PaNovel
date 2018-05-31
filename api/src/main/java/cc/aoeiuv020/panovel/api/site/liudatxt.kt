package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.base.DslJsoupNovelContext

/**
 *
 * Created by AoEiuV020 on 2017.10.11-19:39:27.
 */
class Liudatxt : DslJsoupNovelContext() { init {
    site {
        name = "溜达小说"
        baseUrl = "http://www.liudatxt.com"
        logo = "https://imgsa.baidu.com/forum/w%3D580/sign=1b4c19b5f0edab6474724dc8c737af81/4afa9ae93901213f074d29a25fe736d12e2e95b9.jpg"
    }
    search {
        post {
            url = "/search.php"
            data {
                +("searchkey" to it)
            }
        }
        /*
        <dl>
            <dt>
                <a href='/5447/'>
                    <img alt='都市之特种狂兵全文阅读' src="/headimgs/5/5447/s5447.jpg" onerror="this.src='/res/images/blank.jpg'" height="155">
                </a>
            </dt>
            <dd>
                <h3>
                    <a href='/so/5447/'>都市之特种狂兵</a>
                    <span class="alias"></span>
                </h3>
            </dd>
            <dd class="book_other">作者：
                <span>枯木</span>状态：
                <span>连载中</span>分类：
                <span>都市言情</span>字数：
                <span>7211327</span>
            </dd>
            <dd class="book_des"> 一手夺命金针，一身玄奇功法！天才少年陆辰自深山中走出，凭借着一手神奇医术与霸道武学，一路...</dd>
            <dd class="book_other">最新章节：
                <a href='/so/5447/11558786.html'>正文 正文_第三千二百六十七章 血蛟战死</a> 更新时间：
                <span>2018-05-21 01:59:56</span>
            </dd>
        </dl>
         */
        document {
            items("#sitembox > dl") {
                name("> dd:nth-child(2) > h3 > a")
                author("> dd:nth-child(3) > span:nth-child(1)")
            }
        }
    }
    // http://www.liudatxt.com/2034/
    detailPageTemplate = "/%s/"
    detail {
        /*
        <div class="bookright">
           <div class="booktitle">
              <h1>完美至尊</h1>
              <span id="author">作者：<a href="/author/观鱼/" target="_blank" rel="nofollow">观鱼</a></span>
           </div>
           <div class="count">
              <ul>
                 <li>分&nbsp;&nbsp;类：<span>玄幻奇幻</span></li>
                 <li>周点击：<span>94</span></li>
                 <li>月点击：<span>5677</span></li>
                 <li>总点击：<span id="Hits">2951590</span></li>
                 <li>状&nbsp;&nbsp;态：<span>连载中</span></li>
                 <li>总推荐：<span>95</span></li>
                 <li>总字数：<span>10445478</span></li>
              </ul>
           </div>
           <div id="bookintro">
              <p> 时代天运，武道纵横！少年林凌自小被封印，受尽欺辱，当一双神秘的眼瞳觉醒时，曾经的强者，古老的神话，神秘的遗迹出现在他双眼！他看到了逝去的时光，看到了远古神魔的秘密，他看到了古代顶阶功法，所有消失在历史长河的强者，通通被他看到了，借着古代强者的指点，他从渺小蝼蚁的世界底层，一步一个脚印，走上俾睨天下之路。</p>
           </div>
           <div class="new">
              <span class="uptime">最后更新：<span>2018-05-21 10:23:05</span></span>
           </div>
        </div>
         */
        document {
            val bookright = element("#bookinfo > div.bookright")
            novel {
                name("> div.booktitle > h1", parent = bookright)
                author(" > div.booktitle > span#author > a", parent = bookright)
            }
            image("#bookimg > img")
            introduction("#bookintro > p", parent = bookright)
            update(" > div.new > span > span", parent = bookright, format = "yyyy-MM-dd HH:mm:ss")
        }
    }
    // http://www.liudatxt.com/so/2034/
    chaptersPageTemplate = "/so/%s/"
    chapters {
        /*
        <li><a href="/so/2034/529879.html" title="正文_第十四章 前往山望镇" target="_blank">正文_第十四章 前往山望镇</a></li>
         */
        document {
            items("#readerlist > ul > li > a")
            lastUpdate("#smallcons > span:nth-child(6)", format = "yyyy-MM-dd HH:mm:ss")
        }
    }
    // http://www.liudatxt.com/so/2034/529879.html
    contentPageTemplate = "/so/%s.html"
    content {
        /*
        <div id="content" style="font-size: 24px; font-family: 华文楷体;">
            第十四章&nbsp;前往山望镇
            <br>&nbsp;&nbsp;&nbsp;&nbsp;＂去不去山望镇？那里有拍卖会呢，说不定能买到不错的东西！＂
            <br><i><a href="/8843/">万神之祖最新章节</a></i>
            <br>&nbsp;&nbsp;&nbsp;&nbsp;众人从蚰蜒落下，向着山望镇走去，这里有着十万大山各方武者，除此外，还有四大门派的弟子，其分别是飞雪宫，狂刀派，落叶门与古颜派！
        </div>
         */
        document {
            // 去广告，"#content > i"都是广告，
            items("#content") {
                it.ownTextList()
            }
        }
    }
}
}

