package com.bunchofstring.test.netcon;

import com.bunchofstring.test.CoreUtils;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NetworkStateProvider {

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
        //Assumption: It is possible for "mDataConnectionState" to appear multiple times in the response
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
        final String result = CoreUtils.executeShellCommand("cmd wifi status");
        String returnValue = "UNKNOWN";
        if(result.contains("Wifi is disabled") || result.contains("Wifi is not connected")){
            returnValue = WIFI_DISCONNECTED;
        }else if(result.contains("Wifi is connected")){
            returnValue = WIFI_CONNECTED;
        }

        LOGGER.log(Level.INFO,"getWifiConnectionState() returnValue="+returnValue+" result="+result);
        return returnValue;
    }
}
