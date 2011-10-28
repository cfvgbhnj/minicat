package com.fanfou.app.preferences;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.AttributeSet;

import com.fanfou.app.App;
import com.fanfou.app.db.Contents.DirectMessageInfo;
import com.fanfou.app.db.Contents.DraftInfo;
import com.fanfou.app.db.Contents.StatusInfo;
import com.fanfou.app.db.Contents.UserInfo;
import com.fanfou.app.util.Utils;

public class ClearDataDialogPreference extends DialogPreference {

	public ClearDataDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ClearDataDialogPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if (which == DialogInterface.BUTTON_POSITIVE) {
			new CleanTask(context).execute();
		}
	}

	private static class CleanTask extends AsyncTask<Void, Void, Boolean> {
		private Context c;
		private ProgressDialog pd = null;

		public CleanTask(Context context) {
			this.c = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(c);
			pd.setTitle("清空缓存数据");
			pd.setMessage("正在清空缓存数据...");
			pd.setIndeterminate(true);
			pd.show();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			pd.dismiss();
			if (result.booleanValue() == true) {
				Utils.notify(c, "缓存数据已清空");
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				Thread.sleep(300);
				clean(c);
				return true;
			} catch (InterruptedException e) {
				if (App.DEBUG)
					e.printStackTrace();
				return false;
			}
		}

		private void clean(Context context) {
			ContentResolver cr = context.getContentResolver();
			cr.delete(StatusInfo.CONTENT_URI, null, null);
			cr.delete(UserInfo.CONTENT_URI, null, null);
			cr.delete(DirectMessageInfo.CONTENT_URI, null, null);
			cr.delete(DraftInfo.CONTENT_URI, null, null);
		}
	}

}
