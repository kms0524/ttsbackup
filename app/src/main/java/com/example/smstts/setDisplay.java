package com.example.smstts;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

public class setDisplay extends AppCompatActivity {

    Button changeTTS;
    private ImageButton back2main;
    private Button helpbutton;
    private Button notificationButton;
    private Button permissionButton;

    private AlertDialog enableNotificationListenerAlertDialog;

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listener";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    boolean isListenerAccepted = false;


    private boolean isNotificationServiceEnabled() {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setdisplay);

        changeTTS = (Button) findViewById(R.id.changeTTS);
        changeTTS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClassName("com.android.settings", "com.android.settings.Settings$TextToSpeechSettingsActivity");
                startActivity(intent);
            }
        });

        back2main = (ImageButton) findViewById(R.id.back2main);
        back2main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        helpbutton = (Button) findViewById(R.id.help);
        helpbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), helpActivity.class);
                startActivity(intent);
            }
        });

        notificationButton = (Button) findViewById(R.id.notification);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(setDisplay.this);
                alertDialogBuilder.setTitle("알림 액세스 허가");
                alertDialogBuilder.setMessage("어플리케이션의 기능을 정상적으로 작동하기 위해선, 알림 액세스를 설정해야 합니다.\n설정하시겠습니까?");
                alertDialogBuilder.setPositiveButton("네",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (isListenerAccepted == false) {
                                    startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                                    isListenerAccepted = true;
                                } else {
                                    Toast.makeText(setDisplay.this, "알림 액세스가 이미 설정되어 있습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                alertDialogBuilder.setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isListenerAccepted = false;
                                Toast.makeText(setDisplay.this, "알림 액세스가 허락되지 않았습니다. 카카오톡 기능을 정상적으로\n" +
                                        "이용할 수 없습니다. 기능을 정상적으로 사용하기 위해선 알림 액세스를 설정해 주시기 바랍니다", Toast.LENGTH_SHORT).show();
                            }
                        });
                alertDialogBuilder.show();
            }

        });



        permissionButton = (Button) findViewById(R.id.permission_button);
        permissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionListener permissionListener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        Toast.makeText(setDisplay.this, "권한이 설정되어있습니다.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Toast.makeText(setDisplay.this, "권한이 설정되지 않았습니다", Toast.LENGTH_SHORT).show();
                    }
                };

                TedPermission.with(setDisplay.this)
                        .setRationaleMessage("어플리케이션을 정상적으로 실행하기 위해선, 주소록과 문자 메세지에 대한 접근 권한이 필요합니다.")
                        .setRationaleConfirmText("확인")
                        .setDeniedCloseButtonText("거부")
                        .setDeniedMessage("어플리케이션이 정상적으로 작동하지 않습니다. 정상적으로 작동시키기 위해선, 권한을 설정해 주시길 바랍니다")
                        .setPermissionListener(permissionListener)
                        .setPermissions(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)
                        .check();
            }
        });
    }

}