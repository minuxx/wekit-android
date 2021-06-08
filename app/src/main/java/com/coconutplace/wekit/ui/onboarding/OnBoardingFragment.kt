package com.coconutplace.wekit.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.coconutplace.wekit.R
import kotlinx.android.synthetic.main.fragment_onboarding.view.*

class OnBoardingFragment : Fragment() {
    private var imageResource = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            imageResource = requireArguments().getInt(IMG_RESOURCE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding, container, false)
        view.onboarding_onboarding_iv.setImageResource(imageResource)

        return view
    }

    companion object {
        private const val IMG_RESOURCE = "IMG_RESOURCE"

        fun newInstance(
            imageResource: Int
        ): OnBoardingFragment {
            val fragment = OnBoardingFragment()
            val args = Bundle()
            args.putInt(IMG_RESOURCE, imageResource)

            fragment.arguments = args
            return fragment
        }
    }
}