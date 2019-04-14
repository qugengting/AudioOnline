package com.qugengting.audio;

import android.app.Application;

import com.qugengting.http.download.DownloadConfiguration;
import com.qugengting.http.download.DownloadManager;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author:qugengting
 * @date:2019/4/12
 * Description:
 */
public class App extends Application {
    public static ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(5,
            new BasicThreadFactory.Builder().namingPattern("example-schedule-pool-%d")
                    .daemon(true).build());
    @Override
    public void onCreate() {
        super.onCreate();
        //https://github.com/houjinyun/HttpFileUploaderAndDownloader
        DownloadConfiguration downloadConfiguration = new DownloadConfiguration.Builder(getApplicationContext())
                .setCacheDir(getExternalCacheDir())        //设置下载缓存目录，必须设置
              .setTaskExecutor(executorService)    //同上传类似
              .setThreadPriority(5)  //同上传类似
              .setThreadPoolCoreSize(5)  //同上传类似
  			.build();
        DownloadManager.getInstance(this).init(downloadConfiguration);
    }
}