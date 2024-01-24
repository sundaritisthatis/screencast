package com.dm.screencast;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

class pData {

    String host = "";
    String ip = "";
    int dns = Integer.MAX_VALUE;
    int cnt = Integer.MAX_VALUE;

    String net = "NO_CONNECTION";
    String connState = "Not Connected";

    // various methods like getId, getTicketType, setId, setTicketType
    // would follow from here.

}

public class wifiMan implements Serializable {


    Context tcn;
    ConnectivityManager connManager;;
    NetworkInfo wifi;
    String IP ;
    ArrayList GatwayList=new ArrayList();
    public wifiMan(Context cn){
        tcn = cn;
        connManager = (ConnectivityManager) cn.getSystemService(cn.CONNECTIVITY_SERVICE);


    }



    public pData ping(byte[] IP,int port) {
        pData Pdata = new pData();

        if (isWifi()) {
            Pdata.net = getNetworkType();
            try {

                long start = System.currentTimeMillis();

                String ip = "";//ipstr(IP);
                Log.i("ErrorHand",ip);
//                Toast.makeText(tcn, ip,Toast.LENGTH_SHORT).show();
                InetAddress addr = InetAddress.getByName(ip);

            } catch (Exception ex) {
                Log.i("ErrorHand","In error"+Pdata.ip);
                if (Pdata.host == ""){
                    Pdata.host = "host Error";

                }else if(Pdata.ip == ""){
                    Pdata.ip = "ip Error";
                }

            }
        }
        return Pdata;
    }


    @Nullable
    public String getNetworkType() {

        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            return activeNetwork.getTypeName();
        }
        return null;
    }

    public boolean isWifi() {
        wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiManager wifiMgr = (WifiManager) tcn.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) {
            if (wifi.isConnected()){
                IP = Formatter.formatIpAddress(wifiMgr.getConnectionInfo().getIpAddress());
            }
            return wifi.isConnected();
        }
        else {
            return false;
        }
    }

    byte[] ipStr(Object preIPobj){
        String preIP = (String) preIPobj;
        preIP = preIP.substring(1, preIP.length());
//        Toast.makeText(tcn,preIP,Toast.LENGTH_SHORT).show();
        try{
            InetAddress addr = InetAddress.getByName(preIP);
            return addr.getAddress();
        }
        catch (Exception e){

//            Toast.makeText(tcn,preIP,Toast.LENGTH_SHORT).show();
            return "Unknown Host".getBytes();
        }

//        int index = preIP.lastIndexOf(".");
//        Toast.makeText(tcn,String.valueOf(index) ,Toast.LENGTH_SHORT).show();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public byte[] defGatway(){
        Log.i("Gayway","Finding ");
        Network network = connManager.getActiveNetwork();
        LinkProperties linkProperties = connManager.getLinkProperties(network);
//        int jk = 0;
        for (RouteInfo routeInfo: linkProperties.getRoutes()) {
//            jk++;
//            GatwayList.add(String.valueOf(routeInfo.getGateway()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (routeInfo.isDefaultRoute() && routeInfo.hasGateway()) {
                    GatwayList.add(String.valueOf(routeInfo.getGateway()));
                }
            }
        }
        if (GatwayList.size()== 0){
            Toast.makeText(tcn,"GatWay not Found" ,Toast.LENGTH_SHORT).show();
            return "Zero".getBytes();
        }
        if (GatwayList.size()== 1){
            String a = (String) GatwayList.get(0);
            Log.i("Gayway",a);
            return ipStr(GatwayList.get(0));
        }
        Toast.makeText(tcn,"Multiple GatWay Found" ,Toast.LENGTH_SHORT).show();
        return "Many".getBytes();
    }
}

