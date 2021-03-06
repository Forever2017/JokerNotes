package com.frid.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.frid.adapter.QueryMItemAdapter;
import com.frid.adapter.QueryMItemAdapter.DelItem;
import com.frid.adapter.QueryTableMItemAdapter;
import com.frid.data.TestMsg;
import com.frid.data.ThreadEpcTest;
import com.frid.data.ThreadEpcTest.EpcResult;
import com.frid.fridapp.R;
import com.frid.pojo.GsonItemCheck;
import com.frid.pojo.GsonState;
import com.frid.pojo.GsonStateU;
import com.frid.pojo.GsonStock;
import com.frid.pojo.GsonStockCheck;
import com.frid.tool.ASHttp;
import com.frid.tool.JsonTool;
import com.frid.tool.VTool;
import com.frid.tool.ASHttp.AsyncHttp;
import com.frid.tool.VTool.CallbackVT;
import com.google.gson.Gson;
import com.pnikosis.materialishprogress.ProgressWheel;

import device.frid.Device;
import device.frid.Device.LoopEpc;
/**核对\查询*/
public class QueryFragment extends FragmentJoker implements OnClickListener,OnItemClickListener{
	private ProgressWheel pw;//旋转条
	private Button QueryScan,QueryUpload,QueryReset;
	private ListView QueryTableList;
	private QueryTableMItemAdapter mAdapter;
	private Device device;
	private boolean isSubmit = false;
	//
	private TextView TableDetailSum;

	//重置需要清空的参数
	private List<GsonItemCheck> boxList;//所有扫描到的epc商品存储
	private List<GsonItemCheck> uiList;//主列表展示List



	//子菜单显示的list
	private ListView QueryList;
	private List<GsonItemCheck> somList;
	private QueryMItemAdapter mItemAdapter;


	public QueryFragment() { super(R.layout.fragment_query2); }

	@Override
	public void init() {
		device = new Device(getActivity());
		scrapEpcList = new ArrayList<String>();
		UnreadEpcList = new ArrayList<String>();
		QueryTableList = (ListView) findViewById(R.id.QueryTableCenter);
		QueryList = (ListView) findViewById(R.id.QueryList);
		pw = (ProgressWheel) findViewById(R.id.QueryProgressWheel);
		QueryScan = (Button) findViewById(R.id.QueryCheckScan);
		QueryUpload = (Button) findViewById(R.id.QueryUpload);
		QueryReset = (Button) findViewById(R.id.QueryReset);

		TableDetailSum = (TextView) findViewById(R.id.TableDetailSum);


		QueryScan.setOnClickListener(this);
		QueryUpload.setOnClickListener(this);
		QueryReset.setOnClickListener(this);

		pw.spin();
		data();
	}

	//清空 重置 
	private void clearUI() {
		boxList.clear();
		listPrimaryUpdate();
		listSecondaryUpdate("");
		checkSubmit(false);

		num = 1;//测试用
	}

	//更新主列表数据
	private void listPrimaryUpdate() {
		int sum = 0;//商品合计
		uiList.clear();
		for (GsonItemCheck gic : boxList) {//
			gic.setCurrent(1);
			if(uiList.size()>0){
				boolean temp = false;
				for (GsonItemCheck uiItem : uiList) {
					if(gic.getId().equals(uiItem.getId())){
						uiItem.setCurrent(uiItem.getCurrent()+1);
						temp = true;
						break;
					}
				}
				if(!temp) uiList.add(gic);
			}
			else
				uiList.add(gic);
			sum++;
		}
		mAdapter.notifyDataSetChanged();
		//合计
		TableDetailSum.setText(sum+"");

	}
	//更新主列表数据
	/*private void listPrimaryUpdate(GsonItemCheck gic) {

		if(uiList.size()>0)
			try{
				boolean temp = false;
				for (GsonItemCheck uiItem : uiList) {
					if(gic.getId().equals(uiItem.getId())){
						uiItem.setCurrent(uiItem.getCurrent()+1);
						temp = true;
						break;
					}
				}
				if(!temp) uiList.add(gic);
			} catch (Exception e) {
				// TODO: handle exception
				Log.e("", "");
			}
		else
			uiList.add(gic);

		sum++;
		mAdapter.notifyDataSetChanged();
		//合计
		TableDetailSum.setText(sum+"");


	}*/
	//更新次列表数据
	private void listSecondaryUpdate(String id) {
		somList.clear();
		for (GsonItemCheck gic : boxList) {//
			if(gic.getId().equals(id))
				somList.add(gic);
		}
		mItemAdapter.notifyDataSetChanged();
	}

