package com.jiepier.floatmusic.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jiepier.floatmusic.R;
import com.jiepier.floatmusic.base.App;
import com.jiepier.floatmusic.base.BaseActivity;
import com.jiepier.floatmusic.bean.ClickEvent;
import com.jiepier.floatmusic.bean.Music;
import com.jiepier.floatmusic.util.ImageTools;
import com.jiepier.floatmusic.util.MusicIconLoader;
import com.jiepier.floatmusic.util.MusicUtil;
import com.jiepier.floatmusic.util.PlayBgShape;
import com.jiepier.floatmusic.util.PlayPageTransformer;
import com.jiepier.floatmusic.widget.CDView;
import com.jiepier.floatmusic.widget.LrcView;
import com.jiepier.floatmusic.widget.PagerIndicator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * Created by JiePier on 16/12/1.
 */
public class MusicActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout mPlayContainer;
    private ImageView mPlayBackImageView; // back button
    private TextView mMusicTitle; // music title
    private ViewPager mViewPager; // cd or lrc
    private CDView mCdView; // cd
    private SeekBar mPlaySeekBar; // seekbar
    private ImageButton mStartPlayButton; // start or pause
    private TextView mSingerTextView; // singer
    private LrcView mLrcViewOnFirstPage; // single line lrc
    private LrcView mLrcViewOnSecondPage; // 7 lines lrc
    private PagerIndicator mPagerIndicator; // indicator
    private int mPosition;

    // cd view and lrc view
    private ArrayList<View> mViewPagerContent = new ArrayList<View>(2);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public int initContentView() {
        return R.layout.play_activity_layout;
    }

    @Override
    public void initUiAndListener() {
        setupViews();

        mPosition = getIntent().getIntExtra("position",-1);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                    R.drawable.music);
        mCdView.setImage(ImageTools.scaleBitmap(bmp,
                (int) (App.sScreenWidth * 0.8)));
        mCdView.start();
        setBackground(mPosition);

        Music music = MusicUtil.sMusicList.get(mPosition);
        mSingerTextView.setText(music.getArtist());
        mMusicTitle.setText(music.getTitle());
        mStartPlayButton
                .setImageResource(R.drawable.player_btn_pause_normal);
        mPlaySeekBar.setMax(music.getDuration());
    }

    @Override
    protected boolean isApplyStatusBarTranslucency() {
        return true;
    }

    @Override
    protected boolean isApplyStatusBarColor() {
        return false;
    }

    /**
     * 初始化view
     */
    private void setupViews() {
        mPlayContainer = (LinearLayout) findViewById(R.id.ll_play_container);
        mPlayBackImageView = (ImageView) findViewById(R.id.iv_play_back);
        mMusicTitle = (TextView) findViewById(R.id.tv_music_title);
        mViewPager = (ViewPager) findViewById(R.id.vp_play_container);
        mPlaySeekBar = (SeekBar) findViewById(R.id.sb_play_progress);
        mStartPlayButton = (ImageButton) findViewById(R.id.ib_play_start);
        mPagerIndicator = (PagerIndicator) findViewById(R.id.pi_play_indicator);

        // 动态设置seekbar的margin
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) mPlaySeekBar
                .getLayoutParams();
        p.leftMargin = (int) (App.sScreenWidth * 0.1);
        p.rightMargin = (int) (App.sScreenWidth * 0.1);

        mPlaySeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);

        initViewPagerContent();
        // 设置viewpager的切换动画
        mViewPager.setPageTransformer(true, new PlayPageTransformer());
        mPagerIndicator.create(mViewPagerContent.size());
        mViewPager.setOnPageChangeListener(mPageChangeListener);
        mViewPager.setAdapter(mPagerAdapter);

        mPlayBackImageView.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        allowBindService();
    }

    @Override
    protected void onPause() {
        allowUnBindService();
        super.onPause();
    }

    private ViewPager.OnPageChangeListener mPageChangeListener =
            new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    if (position == 0) {
                        if (mPlayService.isPlaying())
                            mCdView.start();
                    } else {
                        mCdView.pause();
                    }
                    mPagerIndicator.current(position);
                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {
                }

                @Override
                public void onPageScrollStateChanged(int arg0) {
                }
            };

    /**
     * 拖动进度条
     */
    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                    mPlayService.seek(progress);
                    mLrcViewOnFirstPage.onDrag(progress);
                    mLrcViewOnSecondPage.onDrag(progress);
                    //Log.w("haha",progress*MusicUtil.sMusicList.get(mPlayService.getPlayingPosition()).getDuration()/100+"");
                }
            };

    private PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return mViewPagerContent.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        /**
         * 该方法是PagerAdapter的预加载方法，系统调用 当显示第一个界面时，
         * 第二个界面已经预加载，此时调用的就是该方法。
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewPagerContent.get(position));
            return mViewPagerContent.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }
    };

    /**
     * 初始化viewpager的内容
     */
    private void initViewPagerContent() {
        View cd = View.inflate(this, R.layout.play_pager_item_1, null);
        mCdView = (CDView) cd.findViewById(R.id.play_cdview);
        mSingerTextView = (TextView) cd.findViewById(R.id.play_singer);
        mLrcViewOnFirstPage = (LrcView) cd.findViewById(R.id.play_first_lrc);

        View lrcView = View.inflate(this, R.layout.play_pager_item_2, null);
        mLrcViewOnSecondPage = (LrcView) lrcView
                .findViewById(R.id.play_first_lrc_2);

        mViewPagerContent.add(cd);
        mViewPagerContent.add(lrcView);
    }

    @SuppressWarnings("deprecation")
    private void setBackground(int position) {
        Music currentMusic = MusicUtil.sMusicList.get(position);
        Bitmap bgBitmap = MusicIconLoader.getInstance().load(
                currentMusic.getImage());
        if (bgBitmap == null) {
            bgBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_launcher);
        }
        mPlayContainer.setBackgroundDrawable(
                new ShapeDrawable(new PlayBgShape(bgBitmap)));
    }

    /**
     * 上一曲
     *
     * @param view
     */
    public void pre(View view) {
        mPlayService.pre(); // 上一曲
    }

    /**
     * 播放 or 暂停
     *
     * @param view
     */
    public void play(View view) {
        if (mPlayService.isPlaying()) {
            mPlayService.pause(); // 暂停
            mCdView.pause();
            mStartPlayButton
                    .setImageResource(R.drawable.player_btn_play_normal);
        } else {
            onPlay(mPlayService.resume()); // 播放
        }
    }

    /**
     * 上一曲
     *
     * @param view
     */
    public void next(View view) {
        mPlayService.next(); // 上一曲
    }

    /**
     * 播放时调用 主要设置显示当前播放音乐的信息
     *
     * @param position
     */
    private void onPlay(int position) {
        Music music = MusicUtil.sMusicList.get(position);

        mMusicTitle.setText(music.getTitle());
        mSingerTextView.setText(music.getArtist());
        mPlaySeekBar.setMax(music.getDuration());
        Bitmap bmp = MusicIconLoader.getInstance().load(music.getImage());
        if (bmp == null)
            bmp = BitmapFactory.decodeResource(getResources(),
                    R.drawable.music);
        mCdView.setImage(ImageTools.scaleBitmap(bmp,
                (int) (App.sScreenWidth * 0.8)));

        if (mPlayService.isPlaying()) {
            mCdView.start();
            mStartPlayButton
                    .setImageResource(R.drawable.player_btn_pause_normal);
        } else {
            mCdView.pause();
            mStartPlayButton
                    .setImageResource(R.drawable.player_btn_play_normal);
        }
    }

    private void setLrc(int position) {
        Music music = MusicUtil.sMusicList.get(position);
        String lrcPath = MusicUtil.getLrcDir() + music.getTitle() + ".lrc";
        mLrcViewOnFirstPage.setLrcPath(lrcPath);
        mLrcViewOnSecondPage.setLrcPath(lrcPath);
    }

    @Override
    public void onPublish(int progress) {
        mPlaySeekBar.setProgress(progress*MusicUtil.sMusicList.get(mPlayService.getPlayingPosition()).getDuration()/100);
        if (mLrcViewOnFirstPage.hasLrc())
            mLrcViewOnFirstPage.changeCurrent(progress);
        if (mLrcViewOnSecondPage.hasLrc())
            mLrcViewOnSecondPage.changeCurrent(progress);
    }

    @Override
    public void onChange(int position) {
        setBackground(position);
        onPlay(position);
        setLrc(position);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play_back:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ClickEvent event) {
        onPlay(mPlayService.getPlayingPosition());
    };
}
