package com.coconutplace.wekit.ui.signup

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_PERSONAL_INFO
import com.coconutplace.wekit.utils.GlobalConstant.Companion.FLAG_TNC
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RuleDialog(flag: Int): BottomSheetDialogFragment() {
    private var flag = FLAG_TNC

   init {
       this.flag = flag
   }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            isFitToContents = true
            isCancelable = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.bottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.dialog_rule, container, false)

        val titleTv: TextView = view.findViewById(R.id.rule_title_tv)
        val contentTv: TextView = view.findViewById(R.id.rule_content_tv)

        if(this.flag == FLAG_TNC){
            titleTv.text = getString(R.string.rule_terms_n_conditions)
            contentTv.text = getString(R.string.rule_terms_n_conditions_content).htmlToString()
        }else if(this.flag == FLAG_PERSONAL_INFO){
            titleTv.text = getString(R.string.rule_personal_information)
            contentTv.text = getString(R.string.rule_personal_information_content).htmlToString()
        }

        return view
    }

    private fun String.htmlToString() : String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(this).toString()
        }
    }
}