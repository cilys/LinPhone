package con.cilys.linphone;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import con.cilys.linphone.utils.L;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.LogCollectionState;
import org.linphone.mediastream.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class LinphoneService extends Service {
    private static LinphoneService instance;

    public static boolean isReady(){
        return instance != null;
    }

    public static LinphoneService getInstance() {
        return instance;
    }

    private Core mCore;

    public static Core getCore(){
        return instance.mCore;
    }

    private Handler mHandler;
    private CoreListenerStub mCoreListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        String basePath = getFilesDir().getAbsolutePath();
        Factory.instance().setLogCollectionPath(basePath);
        Factory.instance().enableLogCollection(LogCollectionState.Enabled);
        Factory.instance().setDebugMode(true, getString(R.string.app_name));

        dumpDeviceInformation();
        dumpInstalledLinphoneInformation();

        mHandler = new Handler();

        mCoreListener = new CoreListenerStub(){
            @Override
            public void onCallStateChanged(Core lc, Call call, Call.State state, String message) {
                super.onCallStateChanged(lc, call, state, message);

                L.e("onCallStateChanged msg = " + message);

                if (state == Call.State.IncomingReceived) {
                    Toast.makeText(LinphoneService.this, "Incoming call received, answering it automatically", Toast.LENGTH_LONG).show();
                    // For this sample we will automatically answer incoming calls
                    CallParams params = getCore().createCallParams(call);
                    params.enableVideo(true);
                    call.acceptWithParams(params);
                } else if (state == Call.State.Connected) {
                    // This stats means the call has been established, let's start the call activity
                    Intent intent = new Intent(LinphoneService.this, CallActivity.class);
                    // As it is the Service that is starting the activity, we have to give this flag
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);


                }
            }
        };

        try {
            // Let's copy some RAW resources to the device
            // The default config file must only be installed once (the first time)
            copyIfNotExist(R.raw.linphonerc_default, basePath + "/.linphonerc");

            // The factory config is used to override any other setting, let's copy it each time
            copyFromPackage(R.raw.linphonerc_factory, "linphonerc");
        } catch (IOException ioe) {
            L.printException(ioe);
        }

        // Create the Core and add our listener
        mCore = Factory.instance()
                .createCore(basePath + "/.linphonerc", basePath + "/linphonerc", this);
        mCore.addListener(mCoreListener);
        // Core is ready to be configured
        configureCore();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // If our Service is already running, no need to continue
        if (instance != null) {
            return START_STICKY;
        }

        // Our Service has been started, we can keep our reference on it
        // From now one the Launcher will be able to call onServiceReady()
        instance = this;

        // Core must be started after being created and configured
        mCore.start();
        // We also MUST call the iterate() method of the Core on a regular basis
        mHandler.postDelayed(coreRunnable, 20);

        return START_STICKY;
    }

    private Runnable coreRunnable = new Runnable(){
        @Override
        public void run() {
            if (mCore != null) {
                mCore.iterate();
            }
            mHandler.postDelayed(this, 20);
        }
    };

    @Override
    public void onDestroy() {
        if (mHandler != null && coreRunnable != null) {
            mHandler.removeCallbacks(coreRunnable);
        }

        if (mCore != null) {
            mCore.removeListener(mCoreListener);

            mCore.stop();
        }
        // A stopped Core can be started again
        // To ensure resources are freed, we must ensure it will be garbage collected
        mCore = null;
        // Don't forget to free the singleton as well
        instance = null;

        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // For this sample we will kill the Service at the same time we kill the app
        stopSelf();

        super.onTaskRemoved(rootIntent);
    }

    private void dumpDeviceInformation() {
        StringBuilder sb = new StringBuilder();
        sb.append("DEVICE=").append(Build.DEVICE).append("\n");
        sb.append("MODEL=").append(Build.MODEL).append("\n");
        sb.append("MANUFACTURER=").append(Build.MANUFACTURER).append("\n");
        sb.append("SDK=").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("Supported ABIs=");
        for (String abi : Version.getCpuAbis()) {
            sb.append(abi).append(", ");
        }
        sb.append("\n");
        L.i(sb.toString());
    }

    private void dumpInstalledLinphoneInformation() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException nnfe) {
            L.printException(nnfe);
        }

        if (info != null) {
            L.i("[Service] Linphone version is ", info.versionName + " (" + info.versionCode + ")");
        } else {
            L.i("[Service] Linphone version is unknown");
        }
    }

    private void configureCore() {
        // We will create a directory for user signed certificates if needed
        String basePath = getFilesDir().getAbsolutePath();
        String userCerts = basePath + "/user-certs";
        File f = new File(userCerts);
        if (!f.exists()) {
            if (!f.mkdir()) {
                L.e(userCerts + " can't be created.");
            }
        }
        mCore.setUserCertificatesPath(userCerts);
    }

    private void copyIfNotExist(int ressourceId, String target) throws IOException {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists()) {
            copyFromPackage(ressourceId, lFileToCopy.getName());
        }
    }

    private void copyFromPackage(int ressourceId, String target) throws IOException {
        FileOutputStream lOutputStream = openFileOutput(target, 0);
        InputStream lInputStream = getResources().openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = lInputStream.read(buff)) != -1) {
            lOutputStream.write(buff, 0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }
}
