package com.brili99.e_ktpvoiceintroducer;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

//import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {
    // Initializing all variables..
    private TextView startTV, stopTV, playTV, stopplayTV, statusTV, btnModeRegis;

    // creating a variable for medi recorder object class.
    private MediaRecorder mRecorder;

    // creating a variable for mediaplayer class
    private MediaPlayer mPlayer;

    // string variable is created for storing a file name
    private static String mFileName = null;

    // constant for storing audio permission
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    //Intialize attributes
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    TextView tag_data, web_response;
    RequestQueue queue;
    String androidId;
    MediaPlayer mediaPlayer;
    IsoDep isoDep;
    SweetAlertDialog pDialog;
    Drawable imgMode;
    File file;
    //    TextToSpeech tts;
//    SweetAlertDialog pDialog;
    final static String TAG = "nfc_test";
//    private String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

    private boolean modeRegis = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tag_data = (TextView) findViewById(R.id.tag_data);
        web_response = (TextView) findViewById(R.id.web_response);
        statusTV = (TextView) findViewById(R.id.idTVstatus);
        startTV = (TextView) findViewById(R.id.btnRecord);
        stopTV = (TextView) findViewById(R.id.btnStop);
        playTV = (TextView) findViewById(R.id.btnPlay);
        stopplayTV = (TextView) findViewById(R.id.btnStopPlay);
        btnModeRegis = (TextView) findViewById(R.id.btnModeRegis);

        mFileName = getFilesDir().getAbsolutePath();
        mFileName += "/AudioRecording.3gp";
        file = new File(mFileName);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            stopTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary, this.getTheme()));
            playTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary, this.getTheme()));
            stopplayTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary, this.getTheme()));
        } else {
            stopTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary));
            playTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary));
            stopplayTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary));
        }

        startTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start recording method will
                // start the recording of audio.
                startRecording();
            }
        });

        stopTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pause Recording method will
                // pause the recording of audio.
                pauseRecording();

            }
        });
        playTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // play audio method will play
                // the audio which we have recorded
                playAudio();
            }
        });
        stopplayTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pause play method will
                // pause the play of audio
                pausePlaying();
            }
        });

        btnModeRegis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchModeRegis();
            }
        });


        queue = Volley.newRequestQueue(this);
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            tag_data.setText("NO NFC Capabilities");
            Toast.makeText(this, "NO NFC Capabilities",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }
    }

    private void switchModeRegis() {
        if (modeRegis) {
            modeRegis = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                btnModeRegis.setBackgroundColor(getResources().getColor(R.color.bs_secondary, this.getTheme()));
            } else {
                btnModeRegis.setBackgroundColor(getResources().getColor(R.color.bs_secondary));
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                imgMode = this.getResources().getDrawable(R.drawable.ic_baseline_close_24, this.getTheme());
            } else {
                imgMode = this.getResources().getDrawable(R.drawable.ic_baseline_close_24);
            }
            btnModeRegis.setCompoundDrawablesWithIntrinsicBounds(imgMode, null, null, null);
        } else {
            if (file.exists()) {
                modeRegis = true;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    btnModeRegis.setBackgroundColor(getResources().getColor(R.color.bs_primary, this.getTheme()));
                } else {
                    btnModeRegis.setBackgroundColor(getResources().getColor(R.color.bs_primary));
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    imgMode = this.getResources().getDrawable(R.drawable.ic_baseline_check_24, this.getTheme());
                } else {
                    imgMode = this.getResources().getDrawable(R.drawable.ic_baseline_check_24);
                }
                btnModeRegis.setCompoundDrawablesWithIntrinsicBounds(imgMode, null, null, null);
            } else {
                Toast.makeText(this, "Isi rekaman terlebih dahulu", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startRecording() {
        // check permission method is used to check
        // that the user has granted permission
        // to record nd store the audio.
        if (CheckPermissions()) {

            // setbackgroundcolor method will change
            // the background color of text view.
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                stopTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
                startTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary, this.getTheme()));
                playTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary, this.getTheme()));
                stopplayTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary, this.getTheme()));
            } else {
                stopTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
                startTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary));
                playTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary));
                stopplayTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary));
            }

            // we are here initializing our filename variable
            // with the path of the recorded audio file.
            Log.e("mFileName", mFileName);

            // below method is used to initialize
            // the media recorder class
            mRecorder = new MediaRecorder();

            // below method is used to set the audio
            // source which we are using a mic.
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            // below method is used to set
            // the output format of the audio.
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            // below method is used to set the
            // audio encoder for our recorded audio.
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            // below method is used to set the
            // output file location for our recorded audio
            mRecorder.setOutputFile(mFileName);
            try {
                // below method will prepare
                // our audio recorder class
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("TAG", "prepare() failed");
            }
            // start method will start
            // the audio recording.
            mRecorder.start();
            statusTV.setText("Recording Started");
        } else {
            // if audio recording permissions are
            // not granted by user below method will
            // ask for runtime permission for mic and storage.
            RequestPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // this method is called when user will
        // grant the permission for audio recording.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToRead = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore && permissionToRead) {
//                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    } else {
//                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE
        }, REQUEST_AUDIO_PERMISSION_CODE);
    }


    public void playAudio() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            stopTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary, this.getTheme()));
            startTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
            playTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary, this.getTheme()));
            stopplayTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
        } else {
            stopTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary));
            startTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
            playTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary));
            stopplayTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
        }

        // for playing our recorded audio
        // we are using media player class.
        mPlayer = new MediaPlayer();
        try {
            // below method is used to set the
            // data source which will be our file name
            mPlayer.setDataSource(mFileName);

            // below method will prepare our media player
            mPlayer.prepare();

            // below method will start our media player.
            mPlayer.start();
            statusTV.setText("Recording Started Playing");
        } catch (IOException e) {
            Log.e("TAG", "prepare() failed");
        }
    }

    public void pauseRecording() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            stopTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary, this.getTheme()));
            startTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
            playTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
            stopplayTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
        } else {
            stopTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary));
            startTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
            playTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
            stopplayTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
        }

        // below method will stop
        // the audio recording.
        mRecorder.stop();

        // below method will release
        // the media recorder class.
        mRecorder.release();
        mRecorder = null;
        statusTV.setText("Recording Stopped");
    }

    public void pausePlaying() {
        // this method will release the media player
        // class and pause the playing of our recorded audio.
        mPlayer.release();
        mPlayer = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            stopTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary, this.getTheme()));
            startTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
            playTV.setBackgroundColor(getResources().getColor(R.color.purple_200, this.getTheme()));
            stopplayTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary, this.getTheme()));
        } else {
            stopTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary));
            startTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
            playTV.setBackgroundColor(getResources().getColor(R.color.purple_200));
            stopplayTV.setBackgroundColor(getResources().getColor(R.color.bs_secondary));
        }

        statusTV.setText("Recording Play Stopped");
    }


    @Override
    protected void onResume() {
        super.onResume();
        assert nfcAdapter != null;
//        web_response.setText("onResume");
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    protected void onPause() {
        super.onPause();
        //Onpause stop listening
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            assert tag != null;
            String data = detectTagData(tag).replace(" ", "");
            byte[] payload = data.getBytes();

//            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
//                Log.d("tagAction", "ACTION_TAG_DISCOVERED");
//            } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
//                Log.d("tagAction", "ACTION_TECH_DISCOVERED");
//            } else if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
//                Log.d("tagAction", "ACTION_NDEF_DISCOVERED");
//            }

//            tag_data.setText(data);
//            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
//            pDialog.setTitleText("Loading");
//            pDialog.setCancelable(false);
//            pDialog.show();

            // Cek apakah id nfc valid
            if (data.length() != 14) {
                pDialog.dismiss();
                pDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
                pDialog.setTitleText("E-KTP anda tidak valid")
                        .setContentText(data)
                        .show();
            } else {
                if (!modeRegis) {
                    playAudioWeb(data);
                    pDialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
                    pDialog.setTitleText("ID NFC E-KTP anda")
                            .setContentText(data)
                            .show();
                } else {
                    String bs64 = fileToBase64(mFileName);
                    String fileSize = getFileSize(mFileName);
                    if (bs64 != "") {
                        sendVoiceToServer(data, bs64, fileSize);
                        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                        pDialog.setTitleText("Loading");
                        pDialog.setCancelable(false);
                        pDialog.show();
                    } else {
                        pDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
                        pDialog.setTitleText("Error")
                                .setContentText("Ada masalah dalam konversi data")
                                .show();
                    }
                }
            }
//            Log.d("payload", toHex(payload));

//            sendToServer(data.replace("\n", "~"));
        }
    }

    private static String fileToBase64(String path) {
        String base64 = "";
        try {
            File file = new File(path);
            byte[] buffer = new byte[(int) file.length() + 100];
            @SuppressWarnings("resource")
            int length = new FileInputStream(file).read(buffer);
            base64 = Base64.encodeToString(buffer, 0, length,
                    Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return base64;
    }

    private String getFileSize(String path) {
        String size = "0";
        File file = new File(path);
        size = Long.toString(file.length());
        return size;
    }

//    private void sendToServer(String data) {
//        String url = "https://alkaira.com/ektp/device.php?id=" + androidId + "&data=" + data;
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        // Display the first 500 characters of the response string.
////                        web_response.setText("Web response: " + response);
////                        tts.speak(response, TextToSpeech.QUEUE_FLUSH, null);
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                web_response.setText("Text to speech not working in this device!");
//            }
//        });
//        queue.add(stringRequest);
//    }

    private void sendVoiceToServer(String id_nfc, String fileBase64, String fileSize) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = "https://alkaira.com/ektp/updateVoice.php";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("androidId", androidId);
            jsonBody.put("id_nfc", id_nfc);
            jsonBody.put("voice_size", fileSize);
            jsonBody.put("voice", fileBase64);
            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("VOLLEY", response);
//                    web_response.setText(response);
                    response = response.trim();
                    if (response.equals("200")) {
                        showSweetAlert("Sukses", "Berhasil memperbarui suara", SweetAlertDialog.SUCCESS_TYPE);
                        switchModeRegis();
                    } else if (response.equals("500")) {
                        showSweetAlert("Error", "Error pada server", SweetAlertDialog.ERROR_TYPE);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                    showSweetAlert("Error", error.toString(), SweetAlertDialog.ERROR_TYPE);
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
//                        showSweetAlert("Sukses",responseString, SweetAlertDialog.SUCCESS_TYPE);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            queue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showSweetAlert(String title, String desc, int icon) {
        pDialog.dismiss();
//        pDialog.dismissWithAnimation();
        pDialog = new SweetAlertDialog(this, icon);
        pDialog.setTitleText(title)
                .setContentText(desc)
                .show();
    }

    private void playAudioWeb(String data) {
        String audioUrl = "https://alkaira.com/ektp/device.php?id=" + androidId + "&data=" + data;

        // Stop from playing
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        // initializing media player
        mediaPlayer = new MediaPlayer();

        // below line is use to set the audio
        // stream type for our media player.
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // below line is use to set our
        // url to our media player.
        try {
            mediaPlayer.setDataSource(audioUrl);
            // below line is use to prepare
            // and start our media player.
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
        // below line is use to display a toast message.
//        Toast.makeText(this, "Audio started playing..", Toast.LENGTH_SHORT).show();
    }

    //For detection
    private String detectTagData(Tag tag) {
        StringBuilder sb = new StringBuilder();
        byte[] id = tag.getId();
//        sendToServer(toHex(id).replace(" ", ""));
//        playAudioWeb(toHex(id).replace(" ", ""));
//        sb.append("ID NFC E-KTP anda: ").append('\n').append(toHex(id).toUpperCase()).append('\n');
        sb.append(toHex(id).toUpperCase());
//        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
//                .setTitleText("ID NFC E-KTP anda")
//                .setContentText(toHex(id).toUpperCase())
//                .show();

//        pDialog.setTitleText("ID NFC E-KTP anda")
//                .setContentText(toHex(id).toUpperCase())
//                .show();
//        sb.append("ID (hex): ").append(toHex(id)).append('\n');
//        sb.append("ID (reversed hex): ").append(toReversedHex(id)).append('\n');
//        sb.append("ID (dec): ").append(toDec(id)).append('\n');
//        sb.append("ID (reversed dec): ").append(toReversedDec(id)).append('\n');
//
//        String prefix = "android.nfc.tech.";
//        sb.append("Technologies: ");
//        for (String tech : tag.getTechList()) {
//            sb.append(tech.substring(prefix.length()));
//            sb.append(", ");
//        }
//        sb.append('\n');

//        sb.delete(sb.length() - 2, sb.length());

//        for (String tech : tag.getTechList()) {
//            if (tech.equals(MifareClassic.class.getName())) {
//                sb.append('\n');
//                String type = "Unknown";
//
//                try {
//                    MifareClassic mifareTag = MifareClassic.get(tag);
//
//                    switch (mifareTag.getType()) {
//                        case MifareClassic.TYPE_CLASSIC:
//                            type = "Classic";
//                            break;
//                        case MifareClassic.TYPE_PLUS:
//                            type = "Plus";
//                            break;
//                        case MifareClassic.TYPE_PRO:
//                            type = "Pro";
//                            break;
//                    }
//                    sb.append("Mifare Classic type: ");
//                    sb.append(type);
//                    sb.append('\n');
//
//                    sb.append("Mifare size: ");
//                    sb.append(mifareTag.getSize() + " bytes");
//                    sb.append('\n');
//
//                    sb.append("Mifare sectors: ");
//                    sb.append(mifareTag.getSectorCount());
//                    sb.append('\n');
//
//                    sb.append("Mifare blocks: ");
//                    sb.append(mifareTag.getBlockCount());
//                } catch (Exception e) {
//                    sb.append("Mifare classic error: " + e.getMessage());
//                }
//            }
//
//            if (tech.equals(MifareUltralight.class.getName())) {
//                sb.append('\n');
//                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
//                String type = "Unknown";
//                switch (mifareUlTag.getType()) {
//                    case MifareUltralight.TYPE_ULTRALIGHT:
//                        type = "Ultralight";
//                        break;
//                    case MifareUltralight.TYPE_ULTRALIGHT_C:
//                        type = "Ultralight C";
//                        break;
//                }
//                sb.append("Mifare Ultralight type: ");
//                sb.append(type);
//            }
//        }

//        isoDep = IsoDep.get(tag);
//        if (isoDep != null) {
//            sb.append('\n').append("Iso Deep pass").append('\n');
//            try {
//                // Connect to the remote NFC device
//                isoDep.connect();
//                // Build SELECT AID command for our loyalty card service.
//                // This command tells the remote device which service we wish to communicate with.
////                Log.i(TAG, "Requesting remote AID: " + SAMPLE_LOYALTY_CARD_AID);
////                byte[] command = BuildSelectApdu("00A40000026FF2");
//                // Send command to remote device
////                Log.i(TAG, "Sending: " + ByteArrayToHexString(command));
////                byte[] command_get_img = {};
////                byte[] result1 = isoDep.transceive(BuildSelectApdu("00A40000027F0A"));
////                sb.append("ID (hex): ").append(toHex(result1)).append('\n');
////                byte[] result2 = isoDep.transceive(BuildSelectApdu("00A40000026FF2"));
////                sb.append("ID (hex): ").append(toHex(result2)).append('\n');
////                sb.append("ID (hex): ").append(toHex(isoDep.transceive(BuildSelectApdu("00A40000027F0A")))).append('\n');
////                sb.append("ID (hex): ").append(toHex(isoDep.transceive(BuildSelectApdu("00A40000026FF2")))).append('\n');
//                byte[] status_success = {
//                        (byte) 0x90,
//                        (byte) 0x00
//                };
//                byte[] command_get_img = {
//                        (byte) 0x00, // CLA = 00 (first interindustry command set)
//                        (byte) 0xA4, // INS = A4 (SELECT)
//                        (byte) 0x00, // P1  = 00 (select file by DF name)
//                        (byte) 0x00, // P2  = 00 (first or only file; no FCI)
//                        (byte) 0x02, // Lc  = 2  (data/AID has 2 bytes)
//                        (byte) 0x7F, (byte) 0x0A // AID 7F0A
//                };
//                byte[] getStatus = {
//                        (byte) 0x00, // CLA = 00 (first interindustry command set)
//                        (byte) 0xA4, // INS = A4 (SELECT)
//                        (byte) 0x00, // P1  = 00 (select file by DF name)
//                        (byte) 0x00, // P2  = 00 (first or only file; no FCI)
//                        (byte) 0x02, // Lc  = 6  (data/AID has 2 bytes)
//                        (byte) 0x6F, (byte) 0xF2 // AID = 6FF2
//                };
//
//                byte[] getImgSize = {
//                        (byte) 0x00,
//                        (byte) 0xB0,
//                        (byte) 0x00,
//                        (byte) 0x00,
//                        (byte) 0x02
//                };
////                sb.append("ID (hex): ").append(toHex(isoDep.transceive(getStatus))).append('\n');
////                sb.append("ID (hex): ").append(toHex(isoDep.transceive(command_get_img))).append('\n');
//
//                if (Arrays.equals(isoDep.transceive(getStatus), status_success)) {
//                    sb.append("Get status success").append('\n');
//                } else {
//                    sb.append("Get status fail").append('\n');
//                }
//                if (Arrays.equals(isoDep.transceive(command_get_img), status_success)) {
//                    sb.append("Get img success").append('\n');
//                    byte[] imgSize = isoDep.transceive(getImgSize);
//                    sb.append("getImgSize: ").append(toHex(imgSize)).append('\n');
//                    byte[] getImgCode = {
//                            (byte) 0x00,
//                            (byte) 0xB0,
//                            (byte) 0x00,
//                            (byte) 0x00,
//                            (byte) 0x10
//                    };
//                    byte[] getImg = isoDep.transceive(getImgCode);
//                    sb.append("Img: ").append(toHex(getImg)).append('\n');
//                } else {
//                    sb.append("Get img fail").append('\n');
//                }
//                // If AID is successfully selected, 0x9000 is returned as the status word (last 2
//                // bytes of the result) by convention. Everything before the status word is
//                // optional payload, which is used here to hold the account number.
////                int resultLength = result.length;
////                byte[] statusWord = {result[resultLength-2], result[resultLength-1]};
////                byte[] payload = Arrays.copyOf(result, resultLength-2);
////                if (Arrays.equals(SELECT_OK_SW, statusWord)) {
////                    // The remote NFC device will immediately respond with its stored account number
////                    String accountNumber = new String(payload, "UTF-8");
////                    Log.i(TAG, "Received: " + accountNumber);
////                    // Inform CardReaderFragment of received account number
////                    mAccountCallback.get().onAccountReceived(accountNumber);
////                }
//            } catch (IOException e) {
//                sb.append("Error communicating with card: " + e.toString()).append('\n');
////                Log.e(TAG, "Error communicating with card: " + e.toString());
//            }
//        }

        Log.v(TAG, sb.toString());
        return sb.toString();
    }

//    public static byte[] mergeArray(byte[] a, byte[] b) {
//        int a1 = a.length;
//        int b1 = b.length;
//        int c1 = a1 + b1;
//        byte[] c = new byte[c1];
//        System.arraycopy(a, 0, c, 0, a1);
//        System.arraycopy(b, 0, c, a1, b1);
//        return c;
//    }

//    // AID for our loyalty card service.
//    private static final String SAMPLE_LOYALTY_CARD_AID = "F222222222";
//    // ISO-DEP command HEADER for selecting an AID.
//    // Format: [Class | Instruction | Parameter 1 | Parameter 2]
//    private static final String SELECT_APDU_HEADER = "00A40000026FF2";
//    // "OK" status word sent in response to SELECT AID command (0x9000)
//    private static final byte[] SELECT_OK_SW = {(byte) 0x90, (byte) 0x00};
//
//    public static byte[] BuildSelectApdu(String aid) {
//        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
//        return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X", aid.length() / 2) + aid);
//    }

//    public static byte[] HexStringToByteArray(String s) {
//        int len = s.length();
//        byte[] data = new byte[len / 2];
//        for (int i = 0; i < len; i += 2) {
//            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//                    + Character.digit(s.charAt(i + 1), 16));
//        }
//        return data;
//    }

    //For reading and writing
//    private String detectTagData(Tag tag) {
//        StringBuilder sb = new StringBuilder();
//        byte[] id = tag.getId();
//        sb.append("NFC ID (dec): ").append(toDec(id)).append('\n');
//        for (String tech : tag.getTechList()) {
//            if (tech.equals(MifareUltralight.class.getName())) {
//                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
//                String payload;
//                payload = readTag(mifareUlTag);
//                sb.append("payload: ");
//                sb.append(payload);
//                writeTag(mifareUlTag);
//            }
//        }
//    Log.v("test",sb.toString());
//    return sb.toString();
//}
    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    private long toDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private long toReversedDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    //    public void writeTag(MifareUltralight mifareUlTag) {
//        try {
//            mifareUlTag.connect();
//            mifareUlTag.writePage(4, "get ".getBytes(Charset.forName("US-ASCII")));
//            mifareUlTag.writePage(5, "fast".getBytes(Charset.forName("US-ASCII")));
//            mifareUlTag.writePage(6, " NFC".getBytes(Charset.forName("US-ASCII")));
//            mifareUlTag.writePage(7, " now".getBytes(Charset.forName("US-ASCII")));
//        } catch (IOException e) {
//            Log.e(TAG, "IOException while writing MifareUltralight...", e);
//        } finally {
//            try {
//                mifareUlTag.close();
//            } catch (IOException e) {
//                Log.e(TAG, "IOException while closing MifareUltralight...", e);
//            }
//        }
//    }
    public String readTag(MifareUltralight mifareUlTag) {
        try {
            mifareUlTag.connect();
            byte[] payload = mifareUlTag.readPages(4);
            return new String(payload, Charset.forName("US-ASCII"));
        } catch (IOException e) {
            Log.e(TAG, "IOException while reading MifareUltralight message...", e);
        } finally {
            if (mifareUlTag != null) {
                try {
                    mifareUlTag.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing tag...", e);
                }
            }
        }
        return null;
    }


}