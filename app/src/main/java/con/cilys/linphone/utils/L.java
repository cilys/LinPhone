package con.cilys.linphone.utils;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;

/**
 * 基于系统的日志工具
 */
public class L {
    private final static String TAG = L.class.getSimpleName();

    private final static int VERBOSE = 1;
    private final static int DEBUG = 2;
    private final static int INFO = 3;
    private final static int WARN = 4;
    private final static int ERROR = 5;
    private final static int EXCEPTION = 6;

    private static int level = 0;

    private final static String appendMsg = "[日志输出] ";

    public static void v(String tag, String msg){
        if (msg == null) {
            return;
        }
        if (tag == null) {
            tag = TAG;
        }

        if (outlog(VERBOSE)) {
            String log = getCurrentTime() + " " + appendMsg + msg;
            Log.v(tag, log);

            if (writeLogToFile) {
                writeLogToFile(tag, log);
            }
        }
    }

    public static void v(String msg) {
        v(null, msg);
    }

    public static void d(String tag, String msg) {
        if (msg == null) {
            return;
        }
        if (tag == null) {
            tag = TAG;
        }

        if (outlog(DEBUG)) {
            String log = getCurrentTime() + " " + appendMsg + msg;
            Log.d(tag, log);

            if (writeLogToFile) {
                writeLogToFile(tag, log);
            }
        }
    }
    public static void d(String msg) {
        d(null, msg);
    }

    public static void i(String tag, String msg) {
        if (msg == null) {
            return;
        }
        if (tag == null) {
            tag = TAG;
        }

        if (outlog(INFO)) {
            String log = getCurrentTime() + " " + appendMsg + msg;
            Log.i(tag, log);

            if (writeLogToFile) {
                writeLogToFile(tag, log);
            }
        }
    }
    public static void i(String msg) {
        i(null, msg);
    }


    public static void w(String tag, String msg) {
        if (msg == null) {
            return;
        }
        if (tag == null) {
            tag = TAG;
        }

        if (outlog(WARN)) {
            String log = getCurrentTime() + " " + appendMsg + msg;
            Log.w(tag, log);

            if (writeLogToFile) {
                writeLogToFile(tag, log);
            }
        }
    }
    public static void w(String msg) {
        w(null, msg);
    }

    public static void e(String tag, String msg) {
        if (msg == null) {
            return;
        }
        if (tag == null) {
            tag = TAG;
        }

        if (outlog(ERROR)) {
            String log = getCurrentTime() + " " + appendMsg + msg;
            Log.e(tag, log);

            if (writeLogToFile) {
                writeLogToFile(tag, log);
            }
        }
    }
    public static void e(String msg) {
        e(null, msg);
    }

    public static void printException(Throwable e) {
        if (e == null) {
            return;
        }

        if (outlog(EXCEPTION)) {
            e.printStackTrace();

            String err = throwableToStr(e);

            String log = getCurrentTime() + " " + appendMsg + err;

            if (writeLogToFile) {
                writeLogToFile("Throwable", log);
            }
        }
    }

    private static void writeLogToFile(String tag, String msg){
//        LogFileUtils.getInstance().startWriteLog(dir, fileName, "[" + tag + "] " + msg);
    }

    private static String dir, fileName;
    public static void setLogFilePath(String dir, String fileName) {
        L.dir = dir;
        L.fileName = fileName;
    }
    private static boolean writeLogToFile = false;

    public static void setWriteLogToFile(boolean writeLogToFile) {
        L.writeLogToFile = writeLogToFile;
    }

    private static boolean outlog(int l){
        if (level <= l) {
            return true;
        }
        return false;
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static String getCurrentTime() {
        return sdf.format(System.currentTimeMillis());
    }

    private static String throwableToStr(Throwable e){
        if (e == null){
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(bos));
        if (bos != null){
            try {
                bos.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return bos.toString();
        }
        return null;
    }
}