package com.artifex.mupdfdemo;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.webthreeapp.sreader.PageFeedDirection;

public class MuPDFPageAdapter extends BaseAdapter {
	private final Context mContext;
	private final MuPDFCore mCore;
	private final SparseArray<PointF> mPageSizes = new SparseArray<PointF>();
	private PageFeedDirection pageFeedDirection = null;

	public MuPDFPageAdapter(Context c, MuPDFCore core) {
		mContext = c;
		mCore = core;
		if(pageFeedDirection == null)this.pageFeedDirection = new PageFeedDirection("right");
	}

	//added by aoyagi
	//ページ送りの向きを指定するコンストラクタ
	public MuPDFPageAdapter(Context c, MuPDFCore core, String pageFeedDirection) {
		this(c, core);
		this.pageFeedDirection = new PageFeedDirection(pageFeedDirection);
	}


	public int getCount() {
		return mCore.countPages();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		final MuPDFPageView pageView;
		//added by aoyagi ページ送り方向によってリストとビューの対応順を変える
		int position3 = 0;
		if(pageFeedDirection.equals("left")){
			position3 =  getCount() -1 - position;

		}else {
			//左でなければディフォルトの右
			position3 =   position;
		}
		final int position2 =   position3;

		if (convertView == null) {
			pageView = new MuPDFPageView(mContext, mCore, new Point(parent.getWidth(), parent.getHeight()));
		} else {
			pageView = (MuPDFPageView) convertView;
		}


		PointF pageSize = mPageSizes.get(position2);
		if (pageSize != null) {
			// We already know the page size. Set it up
			// immediately
			pageView.setPage(position2, pageSize);
		} else {
			// Page size as yet unknown. Blank it for now, and
			// start a background task to find the size
			pageView.blank(position2);
			AsyncTask<Void,Void,PointF> sizingTask = new AsyncTask<Void,Void,PointF>() {
				@Override
				protected PointF doInBackground(Void... arg0) {
					return mCore.getPageSize(position2);
				}

				@Override
				protected void onPostExecute(PointF result) {
					super.onPostExecute(result);
					// We now know the page size
					mPageSizes.put(position2, result);
					// Check that this view hasn't been reused for
					// another page since we started
					if (pageView.getPage() == position2)
						pageView.setPage(position2, result);
				}
			};

			sizingTask.execute((Void)null);
		}
		return pageView;
	}

	public PageFeedDirection getPageFeedDirection() {
		return pageFeedDirection;
	}

	public void setPageFeedDirection(PageFeedDirection pageFeedDirection) {
		this.pageFeedDirection = pageFeedDirection;
	}


}


