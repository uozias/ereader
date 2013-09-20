package com.artifex.mupdfdemo;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MuPDFReflowAdapter extends BaseAdapter {
	private final Context mContext;
	private final MuPDFCore mCore;

	public MuPDFReflowAdapter(Context c, MuPDFCore core) {
		mContext = c;
		mCore = core;
	}

	@Override
	public int getCount() {
		return mCore.countPages();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final MuPDFReflowView reflowView;
		if (convertView == null) {
			reflowView = new MuPDFReflowView(mContext, mCore, new Point(parent.getWidth(), parent.getHeight()));
		} else {
			reflowView = (MuPDFReflowView) convertView;
		}

		reflowView.setPage(position, new PointF());

		return reflowView;
	}
}
