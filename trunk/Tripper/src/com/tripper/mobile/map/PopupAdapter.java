package com.tripper.mobile.map;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.tripper.mobile.R;

public class PopupAdapter implements InfoWindowAdapter {
	LayoutInflater inflater=null;

	public PopupAdapter(LayoutInflater inflater) {
		this.inflater=inflater;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return(null);
	}

	@Override
	public View getInfoContents(Marker marker) {
		View popup=inflater.inflate(R.layout.map_popup, null);

		TextView tv=(TextView)popup.findViewById(R.id.popupText);

		tv.setText(marker.getTitle());

		return(popup);
	}
}
