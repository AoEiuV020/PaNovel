package cc.aoeiuv020.reader.simple

internal interface SimpleConfigChangedListener {
    fun onTextSizeChanged()
    fun onTextColorChanged()

    fun onBackgroundColorChanged()
    fun onBackgroundImageChanged()

    fun onLineSpacingChanged()
    fun onParagraphSpacingChanged()

    fun onLeftSpacingChanged()
    fun onTopSpacingChanged()
    fun onRightSpacingChanged()
    fun onBottomSpacingChanged()
}