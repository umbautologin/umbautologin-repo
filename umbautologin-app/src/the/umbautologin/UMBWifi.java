package the.umbautologin;

import java.io.*;
import java.net.*;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.util.Log;

/**
 * This code is based on sbautologin code from the android app.
 * 
 * Released under the GPL
 * 
 * @author Joseph Paul Cohen 2012
 * old author was sbautologin
 * 
 */
public class UMBWifi
{
    private static final String TAG = "umbAutoLogin";

    private static int timeoutms = 4000;
    
    public UMBWifi()
    {
    }
    
    /**
     * Attempts to log in to UMB Wifi
     * 
     * @return true if login was performed. false it you were already logged in and no login is required.
     * @throws Exception if login failed.
     */
    public boolean login(String test_url, String uName, String uPwd) throws Exception
    {
        URL testURL = new URL(test_url);
    	
        // disable the automatic following of redirects
        // a 3xx response can be used to determine whether or not the computer
        // is already connected to the Internet
        HttpURLConnection.setFollowRedirects(false);

        // try to visit a website
        Log.d(TAG, "Attempting to visit [" + testURL + "]...");
        
        
//        HttpURLConnection conn = (HttpURLConnection) testURL.openConnection();
//        conn.setRequestMethod("GET");
//        conn.setDoInput(true);
//        conn.setDoOutput(true);
        
        Socket s = new Socket(testURL.getHost(), 80);
        s.setSoTimeout(timeoutms);

        BufferedWriter os = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        
        os.write("GET / HTTP/1.1\n");
        os.write("Host: google.com\n");
        os.write("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:11.0) Gecko/20100101 Firefox/11.0\n");
        os.write("Accept: text/html\n\n\n");
        os.flush();
        	

        
        BufferedReader is = new BufferedReader(new InputStreamReader(s.getInputStream()));
        
        // get the Location header, which contains the redirect URL
        String redirectUrlStr = "";
        int responseCode = 0;
        String toparse;
        while((toparse = is.readLine()) != null){

        	if (toparse.contains("HTTP/1.1 307"))
        		responseCode = 307;
        	
        	if (toparse.contains("HTTP/1.1 200"))
        		responseCode = HttpURLConnection.HTTP_OK;
        	
        	if (toparse.contains("Location:")){
        		redirectUrlStr = toparse.replace("Location:", "");
        	}
        	
        	// thats all we care about
        }
       
        
        
        
        Log.d(TAG, "ResponseCode:" + responseCode);
        if(true && responseCode == 307)
        {

            //////////////////////////
            // fake out ssl
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}
				public void checkServerTrusted(X509Certificate[] certs, String authType) {	
				}
				}
            };
            // Install the all-trusting trust manager
    		SSLContext sc = SSLContext.getInstance("TLS");
    		sc.init(null, trustAllCerts, new java.security.SecureRandom());
    		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    		// Create all-trusting host name verifier
    		HostnameVerifier allHostsValid = new HostnameVerifier() {
    			public boolean verify(String hostname, SSLSession session) {
    				return true;
    			}
    		};
            
    		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    		//////////////////////////
            
    		
    		
    		
    		
    		
    		
    		URL redirectUrl = new URL(redirectUrlStr);
            Log.d(TAG, "Downloading UMB login page [" + redirectUrl + "]...");
            HttpsURLConnection conn = (HttpsURLConnection) redirectUrl.openConnection();
            conn.setReadTimeout(timeoutms);
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
            Log.d(TAG, "Parsing UMB login page...");
            HtmlForm formInfo = new HtmlForm(redirectUrl, html.toString());

            
            
            // prepare to submit the form
            Log.d(TAG, "Signing in...");
            
            formInfo.parameters.put("uName", uName);
            formInfo.parameters.put("uPwd", uPwd);
            
            StringBuilder sb = new StringBuilder();
            for(Map.Entry<String, String> entry : formInfo.parameters.entrySet())
            {
                sb.append(URLEncoder.encode(entry.getKey(), "UTF-8") + '='
                        + URLEncoder.encode(entry.getValue(), "UTF-8") + '&');
            }
            
            String postdata = sb.substring(0, sb.length() - 1);
            
            
            
            // send the request
            
            
            
            Socket s2 = sc.getSocketFactory().createSocket(formInfo.actionUrl.getHost(), 443);
            s2.setSoTimeout(timeoutms);
            BufferedWriter sendlogin = new BufferedWriter(new OutputStreamWriter(s2.getOutputStream()));
            
            sendlogin.write("POST /authUser.php HTTP/1.1\n");
            sendlogin.write("Host: " + formInfo.actionUrl.getHost() +"\n");
            sendlogin.write("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:11.0) Gecko/20100101 Firefox/11.0\n");
            sendlogin.write("Accept: text/html\n");
            sendlogin.write("Referer: " + redirectUrl + "\n");
            sendlogin.write("Content-Type: application/x-www-form-urlencoded\n");
            sendlogin.write("Content-Length: " + postdata.length() + "\n\n");
            sendlogin.write(postdata);
            sendlogin.flush();
            
            //Log.d(TAG, "what was posted:" + postdata);
            
            BufferedReader islogin = new BufferedReader(new InputStreamReader(s2.getInputStream()));
            

            String response = "";
            while((toparse = islogin.readLine()) != null){
            	response += toparse;
            }
            islogin.close();
            s2.close();
            
            Log.d(TAG, "response-login:" + response);
            
            
            //fail=2 means wrong password
            if (response.contains("fail=2"))
            	throw new Exception("Error: fail=2, Check your username and password");
            
            //fail=0 some other error
            if (response.contains("fail=0"))
            	throw new Exception("Error: fail=0, Some internal error happened");
            
            
