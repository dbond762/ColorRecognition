package com.example.dima.colorrecognition;

import android.bluetooth.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private final String TAG = "MyApp";

    Button recognizeCommand;
    Button connect;

    public static DB db;

    final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_DEVICE_ADDRESS = 2;

    BluetoothAdapter bluetooth;

    ConnectThread connectThread;
    ConnectedThread connectedThread;

    private TextToSpeech tts;

    final int VOICE_RECOGNITION_RESULT = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recognizeCommand = findViewById(R.id.recognizeCommand);
        connect = findViewById(R.id.connect);

        recognizeCommand.setOnClickListener(onRecognizeCommand);
        connect.setOnClickListener(onConnect);

        tts = new TextToSpeech(this, this);

        bluetooth = BluetoothAdapter.getDefaultAdapter();
        if (bluetooth == null) {
            Toast.makeText(this, "Ваше устройство не поддерживает bluetooth", Toast.LENGTH_LONG).show();
            finish();
        }

        checkVoiceRecognition();

        db = new DB(this);
        db.open();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!bluetooth.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onDestroy() {
        if (connectThread != null) {
            connectThread.cancel();
        }
        db.close();

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }

    void checkVoiceRecognition() {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0) {
            recognizeCommand.setEnabled(false);
            Toast.makeText(this, "Распознование речи не поддерживается", Toast.LENGTH_LONG).show();
        }
    }

    View.OnClickListener onRecognizeCommand = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            startActivityForResult(intent, VOICE_RECOGNITION_RESULT);
        }
    };

    View.OnClickListener onConnect = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (connectThread != null) {
                connectThread.cancel();
            }

            Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();
            ArrayList<String> names = new ArrayList<>();
            ArrayList<String> addresses = new ArrayList<>();
            for (BluetoothDevice device: pairedDevices) {
                names.add(device.getName());
                addresses.add(device.getAddress());
            }

            Intent intent = new Intent(MainActivity.this, DevicesActivity.class);
            intent.putExtra("names", names);
            intent.putExtra("addresses", addresses);
            startActivityForResult(intent, REQUEST_DEVICE_ADDRESS);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, "Bluetooth не включён", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case REQUEST_DEVICE_ADDRESS:
                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        return;
                    }

                    String address = data.getStringExtra("address");

                    BluetoothDevice device = bluetooth.getRemoteDevice(address);

                    Toast.makeText(this, "Выбрано усройство " + device.getName(), Toast.LENGTH_SHORT).show();

                    connectThread = new ConnectThread(device);
                    connectThread.start();
                }
                break;
            case VOICE_RECOGNITION_RESULT:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> textMatchlist = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.d(TAG, textMatchlist.toString());

                    ArrayList<String> colors = new ArrayList<>();

                    for (int i = 0; i < textMatchlist.size(); i++) {
                        String str = textMatchlist.get(i).toLowerCase();

                        if (str.startsWith("включить")) {
                            colors.add(str.replaceFirst("^включить ", ""));
                        } else if (str.startsWith("выключить")) {
                            if (connectedThread != null) {
                                connectedThread.write("#R0G0B0:".getBytes());
                            }
                            break;
                        } else if (str.startsWith("трек")) {
                            String[] words = str.split(" ");
                            String number;
                            if (words.length >= 2) {
                                number = words[1];
                            } else {
                                Toast.makeText(this, "Неправильная команда", Toast.LENGTH_SHORT).show();
                                continue;
                            }
                            int num = -1;
                            switch (number) {
                                case "0":
                                case "ноль":
                                case "нулевой":
                                    num = 0;
                                    break;
                                case "1":
                                case "один":
                                case "первый":
                                    num = 1;
                                    break;
                                case "2":
                                case "два":
                                case "второй":
                                    num = 2;
                                    break;
                                case "3":
                                case "три":
                                case "третий":
                                    num = 3;
                                    break;
                                case "4":
                                case "четыре":
                                case "четвертый":
                                    num = 4;
                                    break;
                                case "5":
                                case "пять":
                                case "пятый":
                                    num = 5;
                                    break;
                            }
                            if (num == -1) {
                                Toast.makeText(this, "Неправильная команда", Toast.LENGTH_SHORT).show();
                                continue;
                            }
                            if (connectedThread != null) {
                                connectedThread.write(String.format("$%d", num).getBytes());
                            }
                            break;
                        }
                    }

                    if (colors.size() > 0) {
                        Toast.makeText(this, "По вашему запросу было найденно\n" + colors.toString(), Toast.LENGTH_LONG).show();
                        Cursor cursor = db.getColorByNames(colors);

                        if (cursor == null) {
                            Toast.makeText(this, "Ничего не найденно", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "cursor == null");
                            return;
                        }
                        int colorVal;
                        if (cursor.moveToFirst()) {
                            colorVal = cursor.getInt(cursor.getColumnIndex(DB.COLOR_VALUE));
                        } else {
                            Toast.makeText(this, "Ничего не найденно", Toast.LENGTH_SHORT).show();
                            cursor.close();
                            return;
                        }
                        cursor.close();
                        Log.d(TAG, Integer.toHexString(colorVal));

                        int red = Color.red(colorVal);
                        int green = Color.green(colorVal);
                        int blue = Color.blue(colorVal);

                        String msg = String.format("#R%dG%dB%d:", red, green, blue);
                        Toast.makeText(this, "Будет установлено " + msg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, msg);

                        if (connectedThread == null) {
                            Log.d(TAG, "connectedThread == null");
                        }

                        if (connectedThread != null) {
                            connectedThread.write(msg.getBytes());
                        }
                    } else {
                        Toast.makeText(this, "Неправильная команда", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Voice recognition error");
                }
                break;
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale locale = new Locale("ru");
            tts.setLanguage(locale);
        } else {
            Log.d(TAG, "Ошибка в инициализации синтезатора речи.");
            Toast.makeText(this, "Синтезатор речи не работает", Toast.LENGTH_LONG).show();
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket connSocket;

        ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmpSock = null;

            try {
                tmpSock = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.d(TAG, "device.createRfcommSocketToServiceRecord(MY_UUID)");
                e.printStackTrace();
            }

            connSocket = tmpSock;
        }

        @Override
        public void run() {
            bluetooth.cancelDiscovery();

            try {
                connSocket.connect();
            } catch (IOException connExp) {
                Log.d(TAG, "connSocket.connect();");
                connExp.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Нет подключения. Проверьте Bluetooth-устройство, с которым хотите соединиться!", Toast.LENGTH_LONG).show();
                    }
                });


                try {
                    connSocket.close();
                } catch (IOException closeExp) {
                    Log.d(TAG, "connSocket.close();");
                    closeExp.printStackTrace();
                }

                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recognizeCommand.setEnabled(true);
                }
            });


            connectedThread = new ConnectedThread(connSocket);
            connectedThread.start();
        }

        void cancel() {
            try {
                connSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream inStream;
        private final OutputStream outStream;

        ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        @Override
        public void run() {
            final byte[] buffer = new byte[1024];

            while (true) {
                try {
                    int bytes = inStream.read(buffer);
                    String msg = new String(buffer).trim();

                    if (msg.equals("M")) {
                        tts.speak("Есть", TextToSpeech.QUEUE_FLUSH, null);
                    } else if (msg.equals("m")) {
                        tts.speak("Нет", TextToSpeech.QUEUE_FLUSH, null);
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        void write(byte[] bytes) {
            try {
                outStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
