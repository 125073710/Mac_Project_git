package com.itheima.xfyuntest01.bean;

import java.util.List;

/**
 * Created by Apple on 2016/10/8.
 */
public class VoiceInfo {
    public int bg;
    public int ed;
    public boolean ls;
    public int sn;
    public List<WS> ws;

    public class WS{
        public int bg;
        public List<CW> cw;
    }

    public class CW{
        public String sc;
        public String w;
    }
}
