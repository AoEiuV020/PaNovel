package cc.aoeiuv020.panovel.migration

import android.content.Context
import cc.aoeiuv020.panovel.util.VersionName

/**
 * Created by AoEiuV020 on 2018.05.17-16:16:10.
 */
abstract class Migration {
    /**
     * 从哪个版本开始需要这个Migration,
     * 低于这个版本的需要升级到这个patchVersion版本，
     */
    abstract val to: VersionName

    abstract val message: String

    /**
     * 实际的升级方法，
     *
     * TODO: 考虑下要不要询问是否删除旧版数据，先不删除旧版数据，
     */
    abstract fun migrate(ctx: Context, from: VersionName)

    override fun toString(): String {
        return "${javaClass.simpleName}(to=$to, message=$message)"
    }
}