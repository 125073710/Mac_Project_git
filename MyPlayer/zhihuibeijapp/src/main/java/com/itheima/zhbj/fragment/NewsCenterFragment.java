package com.itheima.zhbj.fragment;

import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.itheima.zhbj.R;
import com.itheima.zhbj.activity.MainActivity;
import com.itheima.zhbj.adapter.NewsCenterGroupImageViewAdapter;
import com.itheima.zhbj.adapter.NewsCenterTabVPAdapter;
import com.itheima.zhbj.base.BaseFragment;
import com.itheima.zhbj.base.BaseLoadNetDataOperator;
import com.itheima.zhbj.base.NewsCenterContentTabPager;
import com.itheima.zhbj.bean.NewsCenterBean;
import com.itheima.zhbj.bean.NewsCenterGroupImageViewBean;
import com.itheima.zhbj.utils.CacheUtils;
import com.itheima.zhbj.utils.Constant;
import com.itheima.zhbj.utils.MyLogger;
import com.itheima.zhbj.utils.MyToast;
import com.itheima.zhbj.view.RecycleViewDivider;
import com.viewpagerindicator.TabPageIndicator;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.OnClick;
import okhttp3.Call;

/**
 * Created by Apple on 2016/9/24.
 */
public class NewsCenterFragment extends BaseFragment implements BaseLoadNetDataOperator, ViewPager.OnPageChangeListener {

    private static final String TAG = "NewsCenterFragment";
    private TabPageIndicator tabPageIndicator;
    private ImageButton ibNext;
    private ViewPager vpNewsCenterContent;

    private List<NewsCenterContentTabPager> views;
    private NewsCenterBean newsCenterBean;

    private Map<Integer,View> cacheViews = new HashMap<>();
    private RecyclerView rvGroupImageView;
    private LinearLayoutManager llm;
    private GridLayoutManager glm;

    @Override
    public void initTitle() {
        setIbMenuDisplayState(true);
        setIbPicTypeDisplayState(false);
        setTitle("新闻");
    }

