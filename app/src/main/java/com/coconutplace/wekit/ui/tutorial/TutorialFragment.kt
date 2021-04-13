package com.coconutplace.wekit.ui.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.coconutplace.wekit.databinding.FragmentTutorialBinding

class TutorialFragment : Fragment() {
    private lateinit var binding: FragmentTutorialBinding
    private lateinit var title: String
    private var imageResource = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            imageResource = requireArguments().getInt(IMG_RESOURCE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTutorialBinding.inflate(inflater, container, false)
        binding.tutorialOnboarding.setImageResource(imageResource)

        return binding.root
    }

    companion object {
        private const val IMG_RESOURCE = "IMG_RESOURCE"

        fun newInstance(
            imageResource: Int
        ): TutorialFragment {
            val fragment = TutorialFragment()
            val args = Bundle()
            args.putInt(IMG_RESOURCE, imageResource)

            fragment.arguments = args
            return fragment
        }
    }
}