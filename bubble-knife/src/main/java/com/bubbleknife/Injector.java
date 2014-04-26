package com.bubbleknife;


import android.app.Activity;

import java.lang.reflect.Method;

public class Injector {
    private Injector() {

    }

    public static void inject(Activity activity) {
        try {
            Class<?> injector = Class.forName(activity.getClass().getName() + InjectorProcessor.SUFFIX);
            Method inject = injector.getMethod("inject", activity.getClass());
            inject.invoke(null, activity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("hmm something wrong! " + e);
        }
    }



}