	private void data() {
		boxList = new ArrayList<GsonItemCheck>();
		uiList = new ArrayList<GsonItemCheck>();

		mAdapter = new QueryTableMItemAdapter(getActivity(), uiList);
		QueryTableList.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
		QueryTableList.setOnItemClickListener(this);

		somList = new ArrayList<GsonItemCheck>();
		mItemAdapter = new QueryMItemAdapter(getActivity(), somList,new DelItem(){
			@Override
			public void onResult(String epc,String id) {
				super.onResult(epc,id);
				int i = -1;
				for (int j = 0; j < boxList.size(); j++) {
					if(boxList.get(j).getEpc().equals(epc)) i = j;
				}

				if(i!=-1)boxList.remove(i);

				listPrimaryUpdate();
				listSecondaryUpdate(id);
			}
		});
		QueryList.setAdapter(mItemAdapter);
		mItemAdapter.notifyDataSetChanged();
	}



	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.QueryCheckScan://扫描
			if(QueryScan.getText().toString().equals(getResources().getString(R.string.ScanGood))){
				checkSubmit(false);
				//核对单商品为空时不做扫描
				//				if(list.size()==0){Toast("没有核对货品."); return;}

				//				if(boxList.size() >= sum) { Toast("所有商品已核实."); return; }

				//1.改变文字 2.锁定上传 3.显示旋转动画 4.执行真实操作
				QueryScan.setText(getResources().getString(R.string.Stop));
				pw.setVisibility(View.VISIBLE);
				//真实扫描
				device.biBi(true);
				scanFrid();

			}else{//点击停止
				checkSubmit(true);
				//1.改变文字  2.锁定上传 3.显示旋转动画 4.执行真实操作
				QueryScan.setText(getResources().getString(R.string.ScanGood));
				pw.setVisibility(View.GONE);
				//真实停止扫描
				device.stopSearch();
				device.biBi(false);
//				tet.stop();

				//				scrapEpcList.clear();
				UnreadEpcList.clear();
			}
			break;
		case R.id.QueryUpload://提交核实
			if(isSubmit){
				VTool.SelectPeopleDialog(getActivity(), new CallbackVT() {
					@Override
					public void ReturnData(String msg) {
						super.ReturnData(msg);
						//得到送货员ID
						if(msg!=null&&!msg.equals("")){
							ASHttp.UploadEpc(getActivity(),new JsonTool().getEpcCheck(Integer.parseInt(msg), boxList), new AsyncHttp() {
								public void onResult(boolean b, String msg) {
//									msg = TestMsg.updateMSG("Success", msg);
									if(b){
										GsonStateU gc = new Gson().fromJson(msg, GsonStateU.class);
										if(gc.getResponseCode().equals("0000")){
//											Toast("提交成功.");
											clearUI();

											new AlertDialog.Builder(getActivity())
											.setTitle("提交成功,移库单号:")
											.setMessage(gc.getStockTransferExternalId())
											.setNegativeButton("确定",null).show();


										}else Toast("Error:"+gc.getResponseMessage());
									}
								};
							} );
						}else
							Toast("送货员错误.");
					}
				});

			}
			break;
		case R.id.QueryReset://重置商品列表
			clearUI();
			break;
		default:
			break;
		}
	}
	/**
	检索扫描到的EPC思路
	1、不匹配的epc直接跳过，一个用来装废弃epc的list？
	2、已经匹配到的epc不做检索，直接丢入废弃epc的list还是新建一个list？
	3、避免过多的去调用checkEpcid的方法，先本地确认当前扫描到的epc需不需要去服务器获取epc的物品信息
	4、 废弃epc的list（已经使用过的、查询过 但是没有查到对应物品的、）*/
	private void scanFrid() {
		isCheck = true;
		/*已找到的epc加入到废弃epc*/
		scrapEpcList.clear();
		for (GsonItemCheck gi : boxList) scrapEpcList.add(gi.getEpc());
		//
		device.startSearch(new LoopEpc() {
			@Override
			public void ReturnEpc(final String epc) {
				super.ReturnEpc(epc);
				//DF2018072100000000000019  
				if(scrapEpcList.contains(epc)) return;

				UnreadEpcList.add(epc);

				if(isCheck){//网络开关  打开
					epcCheck();
				}
			}
		});
		//测试扫描
		/*tet = new ThreadEpcTest(new EpcResult() {
			@Override
			public void onResult(String epc) {
				super.onResult(epc);
				//DF2018072100000000000019  
				if(scrapEpcList.contains(epc)) return;

				UnreadEpcList.add(epc);

				if(isCheck){//网络开关  打开
					epcCheck();
				}
			}
		});*/
	}
