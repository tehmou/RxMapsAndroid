package com.tehmou.rxmaps.view;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tehmou.rxmaps.network.MapNetworkAdapter;
import com.tehmou.rxmaps.network.MapNetworkAdapterSimple;
import com.tehmou.rxmaps.network.NetworkClientOkHttp;

/**
 * Created by ttuo on 26/08/14.
 */
public class MapFragment extends Fragment {
    private final static String URL_FORMAT_KEY = "urlFormat";
    private MapView mapView;

    public static MapFragment newInstance(final String urlFormat) {
        final MapFragment mapFragment = new MapFragment();
        final Bundle arguments = new Bundle();
        arguments.putString(URL_FORMAT_KEY, urlFormat);
        mapFragment.setArguments(arguments);
        return mapFragment;
    }

    private String getUrlFormat() {
        return getArguments().getString(URL_FORMAT_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mapView = new MapView(getActivity());
        return mapView;
    }

    @Override
    public void onStart() {
        super.onStart();
        NetworkClientOkHttp networkClient = new NetworkClientOkHttp();
        MapNetworkAdapter mapNetworkClient =
                new MapNetworkAdapterSimple(networkClient, getUrlFormat());
        MapViewModel mapViewModel = new MapViewModel(mapNetworkClient);
        mapView.setViewModel(mapViewModel);
    }
}
