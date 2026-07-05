package com.baiwang.utility.encrypt;

import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class MyAES {
	/** 
	 * 加密 
	 *  
	 * @param content 需要加密的内容 
	 * @param password  加密密码 
	 * @return 
	 */  
	public static byte[] encrypt(byte[] content, String password) {  
	        try {             
	                byte[] enCodeFormat = password.getBytes();
	                SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");  
	                Cipher cipher = Cipher.getInstance("AES");// 创建密码器  
	                cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化  
	                byte[] result = cipher.doFinal(content);  
	                return result; // 加密  
	        }  catch (Exception e) {  
	                e.printStackTrace();  
	        }  
	        return null;  
	}  
	
	/**解密 
	 * @param content  待解密内容 
	 * @param password 解密密钥 
	 * @return 
	 */  
	public static byte[] decrypt(byte[] content, String password) {  
	        try {  
	        		 byte[] enCodeFormat = password.getBytes();
	                 SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");              
	                 Cipher cipher = Cipher.getInstance("AES");// 创建密码器  
	                 cipher.init(Cipher.DECRYPT_MODE, key);// 初始化  
	                 byte[] result = cipher.doFinal(content);  
	                 return result; // 加密  
	        } catch (Exception e) {  
	                e.printStackTrace();  
	        }  
	        return null;  
	} 
	
	public static String MD5(byte[] data) throws Exception {
		MessageDigest md5 = MessageDigest.getInstance("MD5"); 
		md5.update(data);
        byte b[] = md5.digest(); 
        int i; 
        StringBuffer buf = new StringBuffer(""); 
        for (int offset = 0; offset < b.length; offset++) { 
                i = b[offset]; 
                if(i<0) i+= 256; 
                if(i<16) 
                buf.append("0"); 
                buf.append(Integer.toHexString(i)); 
        } 
        return buf.toString();
    }
	
	public static byte[] decryptBASE64(String key) throws Exception {  
        return (new BASE64Decoder()).decodeBuffer(key);  
    }  
  
    public static String encryptBASE64(byte[] key) throws Exception {  
        return (new BASE64Encoder()).encodeBuffer(key);  
    }
    
}
