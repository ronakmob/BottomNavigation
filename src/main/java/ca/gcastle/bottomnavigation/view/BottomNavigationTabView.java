package ca.gcastle.bottomnavigation.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.gcastle.bottomnavigation.R;

/**
 * Created by graeme.castle on 12/04/2016.
 */
public class BottomNavigationTabView extends FrameLayout {

    GestureDetector mDetector;
    private OnClickListener mClickListener;
    private int mColor;
    private ImageView mImageView;
    private TextView mTextView;
    private TextView mBadge;

    private int mImageViewTop;
    private int mTextViewHeight = 0;

    private int m16dp;
    private int m6dp;

    private boolean alwaysShowText;
    private float   unselectedAlpha;

    public BottomNavigationTabView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if(mClickListener != null) {
                    mClickListener.onClick(BottomNavigationTabView.this);
                }
                return false;
            }
        });

        m16dp = BottomNavigationUtils.getPixelsFromDP(getContext(), 16);
        m6dp  = BottomNavigationUtils.getPixelsFromDP(getContext(), 6);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.bottomTab);

        CharSequence text    = a.getString(R.styleable.bottomTab_tabText);
        Drawable imageResId  = a.getDrawable(R.styleable.bottomTab_tabImage);
        mColor               = a.getColor(R.styleable.bottomTab_tabColor, 0x00000000);
        int textColor        = a.getColor(R.styleable.bottomTab_textColor, 0xFFFFFFFF);
        alwaysShowText       = a.getBoolean(R.styleable.bottomTab_alwaysShowText, true);
        unselectedAlpha      = a.getFloat(R.styleable.bottomTab_unselectedAlpha, 0.8f);

        a.recycle();

        LayoutInflater.from(context).inflate(getTabView(), this, true);

        mImageView      = (ImageView) findViewById(R.id.imageView);
        mBadge          = (TextView)  findViewById(R.id.badge);
        mTextView       = (TextView)  findViewById(R.id.textView);

        mImageView.setImageDrawable(imageResId);
        mTextView.setText(text);
        mTextView.setTextColor(textColor);

        if(alwaysShowText) {
            mTextViewHeight = getTextViewHeight();
            mImageViewTop = m6dp;
        } else {
            mImageViewTop = m16dp;
        }

        setAlpha(unselectedAlpha);
    }

    private int getTextViewHeight() {
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE/2, MeasureSpec.AT_MOST);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        mTextView.measure(widthMeasureSpec, heightMeasureSpec);
        return mTextView.getMeasuredHeight();
    }

    protected void setSelected() {
        mTextViewHeight = getTextViewHeight();
        mImageViewTop = m6dp;
        setAlpha(1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;

        int imageViewLeft   = (width - mImageView.getMeasuredWidth()) / 2;
        //int imageViewTop    = m16dp - mTextViewHeight;
        int imageViewRight  = imageViewLeft + mImageView.getMeasuredWidth();
        int imageViewBottom = mImageViewTop + mImageView.getMeasuredHeight();

        mImageView.layout(imageViewLeft,
                          mImageViewTop,
                          imageViewRight,
                          imageViewBottom);

        mBadge.layout(imageViewRight - mBadge.getMeasuredWidth(),
                      imageViewBottom - mBadge.getMeasuredHeight(),
                      imageViewRight,
                      imageViewBottom);

        int textViewLeft = (width - mTextView.getMeasuredWidth()) / 2;
        int textViewTop  = imageViewBottom + m6dp;

        mTextView.layout(textViewLeft,
                         textViewTop,
                         textViewLeft + mTextView.getMeasuredWidth(),
                         textViewTop + mTextViewHeight);
    }

    public int getTabView() {
        return R.layout.view_bottomnavigation_tab;
    }

    public int getColor() {
        return mColor;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return false;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mClickListener = l;
    }

    public void setCount(int count) {
        if(count == 0) {
            mBadge.setVisibility(View.GONE);
        } else {
            mBadge.setText(count);
            mBadge.setVisibility(View.VISIBLE);
        }
    }

    public Animator getAnimatorSet(boolean maximise) {
        AnimatorSet set = new AnimatorSet();

        ValueAnimator textSizeAnimator = null;
        ValueAnimator imageLocationAnimation = null;
        ValueAnimator alphaAnimator = null;

        if(maximise) {
            if(!alwaysShowText) {
                textSizeAnimator        = ValueAnimator.ofInt(0, mTextView.getMeasuredHeight());
                imageLocationAnimation  = ValueAnimator.ofInt(m16dp, m6dp);
            }
            alphaAnimator               = ValueAnimator.ofFloat(unselectedAlpha, 1f);
        } else {
            if(!alwaysShowText) {
                textSizeAnimator        = ValueAnimator.ofInt(mTextView.getMeasuredHeight(), 0);
                imageLocationAnimation  = ValueAnimator.ofInt(m6dp, m16dp);
            }
            alphaAnimator               = ValueAnimator.ofFloat(1f, unselectedAlpha);
        }

        if(textSizeAnimator != null)        textSizeAnimator.addUpdateListener(mTextViewHeightAnimatorListener);
        if(imageLocationAnimation != null)  imageLocationAnimation.addUpdateListener(mImageTopAnimatorListener);
        if(alphaAnimator != null)           alphaAnimator.addUpdateListener(mAlphaAnimatorListener);

        List<Animator> animatorList = new ArrayList<>();
        if(textSizeAnimator != null)        animatorList.add(textSizeAnimator);
        if(imageLocationAnimation != null)  animatorList.add(imageLocationAnimation);
        if(alphaAnimator != null)           animatorList.add(alphaAnimator);

        set.playTogether(animatorList);
        return set;
    }

    ValueAnimator.AnimatorUpdateListener mTextViewHeightAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mTextViewHeight = (int) animation.getAnimatedValue();
        }
    };

    ValueAnimator.AnimatorUpdateListener mImageTopAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mImageViewTop = (int) animation.getAnimatedValue();
        }
    };

    ValueAnimator.AnimatorUpdateListener mAlphaAnimatorListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            setAlpha((Float) animation.getAnimatedValue());
            requestLayout();
        }
    };
}
