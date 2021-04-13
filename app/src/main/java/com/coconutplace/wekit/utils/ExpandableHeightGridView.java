package com.coconutplace.wekit.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;

public class ExpandableHeightGridView extends GridView {

    //스크롤이 있는 viewGroup(WEKIT의 경우 recyclerview, scrollview등) 내에
    // GridView를 사용할 경우 GridView의 row가 1줄만 표현되는데,
    // ExpandableHeightGridView를 쓰면 GridView에 Add된 item 수 만큼 전부 표현된다(스크롤 없어짐)

    boolean expanded = true;
    public ExpandableHeightGridView(Context context) {
        super(context);
    }
    public ExpandableHeightGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableHeightGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public boolean isExpanded() { return expanded; }

    @Override public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isExpanded()) {
            int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = getMeasuredHeight();
        }
        else{
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
    public void setExpanded(boolean expanded) { //expandable 쓰기 싫을땐 false로 설정
        this.expanded = expanded;
    }
}
