package com.intelliviz.wearableapplication;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;

import static android.R.attr.path;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    public static final String TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private int _value;
    private Spinner mWeatherCondiitonsSpinner;
    private  EditText mMinTemp;
    private EditText mMaxTemp;
    /**
     * A numeric value that identifies the notification that we'll be sending.
     * This value needs to be unique within this app, but it doesn't need to be
     * unique system-wide.
     */
    public static final int NOTIFICATION_ID = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        String[] weatherConditions = getResources().getStringArray(R.array.weather_conditions);
        mWeatherCondiitonsSpinner = (Spinner)findViewById(R.id.weather_conditions_spinner);

        // This one example of how to create an ArrayAdapter
        //ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.number_of_players, R.layout.num_player_spinner_item);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, weatherConditions);
        mWeatherCondiitonsSpinner.setAdapter(adapter);
        mWeatherCondiitonsSpinner.setSelection(0);

        _value = 0;

        mMinTemp = (EditText) findViewById(R.id.minTemp_textview);
        mMaxTemp = (EditText) findViewById(R.id.maxTemp_textview);
    }

    /**
     * Send a sample notification using the NotificationCompat API.
     */
    public void sendNotification(View view) {
        sendTestData();
    }

    public void sendNotification2(View view) {

        // BEGIN_INCLUDE(build_action)
        /** Create an intent that will be fired when the user clicks the notification.
         * The intent needs to be packaged into a {@link android.app.PendingIntent} so that the
         * notification service can fire it on our behalf.
         */
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://developer.android.com/reference/android/app/Notification.html"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        // END_INCLUDE(build_action)

        // BEGIN_INCLUDE (build_notification)
        /**
         * Use NotificationCompat.Builder to set up our notification.
         */
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        /** Set the icon that will appear in the notification bar. This icon also appears
         * in the lower right hand corner of the notification itself.
         *
         * Important note: although you can use any drawable as the small icon, Android
         * design guidelines state that the icon should be simple and monochrome. Full-color
         * bitmaps or busy images don't render well on smaller screens and can end up
         * confusing the user.
         */
        builder.setSmallIcon(R.drawable.ic_stat_notification);

        // Set the intent that will fire when the user taps the notification.
        builder.setContentIntent(pendingIntent);

        // Set the notification to auto-cancel. This means that the notification will disappear
        // after the user taps it, rather than remaining until it's explicitly dismissed.
        builder.setAutoCancel(true);

        /**
         *Build the notification's appearance.
         * Set the large icon, which appears on the left of the notification. In this
         * sample we'll set the large icon to be the same as our app icon. The app icon is a
         * reasonable default if you don't have anything more compelling to use as an icon.
         */
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));

        /**
         * Set the text of the notification. This sample sets the three most commononly used
         * text areas:
         * 1. The content title, which appears in large type at the top of the notification
         * 2. The content text, which appears in smaller text below the title
         * 3. The subtext, which appears under the text on newer devices. Devices running
         *    versions of Android prior to 4.2 will ignore this field, so don't use it for
         *    anything vital!
         */
        builder.setContentTitle("BasicNotifications Sample");
        builder.setContentText("Time to learn about notifications!");
        builder.setSubText("Tap to view documentation about notifications.");

        // END_INCLUDE (build_notification)

        // Create a big text style for the second page
        NotificationCompat.BigTextStyle secondPageStyle = new NotificationCompat.BigTextStyle();
        secondPageStyle.setBigContentTitle("Page 2")
                .bigText("A lot of text...");
        // Create second page notification
        Notification secondPageNotification =
                new NotificationCompat.Builder(this)
                        .setStyle(secondPageStyle)
                        .build();

        // Extend the notification builder with the second page
        Notification notification = builder
                .extend(new NotificationCompat.WearableExtender()
                        .addPage(secondPageNotification))
                .build();


        // BEGIN_INCLUDE(send_notification)
        /**
         * Send the notification. This will immediately display the notification icon in the
         * notification bar.
         */
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        // END_INCLUDE(send_notification)
    }

    public void sendTestData() {
        if(mGoogleApiClient.isConnected()) {
            PutDataMapRequest putRequest = PutDataMapRequest.create("/PHONE2WEAR");
            DataMap map = putRequest.getDataMap();
            map.putInt("testdata", _value++);

            String weatherItem = (String)mWeatherCondiitonsSpinner.getSelectedItem();
            Bitmap bitmap = getBitmap(weatherItem);
            Asset asset = createAssetFromBitmap(bitmap);
            map.putAsset("imageData", asset);

            int minTemp = Integer.parseInt(mMinTemp.getText().toString());
            map.putInt("mintemp", minTemp);
            int maxTemp = Integer.parseInt(mMaxTemp.getText().toString());
            map.putInt("maxtemp", maxTemp);

            PutDataRequest request = putRequest.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult =
                    Wearable.DataApi.putDataItem(mGoogleApiClient, request);

            pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                    if (!dataItemResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Failed to send test data " + path);
                    } else {
                        Log.e(TAG, "Successfully send test data " + path);
                    }
                }
            });
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        byte[] data = byteStream.toByteArray();
        Log.d(TAG, "Number of bytes in asset image: " + data.length);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    private Bitmap getBitmap(String condition) {
        Drawable image = null;
        Bitmap bitmap = null;
        if(condition.equals("Clear Day")) {
            image = ResourcesCompat.getDrawable(getResources(), R.mipmap.clear_day, null);
        } else if(condition.equals("Clear Night")) {
            image = ResourcesCompat.getDrawable(getResources(), R.mipmap.clear_night, null);
        } else if(condition.equals("Cloudy")) {
            image = ResourcesCompat.getDrawable(getResources(), R.mipmap.cloudy, null);
        } else if(condition.equals("Fog")) {
            image = ResourcesCompat.getDrawable(getResources(), R.mipmap.fog, null);
        } else if(condition.equals("Partly Cloudy")) {
            image = ResourcesCompat.getDrawable(getResources(), R.mipmap.partly_cloudy, null);
        } else if(condition.equals("Rain")) {
            image = ResourcesCompat.getDrawable(getResources(), R.mipmap.rain, null);
        } else if(condition.equals("Sleet")) {
            image = ResourcesCompat.getDrawable(getResources(), R.mipmap.sleet, null);
        } else if(condition.equals("Snow")) {
            image = ResourcesCompat.getDrawable(getResources(), R.mipmap.snow, null);
        } else if(condition.equals("Sunny")) {
            image = ResourcesCompat.getDrawable(getResources(), R.mipmap.sunny, null);
        } else if(condition.equals("Wind")) {
            image = ResourcesCompat.getDrawable(getResources(), R.mipmap.wind, null);
        } else {
            return null;
        }

        bitmap = ((BitmapDrawable)image).getBitmap();
        return bitmap;
    }
}
