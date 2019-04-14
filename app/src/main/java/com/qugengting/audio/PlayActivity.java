package com.qugengting.audio;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qugengting.http.download.DownloadManager;
import com.qugengting.http.download.FileDownloadTask;
import com.qugengting.http.download.FileType;
import com.qugengting.http.download.listener.OnDownloadingListener;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayActivity extends Activity implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    private static final String TAG = PlayActivity.class.getSimpleName();
    public MediaPlayer mediaPlayer; // 媒体播放器
    private Timer timer = new Timer(); // 计时器
    private boolean isPrepare = false;
    private SeekBar seekBar;
    private static final String PATH = "http://47.107.185.144:8080/qugengting/stream/mp3/hongloumeng1-1";
    private static final String PATH2 = "http://file.kuyinyun.com/group1/M00/90/B7/rBBGdFPXJNeAM-nhABeMElAM6bY151.mp3";
    private TextView tvCurrent;
    private TextView tvDuration;
    private Button btnPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        seekBar = findViewById(R.id.seekBar);
        tvCurrent = findViewById(R.id.tv_current);
        tvDuration = findViewById(R.id.tv_duration);
        btnPlay = findViewById(R.id.btn_play);
        btnPlay.setEnabled(false);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    btnPlay.setText("继续");
                } else {
                    play();
                    btnPlay.setText("暂停");
                }
            }
        });
        seekBar.setProgress(0);//设置进度为0
        seekBar.setSecondaryProgress(0);//设置缓冲进度为0
        seekBar.setEnabled(false);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar mSeekBar, int progress, boolean fromUser) {
                tvCurrent.setText(getTime(mSeekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar mSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar mSeekBar) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mSeekBar.getProgress());
                    tvCurrent.setText(getTime(mSeekBar.getProgress()));
                } else {
                    if (isPrepare) {
                        mediaPlayer.seekTo(mSeekBar.getProgress());
                        tvCurrent.setText(getTime(mSeekBar.getProgress()));
                        mediaPlayer.start();
                        btnPlay.setText("暂停");
                    }
                }
            }
        });
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 每一秒触发一次
        timer.schedule(timerTask, 0, 1000);

        init(PATH);
    }

    TimerTask timerTask = new TimerTask() {

        @Override
        public void run() {
            if (mediaPlayer == null) {
                return;
            }
            if (mediaPlayer.isPlaying() && seekBar.isPressed() == false) {
                handler.sendEmptyMessage(0);
            }
        }
    };

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            tvCurrent.setText(getTime(seekBar.getProgress()));
        }
    };

    public void play() {
        if (isPrepare) {
            mediaPlayer.start();
        }
    }

    /**
     * @param url url地址
     */
    public void init(String url) {
        //下载和在线播放同步进行，因为音频文件受网速和大小影响，不能确定哪种方法能最快速播放，当下载完在线播放还没能开始时，就可以使用下载文件播放了
        //①下载
        DownloadManager.getInstance(this).downloadFile(FileType.TYPE_AUDIO, "111", url, new OnDownloadingListener() {
            @Override
            public void onDownloadFailed(FileDownloadTask task, int errorType, String msg) {
                Log.e(TAG, "ERR: " + msg);
            }

            @Override
            public void onDownloadSucc(FileDownloadTask task, File outFile) {
                Log.e(TAG, "file : " + outFile.getAbsolutePath());
                if (!isPrepare) {
                    seekBar.setSecondaryProgress(seekBar.getMax());
                    try {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(outFile.getAbsolutePath());
                        mediaPlayer.prepareAsync();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //②在线播放
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 停止
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // 播放准备
    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepare = true;
        seekBar.setEnabled(true);
        btnPlay.setEnabled(true);
        seekBar.setMax(mp.getDuration());
        tvDuration.setText(getTime(mp.getDuration()));
        Log.e(TAG, "onPrepared");
        Log.e(TAG, "总长: " + mp.getDuration());
    }

    // 播放完成
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e(TAG, "onCompletion");
        isPrepare = false;
        btnPlay.setText("开始");
        tvCurrent.setText("00:00:00");
        seekBar.setProgress(0);
        init(PATH);
    }

    /**
     * 缓冲更新
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        seekBar.setSecondaryProgress(seekBar.getMax() * percent / 100);
        int currentProgress = seekBar.getMax() * mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration();
        Log.e(currentProgress + "% play", percent + " buffer");
        Log.e(TAG, "缓冲更新cur: " + mediaPlayer.getCurrentPosition() + " , dur: " + mediaPlayer.getDuration());
    }

    private String getTime(int duration) {
        int i = duration / 1000;
        int h = i / (60 * 60);
        String sh;
        if (h == 0) {
            sh = "00";
        } else {
            sh = String.valueOf(h);
        }
        int m = i / 60 % 60;
        String sm;
        if (m == 0) {
            sm = "00";
        } else {
            sm = String.valueOf(m);
            if (sm.length() == 1) {
                sm = "0" + sm;
            }
        }
        int s = i % 60;
        String ss;
        if (s == 0) {
            ss = "00";
        } else {
            ss = String.valueOf(s);
            if (ss.length() == 1) {
                ss = "0" + ss;
            }
        }
        return sh + ":" + sm + ":" + ss;
    }

}
