package cc.aoeiuv020.panovel.server

import cc.aoeiuv020.panovel.local.toBean

/**
 *
 * Created by AoEiuV020 on 2018.04.06-10:43:57.
 */

class RequestMessage(
        val action: Action = Action.UNKNOWN,
        val data: String = "{}"
) : ServerData

class ResponseMessage(
        val action: Action = Action.UNKNOWN,
        val data: String = "{}"
) : ServerData {
    inline fun <reified T> getRealData(): T {
        return data.toBean()
    }
}


enum class Action {
    UPDATE,
    BOOKSHELF_ADD, BOOKSHELF_REMOVE,
    UNKNOWN,
}
