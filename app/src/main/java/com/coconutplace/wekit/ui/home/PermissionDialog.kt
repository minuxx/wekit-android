package com.coconutplace.wekit.ui.home

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.coconutplace.wekit.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

class PermissionDialog : BottomSheetDialogFragment() {
    private lateinit var callback: OnBackPressedCallback

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.apply {
            isFitToContents = true
            isCancelable = false
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        setupBackPressListener()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(context, "aaaa", Toast.LENGTH_SHORT).show()
                if(!TedPermission.isGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)){
                    requireActivity().finish()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_permission, container, false)
    }

    private fun setupBackPressListener() {
        this.view?.isFocusableInTouchMode = true
        this.view?.requestFocus()
        this.view?.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {

                true
            } else
                false
        }
    }

    override fun onStart() {
        super.onStart()
        view?.findViewById<TextView>(R.id.permission_keep_tv)?.setOnClickListener {
            TedPermission.with(context)
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .setDeniedMessage(requireActivity().getString(R.string.denied_permission))
                .check()
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.bottomSheetDialog)
    }

    var permissionlistener: PermissionListener = object : PermissionListener {
        override fun onPermissionGranted() {
            dismiss()
        }

        override fun onPermissionDenied(deniedPermissions: List<String>) {
//            android.os.Process.killProcess(android.os.Process.myPid())
            requireActivity().finish()
        }
    }
}