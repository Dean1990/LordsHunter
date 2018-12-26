package com.deanlib.lordshunter.ui.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.deanlib.lordshunter.R;
import com.deanlib.lordshunter.Utils;
import com.deanlib.lordshunter.app.Constant;
import com.deanlib.lordshunter.data.entity.OCR;
import com.deanlib.lordshunter.ui.adapter.OCRListAdapter;
import com.deanlib.ootblite.utils.DLog;
import com.deanlib.ootblite.utils.FormatUtils;
import com.deanlib.ootblite.utils.PopupUtils;
import com.deanlib.ootblite.utils.network.NetworkManager;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * OCR包管理
 *
 * @author dean
 * @time 2018/12/25 2:58 PM
 */
public class OCRManageActivity extends BaseActivity {

    @BindView(R.id.listView)
    ListView listView;
    OCRListAdapter mOCRListAdapter;
    List<OCR> mOCRList;

    AlertDialog mDownloadDialog;
    ProgressBar mDownloadProgressBar;
    TextView tvProgressInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_manage);
        ButterKnife.bind(this);

        boolean autoDownloadOCRData = getIntent().getBooleanExtra("autoDownloadOCRData", false);

        init();
        loadData();

        if (autoDownloadOCRData) {
            //下载本地语言对应的OCR包
            for (OCR ocr : mOCRList){
                if (ocr.getName().equals(Constant.OCR_LANGUAGE)){
                    checkWifi2DownloadDataPackage(ocr);
                    break;
                }
            }
        }
    }

    private void init(){
        listView.setAdapter(mOCRListAdapter = new OCRListAdapter(mOCRList = new ArrayList<>()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mOCRList.get((int)id).isExist()){
                    checkWifi2DownloadDataPackage(mOCRList.get((int)id));
                }else {
                    PopupUtils.sendToast(R.string.data_package_exist);
                }
            }
        });
    }

    private void loadData(){

        for (String lang : Constant.OCR_LANGUAGES){
            mOCRList.add(Utils.getOCR(lang));
        }
        mOCRListAdapter.notifyDataSetChanged();
    }


    @OnClick(R.id.layoutBack)
    public void onViewClicked() {
        finish();
    }

    private void checkWifi2DownloadDataPackage(OCR ocr) {
        if (NetworkManager.getAPNType(this) != NetworkManager.TYPE_WIFI) {
            new AlertDialog.Builder(this).setTitle(R.string.attention)
                    .setMessage(R.string.attention_not_wifi)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            downloadDataPackage(ocr);
                        }
                    }).setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            downloadDataPackage(ocr);
        }
    }

    private void downloadDataPackage(OCR ocr) {
        FileDownloader.setup(this);
        FileDownloader.getImpl().create("http://file2001552359.nos-eastchina1.126.net/tessdata/" + ocr.getName() + ".traineddata")
                .setPath(ocr.getFile().getAbsolutePath()).setListener(new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                DLog.d("FileDownloadListener.pending");
                View progressView = View.inflate(OCRManageActivity.this, R.layout.layout_progress2, null);
                mDownloadProgressBar = progressView.findViewById(R.id.progress);
                tvProgressInfo = progressView.findViewById(R.id.tvProgressInfo);
                mDownloadDialog = new AlertDialog.Builder(OCRManageActivity.this)
                        .setTitle(getString(R.string.download_data_package)).setView(progressView)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //取消
                                FileDownloader.getImpl().pauseAll();

                                dialog.dismiss();
                            }
                        }).setCancelable(false).show();
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                DLog.d("FileDownloadListener.progress ->" + soFarBytes + "   total:" + totalBytes);
                if (mDownloadDialog != null && mDownloadDialog.isShowing()
                        && mDownloadProgressBar != null && tvProgressInfo != null) {
                    tvProgressInfo.setText(getString(R.string.doalowning_info_,
                            FormatUtils.formatFileSize(totalBytes),
                            FormatUtils.formatFileSize(soFarBytes),
                            task.getSpeed() + "KB/s"));
                    mDownloadProgressBar.setMax(totalBytes);
                    mDownloadProgressBar.setProgress(soFarBytes);
                }
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                DLog.d("FileDownloadListener.completed");
                FileDownloader.getImpl().clearAllTaskData();
                if (mDownloadDialog != null && mDownloadDialog.isShowing()) {
                    PopupUtils.sendToast(R.string.download_completed);
                    mDownloadDialog.dismiss();
                }
                ocr.setExist(true);
                mOCRListAdapter.notifyDataSetChanged();
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                DLog.d("FileDownloadListener.paused");
                if (ocr.getFile().exists()) {
                    ocr.getFile().delete();
                }
                FileDownloader.getImpl().clearAllTaskData();
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                e.printStackTrace();
                DLog.d("FileDownloadListener.error");
                if (ocr.getFile().exists()) {
                    ocr.getFile().delete();
                }
                FileDownloader.getImpl().clearAllTaskData();
                PopupUtils.sendToast(R.string.download_error);
                if (mDownloadDialog != null && mDownloadDialog.isShowing()) {
                    mDownloadDialog.dismiss();
                }
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                DLog.d("FileDownloadListener.warn");
            }
        }).start();
    }
}
