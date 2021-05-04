package com.coconutplace.wekit.ui.signup

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
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
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.permissionDialog)
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

        if(this.flag == FLAG_PERSONAL_INFO){
            titleTv.text = getString(R.string.personal_information)
            contentTv.text = getString(R.string.personal_information_content)
        }

        return view
    }

}