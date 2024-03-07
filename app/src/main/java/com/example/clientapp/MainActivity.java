package com.example.clientapp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    public static final int SERVER_PORT = 33768;
    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private Thread receiveThread;
    private LinearLayout msgList;
    private Handler handler;
    private int blueColor;
    private EditText edMessage;

    String modulus = "00:af:b7:c7:84:23:69:2f:7b:4b:47:fe:48:b7:54:\n" +
            "93:ac:30:27:05:81:ea:25:9d:b2:af:6c:bd:a2:2a:\n" +
            "4e:29:f8:40:30:e1:27:20:51:c5:fa:37:5a:3a:0a:\n" +
            "5a:aa:e0:45:35:c3:6d:25:19:d9:cd:ba:da:66:57:\n" +
            "5d:1d:b8:88:b7"; // Replace with your modulus

    String exponent = "10001"; // Exponent in hex

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Client");
        modulus = modulus.replace(":", "").replace("\n", "").replace(" ", "");


        blueColor = ContextCompat.getColor(this, R.color.blue);
        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);

        Button connectButton = findViewById(R.id.connect_server);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToServer();
            }
        });

        Button sendButton = findViewById(R.id.send_data);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void connectToServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Replace "SERVER_IP" with the actual IP address of the server
                    // and "SERVER_PORT" with the port number used by the server
                    socket = new Socket("localhost", SERVER_PORT);
                    output = new PrintWriter(socket.getOutputStream(), true);
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessage("Connected to Server!!", blueColor);
                        }
                    });

                    startReceivingMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                    showMessage("Error connecting to the server: " + e.getMessage(), Color.RED);
                }
            }
        }).start();
    }

    private void startReceivingMessages() {
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String message;
                    while ((message = input.readLine()) != null) {
                        final String finalMessage = message;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showMessage("Server: " + finalMessage, blueColor);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showMessage("Error receiving messages: " + e.getMessage(), Color.RED);
                }
            }
        });
        receiveThread.start();
    }

    private void sendMessage() {
        final String message = edMessage.getText().toString().trim();
        String encryptedText = null;
        try {
            RSAEncryptor encryptor = new RSAEncryptor(modulus, exponent);
             encryptedText = encryptor.encrypt(message);
          //  Log.d("Encrypted Text", encryptedText);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!message.isEmpty()) {
            String finalEncryptedText = encryptedText;
            Log.d("dec", encryptedText);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        output.println(finalEncryptedText);
                        output.flush();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showMessage("Client: " + finalEncryptedText, blueColor);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        showMessage("Error sending message: " + e.getMessage(), Color.RED);
                    }
                }
            }).start();
        }
    }

    private void showMessage(final String message, final int color) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView textView = new TextView(MainActivity.this);
                textView.setTextColor(color);
                textView.setText(message);
                textView.setTextSize(20);
                textView.setPadding(0, 5, 0, 0);
                msgList.addView(textView);
            }
        });
    }
    private TextView textView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        TextView tv = new TextView(this);
        tv.setTextColor(color);
        tv.setText(message);
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (socket != null) {
                output.println("Disconnect");
                output.flush();
                socket.close();
            }
            if (receiveThread != null) {
                receiveThread.interrupt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}