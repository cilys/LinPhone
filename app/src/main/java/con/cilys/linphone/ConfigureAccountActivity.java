package con.cilys.linphone;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;

import con.cilys.linphone.base.BaseAc;

public class ConfigureAccountActivity extends BaseAc {
    private EditText mUsername, mPassword, mDomain;
    private RadioGroup mTransport;
    private Button mConnect;

    private AccountCreator mAccountCreator;
    private CoreListenerStub mCoreListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.configure_account);

        // Account creator can help you create/config accounts, even not sip.linphone.org ones
        // As we only want to configure an existing account, no need for server URL to make requests
        // to know whether or not account exists, etc...
        mAccountCreator = LinphoneService.getCore().createAccountCreator(null);

        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        mDomain = (EditText) findViewById(R.id.domain);
        mDomain.setText("Training");
        mTransport = (RadioGroup) findViewById(R.id.assistant_transports);

        mConnect = (Button) findViewById(R.id.configure);
        mConnect.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        configureAccount();
                    }
                });

        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String message) {
                if (state == RegistrationState.Ok) {
                    finish();
                } else if (state == RegistrationState.Failed) {
                    showToast("Failure: " + message);
                }
            }
        };

        LinphoneService.getCore().addListener(mCoreListener);
    }

    @Override
    protected void onDestroy() {
        LinphoneService.getCore().removeListener(mCoreListener);

        super.onDestroy();
    }

    private void configureAccount() {
        // At least the 3 below values are required
        mAccountCreator.setUsername(mUsername.getText().toString());
        mAccountCreator.setDomain(mDomain.getText().toString());
        mAccountCreator.setPassword(mPassword.getText().toString());

        // By default it will be UDP if not set, but TLS is strongly recommended
        switch (mTransport.getCheckedRadioButtonId()) {
            case R.id.transport_udp:
                mAccountCreator.setTransport(TransportType.Udp);
                break;
            case R.id.transport_tcp:
                mAccountCreator.setTransport(TransportType.Tcp);
                break;
            case R.id.transport_tls:
                mAccountCreator.setTransport(TransportType.Tls);
                break;
        }


        // This will automatically create the proxy config and auth info and add them to the Core
        ProxyConfig cfg = mAccountCreator.createProxyConfig();


        cfg.setServerAddr("<sip:core1-hk.netustay.com;transport=udp>");

        // Make sure the newly created one is the default
        LinphoneService.getCore().setDefaultProxyConfig(cfg);
    }
}
