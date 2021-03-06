package com.itheima.zhbj.base;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.itheima.zhbj.R;
import com.itheima.zhbj.adapter.NewsListAdapter;
import com.itheima.zhbj.adapter.SwitchImageVPAdapter;
import com.itheima.zhbj.bean.NewsCenterTabBean;
import com.itheima.zhbj.utils.CacheUtils;
import com.itheima.zhbj.utils.Constant;
import com.itheima.zhbj.utils.MyLogger;
import com.itheima.zhbj.utils.MyToast;
import com.itheima.zhbj.view.RecycleViewDivider;
import com.itheima.zhbj.view.RefreshRecyclerView;
import com.itheima.zhbj.view.SwitchImageViewViewPager;
import com.squareup.picasso.Picasso;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;

/**
 * Created by Apple on 2016/9/27.
 * 新闻中心的内容tab页面
 */
public class NewsCenterContentTabPager implements ViewPager.OnPageChangeListener, RefreshRecyclerView.OnRefreshListener, RefreshRecyclerView.OnLoadMoreListener {

    private static final String TAG = "NewsCenterContentTabPager";
//    @BindView(R.id.vp_switch_image)
    SwitchImageViewViewPager vpSwitchImage;
//    @BindView(R.id.tv_title)
    TextView tvTitle;
//    @BindView(R.id.ll_point_container)
    LinearLayout llPointContainer;
    @BindView(R.id.rv_news)
    RefreshRecyclerView rvNews;
    private Context context;
    public View view;
    private NewsCenterTabBean newsCenterTabBean;
    private List<ImageView> imageViews;

    //处理轮播图的自动切换  （消息机制）
    private Handler mHandler = new Handler();
    //判断是否在切换
    private boolean hasSwitch;


    //切换的任务
    private class SwitchTask implements  Runnable{

        @Override
        public void run() {
            if(vpSwitchImage != null){
                //切换逻辑
                int currentItem = vpSwitchImage.getCurrentItem();
                //判断是否在了最后一页
                if(currentItem == newsCenterTabBean.data.topnews.size() - 1){
                    currentItem = 0;
                }else{
                    currentItem++;
                }
                vpSwitchImage.setCurrentItem(currentItem);
            }
            mHandler.postDelayed(this,3000);
        }
    }

    //开始切换
    public void startSwitch(){
        if(!hasSwitch){
            //往Handler里面的消息队列里面发送一个延时的消息
            mHandler.postDelayed(new SwitchTask(),3000);
        }
    }

    //停止切换
    public void stopSwitch(){
        //清空消息队列
        mHandler.removeCallbacksAndMessages(null);
        hasSwitch = false;
    }


    public NewsCenterContentTabPager(Context context) {
        this.context = context;
        view = initView();
    }

    //加载布局
    private View initView() {
        View view = LayoutInflater.from(context).inflate(R.layout.newscenter_content_tab, null);
        ButterKnife.bind(this,view);
        return view;
    }

