package com.https.test.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

	public static final String SBLX_1 = "1";
	public static final String SBLX_2 = "2";
	public static final String SBLX_3 = "3";
	public static final String SBLX_4= "4";
	public static final String SBLX_5 = "5";

	
	public static final String GP_FPKJ = "GP_FPKJ"; // 发票开具
	public static final String GP_FPCX = "GP_FPCX"; // 发票查询
	public static final String GP_SKSBXXCX = "GP_SKSBXXCX"; //税控设备信息查询
	public static final String GP_YBJGCX = "GP_YBJGCX"; //异步请求结果查询

	/**
	 * 获取指定格式时间(yyyy-MM-dd)
	 * @return
	 */
	public static String formatToDay(){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format((new Date()));
	}
	
	/**
	 * 获取指定格式时间(yyyyMMddHHmmss)
	 * @return
	 */
	public static String formatToTime(){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
		return sdf.format((new Date()));
	}
	
	/************************************************************************
	 * 获取9位随机数
	 */
	public static String randNineData(){
		return randData()+randFiveData();
	}
	
	/************************************************************************
	 * 获取5位随机数
	 */
	public static String randFiveData(){
		return String.valueOf((int)(Math.random()*90000+10000));
	}
	
	/************************************************************************
	 * 获取4位随机数
	 */
	public static String randData(){
		return String.valueOf((int)(Math.random()*9000+1000));
	}
}
