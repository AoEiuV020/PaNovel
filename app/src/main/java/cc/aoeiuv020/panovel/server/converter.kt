package cc.aoeiuv020.panovel.server

import cc.aoeiuv020.panovel.data.entity.Novel
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel as ServerNovel

/**
 * Created by AoEiuV020 on 2018.05.31-19:31:04.
 */
fun Novel.toServer() = ServerNovel().also { sn ->
    sn.site = site
    sn.author = author
    sn.name = name
    sn.detail = detail
    sn.chaptersCount = chaptersCount
    sn.receiveUpdateTime = receiveUpdateTime
    sn.checkUpdateTime = checkUpdateTime
}