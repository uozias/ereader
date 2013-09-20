package com.webthreeapp.sreader;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.mixi.android.sdk.CallbackListener;
import jp.mixi.android.sdk.Config;
import jp.mixi.android.sdk.ErrorInfo;
import jp.mixi.android.sdk.HttpMethod;
import jp.mixi.android.sdk.MixiContainer;
import jp.mixi.android.sdk.MixiContainerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;



/*
 * 各種snsに投稿するためのカスタムダイアログ
 *
 */

public class ShareDialogFragment extends DialogFragment {

	private String TAG = "ShareDialogFragment";

	private Bundle savedInstanceState = null;

	//ボタン
	private MySwitch facebookButton = null;
	private MySwitch twitterButton = null;
	private MySwitch mixiButton = null;
	private MySwitch lineButton = null;

	private Button shareButton = null;
	private TextView sendContentText = null;


	//ログイン状態
	private boolean fbLogin = false;
	private boolean twitterLogin = false;
	private boolean lineLogin = false;
	private boolean mixiLogin = false;


	private boolean sendingFbFeed = false; //通信中フラグ

	//Facebook関連
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private Session.StatusCallback statusCallback = new FBSessionStatusCallback();

	//Twitter関連
	 private String mCallbackURL;
    private Twitter mTwitter;
    private RequestToken mRequestToken;

    //mixi関連
    public MixiContainer mContainer = null;;
    private static int AUTHORIZE_REQUEST_CODE = 394;
    public boolean mxIsLogingIn = false;
    public boolean mxInstalled = false;

    //line関連
    public boolean lnInstalled = false;

    //メッセージ
    private String defaultString;
    private String mLink = "";

    Resources res;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		this.savedInstanceState = savedInstanceState;

		Dialog dialog = new Dialog(getActivity());
		dialog.setContentView(R.layout.share_dialog);

		res = getResources();

		//投稿スペースなどを画面サイズに対応した大きさにする
		Display display = getActivity().getWindowManager().getDefaultDisplay();

		sendContentText = (TextView) dialog.findViewById(R.id.sendContentText);
		sendContentText.setWidth(display.getWidth() - 40);

		shareButton = (Button) dialog.findViewById(R.id.button_share);
		shareButton.setWidth(display.getWidth() - 40);


		dialog.setTitle(res.getString(R.string.share));

		//デフォルトメッセージ設定
		mLink = getArguments().getString("playURL");
		defaultString = getArguments().getString("bookNameJ") + "を" + getArguments().getString("pageNum") + "ページまで読みました！ " + mLink;
		sendContentText.setText(defaultString);

		/*
		 * ボタンにイベント設定
		 */

		//facebookボタン
		facebookButton = (MySwitch) dialog.findViewById(R.id.facebookButton);
		facebookButton.setOnCheckedChangeListener(new FBOnCheckedChangeListener(this, savedInstanceState)); //トグルボタンバージョン
		//facebookButton.setOnDrawerOpenListener(new FBOnDrawerOpenListener()); //slidingDrawerを使ったバージョン
		//facebookButton.setOnDrawerCloseListener(new FBOnDrawerCloseListener(this));


		//facebook session管理
		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(getActivity(), null, statusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(getActivity());
				session.addCallback(statusCallback);
			}
			Session.setActiveSession(session);

