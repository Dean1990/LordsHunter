package com.deanlib.lordshunter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.deanlib.lordshunter.entity.Report;
import com.deanlib.ootb.data.db.DB;
import com.deanlib.ootb.utils.PopupUtils;

import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 接收并保存数据
 *
 * @author dean
 * @time 2018/9/4 下午5:26
 */
public class SavaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
//        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        ArrayList<Uri> images = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);

        Observable.just(deal(text,images)).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Void>() {
                    @Override
                    public void accept(Void aVoid) throws Exception {

                    }
                });
    }

    private Void deal(String text, List<Uri> images){
        List<Report> reports = Utils.parseText(text,images);
        //todo md5 重复性验证

        //todo 图片识别

        for (Report report:reports) {
            try {
                DB.getDbManager().save(report);
            } catch (DbException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
