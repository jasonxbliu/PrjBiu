package com.jaf.biubiu;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.jaf.bean.BeanNearbyItem;
import com.jaf.bean.BeanRequestAnswerList;
import com.jaf.bean.BeanRequestTopicQuestionList;
import com.jaf.jcore.BaseActionBarActivity;
import com.jaf.jcore.BindView;
import com.jaf.jcore.Http;
import com.jaf.jcore.HttpCallBack;
import com.jarrah.photo.PopupUtil;

import org.json.JSONObject;

import java.io.Serializable;

public class ActivityDetail extends BaseActionBarActivity {

	private static final String KEY_ARGS = "question_data";
	private Fragment mDisplayFragment;
	private Extra extra;

	@BindView(id = R.id.editPanel)
	private View mEditPanel;

	@BindView(id = R.id.msgEdit)
	public EditTextBackEvent mEditText;

	@BindView(id = R.id.send, onClick = "onSendClick")
	private View mSend;
	private Dialog mDialog;

	@Override
	protected View getActionBarView() {
		mActionBarView = getLayoutInflater().inflate(
				R.layout.view_action_bar_detail, null);
		mActionBarView.findViewById(R.id.barOption).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						popupReportAbuse();
					}
				});
		return mActionBarView;
	}

	private void popupReportAbuse() {
		View v = getLayoutInflater().inflate(R.layout.popup_report_question,
				null);
		v.findViewById(R.id.btnOK).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Http http = new Http();
						// type 1 举报问题 2 举报评论
						http.url(Constant.API)
								.JSON(U.buildReportAbuse(
										extra.fromNearby.getQuestId(), 1, ""))
								.post(new HttpCallBack() {
									@Override
									public void onResponse(JSONObject response) {
										super.onResponse(response);
										Toast.makeText(ActivityDetail.this,
												R.string.reportSuccess,
												Toast.LENGTH_SHORT).show();
									}
								});
						mDialog.dismiss();
					}
				});

		v.findViewById(R.id.btnCancel).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mDialog.dismiss();
					}
				});

		mDialog = PopupUtil.makePopup(this, v);
		mDialog.show();
	}

	@Override
	protected int onLoadViewResource() {
		return R.layout.activity_detail;
	}

	@Override
	protected void onViewDidLoad(Bundle savedInstanceState) {
		extra = getData();
		if (extra == null) {
			L.dbg("activity question getData is null!");
			return;
		} else {
			// 附近
			if (extra.fromNearby != null) {
				BeanRequestAnswerList arg = U.buildAnswerArgs(true, 0,
						extra.questId);
				mDisplayFragment = FragmentQA.newInstance(arg);
			}

			if (mDisplayFragment != null) {
				FragmentTransaction trans = getSupportFragmentManager()
						.beginTransaction();
				trans.replace(R.id.container, mDisplayFragment);
				trans.commit();
			} else {
				L.dbg("display fragment is null");
			}
		}

		mEditText.requestFocus();
		mEditText
				.setOnEditTextImeBackListener(new EditTextBackEvent.EditTextImeBackListener() {
					@Override
					public void onImeBack(EditTextBackEvent ctrl, String text) {
						L.dbg("key board hidden");
					}
				});
	}

	public Extra getData() {
		return (Extra) getIntent().getSerializableExtra(KEY_ARGS);
	}

	public static void start(Activity activity, final Extra extra) {
		Intent intent = new Intent();
		intent.putExtra(KEY_ARGS, extra);
		intent.setClass(activity, ActivityDetail.class);
		activity.startActivity(intent);
	}

	public static class Extra implements Serializable {
		// nearby pair
		int questId;
		BeanNearbyItem fromNearby;

		// topic pair
		String topicTitle;
		BeanRequestTopicQuestionList fromTopic;
	}

	public void onSendClick(View v) {
		final CharSequence text = mEditText.getText();
		if (TextUtils.isEmpty(text)) {
			Toast.makeText(this, R.string.saySomething, Toast.LENGTH_SHORT)
					.show();
		} else {
			LocationManager.getInstance().requestLocation(
					new LocationManager.JLsn() {
						@Override
						public void onResult(double latitude, double longitude,
								BDLocation location) {
							super.onResult(latitude, longitude, location);
							doPost(text.toString(), location);
						}
					});
		}
	}

	public void doPost(String text, BDLocation location) {
		if (extra.fromNearby == null)
			return;

		Http http = new Http();
		int qid = extra.fromNearby.getQuestId();
		JSONObject jo = U.buildPostAnswerQuestion(text, location.getAddrStr(),
				qid);

		// if(mEditText.getTag(0) != null) {
		// L.dbg("answer comment");
		// BeanAnswerItem beanAnswerItem = (BeanAnswerItem) mEditText.getTag(0);
		// jo = U.postAnswerComment(text, beanAnswerItem.getAnsId());
		// }

		http.url(Constant.API).JSON(jo).post(new HttpCallBack() {
			@Override
			public void onResponse(JSONObject response) {
				super.onResponse(response);
				L.dbg("reply success");
				FragmentQA fa = (FragmentQA) mDisplayFragment;
				fa.refreshAnswer();
				mEditText.setText("");
				U.hideSoftKeyboard(ActivityDetail.this);
			}
		});
	}

}
