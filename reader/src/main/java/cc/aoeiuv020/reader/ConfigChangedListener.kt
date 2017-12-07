package cc.aoeiuv020.reader

internal interface ConfigChangedListener {
    fun onConfigChanged(name: ReaderConfigName)
}