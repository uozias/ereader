package com.webthreeapp.sreader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class AdSpaceView extends ImageView {

	private Uri uri = null;

	public AdSpaceView(Context context) {
		super(context);

		//TODO 適切な画像をセット
		this.setImageResource(R.drawable.ad_dummy);

		//大きさ決める 高さを60dpで固定 トップマージン5p
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 60);
		params.setMargins(0, 10, 0, 0);
		this.setLayoutParams(params);

		//ビューの大きさに合わせ画像を拡大
		this.setScaleType(ImageView.ScaleType.CENTER_CROP);


		//タッチされた時の処理
		this.setOnTouchListener(new AdTouchListener(context));


	}


	//広告タッチイベントをもらう独自リスナー
	private class AdTouchListener implements OnTouchListener{
		private Context context = null;

		public AdTouchListener(Context context){
			//インテントを発行するために、コンストラクタでコンテクストをもらう
			this.context = context;

		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			Intent adIntent = new Intent();
			adIntent.setAction(Intent.ACTION_VIEW);

			//リンク先をセット適切に決める
			if (uri == null){
				throw new Error("uri is not set");
			}
			adIntent.setData(uri);
			context.startActivity(adIntent);
			return false;
		}

	}

	//リンク先のセッター
	public void setUri (String uriString){

		uri = Uri.parse(uriString);
	}
}
