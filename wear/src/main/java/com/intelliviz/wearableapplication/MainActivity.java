package com.intelliviz.wearableapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    BroadcastReceiver mBroadcastReceiver;
    private final SimpleDateFormat _sdfWatchTime = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat _sdfWatchDate = new SimpleDateFormat("E, MMM d yyyy");
    public static final String LOCAL_ACTION = "weatherBroadCast";
    public static final String WEATHER_MINTEMP = "mintemp";
    public static final String WEATHER_MAXTEMP = "mmatemp";
    public static final String WEATHER_IMAGE = "weatheriamge";
    private GoogleApiClient mGoogleApiClient;

    private TextView mTimeTextView;
    private TextView mDateTextView;
    private TextView mMinTempTextView;
    private TextView mMaxTempTextView;
    private ImageView mConditionImageView;
    private int mImageWidth;
    private int mImageHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTimeTextView = (TextView) stub.findViewById(R.id.timeText);
                mTimeTextView.setText(_sdfWatchTime.format(new Date()));
                mDateTextView = (TextView) stub.findViewById(R.id.dateText);
                mDateTextView.setText(_sdfWatchDate.format(new Date()));
                mConditionImageView = (ImageView) stub.findViewById(R.id.conditionImage);
                Drawable image = ResourcesCompat.getDrawable(getResources(), R.mipmap.rain, null);
                mConditionImageView.setImageDrawable(image);
                Drawable drawable = mConditionImageView.getDrawable();
                mImageWidth = image.getIntrinsicWidth();
                mImageHeight = image.getIntrinsicHeight();
                Log.d("receiver", "Intrinsic size: " + mImageWidth + "  " + mImageHeight);

                mMinTempTextView = (TextView) stub.findViewById(R.id.minTempText);
                mMaxTempTextView = (TextView) stub.findViewById(R.id.maxTempText);
            }
        });
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(LOCAL_ACTION));
        super.onResume();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Get extra data included in the Intent
            int minTemp = intent.getIntExtra(WEATHER_MINTEMP, 666);
            mMinTempTextView.setText("" + minTemp);
            int maxTemp = intent.getIntExtra(WEATHER_MAXTEMP, 666);
            mMaxTempTextView.setText("" + maxTemp);
            byte[] bitmapData = intent.getByteArrayExtra(WEATHER_IMAGE);

            if(bitmapData != null && bitmapData.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                bitmap = Bitmap.createScaledBitmap(bitmap, mImageWidth, mImageHeight, false);
                int height = bitmap.getHeight();
                int width = bitmap.getWidth();
                Log.d("receiver", "ImageView size: " + width + "  " + height);
                mConditionImageView.setAdjustViewBounds(false);
                mConditionImageView.setImageBitmap(bitmap);
            }
        }
    };
}
