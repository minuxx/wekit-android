package com.coconutplace.wekit.ui.rule

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.ui.BaseActivity
import kotlinx.android.synthetic.main.activity_rule.*

class RuleActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rule)

        val tncContentTv: TextView = findViewById(R.id.rule_tnc_content_tv)
        val personalInfoContentTv: TextView = findViewById(R.id.rule_personal_info_content_tv)

        tncContentTv.text = getString(R.string.rule_terms_n_conditions_content).htmlToString()
        personalInfoContentTv.text = getString(R.string.rule_personal_information_content).htmlToString()

        rule_back_btn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v) {
            rule_back_btn -> finish()
        }
    }

    private fun String.htmlToString() : String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(this).toString()
        }
    }
}