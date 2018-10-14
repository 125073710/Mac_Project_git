package com.itheima.videoplayerlist.utils;

import android.media.MediaPlayer;

/**
 * Created by Apple on 2016/10/13.
 * 多媒体的工具类
 */
public final class MediaHelper {

    private MediaHelper() {
    }

    private static MediaPlayer mPlayer;

    //获取多媒体对象
    public static MediaPlayer getInstance(){
        if(mPlayer == null){
            mPlayer = new MediaPlayer();
        }
        return  mPlayer;
    }

    //播放
    public static void play(){
        if(mPlayer != null){
            mPlayer.start();
        }
    }

    //暂停
    public static void pause(){
        if(mPlayer != null){
            mPlayer.pause();
        }
    }

    //释放
    public static void release(){
        if(mPlayer != null){
            mPlayer.release();
            mPlayer = null;
        }
    }

}
