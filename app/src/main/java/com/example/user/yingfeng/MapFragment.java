package com.example.user.yingfeng;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.map.MapView;

/**
 * Created by user on 2016/9/27.
 */

public class MapFragment extends Fragment {
    static MapView mMapView = null;
    private MapController mMapController = null;
    LocationClient mLocClient;

    public MyLocationListener myListener = new MyLocationListener();
    MyLocationOverlay myLocationOverlay = null;
    LocationData locDate = null;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DemoApplication app = (DemoApplication) getActivity().getApplication();

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        return v;
    }
}
