package com.example.khj.fullscreen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends Activity {

    private static final float SCALE = 1.0f/8.0f;

    String URL = "content://com.example.android.softkeyboard/keys";
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 1000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;

    private View mControlsView;

    private boolean mVisible;
    private int interval = 1000; // Repeat this task every 5 seconds.
    private Handler handler;


    WebView view;
    ContentResolver cr;
    Canvas canvas;
    Bitmap bitmap;
    int width;
    int height;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        view = (WebView) this.findViewById(R.id.webView);

        cr = getContentResolver();
        handler = new Handler();


        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        view.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(final WebView view, String url) {
                updateKeyColor();
            }
        });

        view.loadUrl("http://www.naver.com");
        mVisible = true;
    }

    /* Start capturing web view repeatedly */
    Runnable updateKeyColors = new Runnable() {
        @Override
        public void run() {
            view.layout(0, 0, view.getWidth(), view.getHeight());

            Rect lo = new Rect();
            view.getLocalVisibleRect(lo);

            canvas.save();
            canvas.scale(SCALE, SCALE);
            canvas.translate(-lo.left, -lo.top);

            view.draw(canvas);
            canvas.restore();

            Cursor cursor = cr.query(Uri.parse(URL), null, null, null, null);// select
            String where = new String("x =? AND y=?");

            try {
                cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    int x = cursor.getInt(cursor.getColumnIndex("x"));
                    int y = cursor.getInt(cursor.getColumnIndex("y"));
                    int[] location = new int[2];
                    view.getLocationOnScreen(location);

                    ContentValues values1 = new ContentValues();

                    if(y * SCALE < bitmap.getHeight() && x * SCALE < bitmap.getWidth()) {
                        values1.put("color", bitmap.getPixel(x/8, y/8));
                        cr.update(Uri.parse(URL), values1, "x="+x+" AND y="+y, null);
                    }
                }
            } finally {
                cursor.close();
            }
            handler.postDelayed(updateKeyColors, interval);
        }
    };

    void updateKeyColor() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = (int)(size.x * SCALE);
        height = (int)(size.y * SCALE);

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        updateKeyColors.run();
    }
}

