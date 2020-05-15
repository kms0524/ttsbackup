package com.example.smstts;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.core.content.ContextCompat;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;

import android.util.Log;


public class MainActivity extends Activity {

    //test
    private static final String TAG = "MainActivity";
    private FirebaseJobDispatcher jobDispatcher;

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listener";

    private final int CHECK_CODE = 0x1;
    private final int LONG_DURATION = 5000;
    private final int SHORT_DURATION = 1200;

    private Speaker speaker;

    private ToggleButton toggle;
    private CompoundButton.OnCheckedChangeListener toggleListener;
    private TextView smsText;
    private TextView smsSender;
    private ImageButton setButton;

    private BroadcastReceiver smsReceiver;
    private BroadcastReceiver kakaoReceiver;
    public static final String CHANNEL_ID = "exampleServiceChannel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toggle = (ToggleButton) findViewById(R.id.speechToggle);
        smsText = (TextView) findViewById(R.id.sms_text);
        smsSender = (TextView) findViewById(R.id.sms_sender);
        setButton = (ImageButton) findViewById(R.id.setActivity);

        /*if (!isNotificationServicceEnabled()) {

            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        } else if (isNotificationServicceEnabled()) {
            enableNotificationListenerAlertDialog.dismiss();
        }*/


        smsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.samsung.android.messaging", "com.samsung.android.messaging.ui.view.main.WithActivity");
                startActivity(intent);
            }
        });
        setButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), setDisplay.class);
                startActivity(intent);

            }
        });

        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_SMS) +
                        ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.READ_CONTACTS) +
                        ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
                    toggle.setTextOn("");
                    toggle.getTextOn();
                    Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).cancel();
                } else if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_SMS) +
                        ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.READ_CONTACTS) +
                        ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                    toggle.setChecked(false);
                    Toast.makeText(MainActivity.this, "실행이 정상적으로 작동하지 않고 있습니다. 환경설정 에서 권한을 설정 해 주십시오", Toast.LENGTH_SHORT).show();
                }


            }


        });


        toggleListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_SMS) +
                            ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.READ_CONTACTS) +
                            ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                        speaker.allow(false);
                        speaker.speak("");
                    } else if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_SMS) +
                            ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.READ_CONTACTS) +
                            ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
                        speaker.allow(true);
                        speaker.speak("읽기를 시작합니다");
                    }
                } else {
                    speaker.speak("읽기를 중단합니다");
                    speaker.allow(false);
                }
            }
        };

        toggle.setOnCheckedChangeListener(toggleListener);

        checkTTS();
        initializeSMSReceiver();
        registerSmsReceiver();
        initializeKakaoReceiver();
        registerKakaoReceiver();
        createNotificationChannel();

        jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID, "서비스 채널 예", NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsReceiver);
        speaker.destroy();
    }


    private void checkTTS() {
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                speaker = new Speaker(this);
            } else {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);

            }
        }
    }

    private boolean isNotificationServicceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void initializeKakaoReceiver() {
        kakaoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(("android.service.notification.NotificationListenerService"))) {
                    //아래 내용은 카카오 SDK 샘플용 테스트 코드입니다.
                    /*String TestSampleTitle = intent.getExtras().getString("sampleTitle");
                    CharSequence TestSampleBody = intent.getExtras().getCharSequence("sampleBody");
                    String TtestSampleBody = TestSampleBody.toString();
                    if (TestSampleBody != null || TestSampleBody != null) {
                    speaker.pause(LONG_DURATION);
                    speaker.speak("새카톡이" + TestSampleTitle + " 님 으로부터 도착하였습니다");
                    speaker.pause(SHORT_DURATION);
                    speaker.speak(TtestSampleBody);
                    smsSender.setText(TestSampleTitle + " 님 으로 부터 \n카톡이 도착하였습니다");
                    smsText.setText(TtestSampleBody);*/

                    String kTitle = intent.getExtras().getString("realTitle");
                    CharSequence tempKBody = intent.getExtras().getCharSequence("realBody");
                    Log.i("NotificationListener", "샘플 푸시 내용 : " + kTitle);
                    Log.i("NotificationListener", "샘플 푸시 내용 : " + tempKBody);
                    String KBody = tempKBody.toString();

                    if (kTitle != null || KBody != null) {


                        speaker.pause(LONG_DURATION);
                        speaker.speak("새카톡이" + kTitle + " 님 으로부터 도착하였습니다");
                        speaker.pause(SHORT_DURATION);
                        speaker.speak(KBody);
                        smsSender.setText(kTitle + " 님 으로 부터 \n카톡이 도착하였습니다");
                        smsText.setText(KBody);
                    }
                }


            }
        };
    }

    private void initializeSMSReceiver() {

        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {

                    Object[] pdus = (Object[]) bundle.get("pdus");
                    for (int i = 0; i < pdus.length; i++) {
                        byte[] pdu = (byte[]) pdus[i];
                        SmsMessage message = SmsMessage.createFromPdu(pdu);
                        String smsTextBody = message.getDisplayMessageBody();
                        String smsSenderWho = getContactName(message.getOriginatingAddress());

                        speaker.pause(LONG_DURATION);
                        speaker.speak(" 새메세지가" + smsSenderWho + " 님 으로부터 도착하였습니다");
                        speaker.pause(SHORT_DURATION);
                        speaker.speak(smsTextBody);
                        smsSender.setText(smsSenderWho + " 님 으로부터 \n메세지가 도착하였습니다");
                        smsText.setText(smsTextBody);

                    }


                }
            }
        };
    }

    private String getContactName(String phone) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phone));
        String projection[] = new String[]{ContactsContract.Data.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        } else {
            return "알수없는 번호";
        }

    }


    private void registerSmsReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);
    }

    private void registerKakaoReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.service.notification.NotificationListenerService");
        registerReceiver(kakaoReceiver, intentFilter);
    }


}
