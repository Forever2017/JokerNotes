package com.frid.ui.fragment;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;

import com.frid.data.FridApplication;
import com.frid.fridapp.R;
import com.frid.tool.VTool;
import com.frid.tool.VTool.CallbackVT;
import com.frid.ui.LoginActivity;
import com.polling.PollingService;
import com.polling.PollingUtils;
/**设置*/
public class SettingFragment extends FragmentJoker implements OnClickListener{
	private Button SettingExit,SynchSwitch,SettingPower,PollingTime,DeviceNumber;

	public SettingFragment() { super(R.layout.fragment_setting); }

	@Override
	public void init() {
		SynchSwitch = (Button) findViewById(R.id.SynchSwitch);
		SettingExit = (Button) findViewById(R.id.SettingExit);
		SettingPower = (Button) findViewById(R.id.SettingPower);
		PollingTime = (Button) findViewById(R.id.PollingTime);
		DeviceNumber = (Button) findViewById(R.id.DeviceNumber);

		SettingExit.setOnClickListener(this);
		SynchSwitch.setOnClickListener(this);
		SettingPower.setOnClickListener(this);
		PollingTime.setOnClickListener(this);
		DeviceNumber.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.SettingExit:
			VTool.Interaction(getActivity(), "注销账户", "确定注销当前账户?", "取消", "注销", new CallbackVT() {
				@Override
				public void InteractionYes() {
					FridApplication.sp.edit().putBoolean("AUTO_ISCHECK", false).commit();
					startActivity(new Intent(getActivity(),LoginActivity.class));
					getActivity().finish(); 
					super.InteractionYes();
				}
			});
			break;
		case R.id.SynchSwitch:
			Toast("关闭成功.");
			PollingUtils.stopPollingService(getActivity(),PollingService.class, PollingService.ACTION);
			break;
		case R.id.SettingPower://频率设置
			VTool.inputPassWordDialog(getActivity(), new CallbackVT() {
				@Override
				public void ReturnData(String msg) {
					super.ReturnData(msg);
					VTool.powerDialog(getActivity());
				}
			});
			break;
		case R.id.PollingTime://轮询周期设置
			VTool.inputPassWordDialog(getActivity(), new CallbackVT() {
				@Override
				public void ReturnData(String msg) {
					super.ReturnData(msg);
					VTool.pollingDialog(getActivity());
				}
			});
			break;
		case R.id.DeviceNumber://设备号
			VTool.inputIPDialog(getActivity(),"设备号","请输入设备号..",
					FridApplication.DeviceNumber, new CallbackVT() {
				@Override
				public void ReturnData(String msg) {
					super.ReturnData(msg);
					FridApplication.insertIdentity("DeviceNumber",msg);
					Toast("保存成功.");
				}
			});

			/*VTool.inputPassWordDialog(getActivity(), new CallbackVT() {
				@Override
				public void ReturnData(String msg) {
					super.ReturnData(msg);
					VTool.pollingDialog(getActivity());
				}
			});*/
			break;
		default:
			break;
		}
	}



}