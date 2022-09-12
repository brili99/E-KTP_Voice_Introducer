package com.brili99.e_ktpvoiceintroducer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

//import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {
    //Intialize attributes
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    TextView tag_data, web_response;
    RequestQueue queue;
    String androidId;
    MediaPlayer mediaPlayer;
    IsoDep isoDep;
    SweetAlertDialog pDialog;
    //    TextToSpeech tts;
//    SweetAlertDialog pDialog;
    final static String TAG = "nfc_test";
//    private String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tag_data = (TextView) findViewById(R.id.tag_data);
        web_response = (TextView) findViewById(R.id.web_response);
        queue = Volley.newRequestQueue(this);
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//        pDialog = new SweetAlertDialog(this);
        //Initialise NfcAdapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //If no NfcAdapter, display that the device has no NFC
        if (nfcAdapter == null) {
            tag_data.setText("NO NFC Capabilities");
            Toast.makeText(this, "NO NFC Capabilities",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
//        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
//        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE);
//        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);

        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }
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
            String data = detectTagData(tag);
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
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
            pDialog.setTitleText("ID NFC E-KTP anda")
                    .setContentText(data)
                    .show();
//            Log.d("payload", toHex(payload));

//            sendToServer(data.replace("\n", "~"));
        }
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

    private void playAudio(String data) {
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
        playAudio(toHex(id).replace(" ", ""));
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