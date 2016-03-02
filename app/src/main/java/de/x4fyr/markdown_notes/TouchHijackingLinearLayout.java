package de.x4fyr.markdown_notes;

import android.widget.LinearLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class TouchHijackingLinearLayout extends LinearLayout {
    public TouchHijackingLinearLayout(Context context){
        super(context);
    }

    public TouchHijackingLinearLayout(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}
