package com.coconutplace.wekit.ui.routine

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.coconutplace.wekit.R
import com.coconutplace.wekit.data.entities.InnerInterest
import com.coconutplace.wekit.utils.GlobalConstant.Companion.MAX_ROUTINE

class InterestRoutineAdapter(context: Context) :
    RecyclerView.Adapter<InterestRoutineAdapter.InterestViewHolder>() {



    private val mContext = context
    private val interestList = ArrayList<InnerInterest>()
    private var mItemClickListener: OnItemClickListener? = null

    var selectedList = ArrayList<Int>()
    var isMax : Boolean = false

    interface OnItemClickListener {
        fun onClick(position: Int)
    }

    fun setItemClickListener(listener: OnItemClickListener) {
        mItemClickListener = listener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_interest, parent, false)
        return InterestViewHolder(view)
    }

    override fun getItemCount(): Int {
        return interestList.size
    }

    override fun onBindViewHolder(holder: InterestViewHolder, position: Int) {
        holder.onBind(interestList[position], mItemClickListener, position)
        //holder.onClickLayout(position)
    }

    /*
    * cateogry가 늘어날 가능성을 대비해 확장을 고려하여 작성하였음.
    * 후에 카테고리가 늘어나면 서버에서 데이터를 받아서 사용
    * */
    fun addData() {
        interestList.add(
            InnerInterest(
                mContext.resources.getIdentifier(
                    "ic_water",
                    "drawable",
                    mContext.packageName
                ), "시사, 상식"
            )
        )
        interestList.add(
            InnerInterest(
                mContext.resources.getIdentifier(
                    "icn_food",
                    "drawable",
                    mContext.packageName
                ), "클린한 식단"
            )
        )
        interestList.add(
            InnerInterest(
                mContext.resources.getIdentifier(
                    "icn_exercise",
                    "drawable",
                    mContext.packageName
                ), "꾸준한 운동"
            )
        )
        interestList.add(
            InnerInterest(
                mContext.resources.getIdentifier(
                    "icn_reading",
                    "drawable",
                    mContext.packageName
                ), "독서"
            )
        )
        interestList.add(
            InnerInterest(
                mContext.resources.getIdentifier(
                    "icn_language",
                    "drawable",
                    mContext.packageName
                ), "외국어"
            )
        )
        interestList.add(
            InnerInterest(
                mContext.resources.getIdentifier(
                    "icn_test",
                    "drawable",
                    mContext.packageName
                ), "고시, 시험"
            )
        )
        interestList.add(
            InnerInterest(
                mContext.resources.getIdentifier(
                    "icn_diet",
                    "drawable",
                    mContext.packageName
                ), "다이어트"
            )
        )
        interestList.add(
            InnerInterest(
                mContext.resources.getIdentifier(
                    "icn_thinking",
                    "drawable",
                    mContext.packageName
                ), "회고"
            )
        )
        interestList.add(
            InnerInterest(
                mContext.resources.getIdentifier(
                    "icn_writing",
                    "drawable",
                    mContext.packageName
                ), "필사"
            )
        )
        interestList.add(
            InnerInterest(
                mContext.resources.getIdentifier(
                    "icn_plan",
                    "drawable",
                    mContext.packageName
                ), "하루 계획"
            )
        )
    }

    inner class InterestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val interestImage = itemView.findViewById<ImageView>(R.id.item_interest_iv)
        private val interestText = itemView.findViewById<TextView>(R.id.item_interest_tv)
        private val interestLayout =
            itemView.findViewById<ConstraintLayout>(R.id.item_interest_layout)

        fun onBind(interest: InnerInterest, listener: OnItemClickListener?, position: Int) {

            interestImage.setImageResource(interest.interestImg)
            interestText.text = interest.interestText

            if (listener != null) {
                itemView.setOnClickListener {
                    checkItem(position)
                    listener.onClick(position)
                }
            }

        }

        private fun checkItem(position: Int) {
            if (selectedList.contains(position)) { // 클릭한 아이템이 이미 클릭한것이라면
                selectedList.remove(position)
                isMax = false
                interestLayout.setBackgroundResource(R.drawable.bg_interest_routine_item)
            } else {
                if (selectedList.size >= MAX_ROUTINE) { // 새로운 아이템을 클릭했으나 size가 가득 찬 경우
                    isMax = true
                    return
                } else {
                    selectedList.add(position)
                    interestLayout.setBackgroundResource(R.drawable.bg_interest_routine_itemclick)
                }

            }

            Log.e("selectedListSize", selectedList.size.toString())
        }

        private fun checkList(arrayList: ArrayList<Int>) {
            for(i in 0 until arrayList.size) {
                Log.e("selectedList", arrayList[i].toString())
            }
        }

    }

}