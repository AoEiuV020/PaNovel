package cc.aoeiuv020.panovel.list

open class NovelItemActionAdapter : NovelItemActionListener {
    override fun onDotClick(vh: NovelViewHolder) {}
    override fun onDotLongClick(vh: NovelViewHolder): Boolean = false
    override fun onNameClick(vh: NovelViewHolder) {}
    override fun onNameLongClick(vh: NovelViewHolder): Boolean = false
    override fun onLastChapterClick(vh: NovelViewHolder) {}
    override fun onItemClick(vh: NovelViewHolder) {}
    override fun onItemLongClick(vh: NovelViewHolder): Boolean = false
    override fun onStarChanged(vh: NovelViewHolder, star: Boolean) {}
    override fun requireRefresh(vh: NovelViewHolder) {}
}