    //加载数据
    public void loadNetData(final String url){
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        MyToast.show(context,"加载数据失败");
                        //读取缓存
                        try {
                            String json = CacheUtils.readCache(context, url);
                            if(!TextUtils.isEmpty(json)){
                                processData(json);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        MyLogger.i(TAG,response);
                        processData(response);
                        //缓存数据
                        try {
                            CacheUtils.saveCache(context,url,response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //把json字符串转换成对应的数据模型
    public void processData(String json){
        Gson gson = new Gson();
        newsCenterTabBean = gson.fromJson(json, NewsCenterTabBean.class);
        //把数据绑定给对应的控件
        bindDataToView();

        //把当前的NewsCenterContentTabPager对象传递给SwitchImageViewViewPager
        vpSwitchImage.setTabPager(this);
    }

    //绑定数据给控件
    private void bindDataToView(){
        loadSwitchImageViewLayout();
        initSwitchImageView();
        initPoint();
        initRVNews();
    }

    //动态加载轮播图的布局
    private void loadSwitchImageViewLayout() {
        View view = LayoutInflater.from(context).inflate(R.layout.switch_imageview,null);
        //手动的初始化控件
        vpSwitchImage = (SwitchImageViewViewPager) view.findViewById(R.id.vp_switch_image);
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        llPointContainer = (LinearLayout) view.findViewById(R.id.ll_point_container);
        //把轮播图添加给RefreshRecyclerView
        rvNews.addSwitchImageView(view);
    }

    //初始化新闻列表
    private void initRVNews() {
        //设置布局管理器
        rvNews.setLayoutManager(new LinearLayoutManager(context));
        //设置条目的分割线
        rvNews.addItemDecoration(new RecycleViewDivider(context,LinearLayoutManager.HORIZONTAL,1, Color.BLACK));

        //设置数据   RecyclerView   Adapter   ViewHolder
        NewsListAdapter newsListAdapter = new NewsListAdapter(context,newsCenterTabBean.data.news);
        rvNews.setAdapter(newsListAdapter);
        //设置下拉刷新的监听
        rvNews.setOnRefreshListener(this);
        //设置上拉加载的监听
        rvNews.setOnLoadMoreListener(this);
    }

    //初始化点
    private void initPoint() {
        //清空容器里面的布局
        llPointContainer.removeAllViews();
        for (int i = 0; i < newsCenterTabBean.data.topnews.size(); i++) {
            //小圆点
            View view = new View(context);
            //设置背景颜色
            view.setBackgroundResource(R.drawable.point_gray_bg);
            //布局参数
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(5,5);
            //右边距
            params.rightMargin = 10;
            //添加布局
            llPointContainer.addView(view,params);
        }
        //让第一个点的背景为红色
        llPointContainer.getChildAt(0).setBackgroundResource(R.drawable.point_red_bg);
    }

    //初始化轮播图的数据
    private void initSwitchImageView() {
        imageViews = new ArrayList<>();
        int size = newsCenterTabBean.data.topnews.size();
        for (int i = -1; i < size+1; i++) {
            NewsCenterTabBean.TopNewsBean topNewsBean = null;
            if(i == -1){
                //添加最后的一张图片
                topNewsBean = newsCenterTabBean.data.topnews.get(size-1);
            }else if(i == size){
                topNewsBean =  newsCenterTabBean.data.topnews.get(0);
            }else{
                topNewsBean = newsCenterTabBean.data.topnews.get(i);
            }
            ImageView iv = new ImageView(context);
            Picasso.with(context).load(topNewsBean.topimage).into(iv);
            imageViews.add(iv);
        }
        SwitchImageVPAdapter adapter = new SwitchImageVPAdapter(imageViews,newsCenterTabBean.data.topnews);
        vpSwitchImage.setAdapter(adapter);
        //设置轮播图的文字显示
        tvTitle.setText(newsCenterTabBean.data.topnews.get(0).title);
        //给轮播图设置滑动监听
        vpSwitchImage.addOnPageChangeListener(this);
        //让轮播图显示第一张图片
        vpSwitchImage.setCurrentItem(1,false);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    //页面被选中
    @Override
    public void onPageSelected(int position) {
        //修正下标
        int pageIndex = 0;
        //正确数据的大小
        int size = newsCenterTabBean.data.topnews.size();

        if(position == 0){
            pageIndex = size - 1;
            //切换到最后一个页面
            vpSwitchImage.setCurrentItem(size,false);
        }else if(position == size + 1){
            pageIndex = 0;
            //切换到第一个页面
            vpSwitchImage.setCurrentItem(1,false);
        }else{
            pageIndex = position - 1;
        }


        //设置轮播图的文字显示
        tvTitle.setText(newsCenterTabBean.data.topnews.get(pageIndex).title);


        //修改轮播图点的背景
        int childCount = llPointContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = llPointContainer.getChildAt(i);
            if(pageIndex == i){
                //选中的页面
                child.setBackgroundResource(R.drawable.point_red_bg);
            }else{
                //未选中的页面
                child.setBackgroundResource(R.drawable.point_gray_bg);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //加载最新的数据
    @Override
    public void onRefresh() {
        OkHttpUtils
                .get()
                .url(Constant.HOST+newsCenterTabBean.data.more)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        MyToast.show(context,"联网获取数据失败");
                        //隐藏头
                        rvNews.hideHeaderView(false);
                        //让轮播图继续切换
                        startSwitch();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Gson gson = new Gson();
                        NewsCenterTabBean tabBean = gson.fromJson(response, NewsCenterTabBean.class);
                        newsCenterTabBean.data.news.addAll(0,tabBean.data.news);
                        //隐藏头
                        rvNews.hideHeaderView(true);
                        //让轮播图继续切换
                        startSwitch();
                    }
                });
    }

    @Override
    public void onLoadMore() {
        OkHttpUtils
                .get()
                .url(Constant.HOST+newsCenterTabBean.data.more)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        MyToast.show(context,"联网获取数据失败");
                        //隐藏脚
                        rvNews.hideFooterView();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Gson gson = new Gson();
                        NewsCenterTabBean tabBean = gson.fromJson(response, NewsCenterTabBean.class);
                        newsCenterTabBean.data.news.addAll(tabBean.data.news);
                        //隐藏脚
                        rvNews.hideFooterView();
                    }
                });
    }
}
