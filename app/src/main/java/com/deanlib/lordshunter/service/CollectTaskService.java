package com.deanlib.lordshunter.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.Utils;
import com.deanlib.lordshunter.data.entity.Report;
import com.deanlib.lordshunter.event.CollectTaskEvent;
import com.deanlib.lordshunter.ui.view.SaveActivity;
import com.deanlib.lordshunter.ui.view.ViewJump;
import com.deanlib.ootblite.data.SharedPUtils;
import com.deanlib.ootblite.utils.DLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CollectTaskService extends Service {

    NotificationCompat.Builder mNotificationBuilder;
    String CHANNEL_ID = "com.deanlib.lordshunter.service";
    String CHANNEL_NAME = "Service";
    int mServiceNotifiyId;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            DLog.d("onStartCommand");
            EventBus.getDefault().register(this);
            mServiceNotifiyId = (int) System.currentTimeMillis();
//            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
//                mNotificationManager.createNotificationChannel(channel);
//            }

            mNotificationBuilder = new NotificationCompat.Builder(CollectTaskService.this, CHANNEL_ID);
            mNotificationBuilder.setSmallIcon(R.mipmap.notify_icon);
            //builder.setSmallIcon(android.os.Build.VERSION.SDK_INT>20?R.drawable.ic_launcher_round:R.drawable.ic_launcher);
            //builder.setColor(context.getResources().getColor(R.color.icon_blue));
            mNotificationBuilder.setLargeIcon(BitmapFactory.decodeResource(CollectTaskService.this.getResources(), R.mipmap.icon));
            mNotificationBuilder.setAutoCancel(true);
            mNotificationBuilder.setDefaults(Notification.DEFAULT_ALL);
            mNotificationBuilder.setTicker(getString(R.string.collection_working));
            mNotificationBuilder.setContentTitle(getString(R.string.app_name));
            mNotificationBuilder.setContentText(getString(R.string.collection_working));
            mNotificationBuilder.setProgress(0, 0, true);
            mNotificationBuilder.setWhen(System.currentTimeMillis());
            mNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.collection_working)));
            mNotificationBuilder.setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE);

//        mNotificationManager.notify(notifiyId, builder.build());
            startForeground(mServiceNotifiyId, mNotificationBuilder.build());

            String text = intent.getStringExtra("text");
            List<Uri> images = intent.getParcelableArrayListExtra("images");
            Observable.create(new ObservableOnSubscribe<List<Report>>() {
                @Override
                public void subscribe(ObservableEmitter<List<Report>> emitter) throws Exception {
                    //解析
                    EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_SERVICE_MESSAGE, getString(R.string.parse_data)));
                    List<Report> reports = Utils.parseText(CollectTaskService.this, text, images);
                    if (reports == null || reports.size() == 0) {
                        emitter.onError(new Throwable(getString(R.string.invalid_data)));
                        return;
                    }

                    //文字识别
                    EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_SERVICE_MESSAGE, getString(R.string.text_extraction_,0,reports.size())));
                    SharedPUtils sharedPUtils = new SharedPUtils();
                    if("true".equals(sharedPUtils.getCache("cloudocr"))) {
                        Utils.cloudOCR(CollectTaskService.this,reports);
                    }else {
                        Utils.localOCR(CollectTaskService.this,reports);
                    }
                    emitter.onNext(reports);
                    emitter.onComplete();
                }
            }).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Report>>() {

                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(List<Report> reports) {
                            DLog.d("next");
                            if (reports != null && reports.size() > 0) {
                                //缓存
                                SharedPUtils sharedPUtils = new SharedPUtils();
                                sharedPUtils.setCache("unsavareports", reports);

                                EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_UPDATE_UI, reports));

                                DLog.d("SavaActivity.isRunForeground ：" + SaveActivity.isRunForeground);
                                //如果不是运行在前台的，发通知
                                if (!SaveActivity.isRunForeground) {
                                    DLog.d("send notification");
                                    //发通知
                                    int notifiyId = (int) System.currentTimeMillis();
                                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                                        mNotificationManager.createNotificationChannel(channel);
                                    }

                                    PendingIntent pendingIntent = PendingIntent.getActivity(CollectTaskService.this, notifiyId, ViewJump.getSaveIntent(CollectTaskService.this, reports), PendingIntent.FLAG_UPDATE_CURRENT);

                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(CollectTaskService.this, "1");
                                    builder.setSmallIcon(R.mipmap.notify_icon);
                                    //builder.setSmallIcon(android.os.Build.VERSION.SDK_INT>20?R.drawable.ic_launcher_round:R.drawable.ic_launcher);
                                    //builder.setColor(context.getResources().getColor(R.color.icon_blue));
                                    builder.setLargeIcon(BitmapFactory.decodeResource(CollectTaskService.this.getResources(), R.mipmap.icon));
                                    builder.setAutoCancel(true);
                                    builder.setDefaults(Notification.DEFAULT_ALL);
                                    builder.setTicker(getString(R.string.collection_save));
                                    builder.setContentTitle(getString(R.string.app_name));
                                    builder.setContentText(getString(R.string.collection_save));
                                    builder.setWhen(System.currentTimeMillis());
                                    builder.setContentIntent(pendingIntent);
                                    builder.setStyle(new NotificationCompat.BigTextStyle()
                                            .bigText(getString(R.string.collection_save)));

                                    mNotificationManager.notify(notifiyId, builder.build());

                                }
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            DLog.d("error:" + e.getMessage());
                            EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_ERROR, e.getMessage()));
                            EventBus.getDefault().unregister(CollectTaskService.this);
                            stopForeground(true);
                        }

                        @Override
                        public void onComplete() {
                            DLog.d("complete");
                            EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_COMPLETE, null));
                            EventBus.getDefault().unregister(CollectTaskService.this);
                            stopForeground(true);
                        }
                    });
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCollectTaskEvent(CollectTaskEvent event) {
       switch (event.getAction()){
           case CollectTaskEvent.ACTION_SERVICE_MESSAGE:
               String msg = (String) event.getObj();
               mNotificationBuilder.setContentText(msg);
               mNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                       .bigText(msg));
               mNotificationBuilder.setSound(null);
               startForeground(mServiceNotifiyId, mNotificationBuilder.build());
               break;
       }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DLog.d("onDestroy");
        EventBus.getDefault().unregister(this);
        stopForeground(true);
    }
}