    @Override
    public View createContent() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.newscenter_content,(ViewGroup) getView(),false);
        tabPageIndicator = (TabPageIndicator) view.findViewById(R.id.tabPagerIndicator);
        ibNext = (ImageButton) view.findViewById(R.id.ib_next);
        vpNewsCenterContent = (ViewPager) view.findViewById(R.id.vp_newscenter_content);

        ibNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取ViewPager当前显示页面的下标
                int currentItem = vpNewsCenterContent.getCurrentItem();

                if(currentItem != newsCenterBean.data.get(0).children.size() - 1){
                    vpNewsCenterContent.setCurrentItem(currentItem+1);
                }


            }
        });
        initViewPager();

        return view;
    }

    //初始化ViewPager的数据
    private void initViewPager() {
        views = new ArrayList<>();
        for(NewsCenterBean.NewsCenterNewsTabBean tabBean:newsCenterBean.data.get(0).children){
            NewsCenterContentTabPager tabPager = new NewsCenterContentTabPager(getContext());
            views.add(tabPager);
        }
        //设置适配器
        NewsCenterTabVPAdapter adapter = new NewsCenterTabVPAdapter(views,newsCenterBean.data.get(0).children);
        vpNewsCenterContent.setAdapter(adapter);

        //让TabPagerIndicator和ViewPager进行联合
        tabPageIndicator.setViewPager(vpNewsCenterContent);

        //让新闻中心第一个子tab的轮播图开始切换
        views.get(0).startSwitch();

        //给ViewPager设置页面切换监听
        //注意：ViewPager和TabPagerIndicator配合使用，监听只能设置给TabPagerIndicator
        tabPageIndicator.setOnPageChangeListener(this);

    }


    @Override
    public void loadNetData() {
        final String url = Constant.NEWSCENTER_URL;
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        MyLogger.i(TAG,e.getMessage());
                        //吐司
                        MyToast.show(getActivity(),"");
                        //读取缓存
                        try {
                            String json = CacheUtils.readCache(getContext(), url);
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
                        //把response  == json 转换成对应的数据模型
                        processData(response);

                        //缓存数据
                        try {
                            CacheUtils.saveCache(getContext(),url,response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //把Json格式的字符串转换成对应的模型对象
    public void processData(String json){
        hasLoadData = true;//已经加载数据
        Gson gson = new Gson();
        newsCenterBean = gson.fromJson(json, NewsCenterBean.class);

        //把数据传递给MainActivity
        ((MainActivity)getActivity()).setNewsCenterMenuBeanList(newsCenterBean.data);

        //创建布局
        View view = createContent();

        //加载布局
        addView(view);

        //把布局添加到缓存的容器
        cacheViews.put(0,view);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    //新闻中心子tab页面的切换
    @Override
    public void onPageSelected(int position) {
        //当前的开始切换，其他的tab停止切换
        for (int i = 0; i < views.size(); i++) {
            NewsCenterContentTabPager tabPager = views.get(i);
            if(position == i){
                //选中页
                tabPager.startSwitch();
            }else{
                //未选中页
                tabPager.stopSwitch();
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //切换内容
    public void switchContent(int position){
        //标题右边的切换menu
        if(position == 2){
            //显示
            ibPicType.setVisibility(View.VISIBLE);
        }else{
            //隐藏
            ibPicType.setVisibility(View.GONE);
        }

        //先从缓存的容器里面去获取
        View view = cacheViews.get(position);
        if(view == null){
            //创建里面的内容
            container.removeAllViews();
            if(position == 2){
                //组图
                //加载布局
                view = createGroupImageViewLayout();
                addView(view);
                //放入容器
                cacheViews.put(position,view);
                //加载数据
                loadGroupImageViewData(position);
            }

        } else if(view != null){
            //添加布局
            addView(view);
        }


    }

    //加载组图数据
    private void loadGroupImageViewData(int position) {
        //获取路径
       final String url = Constant.HOST + newsCenterBean.data.get(position).url;
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        MyToast.show(getContext(),"获取组图失败");
                        //读取缓存
                        try {
                            String json = CacheUtils.readCache(getContext(), url);
                            if(!TextUtils.isEmpty(json)){
                                processGroupImageViewData(json);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                            MyLogger.i(TAG,response);
                        processGroupImageViewData(response);
                        //缓存数据
                        try {
                            CacheUtils.saveCache(getContext(),url,response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    //处理组图数据
    public void processGroupImageViewData(String json){
        Gson gson = new Gson();
        NewsCenterGroupImageViewBean newsCenterGroupImageViewBean = gson.fromJson(json, NewsCenterGroupImageViewBean.class);
        rvGroupImageView.setAdapter(new NewsCenterGroupImageViewAdapter(newsCenterGroupImageViewBean.data.news,getContext()));
    }

    //加载组图的布局
    private View createGroupImageViewLayout() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.newscenter_group_imageview,(ViewGroup) getActivity().getWindow().getDecorView(),false);
        rvGroupImageView = (RecyclerView) view.findViewById(R.id.rv_group_imageview);
        //有的时候是列表，有的时候是网格
        //线性布局管理器
        llm = new LinearLayoutManager(getContext());
        //网格布局管理器
        glm = new GridLayoutManager(getContext(),2);
        rvGroupImageView.setLayoutManager(llm);
        //添加分割线
        rvGroupImageView.addItemDecoration(new RecycleViewDivider(getContext(),LinearLayoutManager.HORIZONTAL,1,Color.BLACK));
        return view;
    }

    //组图的显示状态
    private int groupImageViewState = LIST_STATE;
    private final static int LIST_STATE = 0;
    private final static int GRID_STATE = 1;

    @OnClick(R.id.ib_pic_type)
    public void switchListGridState(View view){
        if(groupImageViewState == LIST_STATE){
            groupImageViewState = GRID_STATE;
            ibPicType.setBackgroundResource(R.drawable.icon_pic_list_type);
            rvGroupImageView.setLayoutManager(glm);
            rvGroupImageView.addItemDecoration(new RecycleViewDivider(getContext(),GridLayoutManager.VERTICAL,1,Color.BLACK));
        }else{
            groupImageViewState = LIST_STATE;
            ibPicType.setBackgroundResource(R.drawable.icon_pic_grid_type);
            rvGroupImageView.setLayoutManager(llm);
            rvGroupImageView.addItemDecoration(new RecycleViewDivider(getContext(),LinearLayoutManager.HORIZONTAL,1,Color.BLACK));
        }
    }
}
