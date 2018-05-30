package cc.aoeiuv020.panovel.export.impl

import cc.aoeiuv020.base.jar.toBean
import cc.aoeiuv020.panovel.settings.Margins
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * Created by AoEiuV020 on 2018.05.30-11:11:01.
 */

fun Margins.import(json: String) {
    val map = mapOf<String, (JsonElement) -> Unit>(
            "enabled" to { value -> enabled = value.asBoolean },
            "left" to { value -> left = value.asInt },
            "top" to { value -> top = value.asInt },
            "right" to { value -> right = value.asInt },
            "bottom" to { value -> bottom = value.asInt }
    )
    json.toBean<JsonObject>().entrySet().forEach { (key, value) ->
        map[key]?.invoke(value)
    }
    /*
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

       */
}
