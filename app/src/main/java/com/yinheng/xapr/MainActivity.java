package com.yinheng.xapr;

import android.app.Activity;
import android.os.Bundle;

import com.yinheng.xapr.provider.Bases;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bases.sendPackageIgnoreBroadcast(this, "this.is.a.test");
    }
}
