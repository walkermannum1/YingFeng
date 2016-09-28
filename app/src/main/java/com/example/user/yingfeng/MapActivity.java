package com.example.user.yingfeng;

import android.support.v4.app.Fragment;
import android.os.Bundle;

public class MapActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return MapFragment.newInstance();
    }
}
