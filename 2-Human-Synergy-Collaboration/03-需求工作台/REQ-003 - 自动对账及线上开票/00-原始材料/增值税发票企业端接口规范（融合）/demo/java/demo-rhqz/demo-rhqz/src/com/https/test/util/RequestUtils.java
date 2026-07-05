package com.https.test.util;

import org.apache.commons.io.IOUtils;

import javax.net.ssl.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;


/**
 * 请求工具类
 *
 * @author yuanxiaojun
 *
 */
public class RequestUtils {
	private static SSLContext sslContext;
	public static String KEY_STORE_FILE = "D:\\keys\\test\\testISSUE.pfx";
	public static String KEY_STORE_PASS = "123456";

	/**
	 * https post请求方式
	 *
	 * @param xml
	 * @param address
	 * @return
	 */
	public static String getHttpConnectResult(String xml, String address) {
		String resultData = "";
		System.out.println("http请求开始，请求地址：" + address);
		OutputStream wr = null;
		HttpsURLConnection conn = null;
		try {
			URL url = new URL(address);
			conn = (HttpsURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(60000);// 设置连接主机的超时时间
			conn.setReadTimeout(60000);// 设置从主机读取数据的超时时间
			conn.setRequestProperty("Content-type", "application/json"); //设置content-type

			SSLSocketFactory ssf = getSSLContext().getSocketFactory();
			conn.setSSLSocketFactory(ssf);
			wr = conn.getOutputStream();
			wr.write(xml.getBytes("utf-8"));
			wr.flush();
			resultData = IOUtils.toString(conn.getInputStream(), "utf-8");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println("http请求失败！请求地址不正确！请求地址：" + address);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("http请求失败！发生i/o错误，请求地址：" + address);
		} finally {
			try {
				if (wr != null) {
					wr.close();
				}
				if (conn != null) {
					conn.disconnect();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultData;
	}

	public static SSLContext getSSLContext() {
		if (sslContext == null) {
			try {
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				kmf.init(getkeyStore(), KEY_STORE_PASS.toCharArray());
				KeyManager[] keyManagers = kmf.getKeyManagers();
				sslContext=SSLContext.getInstance("SSL");
				TrustManager[] trustManagers={new MyX509TrustManager()};
				sslContext = SSLContext.getInstance("TLSv1.2");
				sslContext.init(keyManagers, trustManagers, new SecureRandom());
				HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				});
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (UnrecoverableKeyException e) {
				e.printStackTrace();
			} catch (KeyStoreException e) {
				e.printStackTrace();
			} catch (KeyManagementException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sslContext;
	}

	public static KeyStore getkeyStore() {
		KeyStore keySotre = null;
		try {
			keySotre = KeyStore.getInstance("PKCS12");
			FileInputStream fis = new FileInputStream(new File(KEY_STORE_FILE));
			keySotre.load(fis, KEY_STORE_PASS.toCharArray());
			fis.close();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		return keySotre;
	}


}
