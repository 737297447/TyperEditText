package cn.refactor.typer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;


/**
 * Create by andy (https://github.com/andyxialm)
 * Create time: 16/10/30 20:10
 * Description : Typewriter writer EditText
 */
public class TyperEditText extends EditText implements ITyperControl {

    private static final int INTERVAL_CHARACTER_WRITE   = 200;
    private static final int INTERVAL_PUNCTUATION_WRITE = 500;

    private int mCharacterWriteInterval;
    private int mPunctuationWriteInterval;
    private int mIndex;

    private boolean mStop;
    private boolean mPause;
    private boolean mTouchable;
    private Editable mEditable;

    public TyperEditText(Context context) {
        this(context, null);
    }

    public TyperEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TyperEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TyperEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return !mTouchable || super.onTouchEvent(event);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type == BufferType.NORMAL ? BufferType.EDITABLE : type);
        if (type != BufferType.NORMAL) {
            mEditable = getText();
            super.setText(null, BufferType.NORMAL);
        }
    }

    @Override
    public void start() {
        if (mEditable == null || mEditable.length() == 0) {
            return;
        }
        recycleWrite(mIndex);
    }

    @Override
    public void stop() {
        mStop = true;
        mIndex = 0;
    }

    @Override
    public void resume() {
        mStop = false;
        mPause = false;
        recycleWrite(++ mIndex);
    }

    @Override
    public void pause() {
        mPause = true;
    }

    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.TyperEditText);
        mCharacterWriteInterval = ta.getInteger(R.styleable.TyperEditText_characterWriteInterval, INTERVAL_CHARACTER_WRITE);
        mPunctuationWriteInterval = ta.getInteger(R.styleable.TyperEditText_punctuationWriteInterval, INTERVAL_PUNCTUATION_WRITE);
        mTouchable = ta.getBoolean(R.styleable.TyperEditText_touchable, false);
        ta.recycle();
    }

    private void recycleWrite(final int index) {
        if (mEditable == null || mEditable.length() == 0 || index == mEditable.length()) {
            return;
        }

        if (mPause || mStop) {
            return;
        }

        CharSequence subText = mEditable.subSequence(0, index + 1);
        setText(subText, BufferType.NORMAL);
        setSelection(subText.length());

        int interval = isChinesePunctuation(mEditable.charAt(mIndex)) ? mPunctuationWriteInterval : mCharacterWriteInterval;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                recycleWrite(++ mIndex);
            }
        }, interval);
    }

    private boolean isChinesePunctuation(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS;
    }
}