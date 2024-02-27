package com.example.clientapp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Client");

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
        if (!message.isEmpty()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        output.println(message);
                        output.flush();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showMessage("Client: " + message, blueColor);
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