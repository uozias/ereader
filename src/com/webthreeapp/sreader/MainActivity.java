package com.webthreeapp.sreader;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/*
		 * 広告を仕込む
		 */

		//広告用のレイアウト
		LinearLayout adList = (LinearLayout) findViewById(R.id.adList);

		//広告をレイアウトに仕込む
		AdSpaceView adSpaceView = (AdSpaceView) new AdSpaceView(this);
		adSpaceView.setUri("http://www.saizo-aoyagi.jp/"); //タップした時に飛ぶ先
		adSpaceView.setImageResource(R.drawable.ad_dummy);//画像セット
		adList.addView(adSpaceView);

		AdSpaceView adSpaceView2 = (AdSpaceView) new AdSpaceView(this);
		adSpaceView2.setUri("https://www.google.co.jp/"); //タップした時に飛ぶ先
		adSpaceView.setImageResource(R.drawable.ad_dummy); //画像セット
		adList.addView(adSpaceView2);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {


		return true;
	}

}
