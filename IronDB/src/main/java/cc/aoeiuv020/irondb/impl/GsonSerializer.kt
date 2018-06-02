package cc.aoeiuv020.irondb.impl

import cc.aoeiuv020.irondb.DataSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type

/**
 * Created by AoEiuV020 on 2018.05.27-15:51:22.
 */
class GsonSerializer(
        private val gson: Gson = GsonBuilder().create()
) : DataSerializer {
    override fun <T> serialize(value: T, type: Type): String =
            gson.toJson(value, type)

    override fun <T> deserialize(string: String, type: Type): T =
            gson.fromJson(string, type)
}