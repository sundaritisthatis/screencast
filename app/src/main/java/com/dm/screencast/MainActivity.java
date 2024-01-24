package com.dm.screencast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {

    wifiMan wifiM;
    boolean[] intArr;
    MyThread2 myThread2;
    EditText edt;
    TextView tx;
    String ip,in;
    Intent it;
    String piID;
    String piPass;
    int port;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context cn = getApplicationContext();
        wifiM = new wifiMan(cn);
        SocketHandler.setContext(cn);
        intArr = new boolean[2];
        myThread2 = new MyThread2();

        piID = "pi";
        piPass = "raspberry";
        port = 22;
        it = new Intent(MainActivity.this, widgetServ.class);
        SocketHandler.setMyThread2(myThread2);

        setContentView(R.layout.activity_main);
        getPerm();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showArpLIst();
        }
        Intent serviceIntent = new Intent(MainActivity.this, widgetServ.class);
        startService(serviceIntent);
        edt = (EditText) findViewById(R.id.edt1);
        tx = findViewById(R.id.sshOUT);
        myThread2.outText = tx;
        Thread t = new Thread(myThread2);
        tx.setMovementMethod(new ScrollingMovementMethod());
        edt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                in = edt.getText().toString();
                if(myThread2.updateCommand){
                    myThread2.sshCommandDef = in;
                }
                else {
                    tx.setVisibility(View.VISIBLE);
                    if (myThread2.connected) {
                        if (in.equals("Close SSH")) {
                            myThread2.stopThread = true;
                        } else {
                            myThread2.sshCommand = in;
                            myThread2.runSSHCommand = true;
                        }
                    } else {
                        Log.i("SSH", "Running New");
                        ip = in;
                        Toast.makeText(getApplicationContext(), in, Toast.LENGTH_SHORT).show();
                        t.start();
                    }
                }
                return false;
            }
        });


    }

    public void getPerm(){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)){
            Intent it2 = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION");
            it2.setData(Uri.parse("package:" + getPackageName()));
//            it2.setFlags(268435456);
            openSomeActivityForResult(it2);
            intArr[1] = true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showArpLIst(){
        if (!(wifiM.isWifi())){
            showDia("ENABLE WIFI ADAPTER", "Please turn on wifi from quick settings of your phone");
//            Toast.makeText(getApplicationContext(),"Code:1233211432" ,Toast.LENGTH_SHORT).show();
        }
    }
    public void showDia(String Title, String msg){
        final Dialog dialog  = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
//        dialog.setContentView(R.layout.dialog_alert);
        dialog.setContentView(R.layout.activity_test);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView text1 = (TextView) dialog.findViewById(R.id.textViewTitle);
        text1.setText(Title);
        TextView text2 = (TextView) dialog.findViewById(R.id.textViewMessage);
        text2.setText(msg);

        Button dialogBtn_cancel = (Button) dialog.findViewById(R.id.buttonNegative);
        dialogBtn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                System.exit(0);
//                Toast.makeText(getApplicationContext(), "Cancel" ,Toast.LENGTH_SHORT).show();

//                dialog.dismiss();
            }
        });

        Button dialogBtn_okay = (Button) dialog.findViewById(R.id.buttonPositive);
        dialogBtn_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent it1 = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                intArr[0] = true;
                openSomeActivityForResult(it1);
                dialog.cancel();
            }

        });

        dialog.show();
    }
    public void openSomeActivityForResult(Intent intF) {
        someActivityResultLauncher.launch(intF);
    }
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public void onActivityResult(ActivityResult result) {
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        // There are no request codes
//                        Intent data = result.getData();
//
//                    }
                    if (intArr[0]){
                        showArpLIst();
                        intArr[0] = false;
                    }
                    if (intArr[1]){
                        getPerm();
                        intArr[1] = false;
                    }
//                    Toast.makeText(getApplicationContext(),"Ok Activity is now closed" ,Toast.LENGTH_SHORT).show();
                }
            }
    );

    public class MyThread2 implements Runnable  {
        Session session;
        ChannelExec channel;
        String sshCommand = "";
        String sshCommandDef = "";
        boolean runSSHCommand = false;
        boolean breakLoop = false;
        boolean connected = false;
        boolean stopThread = false;
        TextView outText;
        boolean updateCommand = false;

        @Override
        public void run() {
            while (!breakLoop){

                if( session==null || !session.isConnected()){
                    Log.i("SSH","Creating Session" );
                    connect(ip,piID,piPass);

                    Log.i("SSH","Connected" );
                    connected = true;
                    // show success in UI with a snackbar alternatively use a toast
                    Snackbar.make(findViewById(android.R.id.content),
                                    "Success!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                if (stopThread){
                    stopThread = false;
                    connected = false;
                    breakLoop = true;
                    close();
                }
                if(runSSHCommand){
                    runSSHCommand = false;
                    try {
                        changeText("SSH: "+sshCommand,true);
                        String outpt = runCommand(sshCommand);
                        changeText(outpt,true);
                        Log.i("SSH",outpt );
                    }
                    catch (Exception e){
                        changeText("Error: "+e.toString(),true);
                        connected = false;
                        e.printStackTrace();
                    }
                }
            }
        }

//        private static Session getSession(){
//            if(session == null || !session.isConnected()){
//                session = connect(hostname,username,password);
//            }
//            return session;
//        }
//
//        private static Channel getChannel(){
//            if(channel == null || !channel.isConnected()){
//                try{
//                    channel = (ChannelShell)getSession().openChannel("shell");
//                    channel.connect();
//
//                }catch(Exception e){
//                    System.out.println("Error while opening channel: "+ e);
//                }
//            }
//            return channel;
//        }

        private void connect(String hostname, String username, String password){

            JSch jSch = new JSch();

            try {

                session = jSch.getSession(username, hostname, 22);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);//session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword(password);
                session.setTimeout(10000);
                String out1 = "Connecting SSH to " + hostname + " - Please wait for few seconds... ";
                changeText(out1,true);
                Log.i("SSH",out1 );

                session.connect();
                changeText("Connected!",true);
                Log.i("SSH","Connected" );
            }catch(Exception e){
                String outT = "An error occurred while connecting to "+hostname+": "+e;
                changeText(outT,true);
                connected = false;
                Log.i("SSH",outT);
            }

        }

        private String readChannelOutput(){
            Log.i("SSH","Reading output");
            byte[] buffer = new byte[1024];
            String line = "";
            try{
                InputStream in = channel.getInputStream();

                while (true){
                    Log.i("SSH","Getting in");
                    while (in.available() > 0) {
                        int i = in.read(buffer, 0, 1024);
                        Log.i("SSH", String.valueOf(i));
                        if (i < 0) {
                            break;
                        }
                        line = new String(buffer, 0, i);
                        Log.i("SSH1",line);
                        System.out.println(line);
                    }
                    Log.i("SSH","Getting in0");
                    if(line.contains("logout")){
                        Log.i("SSH","Getting in1");
                        break;
                    }
                    Log.i("SSH","Getting in2");
                    if (channel.isClosed()){
                        Log.i("SSH","Channel closed");
                        break;
                    }
                    Log.i("SSH","Getting in3");
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ee){
                        Log.i("SSH","Error"+ ee.toString());
                    }
                    Log.i("SSH","Getting in4");
                }

            }catch(Exception e){
                line = "Error in android code :"+ e.toString();
                Log.i("SSH","Error while reading channel output: "+ e);
                System.out.println("Error while reading channel output: "+ e);
            }
            return line;

        }

        public String runCommand(String command) throws JSchException, IOException {

            String ret = "";
            System.out.println("SSH out: "+command);

            if (!session.isConnected()){
                throw new RuntimeException("Not connected to an open session.  Call open() first!");}
            channel = (ChannelExec) session.openChannel("exec");

            channel.setCommand(command);
            channel.setInputStream(null);

//            PrintStream out = new PrintStream(channel.getOutputStream());
////            InputStream in = channel.getInputStream(); // channel.getInputStream();

            channel.connect();
//
//            // you can also send input to your running process like so:
//            // String someInputToProcess = "something";
//            // out.println(someInputToProcess);
//            // out.flush();
            ret = readChannelOutput();
            channel.setOutputStream(System.out);
            channel.disconnect();

            System.out.println("Finished sending commands!");

            return ret;
        }


        private String getChannelOutput(InputStream in) throws IOException{

            byte[] buffer = new byte[1024];
            StringBuilder strBuilder = new StringBuilder();

            String line = "";
            while (true){
                Log.i("SSH","Getting in");
                while (in.available() > 0) {
                    int i = in.read(buffer, 0, 1024);
                    if (i < 0) {
                        Log.i("SSH","i less then 0");
                        break;
                    }
                    strBuilder.append(new String(buffer, 0, i));
                    line = strBuilder.toString();
                    System.out.println(line);
                    Log.i("SSH", line);
                }
                Log.i("SSH","Getting in0");
                if(line.contains("logout")){
                    Log.i("SSH","Getting in1");
                    break;
                }
                Log.i("SSH","Getting in2");
                if (channel.isClosed()){
                    Log.i("SSH","Channel closed");
                    break;
                }
                Log.i("SSH","Getting in3");
                try {
                    Thread.sleep(1000);
                } catch (Exception ee){
                    Log.i("SSH","Error"+ ee.toString());
                }
                Log.i("SSH","Getting in4");
            }

            return strBuilder.toString();
        }
        private void executeCommands(String commands){

            try{

                System.out.println("Sending commands...");
                sendCommands( commands);

                readChannelOutput();
                System.out.println("Finished sending commands!");

            }catch(Exception e){
                System.out.println("An error ocurred during executeCommands: "+e);
            }
        }

        private void sendCommands(String command){

            try{
                PrintStream out = new PrintStream(channel.getOutputStream());

                out.println("#!/bin/bash");

                out.println(command);
                out.println("exit");

                out.flush();
            }catch(Exception e){
                System.out.println("Error while sending commands: "+ e);
            }

        }
        public void close(){
            channel.disconnect();
            session.disconnect();
            System.out.println("Disconnected channel and session");
        }
        public void changeText(String text, boolean appendT){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(appendT){
                        outText.append(text+'\n');
                    }else{
                        outText.setText(text+'\n');
                    }
                }
            });
        }


//        public static void main(String[] args){
//            List<String> commands = new ArrayList<String>();
//            commands.add("ls -l");
//
//            executeCommands(commands);
//            close();
//        }

    }
}