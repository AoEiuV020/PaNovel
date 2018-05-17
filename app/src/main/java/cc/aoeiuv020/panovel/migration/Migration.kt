package cc.aoeiuv020.panovel.migration

import cc.aoeiuv020.panovel.util.VersionName

/**
 * Created by AoEiuV020 on 2018.05.17-16:16:10.
 */
interface Migration {
    /**
     * 从哪个版本开始需要这个Migration,
     * 低于这个版本的需要升级到这个patchVersion版本，
     */
    val to: VersionName

    val message: String get() = ""

    /**
     * 实际的升级方法，
     *
     * TODO: 考虑下要不要询问是否删除旧版数据，先不删除旧版数据，
     */
    fun migrate(from: VersionName)
}