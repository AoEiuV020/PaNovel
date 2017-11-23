@file:Suppress("unused")

package cc.aoeiuv020.panovel.util

import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlin.reflect.KProperty

/**
 * 没用上，先不删了，
 * Created by AoEiuV020 on 2017.11.23-14:52:01.
 */
@Suppress("UNCHECKED_CAST")
class FragmentDelegate<out T : Fragment>(private val clazz: Class<T>) {
    operator fun getValue(thisRef: AppCompatActivity, property: KProperty<*>): T {
        val fm = thisRef.supportFragmentManager
        return (fm.findFragmentByTag(property.name) as? T)
                ?: clazz.newInstance().also { fm.beginTransaction().add(it, property.name).commit() }
    }
}

inline fun <reified T : Fragment> fragmentDelegate(): FragmentDelegate<T> = FragmentDelegate(T::class.java)