//            Log.d(TAG, "SUCCESS: You are signed into the UMB campus network ~ Give $1 to Joseph Paul Cohen?");
//            return(true);
            


//            Log.d(TAG, "testing connection again");
//            Socket s3 = new Socket(testURL.getHost(), 80);
//
//            os = new BufferedWriter(new OutputStreamWriter(s3.getOutputStream()));
//            
//            os.write("GET / HTTP/1.1\n");
//            os.write("Host: google.com\n");
//            os.write("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:11.0) Gecko/20100101 Firefox/11.0\n");
//            os.write("Accept: text/html\n\n\n");
//            os.flush();
//            	
//
//            
//            is = new BufferedReader(new InputStreamReader(s3.getInputStream()));
//            
//            // get the Location header, which contains the redirect URL
//
//            response = "";
//            while((toparse = is.readLine()) != null){
//            	response += toparse;
//            }
//            
//            if (response.contains("HTTP/1.1 200"))
//        		responseCode = HttpURLConnection.HTTP_OK;
//            
//            Log.d(TAG, "response-check:" + response);
//            
//            
            
         // try to connect to the Internet again to see if it worked
            HttpURLConnection con = (HttpURLConnection) testURL.openConnection();
            con.setReadTimeout(timeoutms);
            con.setDoInput(true);
            con.setDoOutput(false);
            con.setRequestMethod("GET");
            responseCode = con.getResponseCode();
            
            if(responseCode == HttpURLConnection.HTTP_OK || responseCode == 302)
            {
                Log.d(TAG, "SUCCESS: You are signed into the UMB campus network ~ Donate to Joseph Paul Cohen?");
                //throw new Exception("SUCCESS: You are signed into the UMB campus network ~ Give $1 to Joseph Paul Cohen?");
                return(true);
            } else
            {
                Log.e(TAG, "Error: Sign in Failed HTTP status code "+responseCode);
                throw new Exception("Error: Sign in Failed HTTP status code "+responseCode);
            }
        } else if(responseCode == HttpURLConnection.HTTP_OK || responseCode == 302)
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
