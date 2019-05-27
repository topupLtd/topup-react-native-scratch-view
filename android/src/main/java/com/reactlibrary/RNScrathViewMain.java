package com.reactlibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.reactlibrary.lib.ScrathImageView;

import java.io.InputStream;

public class RNScrathViewMain extends RelativeLayout {
    private ScrathImageView imagePattern;
    private TextView textScrated;
    private boolean imagePatternLoadEnd = false;
    private boolean imageScratchedLoadEnd = false;
    private boolean isImageScratchRevealed = false;
    private float revealPercent = 98.00f;

    public RNScrathViewMain(Context context) {
        super(context);

        this.setLayoutParams(new android.view.ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));


        this.textScrated = new TextView(context);
        this.textScrated.setLayoutParams(new android.view.ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        this.textScrated.setGravity(Gravity.CENTER);
        this.addView(textScrated);


        this.imagePattern = new ScrathImageView(context);
        this.imagePattern.setLayoutParams(new android.view.ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        this.addView(imagePattern);

        imagePattern.setVisibility(View.INVISIBLE);

        imagePattern.setOnScratchCallback(new ScrathImageView.OnScratchCallback() {
            @Override
            public void onScratch(float percentage) {
                ReactContext reactContext = (ReactContext) getContext();
                if (reactContext != null) {
                    if (percentage >= revealPercent) {
                        if (!isImageScratchRevealed) {
                            isImageScratchRevealed = true;

                            WritableMap event = Arguments.createMap();
                            reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                                    getId(),
                                    RNScratchImageViewManager.ON_REVEALED,
                                    event);
                        }
                    } else {
                        WritableMap event = Arguments.createMap();
                        // Limitamos a dos decimales. Ej: 2.33
                        event.putDouble("value", Math.round(percentage * 100d) / 100d);
                        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                                getId(),
                                RNScratchImageViewManager.ON_REVEAL_PERCENT_CHANGED,
                                event);
                    }
                }
            }

            @Override
            public void onDetach(boolean fingerDetach) {
                textScrated.setVisibility(VISIBLE);
            }
        });
    }

    public void setRevealPercent(float percent) {
        this.revealPercent = percent;
    }

    public void setRevealSize(int revealSize) {
        this.imagePattern.setRevealSize(revealSize);
    }

    public void setImageScratched(String scratedText) {
        textScrated.setText(scratedText);
        imageScratchedLoadEnd = true;
        if (imageScratchedLoadEnd && imagePatternLoadEnd) {
            imagePattern.setVisibility(VISIBLE);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    textScrated.setVisibility(VISIBLE);
                }
            }, 300);

        }
    }

    public void setImagePattern(String uri) {
        //new DownloadImageTask(false).execute(uri);
        imagePattern.setScratchBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_scratch_pattern));
        imagePatternLoadEnd = true;

    }


    private static Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        Bitmap output = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        m.setScale((float) maxWidth / bitmap.getWidth(), (float) maxHeight / bitmap.getHeight());
        canvas.drawBitmap(bitmap, m, new Paint());

        return output;
    }
}
