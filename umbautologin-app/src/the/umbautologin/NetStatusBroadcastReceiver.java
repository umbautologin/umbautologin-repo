
package the.umbautologin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import the.umbautologin.R;
import the.umbautologin.db.*;
import the.umbautologin.model.HistoryItem;


public class NetStatusBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = "umbAutoLogin";
    private Context             context;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        this.context = context;

        final String action = intent.getAction();
        Log.d(TAG, "Broadcast received. Action=" + action);

        SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, 0);
        if(!settings.getBoolean(Constants.PREF_KEY_ACTIVE, true))
        {
            Log.i(TAG, "Disabled. Ignoring broadcast.");
            return;
        }

        NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(ni == null || !ni.isConnected())
        {
            Log.d(TAG, "Not connected");
            return;
        }

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo winfo = wifi.getConnectionInfo();
        String ssid = winfo.getSSID();

        
        Log.d(TAG, "SSID=" + ssid);
        if(Constants.UMB_SSID.equals(ssid))
        {
            SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
            Log.d(TAG, "UMB SSDID detected. SSID=" + ssid);
            UMBWifi s = new UMBWifi();
            HistoryItem h = new HistoryItem();
            h.setDate(new Date());
            try
            {
            	String testURL = getTestURL(context, settings);
            	String uName = getuName(context, settings);
            	String uPwd = getuPwd(context, settings);
            	
                boolean status = s.login(testURL, uName, uPwd);
                h.setSuccess(true);
                if(status)
                {
                    if(prefs.getBoolean(Constants.PREF_KEY_NOTIFY_WHEN_SUCCESS, true))
                    {
                        createNotification(context.getString(R.string.notify_message_success));
                    }
                    h.setMessage("Logged in ~ Donate to Joseph Paul Cohen?");
                } else
                {
                    if(prefs.getBoolean(Constants.PREF_KEY_NOTIFY_WHEN_ALREADY_LOGGED_IN, false))
                    {
                        createNotification(context.getString(R.string.notify_message_already_logged));
                    }
                    h.setMessage("Already logged in");
                }
            } catch(Exception e)
            {
                if(prefs.getBoolean(Constants.PREF_KEY_NOTIFY_WHEN_ERROR, true))
                {
                    createNotification(context.getString(R.string.notify_message_error));
                }
                Log.e(TAG, "Login failed", e);
                h.setSuccess(false);
                h.setMessage("Login failed: " + e.getMessage());
            }
            DBAccesser db = new DBAccesser(context);
            db.addHistoryItem(h);
        } 
//        else if(Constants.STARBUCKS_SSID.equals(ssid))
//        {
//            SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
//            Log.d(TAG, "Starbucks SSDID detected. SSID=" + ssid);
//            Starbucks s = new Starbucks();
//            HistoryItem h = new HistoryItem();
//            h.setDate(new Date());
//            try
//            {
//                boolean status = s.login(getTestURL(context, settings));
//                h.setSuccess(true);
//                if(status)
//                {
//                    if(prefs.getBoolean(Constants.PREF_KEY_NOTIFY_WHEN_SUCCESS, true))
//                    {
//                        createNotification(context.getString(R.string.notify_message_success));
//                    }
//                    h.setMessage("Logged in");
//                } else
//                {
//                    if(prefs.getBoolean(Constants.PREF_KEY_NOTIFY_WHEN_ALREADY_LOGGED_IN, false))
//                    {
//                        createNotification(context.getString(R.string.notify_message_already_logged));
//                    }
//                    h.setMessage("Already logged in");
//                }
//            } catch(Exception e)
//            {
//                if(prefs.getBoolean(Constants.PREF_KEY_NOTIFY_WHEN_ERROR, true))
//                {
//                    createNotification(context.getString(R.string.notify_message_error));
//                }
//                Log.e(TAG, "Login failed", e);
//                h.setSuccess(false);
//                h.setMessage("Login failed: " + e.getMessage());
//            }
//            DBAccesser db = new DBAccesser(context);
//            db.addHistoryItem(h);
//        } 
        else
        {
            Log.d(TAG, "Unknown SSID " + ssid);
        }
    }

    private String getTestURL(Context context, SharedPreferences settings)
    {
        String default_url = context.getString(R.string.defaulturl);
        String s = settings.getString(Constants.PREF_KEY_URL, default_url);
        if(s == null || s.equals(default_url))
            return s;
        s = s.trim();
        try
        {
            new URL(s);
            return s;
        } catch(MalformedURLException mex)
        {
            return default_url;
        }
    }
    
    private String getuName(Context context, SharedPreferences settings)
    {
        String default_uname = context.getString(R.string.defaultusername);
        String s = settings.getString(Constants.PREF_KEY_USERNAME, default_uname);
        if(s == null || s.equals(default_uname))
            return s;
        s = s.trim();
        return s;
    }
    
    private String getuPwd(Context context, SharedPreferences settings)
    {
        String default_upwd = context.getString(R.string.defaultpasswd);
        String s = settings.getString(Constants.PREF_KEY_PASSWORD, default_upwd);
        if(s == null || s.equals(default_upwd))
            return s;
        s = s.trim();
        return s;
    }

    private void createNotification(String message)
    {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.icon, context.getString(R.string.notify_title),
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setLatestEventInfo(context, context.getString(R.string.notify_title), message, contentIntent);
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }

}
