package com.deanlib.lordshunter.data;


import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyPreFilter;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.deanlib.lordshunter.data.entity.Report;
import com.deanlib.ootblite.data.IOUtils;
import com.deanlib.ootblite.utils.FormatUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 持久化工作
 *
 * @author dean
 * @time 2018/10/14 上午11:51
 */
public class Persistence {

    /**
     * 导出数据
     * @param reports
     * @param dir
     * @return
     */
    public static String exportData(List<Report> reports,String dir) {
        String pathfile = null;
        if (reports!=null && reports.size()>0 && !TextUtils.isEmpty(dir)){
            File dirFile = new File(dir);
            if (!dirFile.exists()){
                dirFile.mkdirs();
            }

            File file = new File(dirFile,FormatUtils.convertDateTimestampToString(System.currentTimeMillis(),"yyyy-MM-dd-HH-mm-ss")+".lhdata");
            try {
                PropertyPreFilter filter = new SimplePropertyPreFilter(Report.class,"id","group","date","time","name","image","timestamp");
                IOUtils.writeStr(new FileOutputStream(file),JSON.toJSONString(reports,filter));
                pathfile = file.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return pathfile;
    }

    /**
     * 导入数据
     * @param filepath
     * @return
     */
    public static List<Report> importData(String filepath) {
        List<Report> reports = null;
        if (!TextUtils.isEmpty(filepath) && filepath.endsWith(".lhdata")) {
            File file = new File(filepath);
            if (file.exists()) {
                try {
                    String data = IOUtils.readStr(new FileInputStream(file));
                    if (!TextUtils.isEmpty(data)) {
                        reports = JSON.parseArray(data, Report.class);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

            }
        }
        //PopupUtils.sendToast(R.string.invalid_data);

        return reports;
    }
}
