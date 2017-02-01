package com.intelliviz.wearableapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.intelliviz.wearableapplication.MainActivity.WEATHER_MAXTEMP;
import static com.intelliviz.wearableapplication.MainActivity.WEATHER_MINTEMP;
import static com.intelliviz.wearableapplication.MainActivity.WEATHER_IMAGE;

public class DataLayerListenerService extends WearableListenerService {
    private static final String TAG = "DataLayerSample";
    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";
    private static final int TIMEOUT_MS = 1000;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        final List<DataEvent> events = FreezableUtils
                .freezeIterable(dataEvents);
        Log.d(TAG, "we made it here");

        // Loop through the events and send a message
        // to the node that created the data item.
        for (DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri != null ? uri.getPath() : null;
            if("/PHONE2WEAR".equals(path)) {

                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();

                int mintemp = map.getInt("mintemp");
                int maxtemp = map.getInt("maxtemp");

                Intent localIntent = new Intent(MainActivity.LOCAL_ACTION);
                localIntent.putExtra(WEATHER_MINTEMP, mintemp);
                localIntent.putExtra(WEATHER_MAXTEMP, maxtemp);

                Asset asset = map.getAsset("imageData");

                Bitmap bitmap = loadBitmapFromAsset(asset);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                localIntent.putExtra(WEATHER_IMAGE, byteArray);
                LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            }
        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mGoogleApiClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

}
