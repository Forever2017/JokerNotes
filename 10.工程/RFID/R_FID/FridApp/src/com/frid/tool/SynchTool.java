package com.frid.tool;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.frid.db.DaoLog;
import com.frid.db.DaoProduct;
import com.frid.pojo.DBGsonProduct;
import com.frid.pojo.DBLog;
import com.frid.pojo.GsonState;
import com.frid.tool.ASHttp.AsyncHttp;
import com.google.gson.Gson;

public class SynchTool {
	private DaoProduct PDao;
	private DaoLog LDao;
	private Context context;
	private JsonTool jt;

	public SynchTool(Context context) {
		this.context = context;
		PDao = new DaoProduct(context);
		LDao = new DaoLog(context);
		jt = new JsonTool();
	}

	/**
	 * 保存Log
	 * RFIDCode   EPC的ID
	 * Operation  操作类型
	 * INCONFIRMED = 1   移库单 确认移库 
	 * SHOWPRODUCT2CUSTOMER = 2  保险箱 展示产品  
	 * CUSTOMERBUY = 3  保险箱 顾客购买
	 * CUSTOMERNOTBUY = 4 保险箱 顾客不买
	 * */
	public void saveLog(String RFIDCode,int Operation) {
		LDao.setProduct(new DBLog(RFIDCode,Operation,FTool.getCurrentDate(),""));
	}

	/** 上传 保险箱货品信息 and 操作日志
	 * @return */
	public void upload() {
		Log.e("自动同步 保险箱+操作日志", "自动同步 保险箱+操作日志");
		
		/*从数据库中取出数据*/
		List<DBGsonProduct> dlist = PDao.getProductList();
		List<DBLog> llist = LDao.getProductList();

		if(dlist!=null&&dlist.size()>0){//有保险箱数据
			ASHttp.WarehouseSync(context, jt.getProduct(dlist), new AsyncHttp() {
				@Override
				public void onResult(boolean b, String msg) {
					super.onResult(b, msg);
					if(b){
						GsonState gs = new Gson().fromJson(msg, GsonState.class);
						if(gs.getResponseCode().equals("0000")){
							Toast.makeText(context, "完成同步.", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(context, "同步失败,Error:"+gs.getResponseMessage(), Toast.LENGTH_SHORT).show();
						}
					}else{
						Toast.makeText(context, "网络异常,同步失败.", Toast.LENGTH_SHORT).show();
					}
				}
			});
		}else{
			Toast.makeText(context, "无可同步数据.", Toast.LENGTH_SHORT).show();
		}

		if(llist!=null&&llist.size()>0){//有LOG数据
			ASHttp.UploadOperationLog(context, jt.getLog(llist), new AsyncHttp() {
				@Override
				public void onResult(boolean b, String msg) {
					super.onResult(b, msg);
					if(b&&new Gson().fromJson(msg, GsonState.class).getResponseCode().equals("0000")){
						//成功上传LOG
						LDao.clear();
					}else
						Log.e("FridApp", "LOG上传失败，返回错误："+msg);
				}
			});
		}
	}
}




