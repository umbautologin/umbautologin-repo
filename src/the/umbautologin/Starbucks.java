package the.umbautologin;

import java.io.*;
import java.net.*;
import java.util.Map;

import android.util.Log;

/**
 * Allows the user to accept the Starbucks Wi-Fi terms and conditions without
 * having to open a browser.
 * 
 * Based on source code from: 
 *  http://mangstacular.blogspot.com/2010/10/starbucks-wi-fi.html
 * 
 * @author michael
 */
public class Starbucks
{
    private static final String TAG = "SbAutoLogin";

    public Starbucks()
    {
    }
    
    /**
     * Attempts to log in to Starbucks WiFi.
     * 
     * @return true if login was performed. false it you were already logged in and no login is required.
     * @throws Exception if login failed.
     */
    public boolean login(String test_url) throws Exception
    {
        URL testURL = new URL(test_url);

        // disable the automatic following of redirects
        // a 3xx response can be used to determine whether or not the computer
        // is already connected to the Internet
        HttpURLConnection.setFollowRedirects(false);

        // try to visit a website
        Log.d(TAG, "Attempting to visit [" + testURL + "]...");
        HttpURLConnection conn = (HttpURLConnection) testURL.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(false);
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP)
        {
            // if you haven't accepted the terms and conditions yet, 302 is
            // returned, redirecting you to the login page

            // get the Location header, which contains the redirect URL
            String redirectUrlStr = conn.getHeaderField("Location");

            // go to the redirect URL, which is the Starbucks login page
            conn.disconnect();
            URL redirectUrl = new URL(redirectUrlStr);
            Log.d(TAG, "Downloading Starbucks login page [" + redirectUrl + "]...");
            conn = (HttpURLConnection) redirectUrl.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setRequestMethod("GET");

            // get the HTML of the webpage
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder html = new StringBuilder();
            while((line = in.readLine()) != null)
            {
                html.append(line);
            }
            in.close();
            conn.disconnect();

            // parse the form info out of the HTML
            Log.d(TAG, "Parsing Starbucks login page...");
            HtmlForm formInfo = new HtmlForm(redirectUrl, html.toString());

            // prepare to submit the form
            Log.d(TAG, "Accepting the terms and conditions...");
            conn = (HttpURLConnection) formInfo.actionUrl.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod(formInfo.method);

            // output parameters to request body
            StringBuilder sb = new StringBuilder();
            for(Map.Entry<String, String> entry : formInfo.parameters.entrySet())
            {
                sb.append(URLEncoder.encode(entry.getKey(), "UTF-8") + '='
                        + URLEncoder.encode(entry.getValue(), "UTF-8") + '&');
            }
            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.print(sb.substring(0, sb.length() - 1)); // remove the last '&'
            out.flush();

            // send request
            conn.getResponseCode();
            conn.disconnect();

            // try to connect to the Internet again to see if it worked
            conn = (HttpURLConnection) testURL.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setRequestMethod("GET");
            responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK)
            {
                Log.d(TAG, "SUCCESS: The terms and conditions have been agreed to and you can now connect to the Internet!");
                return(true);
            } else
            {
                Log.e(TAG, "Error: Approval of terms and conditions failed. HTTP status code "+responseCode);
                throw new Exception("Error: Approval of terms and conditions failed. HTTP status code "+responseCode);
            }
        } else if(responseCode == HttpURLConnection.HTTP_OK)
        {
            Log.d(TAG, "You are already connected to the Internet.");
            return(false);
        } else
        {
            Log.e(TAG, "Unknown error: HTTP status code " + responseCode);
            throw new Exception("Unknown error: HTTP status code " + responseCode);
        }
    }

}
