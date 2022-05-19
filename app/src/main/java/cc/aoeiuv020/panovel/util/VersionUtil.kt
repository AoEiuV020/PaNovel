package cc.aoeiuv020.panovel.util

import android.content.Context
import android.content.pm.PackageManager
import cc.aoeiuv020.regex.pick

object VersionUtil {
    fun getAppVersionName(context: Context): String {
        try {
            return context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "0"
    }

    /**
     * https://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java
     *
     *
     * Compares two version strings.
     *
     *
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2.
     * The result is a positive integer if str1 is _numerically_ greater than str2.
     * The result is zero if the strings are _numerically_ equal.
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     */
    fun compare(str1: String, str2: String): Int {
        val (versionName1, date1) = str1.split('-').let {
            it[0] to it.elementAtOrNull(1)
        }
        val (versionName2, date2) = str2.split('-').let {
            it[0] to it.elementAtOrNull(1)
        }
        val list1 = versionName1.split('.')
        val list2 = versionName2.split('.')
        var index = 0
        while (index < list1.size && index < list2.size) {
            if (list1[index] == list2[index]) {
                index++
                continue
            }
            return list1[index].toLong().compareTo(list2[index].toLong())
        }
        if (list1.size != list2.size) {
            return list1.size.compareTo(list2.size)
        }
        if (date1 == date2) {
            return 0
        }
        if (date1 != null && date2 != null) {
            return date1.toLong().compareTo(date2.toLong())
        }
        return if (date1 == null) {
            1
        } else {
            // date2 == null
            -1
        }
    }
}