//	ThreadEpcTest tet;

	/**不需要对比的epc*/
	private List<String> scrapEpcList;
	/**保存未读的epc*/
	private List<String> UnreadEpcList;
	/**check的开关*/
	private boolean isCheck = true;
	/**检查epc货品  测试用*/
	int num = 1;
	private void epcCheck() {

		isCheck = false;
		if(UnreadEpcList.size()>0){//有未读的epc
			ASHttp.CheckRfid(getActivity(), UnreadEpcList.get(0), new AsyncHttp() {
				@Override
				public void onResult(boolean b, String msg) {
					super.onResult(b, msg);
					msg = TestMsg.testCheckFrid(msg,num);
					num = num + 1;

					if(b){
						final GsonItemCheck gi = new Gson().fromJson(msg, GsonItemCheck.class);


						if(gi.getId()!=null&&gi.getId().length()>0){

							//for (GsonItemCheck gitem : list) {  
							//if(gitem.getId().equals(gi.getId())&&gitem.getCurrent()<Integer.parseInt(gitem.getNumber())){
							/*productExternalId匹配到一样*/
							//	GsonItemCheck temp = (GsonItemCheck) gitem.clone();
							gi.setEpc(UnreadEpcList.get(0));
							boxList.add(gi);

							//gitem.setCurrent(gitem.getCurrent()+1);
							device.biBi(true);
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									//更新list展示数据
									listPrimaryUpdate();
								}
							});
							//	}
							//	}


						}





						scrapEpcList.add(UnreadEpcList.get(0));
						UnreadEpcList.remove(0);
						epcCheck();
						// 这里可能需要做“全部找到后，自动停止扫描”
						/*if(sum == current){//全部找到
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									//1.改变文字  2.锁定上传 3.显示旋转动画 4.执行真实操作
									QueryScan.setText(getResources().getString(R.string.ScanGood));
									pw.setVisibility(View.GONE);
									//真实停止扫描
									device.stopSearch();
									device.biBi(false);
//									tet.stop();

									scrapEpcList.clear();
									UnreadEpcList.clear();

									checkSubmit();
								}
							});
						}*/
					}else{
						epcCheck();
					}
				}
			});
		}else{//没有未读的epc
			isCheck = true;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		listSecondaryUpdate(uiList.get(position).getId());
	}


	/**判断是否可以“提交”，按钮 蓝色\灰色（全部核实即可提交）*/
	private void checkSubmit(boolean isBoolean) {
		if(boxList.size()>0) isSubmit = isBoolean;
		else isSubmit = false;

		if(isSubmit) 
			QueryUpload.setBackground(getResources().getDrawable(R.drawable.button_circle_blue_style));
		else  
			QueryUpload.setBackground(getResources().getDrawable(R.drawable.button_circle_gray_style));
	}
}










