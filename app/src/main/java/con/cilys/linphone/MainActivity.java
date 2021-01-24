package con.cilys.linphone;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;


import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;

import java.util.ArrayList;

import con.cilys.linphone.base.BaseAc;
import con.cilys.linphone.utils.L;

public class MainActivity extends BaseAc {
    private ImageView mLed;
    private CoreListenerStub mCoreListener;

    private EditText mSipAddressToCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mLed = (ImageView) findViewById(R.id.led);

        // Monitors the registration state of our account(s) and update the LED accordingly
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String message) {
                updateLed(state);
            }
        };

        mSipAddressToCall = (EditText) findViewById(R.id.address_to_call);
        mSipAddressToCall.setText("8002");

        Button callButton = (Button) findViewById(R.id.call_button);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Core core = LinphoneService.getCore();

                ProxyConfig cfg = core.getDefaultProxyConfig();
                cfg.setRoute("core1-hk.netustay.com");

               String pro = cfg.getServerAddr();


                Address addressToCall =core.interpretUrl(mSipAddressToCall.getText().toString());

//                cfg.setIdentityAddress(addressToCall);

//                addressToCall.setDomain("core1-hk.netustay.com");

                CallParams params = core.createCallParams(null);

                addressToCall.setDisplayName("8001");


                System.out.println("getDisplayName = " + addressToCall.getDisplayName());
                System.out.println("getDomain = " + addressToCall.getDomain());
                System.out.println("getPassword = " + addressToCall.getPassword());
                System.out.println("getMethodParam = " + addressToCall.getMethodParam());
                System.out.println("getScheme = " + addressToCall.getScheme());
                System.out.println("getUsername = " + addressToCall.getUsername());
                System.out.println("getPort = " + addressToCall.getPort());
                System.out.println("getSecure = " + addressToCall.getSecure());


                Switch videoEnabled = (Switch) findViewById(R.id.call_with_video);
                params.enableVideo(videoEnabled.isChecked());

                if (addressToCall != null) {
                    core.inviteAddressWithParams(addressToCall, params);
                }

            }
        });




    }

    @Override
    protected void onStart() {
        super.onStart();

        // Ask runtime permissions, such as record audio and camera
        // We don't need them here but once the user has granted them we won't have to ask again
        checkAndRequestCallPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // The best way to use Core listeners in Activities is to add them in onResume
        // and to remove them in onPause
        LinphoneService.getCore().addListener(mCoreListener);

        // Manually update the LED registration state, in case it has been registered before
        // we add a chance to register the above listener
        ProxyConfig proxyConfig = LinphoneService.getCore().getDefaultProxyConfig();

        if (proxyConfig != null) {
//            proxyConfig.setServerAddr("core1-hk.netustay.com");

            updateLed(proxyConfig.getState());
        } else {
            // No account configured, we display the configuration activity
            startActivity(new Intent(this, ConfigureAccountActivity.class));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Like I said above, remove unused Core listeners in onPause
        LinphoneService.getCore().removeListener(mCoreListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        // Callback for when permissions are asked to the user
        for (int i = 0; i < permissions.length; i++) {
            L.i(
                    "[Permission] "
                            + permissions[i]
                            + " is "
                            + (grantResults[i] == PackageManager.PERMISSION_GRANTED
                            ? "granted"
                            : "denied"));
        }
    }

    private void updateLed(RegistrationState state) {
        switch (state) {
            case Ok: // This state means you are connected, to can make and receive calls & messages
                mLed.setImageResource(R.drawable.led_connected);
                break;
            case None: // This state is the default state
            case Cleared: // This state is when you disconnected
                mLed.setImageResource(R.drawable.led_disconnected);
                break;
            case Failed: // This one means an error happened, for example a bad password
                mLed.setImageResource(R.drawable.led_error);
                break;
            case Progress: // Connection is in progress, next state will be either Ok or Failed
                mLed.setImageResource(R.drawable.led_inprogress);
                break;
        }
    }

    private void checkAndRequestCallPermissions() {
        ArrayList<String> permissionsList = new ArrayList<>();

        // Some required permissions needs to be validated manually by the user
        // Here we ask for record audio and camera to be able to make video calls with sound
        // Once granted we don't have to ask them again, but if denied we can
        int recordAudio =
                getPackageManager()
                        .checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName());
        L.i(
                "[Permission] Record audio permission is "
                        + (recordAudio == PackageManager.PERMISSION_GRANTED
                        ? "granted"
                        : "denied"));
        int camera =
                getPackageManager().checkPermission(Manifest.permission.CAMERA, getPackageName());
        L.i(
                "[Permission] Camera permission is "
                        + (camera == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));

        if (recordAudio != PackageManager.PERMISSION_GRANTED) {
            L.i("[Permission] Asking for record audio");
            permissionsList.add(Manifest.permission.RECORD_AUDIO);
        }

        if (camera != PackageManager.PERMISSION_GRANTED) {
            L.i("[Permission] Asking for camera");
            permissionsList.add(Manifest.permission.CAMERA);
        }

        if (permissionsList.size() > 0) {
            String[] permissions = new String[permissionsList.size()];
            permissions = permissionsList.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }
}
