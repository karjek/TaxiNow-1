package com.home.yassine.taxinow;

import android.content.Context;
import android.util.AttributeSet;
import com.facebook.login.widget.LoginButton;

public class CustomLoginButton extends LoginButton
{
    Context mContext;
    public String mCustomText;

    public CustomLoginButton(Context context)
    {
        super(context);
        mContext = context;
    }

    public CustomLoginButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
    }

    public CustomLoginButton(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        final String fbButtonLabel;
        if (this.getId() == R.id.fb_login_btn)
        {
            fbButtonLabel = mCustomText;
            this.setText(fbButtonLabel);
        }
    }
}
