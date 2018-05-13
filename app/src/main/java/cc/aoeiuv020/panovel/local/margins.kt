package cc.aoeiuv020.panovel.local

import cc.aoeiuv020.base.jar.toBean
import cc.aoeiuv020.base.jar.toJson
import cc.aoeiuv020.pager.IMargins
import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * 排版相关的，上下左右留白的，
 * 单位都是百分比，
 * Created by AoEiuV020 on 2018.03.11-00:45:18.
 */
class Margins(name: String, enabled: Boolean,
              left: Int, top: Int, right: Int, bottom: Int
) : BaseLocalSource(), IMargins {
    override val path: String = "Settings/$name"
    /**
     * 对应的东西是否显示，
     * 除了小说内容，其他都支持不显示，
     */
    override var enabled: Boolean by PrimitiveDelegate(enabled)
    override var left: Int by PrimitiveDelegate(left)
    override var top: Int by PrimitiveDelegate(top)
    override var right: Int by PrimitiveDelegate(right)
    override var bottom: Int by PrimitiveDelegate(bottom)
    override fun toString(): String {
        return "Margins($left, $top, $right, $bottom)"
    }

    fun import(gson: Gson, json: String) {
        json.toBean<JsonObject>(gson).entrySet().forEach { (key, value) ->
            when (key) {
                "enabled" -> this@Margins.enabled = value.asBoolean
                "left" -> this@Margins.left = value.asInt
                "top" -> this@Margins.top = value.asInt
                "right" -> this@Margins.right = value.asInt
                "bottom" -> this@Margins.bottom = value.asInt
            }
        }
    }

    fun export(gson: Gson): String {
        return JsonObject().apply {
            addProperty("enabled", this@Margins.enabled)
            addProperty("left", this@Margins.left)
            addProperty("top", this@Margins.top)
            addProperty("right", this@Margins.right)
            addProperty("bottom", this@Margins.bottom)
        }.toJson(gson)
    }
}
