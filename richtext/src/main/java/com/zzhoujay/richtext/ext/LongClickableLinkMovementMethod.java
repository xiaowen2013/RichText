package com.zzhoujay.richtext.ext;

import android.graphics.Rect;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import com.zzhoujay.richtext.spans.LongClickable;

/**
 * Created by zhou on 16-8-4.
 * 支持长按的MovementMethod
 */
public class LongClickableLinkMovementMethod extends LinkMovementMethod {

    private static final int MIN_INTERVAL = 500;

    private long lastTime;

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                long currTime = System.currentTimeMillis();
                ClickableSpan l = link[0];
                int ls = buffer.getSpanStart(l);
                int le = buffer.getSpanEnd(l);
                // 判断点击的点是否在Image范围内
                ImageSpan[] is = buffer.getSpans(ls, le, ImageSpan.class);
                if (is.length > 0) {
                    Rect r = is[0].getDrawable().getBounds();
                    if (x < r.left || x > r.right) {
                        Selection.removeSelection(buffer);
                        return false;
                    }
                }else if(off<layout.getOffsetToLeftOf(ls)||off>layout.getOffsetToLeftOf(le+1)){
                    // 判断点击位置是否在链接范围内
                    Selection.removeSelection(buffer);
                    return false;
                }

                if (action == MotionEvent.ACTION_UP) {
                    // 如果按下时间超过５００毫秒，触发长按事件
                    if (currTime - lastTime > MIN_INTERVAL && l instanceof LongClickable) {
                        if (!((LongClickable) l).onLongClick(widget)) {
                            // onLongClick返回false代表事件未处理，交由onClick处理
                            l.onClick(widget);
                        }
                    } else {
                        l.onClick(widget);
                    }
                } else {
                    Selection.setSelection(buffer,
                            ls, le);
                }
                lastTime = currTime;
                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }
}
