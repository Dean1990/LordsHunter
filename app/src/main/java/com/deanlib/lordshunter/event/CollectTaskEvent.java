package com.deanlib.lordshunter.event;

public class CollectTaskEvent {

    public static final int ACTION_UPDATE_UI = 1;
    public static final int ACTION_MESSAGE = 2;
    public static final int ACTION_COMPLETE = 3;
    public static final int ACTION_ERROR = 4;

    int action;
    Object obj;

    public CollectTaskEvent(int action, Object obj) {
        this.action = action;
        this.obj = obj;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
