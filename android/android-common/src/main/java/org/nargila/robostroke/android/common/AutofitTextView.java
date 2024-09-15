package org.nargila.robostroke.android.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * A TextView that resizes it's text to be no larger than the width of the view.
 *
 * @author Grantland Chew <grantlandchew@gmail.com>
 */
public class AutofitTextView extends TextView {

    private static final String TAG = "me.grantland.widget.AutoFitTextView";
    private static final boolean SPEW = false;

    // Minimum size of the text in pixels
    private static final int DEFAULT_MIN_TEXT_SIZE = 8; //px
    // How precise we want to be when reaching the target textWidth size
    private static final float PRECISION = 0.5f;

    // Attributes
    private float mMinTextSize;
    private float mMaxTextSize;
    private float mPrecision;
    private Paint mPaint;

    public AutofitTextView(Context context) {
        super(context);
        init();
    }

    public AutofitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mMinTextSize = DEFAULT_MIN_TEXT_SIZE;
        mMaxTextSize = getTextSize();
        mPrecision = PRECISION;
        mPaint = new Paint();
    }

    // Getters and Setters

    public float getMinTextSize() {
        return mMinTextSize;
    }

    public void setMinTextSize(int minTextSize) {
        mMinTextSize = minTextSize;
    }

    public float getMaxTextSize() {
        return mMaxTextSize;
    }

    public void setMaxTextSize(int maxTextSize) {
        mMaxTextSize = maxTextSize;
    }

    public float getPrecision() {
        return mPrecision;
    }

    public void setPrecision(float precision) {
        mPrecision = precision;
    }

    /**
     * Re size the font so the specified text fits in the text box
     * assuming the text box is the specified width.
     */
    private void refitText(String text, int width) {
        if (width > 0) {
            Context context = getContext();
            Resources r = Resources.getSystem();

            int targetWidth = width - getPaddingLeft() - getPaddingRight();
            float newTextSize = mMaxTextSize;
            float high = mMaxTextSize;
            float low = 0;

            if (context != null) {
                r = context.getResources();
            }

            mPaint.set(getPaint());
            mPaint.setTextSize(newTextSize);

            if (mPaint.measureText(text) > targetWidth) {
                newTextSize = getTextSize(r, text, targetWidth, low, high);

                if (newTextSize < mMinTextSize) {
                    newTextSize = mMinTextSize;
                }
            }

            setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);

        }
    }

    // Recursive binary search to find the best size for the text
    private float getTextSize(Resources resources, String text, float targetWidth, float low, float high) {
        float mid = (low + high) / 2.0f;

        mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, mid, resources.getDisplayMetrics()));
        float textWidth = mPaint.measureText(text);

        if (SPEW)
            Log.d(TAG, "low=" + low + " high=" + high + " mid=" + mid + " target=" + targetWidth + " width=" + textWidth);

        if ((high - low) < mPrecision) {
            return low;
        } else if (textWidth > targetWidth) {
            return getTextSize(resources, text, targetWidth, low, mid);
        } else if (textWidth < targetWidth) {
            return getTextSize(resources, text, targetWidth, mid, high);
        } else {
            return mid;
        }
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int lengthBefore, final int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        refitText(text.toString(), this.getWidth());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw) {
            refitText(getText().toString(), w);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        refitText(getText().toString(), parentWidth);
    }
}