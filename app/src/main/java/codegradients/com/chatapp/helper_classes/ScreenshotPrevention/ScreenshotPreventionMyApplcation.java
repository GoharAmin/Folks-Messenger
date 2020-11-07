package codegradients.com.chatapp.helper_classes.ScreenshotPrevention;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import codegradients.com.chatapp.MyAidl;
import codegradients.com.chatapp.helper_classes.ScreenshotPrevention.service.ASS;

/**
 * Created by soochun on 2017-01-16.
 */
public class ScreenshotPreventionMyApplcation extends Application {
    private static final String TAG = ScreenshotPreventionMyApplcation.class.getSimpleName();
    private ServiceConnection mConnection;
    private MyAidl mServiceBinder;

    private static ScreenshotPreventionMyApplcation instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e(TAG, "onServiceConnected");
                mServiceBinder = MyAidl.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e(TAG, "onServiceDisconnected");
            }
        };
        Intent intent = new Intent(instance, ASS.class);
        intent.setAction("abc.def.ghi");
        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public static ScreenshotPreventionMyApplcation getInstance() {
        return instance;
    }

    public void registerScreenshotObserver() {
        try {
            if (mServiceBinder == null) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            int count = 0;
                            while (count != 10) {
                                Thread.sleep(400);
                                if (mServiceBinder != null) {
                                    mServiceBinder.registerScreenShotObserver();
                                    break;
                                }
                                count++;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString(), e);
                            e.printStackTrace();
                        }
                    }
                }.start();
            } else {
                mServiceBinder.registerScreenShotObserver();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            e.printStackTrace();
        }
    }

    public void unregisterScreenshotObserver() {
        try {
            if (mServiceBinder == null) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            int count = 0;
                            while (count != 10) {
                                Thread.sleep(400);
                                if (mServiceBinder != null) {
                                    mServiceBinder.unregisterScreenShotObserver();
                                    break;
                                }
                                count++;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString(), e);
                            e.printStackTrace();
                        }
                    }
                }.start();
            } else {
                mServiceBinder.unregisterScreenShotObserver();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            e.printStackTrace();
        }
    }

    public void allowUserSaveScreenshot(boolean enable){
        try {
            mServiceBinder.setScreenShotEnable(enable);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
