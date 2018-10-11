package com.deanlib.lordshunter.ui.view;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.deanlib.lordshunter.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AdActivity extends BaseActivity {

    @BindView(R.id.tvJump)
    TextView tvJump;
    @BindView(R.id.adView)
    AdView adView;

    CountDownTimer cdt = new CountDownTimer(5000,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            tvJump.setText(getString(R.string.jump_,millisUntilFinished/1000));
        }

        @Override
        public void onFinish() {
            onViewClicked();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ButterKnife.bind(this);

        MobileAds.initialize(this,"ca-app-pub-1273595572669178~1185683572");
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("5BFACD250552284184A24F81C4E12BDF")
                .build();
        adView.loadAd(adRequest);
        cdt.start();

    }

    @OnClick(R.id.tvJump)
    public void onViewClicked() {
        cdt.cancel();
//        ViewJump.toMain(this);
        finish();
    }
}
