package com.example.hyejade;

import android.app.Application;
import android.content.Context;
import io.objectbox.BoxStore;

import com.example.hyejade.data.MyObjectBox;

public class App extends Application {
    private static BoxStore store;
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this; // ✅ Application 인스턴스 보관
        store = MyObjectBox.builder()
                .androidContext(this)
                .build();
    }

    public static BoxStore getStore() {
        return store;
    }

    // ✅ 정적 context() 메서드 추가
    public static Context context() {
        return instance.getApplicationContext();
    }
}