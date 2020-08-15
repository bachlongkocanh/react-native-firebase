package io.invertase.firebase.messaging;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManager;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.google.firebase.messaging.RemoteMessage;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

import io.invertase.firebase.common.ReactNativeFirebaseJSON;

import javax.annotation.Nullable;

public class ReactNativeFirebaseMessagingHeadlessService extends HeadlessJsTaskService {
  private static final long TIMEOUT_DEFAULT = 60000;
  private static final String TIMEOUT_JSON_KEY = "messaging_android_headless_task_timeout";
  private static final String TASK_KEY = "ReactNativeFirebaseMessagingHeadlessTask";
  public  static  Activity activity;
  @Override
  protected @Nullable
  HeadlessJsTaskConfig getTaskConfig(Intent intent) {
    Bundle extras = intent.getExtras();
    if (extras == null) return null;
    RemoteMessage remoteMessage = intent.getParcelableExtra("message");
    ShowWhenLockApp(intent);
    return new HeadlessJsTaskConfig(
      TASK_KEY,
      ReactNativeFirebaseMessagingSerializer.remoteMessageToWritableMap(remoteMessage),
      ReactNativeFirebaseJSON.getSharedInstance().getLongValue(TIMEOUT_JSON_KEY, TIMEOUT_DEFAULT),
      // Prevents race condition where the user opens the app at the same time as a notification
      // is delivered, causing a crash.
      true
    );
  }
  //Auto open app when app closed, killed, or device locked.
  // Create function onCreate on MainActivity.java
  // setFags to accecpt app open over lock screen.
  // activity.getWindow().addFlags(
  // WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
  // WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
  // WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCED|
  // WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
  // )
  @SuppressLint("WrongConstant")
  private void ShowWhenLockApp(Intent myIntent){
    try {
      KeyguardManager myKM = (KeyguardManager) getBaseContext().getSystemService(Context.KEYGUARD_SERVICE);
      if( myKM.inKeyguardRestrictedInputMode()) {
        Log.e("Check lock","DEVICE LOCK");
        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent launchIntent = packageManager.getLaunchIntentForPackage("com.checkserver");
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launchIntent.addFlags(
          WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED +
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD +
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON +
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
          getApplicationContext().startActivity(launchIntent);
      } else {
        //it is not locked
        Log.e("Check lock","DEVICE NOT LOCK");
        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent launchIntent = packageManager.getLaunchIntentForPackage("com.checkserver");
        getApplicationContext().startActivity(launchIntent);
      }
    }
    catch (Exception e){
      Log.e("error",e.getMessage());
    }
  }

}


