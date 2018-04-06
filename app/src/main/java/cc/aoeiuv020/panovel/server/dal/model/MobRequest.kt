package cc.aoeiuv020.panovel.server.dal.model

import cc.aoeiuv020.panovel.local.toBean


/**
 *
 * Created by AoEiuV020 on 2018.04.05-08:01:30.
 */
class MobRequest(
        val data: String = "{}"
) {
    inline fun <reified T> getRealData(): T {
        return data.toBean()
    }

}