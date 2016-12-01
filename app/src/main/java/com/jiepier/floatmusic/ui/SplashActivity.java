package com.jiepier.floatmusic.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.jiepier.floatmusic.R;
import com.jiepier.floatmusic.base.App;
import com.jiepier.floatmusic.service.PlayService;

/**
 * Created by JiePier on 16/11/13.
 */
public class SplashActivity extends AppCompatActivity {

	public static final int REQUEST_CODE = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// no title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.splash_layout);


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
				&& ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
					REQUEST_CODE);
			return;
		}else {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					startActivity(new Intent(SplashActivity.this, MainActivity.class));
					finish();
				}
			}, 2000);
		}
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// Permission granted.
				requestAlertWindowPermission();
			} else {
				// User refused to grant permission.
				Toast.makeText(this,"请先给予读写权限，否则app没法用啊",Toast.LENGTH_LONG).show();
			}
		}

	}

	private static final int MY_REQUEST_CODE = 1;
	private  void requestAlertWindowPermission() {
		Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
		intent.setData(Uri.parse("package:" + getPackageName()));
		startActivityForResult(intent, MY_REQUEST_CODE);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == MY_REQUEST_CODE) {
			if (Settings.canDrawOverlays(this)) {
				//Log.i(LOGTAG, "onActivityResult granted");
				//Log.w("haha","!!!");
				startService(new Intent(this,PlayService.class));
				startActivity(new Intent(this, MainActivity.class));
				finish();
			}
		}else {
			Toast.makeText(App.sContext,"不给权限不给进，大哥",Toast.LENGTH_SHORT).show();
		}
	}
}
