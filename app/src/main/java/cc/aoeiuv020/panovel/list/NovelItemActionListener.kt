package cc.aoeiuv020.panovel.list

interface NovelItemActionListener {
    fun onDotClick(vh: NovelListViewHolder)
    fun onDotLongClick(vh: NovelListViewHolder): Boolean
    fun onNameClick(vh: NovelListViewHolder)
    fun onNameLongClick(vh: NovelListViewHolder): Boolean
    fun onLastChapterClick(vh: NovelListViewHolder)
    fun onItemClick(vh: NovelListViewHolder)
    fun onItemLongClick(vh: NovelListViewHolder): Boolean
    fun onStarChanged(vh: NovelListViewHolder, star: Boolean)
    fun requireRefresh(vh: NovelListViewHolder)
}