			 //facebookのオンオフを記録しておいて、ダイアログ起動時に反映すうｒ
			if (session.getState() == SessionState.OPENED || session.getState() == SessionState.OPENED_TOKEN_UPDATED) {
				fbLogin = true;
				facebookButton.setChecked(true); //ボタンをチェック状態に
			}

		} else {
			//開いて、すでにセッションがOPENだったら
			if (session.getState() == SessionState.OPENED || session.getState() == SessionState.OPENED_TOKEN_UPDATED) {
				fbLogin = true;
				facebookButton.setChecked(true); //ボタンをチェック状態に
			}
		}





		//twitterボタン
		twitterButton = (MySwitch) dialog.findViewById(R.id.twitterButton);
		twitterButton.setOnCheckedChangeListener(new TWOnCheckedChangeListener());

		//lineボタン
		lineButton  = (MySwitch) dialog.findViewById(R.id.lineButton);
		lineButton.setChecked(false);
		lineButton.setOnCheckedChangeListener(new LNOnCheckedChangeListener());


		//LINEインストールチェック
		if(checkInstall(res.getString(R.string.package_name_line))){
			lnInstalled = true;
		}


		//mixiボタン
		mixiButton  = (MySwitch) dialog.findViewById(R.id.mixiButton);
		mixiButton.setOnCheckedChangeListener(new MXOnCheckedChangeListener());

		//mixiセッション開始
		if(checkInstall(res.getString(R.string.package_name_mixi))){
			mxMakeContanior();

	        if(mixiLogin){
	        	mixiButton.setChecked(true);
	        }
		}

		//SNSに投稿
		shareButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String sendingContent = sendContentText.getText().toString();
				if (fbLogin == true) {
					sendFBFeed(sendingContent);
				}
				if (twitterLogin == true){
					sendTWFeed(sendingContent);
				}
				if (mixiLogin){

					sendMXFeed(sendingContent);
				}
				if (lineLogin == true){
					sendLNFeed(sendingContent);
				}
			}
		});

		return dialog;
	}

	/*
	 * facebook
	 *
	 *
	 */

	//facebookボタンのクリック時(トグルボタンバージョン)

	public class FBOnCheckedChangeListener implements OnCheckedChangeListener {

		Fragment fragment = null;
		private Bundle savedInstanceState;

		public FBOnCheckedChangeListener(Fragment fragment, Bundle savedInstanceState) {
			this.fragment = fragment;
			this.savedInstanceState = savedInstanceState;

		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


			if (isChecked) {
				//チェックされた時 = ログイン
				FBcheckLogin();
			} else {
				Session session = Session.getActiveSession();
				//チェックが外れた時 = ログアウト
				if (!session.isClosed()) {
					session.closeAndClearTokenInformation();
				}
			}

		}
	}

	//ログイン処理
	public boolean FBcheckLogin(){
		Session session = Session.getActiveSession();


		//もしセッションがOPEN状態でなければ
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(getActivity(), null, statusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(getActivity());
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(getActivity()).setCallback(statusCallback));

				return false; //新たにログインする時はfalseを返す
			}

		}
		SessionState state = session.getState();
		if (state != SessionState.OPENED ) {
			if (state == SessionState.CLOSED || state == SessionState.CLOSED_LOGIN_FAILED) {
				//セッションが閉じてたら新しいセッションを開始
				session = new Session(getActivity());
				session.addCallback(statusCallback);
				Session.setActiveSession(session); //新しくセッションをつくったら、これをやるのがとても大事


			}
			if (state != SessionState.OPENING && state != SessionState.OPENED && state != SessionState.OPENED_TOKEN_UPDATED){
				//オープンしてるか、オープン処理中じゃなきゃセッションオープンを試みる
				//session.openForPublish(new Session.OpenRequest(getActivity()).setCallback(statusCallback).setPermissions(PERMISSIONS));
				Session.OpenRequest openRequest = new OpenRequest(getActivity()).setCallback(statusCallback).setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK);
				session.openForRead(openRequest);

				return false; //新たにログインする時はfalseを返す
			}

		}

		return true; //ログイン済みならtrueを変えす

	}

	/*
	 * slideing drawerを使ったバージョン
	public class FBOnDrawerOpenListener implements SlidingDrawer.OnDrawerOpenListener {




	@Override
	public void onDrawerOpened() {

		Session session = Session.getActiveSession();
		//チェックが外れた時 = ログアウト
		if (!session.isClosed() ) {
			session.closeAndClearTokenInformation();
		};


	}
	}

	public class FBOnDrawerCloseListener implements SlidingDrawer.OnDrawerCloseListener {

	Fragment fragment = null;


	public FBOnDrawerCloseListener(Fragment fragment) {
		this.fragment = fragment;

	}
	@Override
	public void onDrawerClosed() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() ) {
			if (session.isClosed()) {
				//セッションが閉じてたら新しいセッションを開始
				session = new Session(getActivity());
				Session.setActiveSession(session);
			}
			//セッションオープンを試みる
			session.openForPublish(new Session.OpenRequest(getActivity()).setCallback(statusCallback).setPermissions(PERMISSIONS));
		} else {
			//既にOPEN状態だった時は
			Session.openActiveSession(getActivity(), fragment, true, statusCallback);

		}

	}
	}
	*/

	private void sendFBFeed(String feedString) {
		sendingFbFeed = true;
		Session session = Session.getActiveSession();

		//ログインチェック
		if(FBcheckLogin() != true){
			return; //ログインされてない時はまた今度
		}

		// Check for publish permissions
		List<String> permissions = session.getPermissions();
		//求めるパーミッションがなかったら
		if (!isSubsetOf(PERMISSIONS, permissions)) {
			//パーミッションをリクエスト
			//フラグメントじゃなくてアクティビティの方につけてみた
			Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(getActivity(),
					PERMISSIONS).setCallback(statusCallback).setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK);
			session.requestNewPublishPermissions(newPermissionsRequest);
			return; //迂回してしまう
		}

		//投稿リクエスト
		Request request = Request.newStatusUpdateRequest(session, feedString, new Request.Callback() {
			public void onCompleted(Response response) {
				showFBPublishResult(response.getGraphObject(), response.getError());
			}
		});

		RequestAsyncTask task = new RequestAsyncTask(request);
		task.execute();
	}

	//コレクションに含まれているかチェックする関数
	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}

	//結果をアラートする関数
	private void showFBPublishResult(GraphObject result, FacebookRequestError error) {

		if (error == null) {
			Toast.makeText(getActivity(), "facebook投稿成功", Toast.LENGTH_LONG).show();
			sendContentText.setText(""); //投稿成功したら空に
			sendingFbFeed = false;

		} else {
			Toast.makeText(getActivity(), "facebook投稿失敗", Toast.LENGTH_LONG).show();

		}

	}

	//ログイン状態変化のコールバック
	private class FBSessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState sessionState, Exception exception) {

			//ログインエラーがあったら
			if ((exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
				fbLogin = false;
				facebookButton.setChecked(false);//トグルボタンをオフに
			} else {

					if (sendingFbFeed == true) {
						//まだ送信中の時は、投稿を試みる
						sendFBFeed(sendContentText.getText().toString());
					}

					if (sessionState == SessionState.OPENED || sessionState == SessionState.OPENED_TOKEN_UPDATED){
						//ログインしている時
						fbLogin = true;

						Toast.makeText(getActivity(), "Facebook認証成功", Toast.LENGTH_SHORT).show();

						if (!facebookButton.isChecked()) {
							facebookButton.setChecked(true);//トグルボタンをオンに
						}

					}

					//OPENING時はなにもしない

					if (sessionState == SessionState.CLOSED || sessionState == SessionState.CLOSED_LOGIN_FAILED){
						fbLogin = false;
						if (facebookButton.isChecked()) {
							facebookButton.setChecked(false);//トグルボタンをオフに
						}
					}

			}


		}
	}

	/*
	 * twitter
	 *
	 *
	 */
	private class TWOnCheckedChangeListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

			if(isChecked){

				//アクセストークンチェック
				if (!TwitterUtils.hasAccessToken(getActivity())) {
					//インスタンスをつくる
					mCallbackURL = getString(R.string.twitter_callback_url);
			        mTwitter = TwitterUtils.getTwitterInstance(getActivity());

			        startAuthorize();

				}else{
					twitterLogin = true;
				}


			}else{
				//TODO トークン破棄
			}

		}

	}

	//twitterの認証開始
	private void startAuthorize() {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    mRequestToken = mTwitter.getOAuthRequestToken(mCallbackURL);
                    return mRequestToken.getAuthorizationURL();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String url) {
                if (url != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } else {
                    // 失敗。。。
                }
            }
        };
        task.execute();
    }

	//受け取る処理
	private void twAuthCallback(){

		Intent intent =getActivity().getIntent();

		String mCallbackURL = getString(R.string.twitter_callback_url);
		if (intent == null
	                || intent.getData() == null
	                || !intent.getData().toString().startsWith(mCallbackURL)) {
	            return;
	        }

		String verifier = intent.getData().getQueryParameter("oauth_verifier");

        AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>() {
            @Override
            protected AccessToken doInBackground(String... params) {
                try {
                    return mTwitter.getOAuthAccessToken(mRequestToken, params[0]);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(AccessToken accessToken) {
                if (accessToken != null) {
                    // 認証成功！
                    Toast.makeText(getActivity(), "Twitter認証成功", Toast.LENGTH_SHORT).show();
                    successOAuth(accessToken);
                } else {

                }
            }
        };
        task.execute(verifier);
	}

	//ログイン成功後処理
	private void successOAuth(AccessToken accessToken) {
		 TwitterUtils.storeAccessToken(getActivity(), accessToken);
		 twitterLogin = true;
	}

	//送信
	private void sendTWFeed(String sendingContent){
		 mTwitter = TwitterUtils.getTwitterInstance(getActivity());

		AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
	        @Override
	        protected Boolean doInBackground(String... params) {
	            try {
	                mTwitter.updateStatus(params[0]);
	                return true;
	            } catch (TwitterException e) {
	                e.printStackTrace();
	                return false;
	            }
	        }

	        @Override
	        protected void onPostExecute(Boolean result) {
	            if (result) {
	            	Toast.makeText(getActivity(), "ツイート成功", Toast.LENGTH_LONG).show();
	            	sendContentText.setText(""); //投稿成功したら空に

	            }else{
	            	Toast.makeText(getActivity(), "ツイート失敗", Toast.LENGTH_LONG).show();
	            }
			}
		};
		task.execute(sendingContent);
	}

	/*
	 * mixi
	 *
	 *
	 */

	private class MXOnCheckedChangeListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked ){
				//認証
				if(mxInstalled == false &&  checkInstall(res.getString(R.string.package_name_mixi)) == true){
					mxInstalled = checkInstall(res.getString(R.string.package_name_mixi));
					mxMakeContanior();
				}
				if(mxInstalled){
					mxIsLogingIn = true;
					mContainer.authorize(getActivity(), new String[] {"mixi_apps2","w_voice"}, AUTHORIZE_REQUEST_CODE, new mxLogInCallback());
				}else{

					mixiButton.setChecked(false);
					Toast.makeText(getActivity(), "Mixiアプリがインストールされていません。", Toast.LENGTH_LONG).show();
					//playstoreへ
					Uri uri2 = Uri.parse("market://details?id=" + res.getString(R.string.package_name_mixi));
					Intent it = new Intent(Intent.ACTION_VIEW, uri2);
					startActivity(it);

				}

			}else{

				//ログアウト
				if(mxInstalled){
					mContainer.logout(getActivity(), AUTHORIZE_REQUEST_CODE, new mxLogOutCallback());
				}
			}

		}

	}

	//ログインコールバック
	public class mxLogInCallback implements CallbackListener{
        @Override
        public void onComplete(Bundle values) {
            // 正常時の処理
        	Toast.makeText(getActivity(), "Mixi認証成功", Toast.LENGTH_LONG).show();
        	mxIsLogingIn = false;
        	mixiLogin = true;

        }

        @Override
        public void onError(ErrorInfo e) {
            // エラー時の処理
        	e.getMessage();
        	mixiButton.setChecked(false);
        	mxIsLogingIn = false;
        }

        @Override
        public void onCancel() {
            // ユーザキャンセル時の処理
        	mixiButton.setChecked(false);
        	mxIsLogingIn = false;
        }

        @Override
        public void onFatal(ErrorInfo e) {
            // 異常終了時の処理
        	mixiButton.setChecked(false);
        	mxIsLogingIn = false;
        	if(e.getErrorCode() == 5){
        		//エラーコード5 = 再ログイン必要なら
        		mContainer.logout(getActivity(), AUTHORIZE_REQUEST_CODE, new mxLogOutCallback()); //ログアウト
        	}
        }

	}

	//ログアウトコールバック
	public class mxLogOutCallback implements CallbackListener {

		@Override
		public void onComplete(Bundle values) {
			// TODO 自動生成されたメソッド・スタブ

		}

		@Override
		public void onCancel() {
			// TODO 自動生成されたメソッド・スタブ

		}

		@Override
		public void onFatal(ErrorInfo e) {
			// TODO 自動生成されたメソッド・スタブ

		}

		@Override
		public void onError(ErrorInfo e) {
			// TODO 自動生成されたメソッド・スタブ

		}

	}



	//送信
	private void sendMXFeed(String sendingContent){
		Map<String, String> params = new HashMap<String, String>();
		params.put("status", sendingContent);
		mContainer.send("/voice/statuses/update", HttpMethod.POST, params, new mxSendCallback());

	}

	//送信後コールバック
	public class mxSendCallback implements CallbackListener{

		@Override
		public void onComplete(Bundle values) {

        	Toast.makeText(getActivity(), "Mixi投稿成功", Toast.LENGTH_LONG).show();
        	sendContentText.setText(""); //投稿成功したら空に
		}

		@Override
		public void onCancel() {
			// TODO 自動生成されたメソッド・スタブ

		}

		@Override
		public void onFatal(ErrorInfo e) {
			// TODO 自動生成されたメソッド・スタブ
			Toast.makeText(getActivity(), "Mixi投稿失敗", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onError(ErrorInfo e) {
			// TODO 自動生成されたメソッド・スタブ
			Toast.makeText(getActivity(), "Mixi投稿失敗", Toast.LENGTH_LONG).show();
		}

	}

	private void mxMakeContanior(){
		mxInstalled = true;
		Config config = new Config();
        config.clientId = "mixiapp_" + res.getString(R.string.mixi_app_id);
        mContainer = MixiContainerFactory.getContainer(config);
        mContainer.init(getActivity());

        mixiLogin = mContainer.isAuthorized();

	}

	/*
	 * line
	 *
	 *
	 */
	private class LNOnCheckedChangeListener implements OnCheckedChangeListener  {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked){
				lnInstalled =    checkInstall(res.getString(R.string.package_name_line));
				if(lnInstalled){
					lineLogin = true;
					twitterButton.setChecked(false); //lineと他のを同時にオンにできない
					facebookButton.setChecked(false);
					mixiButton.setChecked(false);
				}else{
					lineButton.setChecked(false);
					Toast.makeText(getActivity(), "LINEアプリがインストールされていません。", Toast.LENGTH_LONG).show();
					//playstoreへ
					Uri uri2 = Uri.parse("market://details?id=" + res.getString(R.string.package_name_line));
					Intent it = new Intent(Intent.ACTION_VIEW, uri2);
					startActivity(it);

				}
			}

		}
	}

	//LINE送信 (インテント発行するだけ)
	private void sendLNFeed(String sendingContent){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		String encodeStr = null;
		try {
			encodeStr = URLEncoder.encode(sendingContent,"utf-8");
		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		}
		intent.setData(Uri.parse("line://msg/text/" + encodeStr));
		startActivity(intent);

	}

	/*
	 *
	 * ライフサイクル
	 *
	 */

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);

		twAuthCallback();




	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);

	}

	@Override
	public void onResume() {
		super.onResume();
		Session session = Session.getActiveSession();

		if (session.getState().equals(SessionState.CLOSED_LOGIN_FAILED)
				|| session.getState().equals(SessionState.CLOSED)) {
			Toast.makeText(getActivity(), "Facebook認証失敗", Toast.LENGTH_LONG).show();

		}
	}

	@Override
	public void onPause() {
		super.onPause();

	}

    @Override
    public void onDestroy() {
    	if(mContainer != null){
    		mContainer.close(getActivity()); //mixi
    	}

        super.onDestroy();
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data); //facebook

	}

	//アプリのインストールチェック
	public boolean checkInstall(String appId){
		try {
		    PackageManager pm = getActivity().getPackageManager();
		    ApplicationInfo appInfo = pm.getApplicationInfo(
		        appId, PackageManager.GET_META_DATA);
		} catch(NameNotFoundException e) {
		    return false;
		}
		return true;
	}

}
