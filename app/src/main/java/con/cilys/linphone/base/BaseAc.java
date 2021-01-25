package con.cilys.linphone.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import con.cilys.linphone.event.Event;
import con.cilys.linphone.event.EventImpl;
import con.cilys.linphone.utils.ToastUtils;

public class BaseAc extends Activity implements EventImpl {

    protected void showToast(String msg) {
        ToastUtils.showToast(this, msg);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Event.getInstance().onSub(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Event.getInstance().unSub(this);
    }

    @Override
    public void onTrigger(int type) {
        changeToMainThread(type);
    }

    private void changeToMainThread(final int type){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onTriggerOnMainThread(type);
            }
        });
    }

    public void onTriggerOnMainThread(int type) {
        if (type == EventImpl.CLOSE_ALL_ACTIVITY) {
            finish();
        }
    }
}