package com.coconutplace.wekit.ui.home

import android.Manifest
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.coconutplace.wekit.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

class PermissionDialog : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_permission, container, false)
    }

    override fun onStart() {
        super.onStart()
        view?.findViewById<TextView>(R.id.permission_keep_tv)?.setOnClickListener {
            TedPermission.with(context)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("권한을 거부하시면 서비스이용이 불가합니다. [설정] > [앱 권한]에서 권한을 허용해주세요.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.permissionDialog)
    }

    var permissionlistener: PermissionListener = object : PermissionListener {
        override fun onPermissionGranted() {


        }

        override fun onPermissionDenied(deniedPermissions: List<String>) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}