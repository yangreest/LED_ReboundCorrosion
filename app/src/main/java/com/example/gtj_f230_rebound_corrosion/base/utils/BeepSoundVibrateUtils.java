package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

import com.example.gtj_f230_rebound_corrosion.R;

public class BeepSoundVibrateUtils {
    private static final long VIBRATE_DURATION = 100L;
    private Vibrator vibrator;
    private SoundPool soundPool;
    private int music;

    public BeepSoundVibrateUtils(Activity activity) {
        //语音
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        music = soundPool.load(activity, R.raw.beep, 1); //把你的声音素材放到res/raw里，第2个参数即为资源文件，第3个为音乐的优先级
        //震动
        vibrator = (Vibrator) activity.getSystemService(Activity.VIBRATOR_SERVICE);
    }

    public void playBeepSoundAndVibrate() {
        soundPool.play(music, 1, 1, 0, 0, 1);  //播放
        vibrator.vibrate(VIBRATE_DURATION);  //震动
    }

    public void clear() {
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
