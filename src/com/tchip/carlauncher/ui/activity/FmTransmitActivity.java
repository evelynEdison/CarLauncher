package com.tchip.carlauncher.ui.activity;

import java.io.File;

import com.tchip.carlauncher.Constant;
import com.tchip.carlauncher.R;
import com.tchip.carlauncher.model.Typefaces;
import com.tchip.carlauncher.view.SwitchButton;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.provider.Settings;

public class FmTransmitActivity extends Activity {

	/**
	 * 开关节点
	 * 
	 * 1：开 0：关
	 */
	private File EnableFm = new File(
			"/sys/devices/platform/mt-i2c.1/i2c-1/1-002c/enable_qn8027");

	/**
	 * 频率节点
	 * 
	 * 频率范围：7600~10800
	 */
	private File SetFmCh = new File(
			"/sys/devices/platform/mt-i2c.1/i2c-1/1-002c/setch_qn8027");

	/**
	 * 系统设置：FM发射开关
	 */
	private String FM_TRANSMITTER_ENABLE = "fm_transmitter_enable";

	/**
	 * 系统设置：FM发射频率
	 */
	private String FM_TRANSMITTER_CHANNEL = "fm_transmitter_channel";

	private Button fm8550, fm9030, fm10570;

	private RelativeLayout layoutBack;
	private TextView textHint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
		setContentView(R.layout.activity_fm_transmit);

		initialLayout();
	}

	private void initialLayout() {
		// 开关
		SwitchButton switchFm = (SwitchButton) findViewById(R.id.switchFm);
		fm8550 = (Button) findViewById(R.id.fm8550);
		fm9030 = (Button) findViewById(R.id.fm9030);
		fm10570 = (Button) findViewById(R.id.fm10570);

		fm8550.setTypeface(Typefaces.get(this, Constant.FONT_PATH
				+ "Font-Droid-Sans-Fallback.ttf"));
		fm9030.setTypeface(Typefaces.get(this, Constant.FONT_PATH
				+ "Font-Droid-Sans-Fallback.ttf"));
		fm10570.setTypeface(Typefaces.get(this, Constant.FONT_PATH
				+ "Font-Droid-Sans-Fallback.ttf"));

		fm8550.setOnClickListener(new MyOnClickListener());
		fm9030.setOnClickListener(new MyOnClickListener());
		fm10570.setOnClickListener(new MyOnClickListener());

		layoutBack = (RelativeLayout) findViewById(R.id.layoutBack);
		layoutBack.setOnClickListener(new MyOnClickListener());

		textHint = (TextView) findViewById(R.id.textHint);

		updateChoseButton(getFmFrequcenyId());

		switchFm.setChecked(isFmTransmitOn());
		setButtonEnabled(isFmTransmitOn());

		switchFm.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Settings.System.putString(getContentResolver(),
						FM_TRANSMITTER_ENABLE, isChecked ? "1" : "0");
				setButtonEnabled(isChecked);
				if (!isChecked) {

					updateChoseButton(0);
				} else {
					int nowId = getFmFrequcenyId();
					updateChoseButton(nowId);

				}
			}
		});
	}

	/**
	 * 按钮是否可用
	 * 
	 * @param isFmTransmitOpen
	 */
	private void setButtonEnabled(boolean isFmTransmitOpen) {
		fm8550.setEnabled(isFmTransmitOpen);
		fm9030.setEnabled(isFmTransmitOpen);
		fm10570.setEnabled(isFmTransmitOpen);
	}

	private int getFmFrequcenyId() {
		int nowFmChannel = 0;
		String fmChannel = Settings.System.getString(getContentResolver(),
				FM_TRANSMITTER_CHANNEL);
		if (isFmTransmitOn() && fmChannel.trim().length() > 0) {
			if ("8550".equals(fmChannel)) {
				nowFmChannel = 1;
			} else if ("10570".equals(fmChannel)) {
				nowFmChannel = 3;
			} else {
				nowFmChannel = 2;
			}
		}

		return nowFmChannel;
	}

	/**
	 * FM发射是否打开
	 * 
	 * @return
	 */
	private boolean isFmTransmitOn() {
		boolean isFmTransmitOpen = false;
		String fmEnable = Settings.System.getString(getContentResolver(),
				FM_TRANSMITTER_ENABLE);
		if (fmEnable.trim().length() > 0) {
			if ("1".equals(fmEnable)) {
				isFmTransmitOpen = true;
			} else {
				isFmTransmitOpen = false;
			}
		}
		return isFmTransmitOpen;
	}

	/**
	 * 根据选中状态更新按钮字体颜色
	 * 
	 * @param which
	 */
	private void updateChoseButton(int which) {
		switch (which) {
		case 0:
			fm8550.setTextColor(Color.BLACK);
			fm9030.setTextColor(Color.BLACK);
			fm10570.setTextColor(Color.BLACK);
			textHint.setText("请打开FM发射开关");
			break;

		case 1:
			fm8550.setTextColor(Color.BLUE);
			fm9030.setTextColor(Color.BLACK);
			fm10570.setTextColor(Color.BLACK);
			textHint.setText("当前发射频率85.5兆赫");
			break;

		case 2:
			fm8550.setTextColor(Color.BLACK);
			fm9030.setTextColor(Color.BLUE);
			fm10570.setTextColor(Color.BLACK);
			textHint.setText("当前发射频率90.3兆赫");
			break;

		case 3:
			fm8550.setTextColor(Color.BLACK);
			fm9030.setTextColor(Color.BLACK);
			fm10570.setTextColor(Color.BLUE);
			textHint.setText("当前发射频率105.7兆赫");
			break;

		default:
			break;
		}
	}

	class MyOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.layoutBack:
				finish();
				break;

			case R.id.fm8550:
				setFmFrequency(8550);
				updateChoseButton(1);
				break;

			case R.id.fm9030:
				setFmFrequency(9030);
				updateChoseButton(2);
				break;

			case R.id.fm10570:
				setFmFrequency(10570);
				updateChoseButton(3);
				break;
			}
		}
	}

	private void setFmFrequency(int frequency) {
		if (frequency >= 7600 || frequency <= 10800) {
			Settings.System.putString(getContentResolver(),
					FM_TRANSMITTER_CHANNEL, "" + frequency);
			Log.v(Constant.TAG, "Set FM Frequency success:" + frequency);
		}
	}

}