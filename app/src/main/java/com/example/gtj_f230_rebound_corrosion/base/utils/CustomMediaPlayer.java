package com.example.gtj_f230_rebound_corrosion.base.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.example.gtj_f230_rebound_corrosion.R;

import java.io.IOException;

public class CustomMediaPlayer {

    public interface CallBack {
        void onCompletion();
    }

    public static MediaPlayer getMediaPlayer(final CallBack callBack) {
        final MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(mp -> {
            // 装载完毕回调
            mediaPlayer.start();
        });
        mediaPlayer.setOnCompletionListener(mp -> {
            // 在播放完毕被回调
            if (callBack != null) {
                callBack.onCompletion();
            }
        });
        return mediaPlayer;
    }

    public static void startSpeaking(Context context, MediaPlayer mediaPlayer, String content) {
        AssetFileDescriptor file = getSpeakingDescriptor(context, content);
        play(mediaPlayer, file);
    }

    private static void play(MediaPlayer mediaPlayer, AssetFileDescriptor file) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(),
                    file.getLength());
            file.close();
            // 通过异步的方式装载媒体资源
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
        }
    }

    public static AssetFileDescriptor getSpeakingDescriptor(Context context, String content) {
        AssetFileDescriptor assetFileDescriptor = null;
        switch (content) {
            case "welcome":  //欢迎使用高铁建仪器
                assetFileDescriptor = context.getResources().openRawResourceFd(R.raw.welcome);
                break;
            case "prompt":
                assetFileDescriptor = context.getResources().openRawResourceFd(R.raw.video_prompt);
                break;
        }
        return assetFileDescriptor;
    }

    /**
     * 释放播放器资源
     */
    public static void releasePlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }
}
