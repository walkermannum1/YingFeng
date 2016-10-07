package com.example.user.yingfeng;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;

/**
 * Created by guang on 2016/10/7.
 */

public abstract class MapBaseFragment extends ABaseFragment {
    //地图相关
    private MapView mMapView = null;
    protected BaiduMap mBaiduMap;

    //定位相关
    private BitmapDescriptor mIconMaker;
    private LocationService locService;
    protected BDLocation myLocation = MapApplication.bdLocation;

    @Override
    protected int inflateContentView() {
        return R.layout.comm_lay_base_map;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);
        mMapView = (MapView) findViewById(R.id.bmapView);
        //隐藏缩放按键
        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();
        mIconMaker = BitmapDescriptorFactory.fromResource(R.drawable.btu_gps_map);
        //地图显示的比例范围
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15.0f));
        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                onMapLoadFinish();
            }
        });
        if(canShowInfoWindow()){
            initMarkerClickEvent();
            initMapClickEvent();
        }
        if(canShowMyLocation()){
            initMyLocation();
            findViewById(R.id.view_my_location).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMyLocation();
                }
            });
        }
    }

    private void initMapClickEvent()
    {
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {

            @Override
            public boolean onMapPoiClick(MapPoi arg0) {
                return false;
            }

            @Override
            public void onMapClick(LatLng arg0) {
                mBaiduMap.hideInfoWindow();
            }
        });
    }

    private void initMarkerClickEvent()
    {
        // 对Marker的点击
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                // 获得marker中的数据
                final AddrInfo info = (AddrInfo) marker.getExtraInfo().get("markerInfo");

                InfoWindow mInfoWindow;
                // 将marker所在的经纬度的信息转化成屏幕上的坐标
                final LatLng ll = marker.getPosition();
                Point p = mBaiduMap.getProjection().toScreenLocation(ll);
                p.y -= 47;
                LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);
                mInfoWindow = new InfoWindow(InfoWindowView(info), llInfo,-15);
                mBaiduMap.showInfoWindow(mInfoWindow);
                return true;
            }
        });
    }

    public void addInfoOverlay(AddrInfo info){
        addInfosOverlay(Arrays.asList(info));
    }

    /**
     * 在百度地图图层添加热点覆盖物
     */
    public void addInfosOverlay(List<AddrInfo> infos)
    {
        mBaiduMap.clear();
        LatLng latLng = null;
        OverlayOptions overlayOptions = null;
        Marker marker = null;
        for (AddrInfo info : infos)
        {
            // 位置
            latLng = new LatLng(info.getLatitude(), info.getLongitude());
            // 图标
            overlayOptions = new MarkerOptions().position(latLng)
                    .icon(mIconMaker).zIndex(5);
            marker = (Marker) (mBaiduMap.addOverlay(overlayOptions));
            Bundle bundle = new Bundle();
            bundle.putSerializable("markerInfo", info);
            marker.setExtraInfo(bundle);
        }
        // 将地图移到到最后一个经纬度位置
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.setMapStatus(u);
    }

    private void initMyLocation(){
        locService = new LocationService(getActivity());
        LocationClientOption mOption = locService.getDefaultLocationClientOption();
        mOption.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        mOption.setCoorType("bd09ll");
        locService.setLocationOption(mOption);
        locService.registerListener(listener);
        locService.start();
    }

    /***
     * 定位结果回调，在此方法中处理定位结果
     */
    BDLocationListener listener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || location.getLatitude() == 4.9E-324 || mMapView == null) {
                return;
            }
            myLocation = location;
            MapApplication.bdLocation = location;
            MyLocationData locData = new MyLocationData.Builder()
                    //去掉光圈
//                    .accuracy(location.getRadius())
                    .accuracy(0)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
        }
    };

    private void showMyLocation(){
        LatLng ll = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15.0f));
        mBaiduMap.animateMapStatus(u);
    }

    public void useOtherMap(AddrInfo addrInfo){
        useOtherMap(addrInfo.getLatitude(), addrInfo.getLongitude(), addrInfo.getName());
    }

    /**
     * 传入的坐标为百度坐标
     * @param latitude
     * @param longitude
     * @param name
     */
    public void useOtherMap(double latitude,double longitude,String name){
        Gps gps = PositionUtil.bd09_To_Gps84(latitude, longitude);
        Uri mUri = Uri.parse(String.format("geo:%s,%s?q=%s",gps.getWgLat(),gps.getWgLon(),name));
        Intent mIntent = new Intent(Intent.ACTION_VIEW,mUri);
        try {
            startActivity(mIntent);
        }catch (Exception e){
            ToastUtil.showMsg("找不到手机地图软件。");
        }
    }

    /**
     * 需要重写InfoWindow的就重写该方法
     * @param addrInfo
     * @return
     */
    protected View InfoWindowView(final AddrInfo addrInfo){
        if(addrInfo == null) return null;
        // 生成一个TextView用户在地图中显示InfoWindow
        TextView textView = new TextView(getActivity());
        textView.setBackgroundResource(R.drawable.location_tips);
        textView.setPadding(30, 20, 30, 50);
        textView.setText(addrInfo.getName());
        textView.setTextColor(0xFFFFFFFF);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInfoWindowClick(addrInfo);
            }
        });
        return textView;
    }

    /**
     * 是否显示我当前的位置
     * @return
     */
    protected boolean canShowMyLocation(){
        return true;
    }

    /**
     * 点击出现标InfoWindow
     * @return
     */
    protected boolean canShowInfoWindow(){
        return false;
    }

    /**
     * 可以重写该方法重写InfoWindow的点击事件
     * @param addrInfo
     */
    protected void onInfoWindowClick(AddrInfo addrInfo){
        useOtherMap(addrInfo.getLatitude(),addrInfo.getLongitude(),addrInfo.getName());
    }

    /**
     * 有些操作必须放在map加载完成后
     */
    protected void onMapLoadFinish(){}

    @Override
    public void onStart()
    {
        mBaiduMap.setMyLocationEnabled(true);
        super.onStart();
    }

    @Override
    public void onStop()
    {
        mBaiduMap.setMyLocationEnabled(false);
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(locService != null){
            locService.unregisterListener(listener);
            locService.stop();
        }
        mMapView.onDestroy();
        mIconMaker.recycle();
        mMapView = null;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mMapView.onPause();
    }
}
