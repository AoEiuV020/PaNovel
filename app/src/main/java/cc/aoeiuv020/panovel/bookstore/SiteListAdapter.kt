package cc.aoeiuv020.panovel.bookstore

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import cc.aoeiuv020.panovel.R
import cc.aoeiuv020.panovel.api.NovelSite
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.site_list_item.view.*

class SiteListAdapter(private val ctx: Activity, private val sites: List<NovelSite>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: View.inflate(ctx, R.layout.site_list_item, null)
        val site = getItem(position)
        view.apply {
            siteName.text = site.name
            Glide.with(ctx).load(site.logo).into(siteLogo)
        }
        return view
    }

    override fun getItem(position: Int) = sites[position]

    override fun getItemId(position: Int) = 0L

    override fun getCount() = sites.size
}