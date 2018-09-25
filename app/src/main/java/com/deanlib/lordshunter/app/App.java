package com.deanlib.lordshunter.app;

import android.app.Application;

import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.entity.Prey;
import com.deanlib.ootblite.OotbConfig;

import java.util.HashSet;
import java.util.Set;

import io.realm.Realm;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //监听app是否在前台运行
        registerActivityLifecycleCallbacks(new ActivityLifecycleListener());

        OotbConfig.init(this,Constant.isDebug);
        Realm.init(this);

        String[] preyNamesChiSim = getResources().getStringArray(R.array.prey_name_chi_sim);
        String[] preyNamesChiTra = getResources().getStringArray(R.array.prey_name_chi_tra);

        for (int i = 0;i < preyNamesChiSim.length;i++) {
            Constant.PREY_NAMES.add(preyNamesChiSim[i]);
            Constant.PREY_NAMES.add(preyNamesChiTra[i]);
            Prey prey = new Prey(preyNamesChiSim[i],preyNamesChiTra[i]);
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
