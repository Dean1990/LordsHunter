package com.deanlib.lordshunter.app;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.Utils;
import com.deanlib.lordshunter.data.entity.Member;
import com.deanlib.lordshunter.data.entity.Prey;
import com.deanlib.ootblite.OotbConfig;
import com.deanlib.ootblite.data.SharedPUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.umeng.commonsdk.UMConfigure;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.realm.Realm;

public class App extends MultiDexApplication {

    //static 代码段可以防止内存泄露
    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white);//全局设置主题颜色
                return new ClassicsHeader(context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new ClassicsFooter(context).setDrawableSize(20);
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //监听app是否在前台运行
        registerActivityLifecycleCallbacks(new ActivityLifecycleListener());

        OotbConfig.init(this,Constant.isDebug);
        Realm.init(this);

        UMConfigure.init(this, "5bb07c3db465f56d3400000c", "channel", UMConfigure.DEVICE_TYPE_PHONE, "");
        UMConfigure.setLogEnabled(Constant.isDebug);

        Constant.OCR_LANGUAGE =getString(R.string.ocr_language);
        Constant.APP_FILE_OCR_DIR = new File(Utils.getDiskCachePath(this)
                +"/lordshunter/datapath/tessdata/");

        SharedPUtils sharedP = new SharedPUtils();
        String hideMember = sharedP.getCache("hideMember");
        if (!TextUtils.isEmpty(hideMember))
            Constant.hideMemberList = JSON.parseArray(hideMember, Member.class);
        if (Constant.hideMemberList==null){
            Constant.hideMemberList = new ArrayList<>();
        }

        String[] preyNames = getResources().getStringArray(R.array.prey_name);
        String[] preyNamesChiSim = getResources().getStringArray(R.array.prey_name_chi_sim);
        String[] preyNamesChiTra = getResources().getStringArray(R.array.prey_name_chi_tra);

        for (int i = 0;i < preyNamesChiSim.length;i++) {
            Constant.PREY_NAMES.add(preyNamesChiSim[i]);
            Constant.PREY_NAMES.add(preyNamesChiTra[i]);
            Prey prey = new Prey(preyNames[i],preyNamesChiSim[i],preyNamesChiTra[i]);
            initNameIndex(preyNamesChiSim[i].toCharArray(),prey);
            initNameIndex(preyNamesChiTra[i].toCharArray(),prey);
        }

    }

    private void initNameIndex(char[] chars,Prey prey){
        for (char ch : chars){
            Set<Prey> preys = Constant.PREY_NAME_INDEX_MAP.get(ch);
            if (preys == null)
                preys = new HashSet<>();
            preys.add(prey);
            Constant.PREY_NAME_INDEX_MAP.put(ch,preys);
        }
    }

}
