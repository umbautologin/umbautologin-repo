
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
    public void onReceive(final Context context, final Intent intent)
    {
        this.context = context;

        final String action = intent.getAction();
        Log.d(TAG, "Broadcast received. Action=" + action);

        final SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, 0);
        if(!settings.getBoolean(Constants.PREF_KEY_ACTIVE, true)){
            Log.i(TAG, "Disabled. Ignoring broadcast.");
            return;
        }

        NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(ni == null || !ni.isConnected()){
            Log.d(TAG, "Not connected");
            return;
        }

        final WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        final WifiInfo winfo = wifi.getConnectionInfo();
        final String ssid = winfo.getSSID();

        
        Log.d(TAG, "SSID=" + ssid);
        if(ssid.contains(Constants.UMB_SSID)){ //Constants.UMB_SSID.toUpperCase().equals(ssid.toUpperCase())){
        	Log.d(TAG, "UMB SSID detected. SSID=" + ssid);
            
        	runUMBLogin(context);
  
        }else if (ssid.contains(Constants.UMBGUEST_SSID)){
        	
        	createNotification(context.getString(R.string.notify_umbguest_error), context);
        }else{
            Log.d(TAG, "Unknown SSID " + ssid);
        }
    }

    
    public static void runUMBLogin(Context context){
    	
    	final SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    	final SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, 0);
    	
        final HistoryItem h = new HistoryItem();
		h.setDate(new Date());
		
        try{
        	
        	String testURL = getTestURL(context, settings);
        	String uName = getuName(context, settings);
        	String uPwd = getuPwd(context, settings);
        	
        	Log.d(TAG, "Calling login API for UMBWifi");
        	final UMBWifi s = new UMBWifi();
            final boolean status = s.login(testURL, uName, uPwd);
            Log.d(TAG, "Login API for UMBWifi returned");
            h.setSuccess(true);
            
			if (status) {
				if (prefs.getBoolean(Constants.PREF_KEY_NOTIFY_WHEN_SUCCESS, true)) {
					createNotification(context
							.getString(R.string.notify_message_success), context);
				}
				h.setMessage("Logged in ~ Thanks Joseph Paul Cohen!");
				DBAccesser db = new DBAccesser(context);
	            db.addHistoryItem(h);
			} else {

				if (prefs.getBoolean(Constants.PREF_KEY_NOTIFY_WHEN_ALREADY_LOGGED_IN, false)) {
					createNotification(context.getString(R.string.notify_message_already_logged), context);
				}
				h.setMessage("Already logged in");
			}
        } catch(Exception e){
            if(prefs.getBoolean(Constants.PREF_KEY_NOTIFY_WHEN_ERROR, true)){
            	
                createNotification(context.getString(R.string.notify_message_error), context);
            }
            Log.e(TAG, "Login failed", e);
            h.setSuccess(false);
            h.setMessage("Login failed: " + e.getMessage());
            DBAccesser db = new DBAccesser(context);
            db.addHistoryItem(h);
        }
    	
    }
    
    private static String getTestURL(Context context, SharedPreferences settings)
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
    
    private static String getuName(Context context, SharedPreferences settings)
    {
        String default_uname = context.getString(R.string.defaultusername);
        String s = settings.getString(Constants.PREF_KEY_USERNAME, default_uname);
        if(s == null || s.equals(default_uname))
            return s;
        s = s.trim();
        return s;
    }
    
    private static String getuPwd(Context context, SharedPreferences settings)
    {
        String default_upwd = context.getString(R.string.defaultpasswd);
        String s = settings.getString(Constants.PREF_KEY_PASSWORD, default_upwd);
        if(s == null || s.equals(default_upwd))
            return s;
        s = s.trim();
        return s;
    }

    private static void createNotification(String message, Context context)
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
