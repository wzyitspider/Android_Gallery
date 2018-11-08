package org.jimmy;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.widget.FrameLayout;


/**
 * Time:2018/11/8
 * Author:Jimmy Wang
 * Email:wzy901213@163.com
 * Blog:https://blog.csdn.net/wzy901213
 * Description:
 */
public class GalleryActivity extends Activity {
    ViewPager viewPager;
    GalleryAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        viewPager = (ViewPager) findViewById(R.id.vp_gallery);
        adapter = new GalleryAdapter(this);
        viewPager.setAdapter(adapter);
        //是否对padding进行裁剪
        viewPager.setClipToPadding(false);
        int itemWidth = (getResources().getDisplayMetrics().widthPixels) / 3;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) viewPager.getLayoutParams();
        layoutParams.leftMargin = itemWidth / 2;
        layoutParams.rightMargin = itemWidth / 2;
        //设置页面的左右padding
        viewPager.setLayoutParams(layoutParams);
        //设置预加载为3
        viewPager.setOffscreenPageLimit(3);
        //设置页面之间的margin为0
        viewPager.setPageMargin(0);
        viewPager.setPageTransformer(true, (view, position) -> {
            if (position < 0) {
                view.setScaleY(0.2f * position + 1);
                view.setAlpha(1f + 0.5f * position);
            } else if (position < 1) {
                view.setAlpha(1f - 0.5f * position);
                view.setScaleY(-0.2f * position + 1);
            } else {
                view.setAlpha(0.3f);
                view.setScaleY(0.8f);
            }
        });
    }
}
