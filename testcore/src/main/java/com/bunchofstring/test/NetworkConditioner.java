package com.bunchofstring.test;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkConditioner {

    private static final Logger LOGGER = Logger.getLogger(NetworkConditioner.class.getSimpleName());

    private final NetworkStateProvider nsp;
    private final boolean isWifiEnabledAtStart;
    private final boolean isDataEnabledAtStart;

    public NetworkConditioner() throws Exception {
        grantWiFiControlPrivileges();
        nsp = new NetworkStateProvider();
        isWifiEnabledAtStart = nsp.isWifiEnabled();
        isDataEnabledAtStart = nsp.isDataEnabled();
    }

    public void reset() throws Exception {
        LOGGER.log(Level.INFO, "reset()");
        setWifiEnabled(isWifiEnabledAtStart);
        setDataEnabled(isDataEnabledAtStart);
    }

    /*private static boolean hasSavedWifiNetwork(final String ssid) throws IOException {
        for(final WifiConfiguration wc : getWiFiManager().getConfiguredNetworks()){
            if(ssid.equals(wc.SSID)) return true;
        }
        return false;
    }*/

    public void setDataEnabled(final boolean enable) throws Exception {
        LOGGER.log(Level.INFO, "setDataEnabled(...) "+enable);
        final String state = enable ? "enable" : "disable";
        CoreUtils.executeShellCommand("svc data "+state);
    }

    /*
    //Prepare
    adb shell
    su

    //(Simulate weak connection (via high packet loss)
    iptables -A INPUT -m statistic --mode random --probability 0.99 -j DROP

    //Restore healthy connection (by reverting the simulation rule)
    iptables -D INPUT -m statistic --mode random --probability 0.99 -j DROP
    */

    public void setWifiEnabled(final boolean enable) throws Exception {
        LOGGER.log(Level.INFO, "setWifiEnabled(...) "+enable);
        final String state = enable ? "enable" : "disable";
        CoreUtils.executeShellCommand("svc wifi "+state);
        if(!enable) {
            awaitDisconnection();
        }
    }

    public void pairConnectWifiNetwork(final String ssid) throws Exception {
        LOGGER.log(Level.INFO, "pairConnectWifiNetwork(...) "+ssid);
        CoreUtils.executeShellCommand("cmd wifi connect-network "+ssid+" open");

        /*
        final String ssidValue = "\""+ssid+"\"";
        WifiManager wm = getWiFiManager();
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = ssidValue;
        int id = wm.addNetwork(wc);
        //wm.disconnect();
        wm.enableNetwork(id, true);
        //wm.reconnect();*/

        awaitConnection();
    }

    public void pairConnectWifiNetwork(final String ssid, final String pass) throws Exception {
        LOGGER.log(Level.INFO, "pairConnectWifiNetwork(...) "+ssid);
        CoreUtils.executeShellCommand("cmd wifi connect-network "+ssid+" wpa2 "+pass);

        /*
        final String ssidValue = "\""+ssid+"\"";
        final String passValue = "\""+pass+"\"";
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            WifiManager wm = getWiFiManager();
            WifiConfiguration wc = new WifiConfiguration();
            wc.SSID = ssidValue;
            wc.preSharedKey = passValue;
            int id = wm.addNetwork(wc);
            wm.disconnect();
            wm.enableNetwork(id, true);
            wm.reconnect();
        }else{
            WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
            builder.setSsid(ssid);
            builder.setWpa2Passphrase(pass);

            WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();

            NetworkRequest.Builder networkRequestBuilder1 = new NetworkRequest.Builder();
            networkRequestBuilder1.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
            networkRequestBuilder1.setNetworkSpecifier(wifiNetworkSpecifier);

            NetworkRequest nr = networkRequestBuilder1.build();
            ConnectivityManager cm = (ConnectivityManager) InstrumentationRegistry
                    .getInstrumentation()
                    .getContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    LOGGER.log(Level.INFO, "onAvailable(...)");

                    cm.bindProcessToNetwork(network);
                }
            };
            cm.requestNetwork(nr, networkCallback);
        }*/

        awaitConnection();
    }

    /*private WifiManager getWiFiManager() throws Exception {
        grantWiFiControlPrivileges();
        final Context c = InstrumentationRegistry.getInstrumentation().getContext();
        return (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
    }*/

    private void grantWiFiControlPrivileges() throws Exception {
        LOGGER.log(Level.INFO, "grantWiFiControlPrivileges()");
        CoreUtils.grantPermission("com.bunchofstring.precisiontime.test.test","android.permission.ACCESS_NETWORK_STATE");
        CoreUtils.grantPermission("com.bunchofstring.precisiontime.test.test","android.permission.ACCESS_WIFI_STATE");
        CoreUtils.grantPermission("com.bunchofstring.precisiontime.test.test","android.permission.CHANGE_WIFI_STATE");

        //Android 11
        CoreUtils.grantPermission("com.bunchofstring.precisiontime.test.test","android.permission.CHANGE_NETWORK_STATE");
        CoreUtils.grantPermission("com.bunchofstring.precisiontime.test.test","android.permission.ACCESS_FINE_LOCATION");
    }

    private void awaitConnection() throws Exception {
        LOGGER.log(Level.INFO, "awaitConnection()");
        final long start = new Date().getTime();
        int backoffWait = 100;
        while(new Date().getTime() - start < 10_000){
            LOGGER.log(Level.INFO, "awaitConnection() foobr");
            if(nsp.isWifiConnected()) {
                Thread.sleep(1000);
                return;
            }
            Thread.sleep(backoffWait);
            backoffWait *= 2;
        }
    }

    private void awaitDisconnection() throws Exception {
        LOGGER.log(Level.INFO, "awaitDisconnection()");
        final long start = new Date().getTime();
        int backoffWait = 100;
        while(new Date().getTime() - start < 10_000){
            LOGGER.log(Level.INFO, "awaitConnection() foobr");
            if(nsp.isWifiDisconnectedAndStable()) {
                Thread.sleep(1000);
                return;
            }
            Thread.sleep(backoffWait);
            backoffWait *= 2;
        }
    }
}
