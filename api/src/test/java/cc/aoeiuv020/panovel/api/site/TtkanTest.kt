package cc.aoeiuv020.panovel.api.site

import org.junit.Test

class TtkanTest : BaseNovelContextText(Ttkan::class) {
    @Test
    fun search() {
        search("都市")
        search("都市疯神榜", "都市言情", "dushifengshenbang-dushiyanqing")
        search("视死如归魏君子", "平层", "shisiruguiweijunzi-pingceng")
    }

    @Test
    fun detail() {
        detail(
            "dushifengshenbang-dushiyanqing", "dushifengshenbang-dushiyanqing", "都市疯神榜", "都市言情",
            "https://static.ttkan.co/cover/dushifengshenbang.jpg?w=120&h=160&q=100",
            "《都市疯神榜》介绍(连载中):\n不作死就不会死,不抽风却一定会死.\n一个贫困窘迫的乡村小子,在御世神器疯神榜的逼迫下,开始了一段疯狂奇妙的成神之旅.\n在教室里向美女老师表白,在女生宿舍调戏大学校花…\n带着全国十大警花去抢劫,带着索里马海盗去扶老奶奶过马路…\n买一百条商业街种萝卜土豆,买十支当红女组合养鸡喂猪…\n陆经纬:自从被一幅疯神榜上了身,感觉整个人都疯疯哒.\n疯神榜:如果你有什么想做却又不敢做,或者做不到的事情,请以抽风任务的形式发布在书评区,也许明天俺就会把它发布给陆经纬.\n《都市疯神榜》情节跌宕起伏、扣人心弦,都市疯神榜是一本情节与文笔俱佳的浪漫言情,各位书友要是觉得都市疯神榜最新章节还不错的话请不要忘记向您qq群和微博里的朋友推荐哦!",
            null
        )
        detail(
            "shisiruguiweijunzi-pingceng", "shisiruguiweijunzi-pingceng", "视死如归魏君子", "平层",
            "https://static.ttkan.co/cover/shisiruguiweijunzi-pingceng.jpg?w=120&h=160&q=100",
            "魏君确认自己被杀死后就能直接无敌，于是他开始疯狂的作死。然后，他发现这个世界有毒。“当初仙门凌驾于朝堂之上作威作福，满朝文武包括朕皆对仙人卑躬屈膝，只有魏君一身是胆，视死如归，在众目睽睽之下大骂朕有辱帝王尊严，更是直言仙人不死，大盗不止。当时朕就下定决心，如此忠臣，朕一定要护他周全。”“仙人说我是天煞孤星，能克死身边一切亲近的人。所以我父母抛弃我，世人害怕我，我感受不到这个世界对我丝毫的善意。直到魏君的出现，他对我说，我没有错，他不怕死，他愿意和我一直做朋友。其实我很想对他说，我们能把革命友谊再升华一下吗？”“我认识魏君的时候，魏君还很弱小，而我是被正魔两道追杀的盖世魔君。我告诉他，救了我就等于和全世界为敌，会死的。魏君笑着对我说，生亦何欢，死亦何苦。我一生纵横无敌，看透了魔门的残酷与正道的虚伪。直到遇到了魏君，我方知这世间当真有视死如归的真英雄。”……魏君：“我只是想死，怎么就这么难呢？”",
            null
        )
    }

    @Test
    fun chapters() {
        chapters(
            "dushifengshenbang-dushiyanqing", "第1章 抽风的星星", "dushifengshenbang-dushiyanqing_1", null,
            "第1018章 妻儿满堂", "dushifengshenbang-dushiyanqing_1017", null,
            1017
        )
        chapters(
            "shisiruguiweijunzi-pingceng", "第1章 死后立马就能变大佬", "shisiruguiweijunzi-pingceng_1", null,
            "第164章 人在家中坐，义父天上来（求月票）", "shisiruguiweijunzi-pingceng_404", null,
            404
        )
    }

    @Test
    fun content() {
        content(
            "dushifengshenbang-dushiyanqing_1017",
            "最傾城時尚大樓，也是最新的最傾城時尚大樓，高達十八層，比原來的最傾城時尚大樓好了太多，只是裡面的很多人都已經換了。",
            "【這本書寫到這裡，已經結束了，並沒有去詳述關於萬象大靈界的事情，以及陸經緯在萬象大靈界的故事，大綱就到這裡，是一個開放的結局，圓圓滿滿地結束了。只是雖然這本書結束了，但是陸經緯的傳說還沒有結束，有了萬古難見的妖孽天賦，有了瘋神榜的相助，他在萬象大靈界會走的更加順利，當然也會走的更遠。最後，謝謝所有正版訂閱了這本書的書友們，是你們讓這本書走到了現在，是你們讓月亮沒有半途放棄，新書《吃掉地球》已經上傳，比老書更加的精彩，請書友們去收藏一下，支持一下，幫助月亮走的更遠，我們在《吃掉地球》見！】(。)",
            35
        )
        content(
            "shisiruguiweijunzi-pingceng_404",
            "董大將軍一句話，把塵珈給徹底弄懵逼了。",
            "“義父待我恩重如山，奉先一定爲義父鞠躬盡瘁，死而後已。”",
            217
        )
    }

}