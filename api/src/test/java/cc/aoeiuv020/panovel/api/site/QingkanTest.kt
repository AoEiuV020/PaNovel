package cc.aoeiuv020.panovel.api.site

import cc.aoeiuv020.panovel.api.noImage
import org.junit.Test

/**
 * Created by AoEiuV020 on 2018.06.03-14:55:54.
 */
class QingkanTest : BaseNovelContextText(Qingkan::class) {
    @Test
    fun search() {
        search("都市")
        search("争虚", "何途", "zhengxu")
        search("盗天仙途", "荆柯守", "daotianxiantu")
    }

    @Test
    fun detail() {
        detail("daotianxiantu", "daotianxiantu", "盗天仙途", "荆柯守",
                noImage,
                "福地产生地仙，洞天来往天仙，我有梅花一株，盗取天机！",
                null)
    }

    @Test
    fun chapters() {
        chapters("daotianxiantu", "序", "daotianxiantu/29249297", null,
                "第三百五十四章 检测", "daotianxiantu/54903911", null,
                308)
    }

    @Test
    fun content() {
        content("daotianxiantu/54903911",
                "古镜六寸高，形制古雅，镜面却有一个虚影，现出极淡的金光，正照着下去，一批拔干净，就换下一批，只是这时发生了变故。",
                "看着骑兵冲了上来，押粮将军冷笑：“你区区两三百轻甲就敢冲击押粮队？也罢，杀光你们，就是一功！”",
                48)
    }

}