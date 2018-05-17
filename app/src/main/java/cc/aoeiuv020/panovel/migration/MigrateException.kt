package cc.aoeiuv020.panovel.migration

/**
 * Created by AoEiuV020 on 2018.05.17-17:06:35.
 */
class MigrateException(
        val migration: Migration,
        message: String? = null,
        cause: Throwable?
) : Exception("迁移数据到版本<${migration.to.name}>失败: $message", cause)