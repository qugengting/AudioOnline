package com.qugengting.http.download.listener;

import com.qugengting.http.download.FileDownloadTask;

import java.io.File;

public interface OnDownloadingListener {

    /**
     * 下载失败
     *
     * @param task Downdload task
     * @param errorType {@link com.qugengting.http.download.DownloadErrorType}
     * @param msg 错误信息
     */
    void onDownloadFailed(FileDownloadTask task, int errorType, String msg);

    /**
     * 下载成功
     *
     * @param task Download task
     * @param outFile 下载成功后的文件
     */
    void onDownloadSucc(FileDownloadTask task, File outFile);

}
