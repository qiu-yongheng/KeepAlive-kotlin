// DeamonConnection.aidl
package com.qyh.keepalivekotlin;

// Declare any non-default types here with import statements

interface DaemonConnection {
    String getProcessName();
    void startRunTimer();
    void stopRunTimer();
}
