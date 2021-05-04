package com.coconutplace.wekit.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.coconutplace.wekit.R

class ChatMemberListAdapter : BaseAdapter() {

    data class MemberInfo(
        val authCount:Int,
        val nickName:String,
        val type:String,
        val todayCount:Int,
        val userIdx:Int
    )

    private val memberList:MutableList<MemberInfo> = ArrayList()
    private var mItemClickListener:OnItemClickListener? = null

    fun clear() {
        memberList.clear()
    }

    interface OnItemClickListener {
        fun onItemClick(memberInfo: MemberInfo)
    }

    fun setItemClickListener(listener: OnItemClickListener){
        mItemClickListener = listener
    }

    override fun getCount(): Int {
        return memberList.size
    }

    override fun getItem(position: Int): MemberInfo {
        return memberList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView1: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(parent?.context).inflate(R.layout.item_chat_member,parent,false)

        val authCount = view.findViewById<View>(R.id.chat_member_auth_count) as Button
        val nickName = view.findViewById<View>(R.id.chat_member_nickname) as TextView
        val hostImageView = view.findViewById<View>(R.id.chat_member_host_img) as ImageView
        val todayCount = view.findViewById<View>(R.id.chat_member_today_auth_count) as TextView

        val tempMember: MemberInfo = memberList[position]
        authCount.text = tempMember.authCount.toString()
        nickName.text = tempMember.nickName
        if(tempMember.type=="host"){
            hostImageView.visibility = View.VISIBLE
        }
        todayCount.text = String.format("[%d]",tempMember.todayCount)

        view.setOnClickListener {
            mItemClickListener?.onItemClick(tempMember)
        }

        return view
    }

    fun addItem( authCount:Int, nickName: String, type:String, todayCount:Int, userIdx:Int) {
        val tempMember: MemberInfo = MemberInfo(authCount, nickName, type, todayCount, userIdx)
        memberList.add(tempMember)
    }
}