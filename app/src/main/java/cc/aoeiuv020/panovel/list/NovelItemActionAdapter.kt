package cc.aoeiuv020.panovel.list

open class NovelItemActionAdapter : NovelItemActionListener {
    override fun onDotClick(vh: NovelListViewHolder) {}
    override fun onDotLongClick(vh: NovelListViewHolder): Boolean = false
    override fun onNameClick(vh: NovelListViewHolder) {}
    override fun onNameLongClick(vh: NovelListViewHolder): Boolean = false
    override fun onLastChapterClick(vh: NovelListViewHolder) {}
    override fun onItemClick(vh: NovelListViewHolder) {}
    override fun onItemLongClick(vh: NovelListViewHolder): Boolean = false
    override fun onStarChanged(vh: NovelListViewHolder, star: Boolean) {}
    override fun requireRefresh(vh: NovelListViewHolder) {}
}