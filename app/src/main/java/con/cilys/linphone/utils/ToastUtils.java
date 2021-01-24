package con.cilys.linphone.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

    public static void showToast(Context cx, String msg){
        if (cx == null) {
            return;
        }
        if (msg == null) {
            return;
        }
        Toast.makeText(cx, msg, Toast.LENGTH_SHORT).show();
    }

}
