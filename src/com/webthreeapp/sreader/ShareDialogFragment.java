package com.webthreeapp.sreader;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/*
 * 各種snsに投稿するためのカスタムダイアログ
 *
 */

public class ShareDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = new Dialog(getActivity()) ;
		dialog.setContentView(R.layout.share_dialog);
		Resources res = getResources();
		dialog.setTitle(res.getString(R.string.share));
		return dialog;
   }
}
