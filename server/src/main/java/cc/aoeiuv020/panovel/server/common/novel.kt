package cc.aoeiuv020.panovel.server.common

import cc.aoeiuv020.panovel.server.dal.model.autogen.Novel

/**
 * Created by AoEiuV020 on 2018.06.19-16:54:09.
 */
val Novel.bookId: String get() = "$name.$author.$site"
