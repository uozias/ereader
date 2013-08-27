package com.webthreeapp.sreader;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;

/*
 * 各種snsに投稿するためのカスタムダイアログ
 *
 */

public class ShareDialogFragment extends DialogFragment {

	private String TAG = "ShareDialogFragment";

	private ToggleButton facebookButton = null;
	private ToggleButton twitterButton = null;
	private ToggleButton mixiButton = null;
	private Button shareButton = null;
	private TextView sendContentText = null;

	private boolean fbLogin = false;

	//Facebookのパーミッション
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");

	private Session.StatusCallback statusCallback = new SessionStatusCallback();


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		Dialog dialog = new Dialog(getActivity());

		dialog.setContentView(R.layout.share_dialog);

		Resources res = getResources();
		dialog.setTitle(res.getString(R.string.share));

		//投稿内容
		sendContentText = (TextView) dialog.findViewById(R.id.sendContentText);



		/*
		 * ボタンにイベント設定
		 */

		//facebookボタン
		facebookButton = (ToggleButton) dialog.findViewById(R.id.facebookButton);
		facebookButton.setOnCheckedChangeListener(new FBOnCheckedChangeListener(this));

		//facebook session管理
		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(getActivity(), null, statusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(getActivity());
			}
			Session.setActiveSession(session);
			/*
			 * TODO facebookのオンオフを記録しておいて、ダイアログ起動時に反映すうｒ
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForPublish(new Session.OpenRequest(this).setCallback(statusCallback).setPermissions(PERMISSIONS));
			}
			*/
		}else{
			//開いて、すでにセッションがOPENだったら
			if(session.getState() == SessionState.OPENED){
				fbLogin = true;
				facebookButton.setChecked(true); //ボタンをチェック状態に
			}
		}


		//twitterボタン
		twitterButton = (ToggleButton) dialog.findViewById(R.id.twitterButton);
		twitterButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO 自動生成されたメソッド・スタブ

			}
		});

		//mixiボタン
		mixiButton = (ToggleButton) dialog.findViewById(R.id.mixiButton);
		mixiButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO 自動生成されたメソッド・スタブ

			}
		});

		//SNSに投稿
		shareButton = (Button) dialog.findViewById(R.id.button_share);
		shareButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (fbLogin == true) {
					sendFBFeed(sendContentText.getText().toString());
				}

			}
		});

		return dialog;
	}

	//facebookボタンのクリック時
	public class FBOnCheckedChangeListener implements OnCheckedChangeListener {

		Fragment fragment = null;

		public FBOnCheckedChangeListener(Fragment fragment) {
			this.fragment = fragment;

		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


			Session session = Session.getActiveSession();


			if(isChecked){
				//チェックされた時 = ログイン
				//もしセッションがOPEN状態でなければ
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
			}else{
				//チェックが外れた時 = ログアウト
				if (!session.isClosed() ) {
					session.closeAndClearTokenInformation();
				};
			}

		}
	}



	private void sendFBFeed(String feedString) {
		Session session = Session.getActiveSession();

		// Check for publish permissions
		List<String> permissions = session.getPermissions();
		//求めるパーミッションがなかったら
		if (!isSubsetOf(PERMISSIONS, permissions)) {
			//パーミッションをリクエスト
			Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS);
			session.requestNewPublishPermissions(newPermissionsRequest);
			return;
		}

		//投稿リクエスト
		Request request = Request.newStatusUpdateRequest(session, feedString, new Request.Callback() {
			public void onCompleted(Response response) {
				showPublishResult(response.getGraphObject(), response.getError());
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
	private void showPublishResult(GraphObject result, FacebookRequestError error) {

		if (error == null) {
			Toast.makeText(getActivity(), "facebook投稿成功", Toast.LENGTH_LONG).show();

		} else {
			Toast.makeText(getActivity(), "facebook投稿失敗", Toast.LENGTH_LONG).show();

		}

	}

	//ログイン状態変化のコールバック
	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {

			//ログインエラーがあったら
			if ((exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
				fbLogin = false;
				facebookButton.setChecked(false);//トグルボタンをオフに
			}

			SessionState sessionState = session.getState();
			if (sessionState == SessionState.OPENED|| sessionState == SessionState.OPENING || sessionState == SessionState.OPENED_TOKEN_UPDATED)
			{
				//ログインしている時
				fbLogin = true;
				if(sessionState == SessionState.OPENED || sessionState == SessionState.OPENED_TOKEN_UPDATED){
					Toast.makeText(getActivity(), "facebook認証成功", Toast.LENGTH_SHORT).show();
				}
				if(!facebookButton.isChecked()){
					facebookButton.setChecked(true);//トグルボタンをオンに
				}

			} else {
				fbLogin = false;
				if(facebookButton.isChecked()){
					facebookButton.setChecked(false);//トグルボタンをオフに
				}
			}
		}
	}

	//fragmentの各ライフサイクル
	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);

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
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

}
