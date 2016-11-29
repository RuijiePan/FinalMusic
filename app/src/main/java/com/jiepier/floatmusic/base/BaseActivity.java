package com.jiepier.floatmusic.base;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.jiepier.floatmusic.service.FxService;
import com.jiepier.floatmusic.service.PlayService;
import com.jiepier.floatmusic.util.AppManager;
import com.jiepier.floatmusic.util.MusicUtil;
import com.jiepier.floatmusic.util.ResourceUtil;
import com.jiepier.floatmusic.util.StatusBarUtil;

import butterknife.ButterKnife;

/**
 * Created by JiePier on 16/11/12.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected PlayService mPlayService;
    protected FxService mFxService;
    private final String TAG = BaseActivity.class.getSimpleName();
    private boolean isBound = false;
    public boolean isFirst = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(initContentView());
        ButterKnife.bind(this);
        setTranslucentStatus(isApplyStatusBarTranslucency());
        setStatusBarColor(isApplyStatusBarColor());
        initUiAndListener();
        AppManager.getAppManager().addActivity(this);
    }

    /**
     * 设置view
     */
    public abstract int initContentView();

    /**
     * init UI && Listener
     */
    public abstract void initUiAndListener();

    /**
     * is applyStatusBarTranslucency
     */
    protected abstract boolean isApplyStatusBarTranslucency();

    /**
     * set status bar translucency
     */
    protected void setTranslucentStatus(boolean on) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if (on) {
                winParams.flags |= bits;
            } else {
                winParams.flags &= ~bits;
            }
            win.setAttributes(winParams);
        }
    }

    protected abstract boolean isApplyStatusBarColor();

    /**
     * use SystemBarTintManager
     */
    public void setStatusBarColor(boolean on) {
        if (on) {
            StatusBarUtil.setColor(this, ResourceUtil.getThemeColor(this), 0);
        }
    }

    public void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    public int getStatusBarHeight() {
        return ResourceUtil.getStatusBarHeight(this);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override protected void onDestroy() {
        AppManager.getAppManager().finishActivity(this);
        super.onDestroy();
    }

    public void allowBindService(){
        getApplicationContext().bindService(new Intent(this,PlayService.class),
                mPlayServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void allowUnBindService(){
        getApplicationContext().unbindService(mPlayServiceConnection);
    }

    public void BindFxService(){
        isBound = getApplicationContext().bindService(new Intent(this,FxService.class),
                mFxServiceConnection, Context.BIND_AUTO_CREATE);
        //Log.w("haha",isBound+"");
    }

    public void unBindFxService(){
        if (isBound) {
            isBound = false;
            isFirst = true;
            getApplicationContext().unbindService(mFxServiceConnection);
            stopService(new Intent(this,FxService.class));
        }
    }

    public PlayService getPlayService(){
        return mPlayService;
    }

    private ServiceConnection mPlayServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mPlayService = ((PlayService.PlayBinder) iBinder).getService();
            mPlayService.setOnMusicEventListener(mMusicEventListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mPlayService = null;
        }
    };

    private ServiceConnection mFxServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mFxService = ((FxService.FxBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mFxService = null;
        }
    };

    public FxService getFxService(){
        return mFxService;
    }

    private PlayService.OnMusicEventListener mMusicEventListener = new PlayService.OnMusicEventListener() {
        @Override
        public void onPublish(int percent) {
            BaseActivity.this.onPublish(percent*100/ MusicUtil.sMusicList.get(mPlayService.getPlayingPosition()).getDuration());
        }

        @Override
        public void onChange(int position) {
            BaseActivity.this.onChange(position);
        }
    };

    public abstract void onPublish(int percent);

    public abstract void onChange(int position);
}
