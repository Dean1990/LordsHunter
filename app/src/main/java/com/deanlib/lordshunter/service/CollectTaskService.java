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
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.Utils;
import com.deanlib.lordshunter.app.Constant;
import com.deanlib.lordshunter.entity.Report;
import com.deanlib.lordshunter.event.CollectTaskEvent;
import com.deanlib.lordshunter.ui.view.SavaActivity;
import com.deanlib.ootblite.utils.PopupUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CollectTaskService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String text = intent.getStringExtra("text");
        List<Uri> images = intent.getParcelableArrayListExtra("images");
        Observable.create(new ObservableOnSubscribe<List<Report>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Report>> emitter) throws Exception {
                //解析
                EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_DIALOG_MESSAGE,getString(R.string.parse_data)));
                List<Report> reports = Utils.parseText(CollectTaskService.this,text, images);
                if (reports==null || reports.size() == 0){
                    PopupUtils.sendToast(R.string.invalid_data);
                    emitter.onError(new Throwable("invalid data"));
                    return;
                }
                //重复性验证
                EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_DIALOG_MESSAGE,getString(R.string.repet_data)));
                List<Report> reports1 = Utils.checkRepet(reports);
                //文字识别
                EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_DIALOG_MESSAGE,getString(R.string.text_extraction)));
                List<Report> reports2 = Utils.ocr(reports1);

                emitter.onNext(reports2);
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
                        if (reports != null && reports.size() > 0) {
                            EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_UPDATE_UI,reports));

                            //如果不是运行在前台的，发通知
                            if (!SavaActivity.isRunForeground) {
                                //发通知
                                int notifiyId = (int) SystemClock.currentThreadTimeMillis();
                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    NotificationChannel channel = new NotificationChannel("1", "Notifiy", NotificationManager.IMPORTANCE_DEFAULT);
                                    mNotificationManager.createNotificationChannel(channel);
                                }

                                Intent intent1 = new Intent();
                                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent1.setClass(CollectTaskService.this, SavaActivity.class);
                                intent1.putParcelableArrayListExtra("reports", (ArrayList<? extends Parcelable>) reports);
                                PendingIntent pendingIntent = PendingIntent.getActivity(CollectTaskService.this, notifiyId, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(CollectTaskService.this, "1");
                                builder.setSmallIcon(R.mipmap.ic_launcher);
                                //builder.setSmallIcon(android.os.Build.VERSION.SDK_INT>20?R.drawable.ic_launcher_round:R.drawable.ic_launcher);
                                //builder.setColor(context.getResources().getColor(R.color.icon_blue));
                                builder.setLargeIcon(BitmapFactory.decodeResource(CollectTaskService.this.getResources(), R.mipmap.ic_launcher));
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
                        EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_DIALOG_DISMISS,null));
                    }

                    @Override
                    public void onComplete() {
                        EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_DIALOG_DISMISS,null));
                    }
                });
        return super.onStartCommand(intent, flags, startId);
    }


}
