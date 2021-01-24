package con.cilys.linphone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_launcher);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check whether the Service is already running
        if (LinphoneService.isReady()) {
            onServiceReady();
        } else {
            // If it's not, let's start it
            startService(new Intent().setClass(this, LinphoneService.class));
            // And wait for it to be ready, so we can safely use it afterwards
            final Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!LinphoneService.isReady()) {
                        mHandler.postDelayed(this, 30);
                    } else {
                        onServiceReady();
                    }
                }
            }, 30);
        }
    }

    private void onServiceReady() {
        // Once the service is ready, we can move on in the application
        // We'll forward the intent action, type and extras so it can be handled
        // by the next activity if needed, it's not the launcher job to do that
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        if (getIntent() != null && getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        intent.setAction(getIntent().getAction());
        intent.setType(getIntent().getType());
        startActivity(intent);

        finish();
    }
}