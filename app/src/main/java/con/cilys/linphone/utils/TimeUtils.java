package con.cilys.linphone.utils;

import java.text.SimpleDateFormat;

public class TimeUtils {
    private static SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

    public static String getCurrentTime(){
        return sdfTime.format(System.currentTimeMillis());
    }

}
