package cc.aoeiuv020.panovel.api.site

import org.junit.Test

/**
 *
 * Created by AoEiuV020 on 2018.03.07-03:24:15.
 */
class SnwxTest : BaseNovelContextText(Snwx::class) {
    @Test
    fun search() {
        search("都市")
        search("恐怖广播", "纯洁滴小龙", "158/158904")
        search("斗破苍穹之玄天帝尊", "唯物98", "301/301259")
        search("修真妖孽战都市", "鲜衣怒马1", "281/281937")
        search("祖魔", "一夜风云起", "66/66076")
        // 这网站有不止一本修真聊天群，作者是一个人，但是显示的不一样，
        search("修真聊天群", "圣骑士的传说", "216/216257")
    }

    @Test
    fun detail() {
        detail("66/66076", "66/66076", "祖魔", "一夜风云起",
                "https://www.snwx8.com/files/article/image/66/66076/66076s.jpg",
                "祖魔的简介：一个超级世家大家族少爷，却天生丹田堵塞，难以修真！本有着疼爱自己的父母，却一夜之间家族没落，自此身负血海深仇！" +
                        "恋人的背叛，父母的离去，最终让他指天怒骂！天道不公，以万物为刍狗！既然天已无道，我愿舍身成魔！以血染天，以杀破道！",
                null)
        // TODO: 获取不到封面，麻烦，是图片有地址但下载不到，要加上onError,
        detail("257/257710", "257/257710", "都市超级神尊", "小萌靓",
                "https://www.snwx8.com/files/article/image/257/257710/257710s.jpg",
                "仙医重生，得逆天系统，开挂打脸一切不服者！昔日萧炎，装逼略逊。仙尊北玄，稍有风骚。数装逼大乘者，还看今朝！",
                null)
        detail("28/28152", "28/28152", "大道独行", "雾外江山",
                "https://www.snwx8.com/files/article/image/28/28152/28152s.jpg",
                "null",
                null)
    }

    @Test
    fun chapters() {
        chapters("0/20",
                "第一章 狠角色", "0/20/9883", null,
                "关于结局", "0/20/7056627", null,
                635)
    }

    @Test
    fun content() {
        content("0/20/9888",
                "(新书上传期间，求会员点击、推荐、收藏……拜谢！)",
                "张卫东微微皱了皱眉头，却是没有回头，继续走他的路。",
                40)
    }
}

