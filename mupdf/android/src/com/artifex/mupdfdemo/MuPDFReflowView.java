package com.artifex.mupdfdemo;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MuPDFReflowView extends WebView implements MuPDFView {
	private final MuPDFCore mCore;
	private final Handler mHandler;
	private final Point mParentSize;
	private int mPage;
	private int mContentHeight;
	AsyncTask<Void,Void,byte[]> mLoadHTML;

	public MuPDFReflowView(Context c, MuPDFCore core, Point parentSize) {
		super(c);
		mHandler = new Handler();
		mCore = core;
		mParentSize = parentSize;
		mContentHeight = parentSize.y;
		getSettings().setJavaScriptEnabled(true);
		addJavascriptInterface(new Object(){
			public void reportContentHeight(String value) {
				mContentHeight = (int)Float.parseFloat(value);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						requestLayout();
					}
				});
			}
		}, "HTMLOUT");
		setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				requestHeight();
			}
		});
	}

	private void requestHeight() {
		// Get the webview to report the content height via the interface setup
		// above. Workaround for getContentHeight not working
		loadUrl("javascript:elem=document.getElementById('content');window.HTMLOUT.reportContentHeight("+mParentSize.x+"*elem.offsetHeight/elem.offsetWidth)");
	}

	@Override
	public void setPage(int page, PointF size) {
		mPage = page;
		if (mLoadHTML != null) {
			mLoadHTML.cancel(true);
		}
		mLoadHTML = new AsyncTask<Void,Void,byte[]>() {
			@Override
			protected byte[] doInBackground(Void... params) {
				return mCore.html(mPage);
			}
			@Override
			protected void onPostExecute(byte[] result) {
				String b64 = Base64.encodeToString(result, Base64.DEFAULT);
				loadData(b64, "text/html; charset=utf-8", "base64");
			}
		};
		mLoadHTML.execute();
	}

	@Override
	public int getPage() {
		return mPage;
	}

	@Override
	public void setScale(float scale) {
		loadUrl("javascript:document.getElementById('content').style.zoom=\""+(int)(scale*100)+"%\"");
		requestHeight();
	}

	@Override
	public void blank(int page) {
	}

	@Override
	public boolean passClickEvent(float x, float y) {
		return false;
	}

	@Override
	public LinkInfo hitLink(float x, float y) {
		return null;
	}

	@Override
	public void selectText(float x0, float y0, float x1, float y1) {
	}

	@Override
	public void deselectText() {
	}

	@Override
	public boolean copySelection() {
		return false;
	}

	@Override
	public void strikeOutSelection() {
	}

	@Override
	public void setSearchBoxes(RectF[] searchBoxes) {
	}

	@Override
	public void setLinkHighlighting(boolean f) {
	}

	@Override
	public void setChangeReporter(Runnable reporter) {
	}

	@Override
	public void update() {
	}

	@Override
	public void addHq(boolean update) {
	}

	@Override
	public void removeHq() {
	}

	@Override
	public void releaseResources() {
		if (mLoadHTML != null) {
			mLoadHTML.cancel(true);
			mLoadHTML = null;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int x, y;
		switch(View.MeasureSpec.getMode(widthMeasureSpec)) {
		case View.MeasureSpec.UNSPECIFIED:
			x = mParentSize.x;
			break;
		default:
			x = View.MeasureSpec.getSize(widthMeasureSpec);
		}
		switch(View.MeasureSpec.getMode(heightMeasureSpec)) {
		case View.MeasureSpec.UNSPECIFIED:
			y = mContentHeight;
			break;
		default:
			y = View.MeasureSpec.getSize(heightMeasureSpec);
		}

		setMeasuredDimension(x, y);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return false;
	}
}
