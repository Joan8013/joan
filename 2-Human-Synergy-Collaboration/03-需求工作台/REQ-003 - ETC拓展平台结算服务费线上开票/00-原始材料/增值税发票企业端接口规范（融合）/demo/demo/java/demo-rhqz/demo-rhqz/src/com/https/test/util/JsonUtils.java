package com.https.test.util;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.baiwang.utility.encrypt.MyAES;

import sun.misc.BASE64Encoder;

public class JsonUtils {

	/**
	 * 拼通用报文(json)
	 * 
	 * @return
	 * @throws Exception 
	 */

	public static String getSendToTaxJson(String interfaceCode, String fpqqlsh,String nsrsbh,String nsrcmc, String fpdm,String fphm,String sblx,String sbbh ,String appid,String contentPassword,String callback_url,String lsh)
			throws Exception {
		String content = "";
		StringBuffer sb = new StringBuffer("");
		
		sb.append("{");
		sb.append("\"interface\": {");
		sb.append("\"globalInfo\": {");
		sb.append("\"appId\": \"").append(appid).append("\","); 
		sb.append("\"interfaceId\": \"\","); 
		sb.append("\"interfaceCode\": \"").append(interfaceCode).append("\","); 
		sb.append("\"requestCode\": \"DZFPQZ\",");
		sb.append("\"requestTime\": \"").append(new Date()).append("\","); 
		sb.append("\"responseCode\": \"Ds\","); 
		sb.append("\"dataExchangeId\": \"").append("DZFPQZ").append(interfaceCode).append(Utils.formatToDay()).append( Utils.randNineData()).append( "\"");
		sb.append("},"); 
		sb.append("\"returnStateInfo\": {");
		sb.append("\"returnCode\": \"\","); 
		sb.append("\"returnMessage\": \"\"");
		sb.append("},"); 
		sb.append("\"data\": {");
		sb.append("\"dataDescription\": {");
		sb.append("\"zipCode\": \"0\"");
		sb.append("},"); 
		sb.append("\"content\": \"");
		if (interfaceCode.equals(Utils.GP_FPKJ)) {
			content = getUploadContent(nsrsbh,nsrcmc,sblx,sbbh,callback_url,lsh);
		}	else if (interfaceCode.equals(Utils.GP_FPCX)) {
			content = getSearchContent(nsrsbh,fpqqlsh);
		}else if (interfaceCode.equals(Utils.GP_SKSBXXCX)) {
			content = getSksbxxcx(nsrsbh, sblx, sbbh, callback_url,lsh);
		}else if (interfaceCode.equals(Utils.GP_YBJGCX)) {
			content = getYbjgcx(nsrsbh, sbbh ,lsh);
		}
		content  = content.replaceAll("\r\n", "").replaceAll("\n", "");//json的报文不允许有换行，base64会产生。因此此处做去换行处理。
		sb.append(content).append("\",");
		sb.append("\"contentKey\":\"");
		String contentSha256 =Sha256Utils.SHA256Encrypt(content.getBytes("UTF-8"));
		String contentKey = MyAES.encryptBASE64(MyAES.encrypt(contentSha256.getBytes("UTF-8"), contentPassword)).replaceAll("\r\n", "").replaceAll("\n", "");
		sb.append(contentKey).append("\"");;
		sb.append("}");
		sb.append("}");
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * 根据加密上传发票内容报文（发票开具）
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getUploadContent(String nsrsbh,String nsrmc,String sblx, String sbbh,String callback_url,String lsh) throws UnsupportedEncodingException {
		StringBuffer content = new StringBuffer("{");
		content.append("\"REQUEST_COMMON_FPKJ\": {");
		content.append("\"NSRSBH\": \""+nsrsbh+"\",");
		content.append("\"SBLX\": \""+sblx+"\",");
		content.append("\"SBBH\": \""+sbbh+"\",");
		content.append("\"FPQQLSH\": \"TEST" + Utils.formatToTime() + "01"+ "\",");
		content.append("\"ZSFS\": \"0\",");
		content.append("\"KPLX\": \"0\",");
		content.append("\"BMB_BBH\": \"32.0\",");
		content.append("\"FPLXDM\": \"026\",");
		content.append("\"XSF_NSRSBH\": \""+nsrsbh+"\",");
		content.append("\"XSF_MC\": \""+nsrmc+"\",");
		content.append("\"XSF_DZDH\": \"南山区蛇口、83484949\",");
		content.append("\"XSF_YHZH\": \"xx银行、88888888888\",");
		content.append("\"GMF_NSRSBH\": \"\",");
		content.append("\"GMF_MC\": \"张三\",");
		content.append("\"GMF_DZDH\": \"购买方地址、电话\",");
		content.append("\"GMF_YHZH\": \"购买方银行账号\",");
		content.append("\"GMF_SJH\": \"\",");
		content.append("\"GMF_DZYX\": \"\",");
		content.append("\"FPT_ZH\": \"\",");
		content.append("\"WX_OPENID\": \"\",");
		content.append("\"KPR\": \"开票人\",");
		content.append("\"SKR\": \"收款人\",");
		content.append("\"FHR\": \"复核人\",");
		content.append("\"YFP_DM\": \"\",");
		content.append("\"YFP_HM\": \"\",");
		content.append("\"JSHJ\": \"113\",");
		content.append("\"HJJE\": \"100\",");
		content.append("\"HJSE\": \"13\",");
		content.append("\"KCE\": \"\",");
		content.append("\"BZ\": \"json测试开票备注\",");
		content.append("\"HYLX\": \"\",");
		content.append("\"BY1\": \"\",");
		content.append("\"BY2\": \"\",");
		content.append("\"BY3\": \"\",");
		content.append("\"BY4\": \"\",");
		content.append("\"BY5\": \"\",");
		content.append("\"BY6\": \"\",");
		content.append("\"BY7\": \"\",");
		content.append("\"BY8\": \"\",");
		content.append("\"BY9\": \"\",");
		content.append("\"BY10\": \"\",");
		content.append("\"WX_ORDER_ID\": \"\",");
		content.append("\"WX_APP_ID\": \"\",");
		content.append("\"ZFB_UID\": \"\",");
		content.append("\"COMMON_FPKJ_XMXX\": [{");
		content.append("\"FPHXZ\": \"0\",");
		content.append("\"SPBM\": \"1010101050000000000\",");
		content.append("\"ZXBM\": \"\",");
		content.append("\"YHZCBS\": \"\",");
		content.append("\"LSLBS\": \"\",");
		content.append("\"ZZSTSGL\": \"\",");
		content.append("\"XMMC\": \"红高粱\",");
		content.append("\"GGXH\": \"500克\",");
		content.append("\"DW\": \"袋\",");
		content.append("\"XMSL\": \"1\",");
		content.append("\"XMDJ\": \"50\",");
		content.append("\"XMJE\": \"50\",");
		content.append("\"SL\": \"0.13\",");
		content.append("\"SE\": \"6.5\",");
		content.append("\"BY1\": \"\",");
		content.append("\"BY2\": \"\",");
		content.append("\"BY3\": \"\",");
		content.append("\"BY4\": \"\",");
		content.append("\"BY5\": \"\"},");
		content.append("{");
		content.append("\"FPHXZ\": \"0\",");
		content.append("\"SPBM\": \"1010101010000000000\",");
		content.append("\"ZXBM\": \"\",");
		content.append("\"YHZCBS\": \"\",");
		content.append("\"LSLBS\": \"\",");
		content.append("\"ZZSTSGL\": \"\",");
		content.append("\"XMMC\": \"东北大米\",");
		content.append("\"GGXH\": \"500克\",");
		content.append("\"DW\": \"袋\",");
		content.append("\"XMSL\": \"1\",");
		content.append("\"XMDJ\": \"50\",");
		content.append("\"XMJE\": \"50\",");
		content.append("\"SL\": \"0.13\",");
		content.append("\"SE\": \"6.5\",");
		content.append("\"BY1\": \"\",");
		content.append("\"BY2\": \"\",");
		content.append("\"BY3\": \"\",");
		content.append("\"BY4\": \"\",");
		content.append("\"BY5\": \"\"}],");
		content.append("\"CALLBACK_URL\": \""+callback_url+"\",");
		content.append("\"LSH\": \""+lsh+"\"");
		content.append("}");
		content.append("}");
		return  new BASE64Encoder().encodeBuffer(content.toString().getBytes("UTF-8"));
	}

	/**
	 * 获取加密查询报文内容(发票查询报文)
	 * @param nsrsbh
	 * @param fpqqlsh
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getSearchContent(String nsrsbh ,String fpqqlsh)
			throws UnsupportedEncodingException {
		StringBuffer content = new StringBuffer("{");
		content.append("\"REQUEST_COMMON_FPCX\":{");
		content.append("\"FPQQLSH\":\"").append(fpqqlsh).append("\",");
		content.append("\"XSF_NSRSBH\":\"").append(nsrsbh).append("\"}}");
		return new BASE64Encoder().encodeBuffer(content.toString().getBytes("UTF-8"));
	}

	/**
	 * 获取加密查询报文内容(税控设备查询报文)
	 * @param nsrsbh
	 * @param sblx
	 * @param sbbh
	 * @param callback_url
	 * @param lsh
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getSksbxxcx(String nsrsbh, String sblx, String sbbh, String callback_url,String lsh)
			throws UnsupportedEncodingException {
		StringBuffer content = new StringBuffer("{");
		content.append("\"REQUEST_COMMON_SKSBXXCX\":{");
		content.append("\"NSRSBH\":\"").append(nsrsbh).append("\",");
		content.append("\"SBLX\":\"").append(sblx).append("\",");
		content.append("\"SBBH\":\"").append(sbbh).append("\",");
		content.append("\"CALLBACK_URL\":\"").append(callback_url).append("\",");
		content.append("\"LSH\":\"").append(lsh).append("\"}}");
		return new BASE64Encoder().encodeBuffer(content.toString().getBytes("UTF-8"));
	}

	/**
	 * 获取加密查询报文内容(异步请求结果查询报文)
	 * @param nsrsbh
	 * @param sbbh
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getYbjgcx(String nsrsbh, String sbbh, String lsh)
			throws UnsupportedEncodingException {
		StringBuffer content = new StringBuffer("{");
		content.append("\"REQUEST_COMMON_YBJGCX\":{");
		content.append("\"NSRSBH\":\"").append(nsrsbh).append("\",");
		content.append("\"SBBH\":\"").append(sbbh).append("\",");
		content.append("\"INTERFACE_CODE\":\"").append(Utils.GP_FPKJ).append("\",");
		content.append("\"LSH\":\"").append("pk012020090418002001").append("\"}}");
		return new BASE64Encoder().encodeBuffer(content.toString().getBytes("UTF-8"));
	}

}
