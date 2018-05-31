package cc.aoeiuv020.panovel.settings

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cc.aoeiuv020.panovel.R
import kotlinx.android.synthetic.main.content_disclaimer.*

/**
 *
 * Created by AoEiuV020 on 2017.12.09-18:24:40.
 */
class DisclaimerFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(R.layout.content_disclaimer, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        tvDisclaimer.text = activity.assets.open("Disclaimer.txt").reader().readText()
    }
}