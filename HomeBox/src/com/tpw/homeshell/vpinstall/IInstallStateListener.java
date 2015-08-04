package com.tpw.homeshell.vpinstall;

import com.tpw.homeshell.vpinstall.VPInstaller.AppKey;

public interface IInstallStateListener {
    public abstract void onInstallStateChange(AppKey appKey, int state);
}