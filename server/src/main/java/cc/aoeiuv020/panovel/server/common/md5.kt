package cc.aoeiuv020.panovel.server.common

import cc.aoeiuv020.base.jar.md5
import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel

/**
 * 计算md5充当推送的tag,
 * 两端计算结果必需一致，
 *
 * Created by AoEiuV020 on 2018.04.17-13:01:51.
 */
fun Novel.md5(): String = "$requesterType.$requesterExtra".md5()
