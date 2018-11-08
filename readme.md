## 我们来看下效果
 ![在这里插入图片描述](https://img-blog.csdnimg.cn/201811081430042.gif)

在这里，我们实现的是，一个ViewPager来显示图片列表。这里一个页面，ViewPage展示了前后的预览，我们让预览页进行Y轴的压缩，并设置透明度为0.5f,所有我们看到gif最后，左右两边的图片有点朦胧感。让预览页和主页面有主从感。我们用分析代码，一一实现上面的效果。

## 第一步
新建一个Activity

```
public class GalleryActivity extends Activity {
    ViewPager viewPager ;
    GalleryAdapter adapter ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        viewPager = (ViewPager) findViewById(R.id.vp_gallery);
        adapter = new GalleryAdapter(this);
        viewPager.setAdapter(adapter);
    }
}
```
GalleryAdapter 的代码

```
public class GalleryAdapter extends PagerAdapter {

    int[] imgs = {R.mipmap.main_0n, R.mipmap.main_1n, R.mipmap.main_2n};
    private Context mContext;

    public GalleryAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView iv = new ImageView(container.getContext());
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        iv.setBackground(mContext.getDrawable(imgs[position % 3]));
        container.addView(iv);
        return iv;
    }

    @Override
    public int getCount() {
        return imgs.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
            return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }
}

```
布局文件代码

```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <android.support.v4.view.ViewPager
        android:id="@+id/vp_gallery"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

</FrameLayout>
```

以上代码相信各位都有写过，一个简单的图片浏览ViewPager,就不赘述了。现在我们将代码改成为可以显示三个页面，一个主页面和前后两个预览页，并设置预览页进行Y轴压缩和透明度为0.5f.

```
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

```
布局文件的修改也很重要

```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:clipChildren="false">

    <android.support.v4.view.ViewPager
        android:id="@+id/vp_gallery"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
    />

</FrameLayout>
```

## 分析代码：
我们给ViewPager 设置左右 Padding.并通过 viewPager.setClipToPadding(false)让viewPager不对其子控件进行Padding部分的裁剪。这样每一个ViewPager就有Padding部分来预览左右的内容，但是这样还不够，我们还需要设置FrameLayout 的  android:clipChildren="false"，这个属性是代表FrameLayout 允许子控件的内容超出其内容，不对超出的部分进行裁剪。这样我们就可以得到左右两边的预览部分了。接着，我们再来看

```
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
```
setPageTransformer这个方法是用来设置ViewPager的转场动画，
可以设置透明度，XY轴缩进等，我们主要了解这个position值，当向后滑动一个页面，他的变化为0->1,当向前滑动时候，他的变化为-1->0;

现在是不是有点酷炫了。但是如果滑动过快的话，这些变化并不是很明显，我们想让ViewPager 的切换动画时间久一点，再久一点！
我们去看下ViewPager 的源码，发现有个smoothScrollTo方法

```
/**
     * Like {@link View#scrollBy}, but scroll smoothly instead of immediately.
     *
     * @param x the number of pixels to scroll by on the X axis
     * @param y the number of pixels to scroll by on the Y axis
     * @param velocity the velocity associated with a fling, if applicable. (0 otherwise)
     */
    void smoothScrollTo(int x, int y, int velocity) {
        if (getChildCount() == 0) {
            // Nothing to do.
            setScrollingCacheEnabled(false);
            return;
        }

        int sx;
        boolean wasScrolling = (mScroller != null) && !mScroller.isFinished();
        if (wasScrolling) {
            // We're in the middle of a previously initiated scrolling. Check to see
            // whether that scrolling has actually started (if we always call getStartX
            // we can get a stale value from the scroller if it hadn't yet had its first
            // computeScrollOffset call) to decide what is the current scrolling position.
            sx = mIsScrollStarted ? mScroller.getCurrX() : mScroller.getStartX();
            // And abort the current scrolling.
            mScroller.abortAnimation();
            setScrollingCacheEnabled(false);
        } else {
            sx = getScrollX();
        }
        int sy = getScrollY();
        int dx = x - sx;
        int dy = y - sy;
        if (dx == 0 && dy == 0) {
            completeScroll(false);
            populate();
            setScrollState(SCROLL_STATE_IDLE);
            return;
        }

        setScrollingCacheEnabled(true);
        setScrollState(SCROLL_STATE_SETTLING);

        final int width = getClientWidth();
        final int halfWidth = width / 2;
        final float distanceRatio = Math.min(1f, 1.0f * Math.abs(dx) / width);
        final float distance = halfWidth + halfWidth
                * distanceInfluenceForSnapDuration(distanceRatio);

        int duration;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            final float pageWidth = width * mAdapter.getPageWidth(mCurItem);
            final float pageDelta = (float) Math.abs(dx) / (pageWidth + mPageMargin);
            duration = (int) ((pageDelta + 1) * 100);
        }
        duration = Math.min(duration, MAX_SETTLE_DURATION);

        // Reset the "scroll started" flag. It will be flipped to true in all places
        // where we call computeScrollOffset().
        mIsScrollStarted = false;
        mScroller.startScroll(sx, sy, dx, dy, duration);
        ViewCompat.postInvalidateOnAnimation(this);
    }
```

重点注意mScroller.startScroll(sx, sy, dx, dy, duration)这行代码，这个duration是控制整个页面切换时间的关键。我们再来看看mScroller

```
   private Scroller mScroller;
```
说明在ViewPager 中这是受私有保护的，但是我们可以通过反射替换掉系统的这个参数。

```
try {
            // 通过class文件获取mScroller属性
            Field mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);
            GalleryViewPagerScroller mScroller = new GalleryViewPagerScroller(mViewPager.getContext(),new AccelerateInterpolator());
            mField.set(mViewPager, mScroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
```
以下是GalleryViewPagerScroller的代码

```
public class GalleryViewPagerScroller extends Scroller {
    private int mDuration = 800;

    public GalleryViewPagerScroller(Context context) {
        super(context);
    }

    public GalleryViewPagerScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    public int getmDuration() {
        return mDuration;
    }

    public void setmDuration(int time) {
        mDuration = time;
    }
}

```
所以，整个过程就是，通过重写startScroll方法设置mDuration为我们想要的时间 ，并实例化一个Scroller 对象，并通过反射把ViewPager 的
私有属性mScroller给替换掉。这样，我们就可以设置ViewPager 转场动画的延迟了.可以实现一些酷炫的ViewPager效果了,赶快用上吧

<h3>写
在最后</h3>

**纯手打,如果你觉得有用,请帮我点赞或者转发!!!!!!!!!!**

[附上代码下载地址](https://github.com/wzyitspider/Android_Gallery)


<blockquote>
  <p>作者：王宗耀 欢迎转载，与人分享是进步的源泉！ <br>
  转载请保留原文地址：<a href="http://blog.csdn.net/wzy901213" target="_blank" rel="nofollow">http://blog.csdn.net/wzy901213</a></p>
</blockquote>
<p><img alt="在这里插入图片描述" src="https://img-blog.csdn.net/20180928104846100?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3d6eTkwMTIxMw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70"></p>






