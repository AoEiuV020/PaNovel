package cc.aoeiuv020.panovel.list

interface NovelItemActionListener {
    fun onDotClick(vh: NovelViewHolder)
    fun onDotLongClick(vh: NovelViewHolder): Boolean
    fun onCheckUpdateClick(vh: NovelViewHolder)
    fun onNameClick(vh: NovelViewHolder)
    fun onNameLongClick(vh: NovelViewHolder): Boolean
    fun onLastChapterClick(vh: NovelViewHolder)
    fun onItemClick(vh: NovelViewHolder)
    fun onItemLongClick(vh: NovelViewHolder): Boolean
    fun onStarChanged(vh: NovelViewHolder, star: Boolean)
    fun refreshChapters(vh: NovelViewHolder)
    fun askUpdate(vh: NovelViewHolder)
}