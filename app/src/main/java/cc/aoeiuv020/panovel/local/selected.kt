package cc.aoeiuv020.panovel.local

/**
 * 记住的选择，
 * Created by AoEiuV020 on 2017.10.04-20:04:38.
 */
object Selected : LocalSource {
    var genre by NovelGenreDelegate()
    var site by NovelSiteDelegate()
}