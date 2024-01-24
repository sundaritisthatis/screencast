package com.dm.screencast;


import android.content.Context;
import android.view.View;

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class SocketHandler {
    private static Context context;
    private static Socket socket;
    private static DataOutputStream dos;
    private static String ip;
    private static String Conn = "";
    private static Thread t;
    private static MainActivity.MyThread2 myThread2;
    private static boolean hasThread = false;
    public static synchronized Context getContext() {
        return context;
    }
    public static synchronized Socket getSocket() {
        return socket;
    }
    public static synchronized DataOutputStream getDos() {
        return dos;
    }
    public static synchronized String getIP() {
        return ip;
    }
    public static synchronized String getConnStat() {
        return Conn;
    }
    public static synchronized boolean hasThreadSet() {return hasThread;}
    public static synchronized Thread getThread(){
        return t;
    }
    public static synchronized MainActivity.MyThread2 getMyThread2(){return myThread2;}

    public static synchronized void setMyThread2(MainActivity.MyThread2 myThread){SocketHandler.myThread2 = myThread;}
    public static synchronized void setContext(Context cn) {
        SocketHandler.context = cn;
    }
    public static synchronized void setConn(String ConnS){SocketHandler.Conn = ConnS;}
    public static synchronized void setData(Socket socket,DataOutputStream dos,String ip,String Connected) {
        SocketHandler.socket = socket;
        SocketHandler.dos = dos;
        SocketHandler.ip = ip;
        SocketHandler.Conn = Connected;
    }
}

