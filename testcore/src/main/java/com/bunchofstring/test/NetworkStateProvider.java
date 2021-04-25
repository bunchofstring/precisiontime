package com.bunchofstring.test;

import android.os.Build;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkStateProvider {

    private static final Logger LOGGER = Logger.getLogger(NetworkStateProvider.class.getSimpleName());

    private static final String DATA_ENABLED = "2";
    private static final String WIFI_ENABLED = "enabled";
    private static final String WIFI_CONNECTED = "CONNECTED/CONNECTED";
    private static final String WIFI_DISCONNECTED = "DISCONNECTED/DISCONNECTED";

    public boolean isDataEnabled() throws Exception {
        return DATA_ENABLED.equals(getDataState());
    }

    public boolean isWifiEnabled() throws Exception {
        return WIFI_ENABLED.equals(getWifiState());
    }

    public boolean isWifiConnected() throws Exception {
        return WIFI_CONNECTED.equals(getWifiConnectionState());
    }

    public boolean isWifiDisconnectedAndStable() throws Exception {
        return WIFI_DISCONNECTED.equals(getWifiConnectionState());
    }

    private String getDataState() throws Exception {
        //TODO: Check assumption that it is possible for "mDataConnectionState" to appear multiple times in the response
        final String result = CoreUtils.executeShellCommand("dumpsys telephony.registry");
        final int lastIndex = result.lastIndexOf("mDataConnectionState");
        if(lastIndex < 0) throw new RuntimeException("Could not determine Data state");

        final String trimmedResult = result.substring(lastIndex).trim();
        final Pattern p = Pattern.compile("mDataConnectionState=(.*)");
        final Matcher m = p.matcher(trimmedResult);
        if(!m.find()) throw new RuntimeException("Could not parse Data state");

        final String returnValue = m.group(1);
        LOGGER.log(Level.INFO,"getDataState() returnValue="+returnValue);
        return returnValue;
    }

    private String getWifiState() throws Exception {
        //Note that it is possible for "Wi-Fi is" to appear multiple times in the response
        final String result = CoreUtils.executeShellCommand("dumpsys wifi");
        final int lastIndex = result.lastIndexOf("Wi-Fi is");
        if(lastIndex < 0) throw new RuntimeException("Could not determine Wifi state");

        final String trimmedResult = result.substring(lastIndex).trim();
        final Pattern p = Pattern.compile("Wi-Fi is (.*)");
        final Matcher m = p.matcher(trimmedResult);
        if(!m.find()) throw new RuntimeException("Could not parse Wifi state");

        final String returnValue = m.group(1);
        LOGGER.log(Level.INFO,"getWifiState() returnValue="+returnValue);
        return returnValue;
    }

    private String getWifiConnectionState() throws Exception {
        //if(CoreUtils.isEmulator()){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            //TODO: Check assumption that it is possible for "mNetworkInfo" to appear multiple times in the response
            final String result = CoreUtils.executeShellCommand("dumpsys wifi");
            final int lastIndex = result.lastIndexOf("mNetworkInfo");
            if (lastIndex < 0) throw new RuntimeException("Could not determine Wifi connection state");

            final String trimmedResult = result.substring(lastIndex).trim();
            final Pattern p = Pattern.compile("state: (.*?),");
            final Matcher m = p.matcher(trimmedResult);
            if (!m.find()) {
                throw new RuntimeException("Could not parse Wifi connection state");
            }

            final String returnValue = m.group(1);
            LOGGER.log(Level.INFO, "getWifiConnectionState() returnValue=" + returnValue);
            return returnValue;
        }else {



            final String result = CoreUtils.executeShellCommand("cmd wifi status");
            String returnValue = "UNKNOWN";
            if(result.contains("Wifi is disabled") || result.contains("Wifi is not connected")){
                returnValue = WIFI_DISCONNECTED;
            }
            if(result.contains("Wifi is connected")){
                returnValue = WIFI_CONNECTED;
            }

            LOGGER.log(Level.INFO,"getWifiConnectionState() returnValue="+returnValue+" result="+result);
            return returnValue;




/*            //TODO: Check assumption that it is possible for "mNetworkInfo" to appear multiple times in the response
            final String result = CoreUtils.executeShellCommand("dumpsys connectivity");
            final int lastIndex = result.indexOf("NetworkAgentInfo");
            String returnValue;
            if(lastIndex < 0){
                if(result.contains("Active default network: none")){
                    returnValue = WIFI_DISCONNECTED;
                }else{
                    //TODO: Trigger a retry instead. Only throw if it fails twice
                    throw new RuntimeException("Could not determine Wifi connection state");
                }
            }else {
                final String trimmedResult = result.substring(lastIndex).trim();
//        final Pattern p = Pattern.compile("WIFI.* (.*)");
                final Pattern p = Pattern.compile("state: (.*?),");
                final Matcher m = p.matcher(trimmedResult);
                if (!m.find()) {
                    throw new RuntimeException("Could not parse Wifi connection state " + result);
                }
                returnValue = m.group(1);
            }
            LOGGER.log(Level.INFO,"getWifiConnectionState() returnValue="+returnValue);
            return returnValue;*/
        }
    }
}
