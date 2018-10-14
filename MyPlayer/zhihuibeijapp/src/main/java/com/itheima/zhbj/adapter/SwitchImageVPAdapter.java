package com.itheima.zhbj.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.itheima.zhbj.base.NewsCenterContentTabPager;
import com.itheima.zhbj.bean.NewsCenterTabBean;

import java.util.List;

/**
 * Created by Apple on 2016/9/27.
 * 轮播图的适配器
 */
public class SwitchImageVPAdapter extends PagerAdapter {
    private List<ImageView> imageViews;
    private List<NewsCenterTabBean.TopNewsBean> topNewsBeanList;



    public SwitchImageVPAdapter(List<ImageView> imageViews, List<NewsCenterTabBean.TopNewsBean> topNewsBeanList) {
        this.imageViews = imageViews;
        this.topNewsBeanList = topNewsBeanList;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView iv = imageViews.get(position);
        container.addView(iv);
        return iv;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return imageViews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
