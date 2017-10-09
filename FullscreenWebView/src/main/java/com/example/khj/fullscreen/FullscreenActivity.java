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
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
    KeyDatabase dbfriend;
    SQLiteDatabase db;
    ContentResolver cr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);


        view = (WebView) this.findViewById(R.id.webView);


        dbfriend = new KeyDatabase(this.getBaseContext());
        db = dbfriend.getWritableDatabase();
        cr = getContentResolver();
        handler = new Handler();


        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        //view.enableSlowWholeDocumentDraw();
        view.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(final WebView view, String url) {
                Log.d("wan_", "called");
                updateKeyColor();
            }
        });

        view.loadUrl("http://www.afreecatv.com");


        mVisible = true;
        //mControlsView = findViewById(R.id.fullscreen_content_controls);
        //mContentView = findViewById(R.id.fullscreen_content);
    }

    /* Start capturing web view repeatedly */
    Runnable updateKeyColors = new Runnable() {
        @Override
        public void run() {
            int width, heigth;
            width = view.getWidth(); heigth = view.getHeight();
/*            view.measure(View.MeasureSpec.makeMeasureSpec(
                    View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            view.layout(0, 0, view.getMeasuredWidth(),
                    view.getMeasuredHeight());
*/
            view.setDrawingCacheEnabled(true);
            //view.buildDrawingCache();
/*            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                    view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);*/
            //bitmap = Bitmap.createBitmap(width,
            //        heigth, Bitmap.Config.RGB_565);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            //bitmap.setHeight(heigth);
            //bitmap.setWidth(width);
            bitmap.setConfig(Bitmap.Config.RGB_565);

            //Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), view.);
            Cursor cursor = cr.query(Uri.parse(URL), null, null, null, null);// select
            String where = new String("x =? AND y=?");

            try {
                cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    int x = cursor.getInt(cursor.getColumnIndex("x"));
                    int y = cursor.getInt(cursor.getColumnIndex("y"));

                    ContentValues values1 = new ContentValues();
                    if(y < bitmap.getHeight() && x < bitmap.getWidth()) {
                        values1.put("color", bitmap.getPixel(x, y));
                        Log.d("fwang", "x:"+x+" y:"+y + " h : " + bitmap.getHeight() );
                        cr.update(Uri.parse(URL), values1, "x="+x+" AND y="+y , null);
                    }
//                    db.update("keys", values1, "x=" + x + " AND " + "y=" + y, null);

                }
            } finally {
                bitmap.recycle();
                cursor.close();
            }
            handler.postDelayed(updateKeyColors, interval);
        }
    };
    /*
    @Override
    protected void onDestroy(){
        bitmap.recycle();
        bitmap = null;
        super.onDestroy();
    }*/

    void updateKeyColor() {
        updateKeyColors.run();
    }

    class KeyDatabase extends SQLiteOpenHelper {//SQLiteOpenHelper -> DB생성을 돕겠다

        public KeyDatabase(Context context) {
            super(context, "swc_key.db", null, 1);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onCreate(SQLiteDatabase db) { //SQLiteOpenHelper 가 DB를 만들고 db의 포인트를 넘겨준다

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub

        }
    }
}

