package com.catchat.app.ui.progress;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.catchat.app.R;

public class CatProgressDialog extends ProgressDialog {
    private SvgPathView mAnimatingPathView;
    private String mMessageText;

    public CatProgressDialog(Context context) {
        super(context);
    }

    public CatProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public static ProgressDialog show(Context c, String message) {
        CatProgressDialog d = new CatProgressDialog(c);
        d.setMessageText(message);
        d.setIndeterminate(true);
        d.show();

        return d;
    }

    public void setMessageText(String message) {
        mMessageText = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cat_progress_dialog);

        TextView textView = (TextView)findViewById(R.id.message_textview);
        textView.setText(mMessageText);

        mAnimatingPathView = (SvgPathView)findViewById(R.id.pathview);
        mAnimatingPathView.setSvgResource(R.raw.catchat_large_bg_outline);
    }

    @Override
    public void show() {
        super.show();

        mAnimatingPathView.startAnimation();
    }

    @Override
    public void dismiss() {
        super.dismiss();

        mAnimatingPathView.stopAnimation();
    }
}
