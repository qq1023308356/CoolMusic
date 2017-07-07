package me.cool.music.utils.permission;

public interface PermissionResult {
    void onGranted();

    void onDenied();
}
