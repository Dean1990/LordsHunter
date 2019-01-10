package com.deanlib.lordshunter.event;

public class CollectTaskEvent {

    public static final int ACTION_UPDATE_UI = 1;//更新数据到界面
    public static final int ACTION_MESSAGE = 2;//界面信息
    public static final int ACTION_SERVICE_MESSAGE = 3;//服务器信息
    public static final int ACTION_COMPLETE = 4;//完成
    public static final int ACTION_ERROR = 5;//错误

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
