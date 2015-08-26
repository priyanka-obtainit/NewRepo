package com.example.shireen.pushwebapplication;
import static com.example.shireen.pushwebapplication.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.example.shireen.pushwebapplication.CommonUtilities.EXTRA_MESSAGE;
import static com.example.shireen.pushwebapplication.CommonUtilities.SENDER_ID;
import static com.example.shireen.pushwebapplication.CommonUtilities.SERVER_URL;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {

  // private String TAG = "** pushAndroidActivity **";

    private TextView mDisplay;

    //TextView RuA,paC,DEF;
    //TextView Defense_Tackles[];

    AsyncTask<Void, Void, Void> mRegisterTask;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {


            super.onCreate(savedInstanceState);
            checkNotNull(SERVER_URL, "SERVER_URL");
            checkNotNull(SENDER_ID, "SENDER_ID");
            // Make sure the device has the proper dependencies.
            GCMRegistrar.checkDevice(this);
            // Make sure the manifest was properly set - comment out this line
            // while developing the app, then uncomment it when it's ready.
            GCMRegistrar.checkManifest(this);
            setContentView(R.layout.activity_main);
            mDisplay = (TextView) findViewById(R.id.display);
            registerReceiver(mHandleMessageReceiver,
                    new IntentFilter(DISPLAY_MESSAGE_ACTION));
            final String regId = GCMRegistrar.getRegistrationId(this);
            if (regId.equals("")) {
                // Automatically registers application on startup.
                GCMRegistrar.register(this, SENDER_ID);
            } else {
                // Device is already registered on GCM, check server.
                if (GCMRegistrar.isRegisteredOnServer(this)) {
                    // Skips registration.
                    mDisplay.append(getString(R.string.already_registered) + "\n");
                } else {
                    // Try to register again, but not in the UI thread.
                    // It's also necessary to cancel the thread onDestroy(),
                    // hence the use of AsyncTask instead of a raw thread.
                    final Context context = this;
                    mRegisterTask = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            boolean registered =
                                    ServerUtilities.register(context, regId);
                            // At this point all attempts to register with the app
                            // server failed, so we need to unregister the device
                            // from GCM - the app will try to register again when
                            // it is restarted. Note that GCM will send an
                            // unregistered callback upon completion, but
                            // GCMIntentService.onUnregistered() will ignore it.
                            if (!registered) {
                                GCMRegistrar.unregister(context);
                            }
                            return null;
                        }
                        @Override
                        protected void onPostExecute(Void result) {
                            mRegisterTask = null;
                        }
                    };
                    mRegisterTask.execute(null, null, null);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            /*
             * Typically, an application registers automatically, so options
             * below are disabled. Uncomment them if you want to manually
             * register or unregister the device (you will also need to
             * uncomment the equivalent options on options_menu.xml).
             */
            /*
            case R.id.options_register:
                GCMRegistrar.register(this, SENDER_ID);
                return true;
            case R.id.options_unregister:
                GCMRegistrar.unregister(this);
                return true;
             */
            case R.id.options_clear:
                mDisplay.setText(null);
                return true;
            case R.id.options_exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

    }

    @Override
    protected void onDestroy() {
        if (mRegisterTask != null) {
            mRegisterTask.cancel(true);
        }
        unregisterReceiver(mHandleMessageReceiver);
        GCMRegistrar.onDestroy(this);
        super.onDestroy();
    }

    private void checkNotNull(Object reference, String name) {
        try
        {
            if (reference == null) {
                throw new NullPointerException(
                        getString(R.string.error_config, name));
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
    private final BroadcastReceiver mHandleMessageReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try{
                        String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
                        mDisplay.append(newMessage + "\n");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }
            };


}
