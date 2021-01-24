package con.cilys.linphone.event;

import java.util.ArrayList;
import java.util.List;

public class Event {
    private static Event event;

    private List<EventImpl> subs;

    private Event(){
        subs = new ArrayList<>();
    }

    public static Event getInstance() {
        if (event == null) {
            synchronized (Event.class) {
                if (event == null) {
                    event = new Event();
                }
            }
        }
        return event;
    }

    public void postEvent(int type) {
        if (subs == null) {
            subs = new ArrayList<>();
        }
        for (EventImpl m : subs) {
            m.onTrigger(type);
        }
    }

    public void onSub(EventImpl m){
        if (m == null) {
            return;
        }
        if (subs != null) {
            if (!subs.contains(m)) {
                subs.add(m);
            }
        }
    }

    public void unSub(EventImpl m){
        if (m == null) {
            return;
        }
        if (subs != null) {
            subs.remove(m);
        }
    }
}
