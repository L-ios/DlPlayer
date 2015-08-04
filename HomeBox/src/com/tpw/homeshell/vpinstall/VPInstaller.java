package com.tpw.homeshell.vpinstall;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.tpw.homeshell.ShortcutInfo;
import com.tpw.homeshell.UserTrackerHelper;
import com.tpw.homeshell.UserTrackerMessage;

import android.content.Context;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class VPInstaller {

    public static final int INSTALL_NOT_SET = 9998;
    public static final int INSTALL_NOT_INSTALLED = 9999;
    public static final int INSTALLING = 10000;
    public static final int INSTALLED = 10001;
    public static final int INSTALL_FAILED = 10002;

    private static final String TAG = "VPInstaller";

    private Context context;
    private IInstallStateListener listener;

    public VPInstaller(Context context) {
        this.context = context;
        this.listener = null;
    }

    public VPInstaller(Context context, IInstallStateListener listener) {
        this.context = context;
        this.listener = listener;
    }

    static public class AppKey {
        public String packageName = null;
        public AppKey(String packageName) {
            this.packageName = packageName;
        }
        public String asString() {
            return packageName;
        }
    }

    private void sendInstallState(AppKey key, int state) {
        if (listener == null) return;
        listener.onInstallStateChange(key, state);
    }

    public boolean installSilently(String apkPath, ShortcutInfo shortcutInfo) {
        if (TextUtils.isEmpty(apkPath)) return false;

        Uri mPackageURI = Uri.fromFile(new File(apkPath));
        int installFlags = 0;

        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (info == null) {
            //Toast.makeText(context, "can't get package info", Toast.LENGTH_SHORT).show();
            sendInstallState(new AppKey(shortcutInfo.intent.getStringExtra(VPUtils.TYPE_PACKAGENAME)), INSTALL_FAILED);
            return false;
        }
        String packageName = info.packageName;

        boolean succeed = true;

        PackageInstallObserver obs = null;
        try {
            sendInstallState(new AppKey(packageName), INSTALLING);

            obs = new PackageInstallObserver();
            // installPackage is a async call, we use obs.wait() to make installSilently a sync call
            installPackage(pm, mPackageURI, obs, 0, info.packageName);
            synchronized (obs) {
                obs.wait();
            }
        } catch (ClassNotFoundException e) {
            succeed = false;
        } catch (NoSuchMethodException e) {
            succeed = false;
        } catch (IllegalArgumentException e) {
            succeed = false;
        } catch (IllegalAccessException e) {
            succeed = false;
        } catch (InvocationTargetException e) {
            succeed = false;
        } catch (InterruptedException e) {
            succeed = false;
        } finally {
            if (obs != null && obs.finished) {
                Log.d(TAG, "set succeed");
                succeed = (obs.result == VPInstallerConst.INSTALL_SUCCEEDED);
            }
            sendInstallState(new AppKey(packageName), succeed ? INSTALLED : INSTALL_FAILED);
        }
        return succeed;
    }

    public void installSilently(String apkPath, ShortcutInfo shortcutInfo, IInstallStateListener listener) {
        if (TextUtils.isEmpty(apkPath)) return;
        this.listener = listener;
        installSilently(apkPath, shortcutInfo);
    }

    //private static void installPackage(android.content.pm.PackageManager pm, Uri uri, IPackageInstallObserver obs, int flags, String packageName)
    private static void installPackage(android.content.pm.PackageManager pm, Uri uri, IPackageInstallObserver obs, int flags, String packageName)
            throws ClassNotFoundException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        // we use reflection to invoke the method below
        // public abstract void installPackage(Uri packageURI, IPackageInstallObserver observer, int flags, String installerPackageName);
        UserTrackerHelper.sendUserReport(
                UserTrackerMessage.MSG_VP_ITEM_INSTALL, packageName);
        Class<?> clz = clz("android.content.pm.PackageManager");
        Class<?>[] paramsType = new Class[4];
        paramsType[0] = Uri.class;
        paramsType[1] = IPackageInstallObserver.class;
        paramsType[2] = int.class;
        paramsType[3] = String.class;

        Method method = clz.getMethod("installPackage", paramsType);
        Object result = method.invoke(pm, uri, obs, flags, packageName);
    }

    private static Class<?> clz(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    private static Object executeMethod(Object ownerObj, String className, String methodName, Object... params) {
        Object resObj = null;
        try {
            Class<?> clz = clz(className);
            Class<?>[] paramsType = new Class[params.length];

            for (int i = 0; i < params.length; i++) {
                paramsType[i] = params[i].getClass();
                if (paramsType[i].getSimpleName().contains("Integer")) {
                    paramsType[i] = int.class;
                }
                if (paramsType[i].getSimpleName().contains("Uri")) {
                    paramsType[i] = Uri.class;
                }
                if (paramsType[i].getSimpleName().contains("PackageInstallObserver")) {
                    paramsType[i] = IPackageInstallObserver.class;
                }
            }

            Method method = clz.getMethod(methodName, paramsType);
            resObj = method.invoke(ownerObj, params);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return resObj;
    }

    static class PackageInstallObserver extends android.content.pm.IPackageInstallObserver.Stub {
        boolean finished;
        int result;

        public void packageInstalled(String name, int status) {
            synchronized (this) {
                notifyAll();
            }
            Log.d(TAG, "packageInstalled status is " + status);
            if (status == VPInstallerConst.INSTALL_FAILED_ALREADY_EXISTS) {
                Log.d(TAG, "INSTALL_FAILED_ALREADY_EXISTS");
            }
            finished = true;
            result = status;
        }
    }
}
