package com.https.test.action;

import com.baiwang.utility.encrypt.MyAES;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

public class CallBackDemoAction extends HttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String secretkey = "F6AD954A498AEDBE";//AES加密密钥

		String reqData = req.getParameter("req");//获取数据报文
		String method = req.getParameter("method");// 获取开票结果的方法名
		String rsData = "";
		if("FPKJ".equals(method)){
			rsData = syncInvoiceResult(reqData,secretkey);
		}
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Cache-Control", "no-cache;must-revalidate");
		PrintWriter pw = null;
		try{
			pw = resp.getWriter();
			pw.write(rsData);
		}finally{
			if(pw!=null)
				pw.close();
		}
		
	}

	public String syncInvoiceResult(String reqData, String secretkey){
		String resStr =null;
		System.out.println("【回调地址接口Demo】====开始进行处理开票结果了=====req："+reqData);
		try {
			String decode = URLDecoder.decode(reqData, "UTF-8");
			System.out.println("【回调地址接口Demo】====进行URLDecoder解密后的结果---"+decode);
			//Base64解密
			byte[] decryptResultBytes = MyAES.decryptBASE64(decode);
			//Aes解密
			decryptResultBytes = MyAES.decrypt(decryptResultBytes,secretkey);
            String decryptResult = new String(decryptResultBytes, "UTF-8");
            System.out.println("【回调地址接口Demo】====进行Aes解密后：" + decryptResult);
            //业务处理
            //返回给发票通处理结果
            resStr="<RESPONSE><RSCODE>0000</RSCODE><RSMSG>更新成功</RSMSG></RESPONSE>";
		} catch (Exception e) {
			e.printStackTrace();
			resStr="<RESPONSE><RSCODE>9999</RSCODE><RSMSG>系统异常！！！</RSMSG></RESPONSE>";
		}
		System.out.println(resStr);
		return resStr;
	}

}
