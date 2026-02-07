package com.bt.chat;
import android.app.Activity;
import android.bluetooth.*;
import android.os.*;
import android.widget.*;
import java.util.*;
import java.io.*;

public class MainActivity extends Activity {
    private final UUID UUID_CHAT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter adapter;
    private BluetoothSocket socket;
    private TextView log;
    private EditText input;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(40,40,40,40);
        log = new TextView(this);
        log.setText("V24 Shield Ready\n");
        l.addView(log);
        Button scn = new Button(this);
        scn.setText("SCAN & CONNECT");
        scn.setOnClickListener(v -> connect());
        l.addView(scn);
        input = new EditText(this);
        l.addView(input);
        Button s = new Button(this);
        s.setText("SEND");
        s.setOnClickListener(v -> sendMsg());
        l.addView(s);
        setContentView(l);
        adapter = BluetoothAdapter.getDefaultAdapter();
        if(Build.VERSION.SDK_INT >= 31) {
            requestPermissions(new String[]{"android.permission.BLUETOOTH_SCAN","android.permission.BLUETOOTH_CONNECT","android.permission.ACCESS_FINE_LOCATION"}, 1);
        }
        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                BluetoothServerSocket ss = adapter.listenUsingInsecureRfcommWithServiceRecord("Chat", UUID_CHAT);
                socket = ss.accept();
                manage();
            } catch (Exception e) {}
        }).start();
    }

    private void connect() {
        Set<BluetoothDevice> paired = adapter.getBondedDevices();
        for(BluetoothDevice d : paired) {
            new Thread(() -> {
                try {
                    socket = d.createInsecureRfcommSocketToServiceRecord(UUID_CHAT);
                    socket.connect();
                    manage();
                } catch (Exception e) {}
            }).start();
        }
    }

    private void manage() {
        runOnUiThread(() -> log.append("CONNECTÉ !\n"));
        try {
            InputStream is = socket.getInputStream();
            byte[] buf = new byte[1024];
            while(true) {
                int len = is.read(buf);
                String m = new String(buf, 0, len);
                runOnUiThread(() -> log.append("Carole: " + m + "\n"));
            }
        } catch (Exception e) {}
    }

    private void sendMsg() {
        try {
            String m = input.getText().toString();
            socket.getOutputStream().write(m.getBytes());
            log.append("Moi: " + m + "\n");
            input.setText("");
        } catch (Exception e) {}
    }
}