package com.https.test.main;

import com.https.test.util.JsonUtils;
import com.https.test.util.RequestUtils;
import com.https.test.util.Utils;

import javax.rmi.CORBA.Util;
import java.util.Date;


public class TestDemo {
	public static void main(String[] args) throws Exception {
		Date startDate = new Date();//初始化开始时间
		String requestUrl = "https://dev.fapiao.com:18944/fpt-rhqz/prepose";//初始化地址
		String appid = "";
		String contentPassword = "";
		String nsrsbh = "";
		String nsrmc = "";
		String sbbh = "";
		String callback_url = "http://192.168.72.187:8080/demo-rhqz/callBackDemoAction";
		String lsh = "";
		//开具报文的FPQQLSH是动态生成的，如果要查询请填写已开具的固定值
		String fpqqlsh = "";// 需要查询发票的流水号
		String fpdm = "";
		String fphm = "";

		//1-税控盘、2-金税盘、3-UKey、4-老税控服务器、5-新型税控服务器 6-数电
		String sblx = Utils.SBLX_1;
//		String sblx = Utils.SBLX_2;
//		String sblx = Utils.SBLX_3;
//		String sblx = Utils.SBLX_4;
//		String sblx = Utils.SBLX_5;
// 		String sblx = Utils.SBLX_6;

		// 通过注释选择接口
		String interfaceCode = Utils.GP_FPKJ;//发票开具
//		 String interfaceCode = Utils.GP_FPCX;//发票查询
//		String interfaceCode = Utils.GP_SKSBXXCX;//税控设备信息查询
//		String interfaceCode = Utils.GP_YBJGCX;//异步请求结果查询

		// 组装请求报文
		String requestData = "";

		if((sblx).equals(Utils.SBLX_1)){
			appid = "667bf7c79996c7a507af365a8f340a6f9009c519d28a59ae3be23cc86c2e1630";//appid
			contentPassword = "F6AD954A498AEDBE";//AES加密密钥
			nsrsbh = "500102010004011";
			nsrmc = "升级版测试用户4011";
			sbbh = "499000138208";

			fpqqlsh = "TEST2020071715000010";
			fpdm = "050000000004";
			fphm = "44533013";

			if(Utils.GP_FPKJ.equals(interfaceCode)){
				callback_url = callback_url+"?method=FPKJ";//拼接开具的方法名
			}
			lsh = "pk0"+sblx+Utils.formatToTime()+"01";//如果要查询相关接口请不要动态生成，请填写请求过的固定值
			System.out.println("异步请求LSH：【"+lsh+"】");
			requestData = JsonUtils.getSendToTaxJson(interfaceCode, fpqqlsh, nsrsbh ,nsrmc , fpdm, fphm,sblx,sbbh, appid,contentPassword,callback_url,lsh);
		}/*else if((sblx).equals(Utils.SBLX_2)){
			appid = "";//appid
			contentPassword = "";//AES加密密钥
			nsrsbh = "";
			nsrmc = "";
			sbbh = "";

			fpqqlsh = "";
			fpdm = "";
			fphm = "";

			if(Utils.GP_FPKJ.equals(interfaceCode)){
				callback_url = callback_url+"?method=FPKJ";//拼接开具的方法名
			}
			lsh = "pk0"+sblx+Utils.formatToTime()+"01";//
			System.out.println("异步请求LSH：【"+lsh+"】");
			requestData = JsonUtils.getSendToTaxJson(interfaceCode, fpqqlsh, nsrsbh ,nsrmc , fpdm, fphm,sblx,sbbh, appid,contentPassword,callback_url,lsh);
		}*/else if((sblx).equals(Utils.SBLX_3)){
			//ukey需要根据用户自己的测试设备进行配置
			callback_url = "";
			requestData = JsonUtils.getSendToTaxJson(interfaceCode, fpqqlsh, nsrsbh,nsrmc ,fpdm, fphm,sblx,sbbh, appid,contentPassword,callback_url,lsh);
		}else if((sblx).equals(Utils.SBLX_4)){
			appid = "6d29f136114544bcc73edcce960c430231183cc192c433e2b9ebcad56e8ceb08";//appid
			contentPassword = "5EE6C2C11DD421F2";//AES加密密钥
			nsrsbh = "110109500321655";
			nsrmc = "百旺电子测试2";

			fpqqlsh = "TEST2020083114374201";
			fpdm = "050003521107";
			fphm = "35662145";

			callback_url = "";
			requestData = JsonUtils.getSendToTaxJson(interfaceCode, fpqqlsh, nsrsbh,nsrmc , fpdm, fphm,sblx,sbbh, appid,contentPassword,callback_url,lsh);
		}else if((sblx).equals(Utils.SBLX_5)){
			appid = "c288b35967a9a36fc897e89b01805451110d88507ce1aa1ab31756d872504767";//appid
			contentPassword = "80AB94991453FA0D";//AES加密密钥
			nsrsbh = "110109500321668";
			nsrmc = "新税控测试1668";
			sbbh = "214324531769";

			fpqqlsh = "xsk01102020061000012";
			fpdm = "088881200610";
			fphm = "17083768";

			callback_url = "";

			requestData = JsonUtils.getSendToTaxJson(interfaceCode, fpqqlsh, nsrsbh,nsrmc , fpdm, fphm,sblx,sbbh, appid,contentPassword,callback_url,lsh);
		}else{//如果是没有设备类型的接口随意选取测试账号进行验证

		}


		System.out.println("组装报文完毕,请求报文为:"+requestData+",开始请求。");
		Date requestStartDate = new Date();//初始化请求开始时间
		// 调用接口
		String rsData = RequestUtils.getHttpConnectResult(requestData,requestUrl);
		Date requestEndDate = new Date();//初始化请求结束时间
		System.out.println("请求完毕，耗时【"+(requestEndDate.getTime()-requestStartDate.getTime())+"ms】");
		Date endDate = new Date();//初始化结束时间
		System.out.println("请求接口结束，获得结果:" + rsData);
		System.out.println("总耗时【"+(endDate.getTime()-startDate.getTime())+"ms】");
	}
}
