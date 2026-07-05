
<?php
/** curl 获取 https 请求  
* @param String $url 请求的url  
* @param Array $data 要发送的数据 
* @param Array $header 请求时发送的header  
* @param int $timeout 超时时间，默认30s  
*/   
function curl_https($url, $data=array(), $header=array(), $timeout=30){   
    $ch = curl_init();   
   // curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false); // 跳过证书检查   
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2); // 从证书中检查SSL加密算法是否存在   
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
    curl_setopt($ch, CURLOPT_VERBOSE, 1);
    curl_setopt($ch, CURLOPT_TIMEOUT, 30);


    curl_setopt($ch, CURLOPT_SSLCERT, "F:\\client.pem");
    curl_setopt($ch, CURLOPT_SSLCERTPASSWD, "123456");
    curl_setopt($ch, CURLOPT_SSLCERTTYPE, "PEM");

    curl_setopt($ch, CURLOPT_URL, $url);   
    curl_setopt($ch, CURLOPT_HTTPHEADER, $header);   
    curl_setopt($ch, CURLOPT_POST, true);   
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($data));   
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);   
    curl_setopt($ch, CURLOPT_TIMEOUT, $timeout);   
    $response = curl_exec($ch);   
    if($error=curl_error($ch)){   
        die($error);   
    }   
    curl_close($ch);   
    return $response;   
  
}    
$url = 'https://dev.fapiao.com:18944/fpt-rhqz/prepose';   
$data = array('name'=>'fdipzone');   
$headers = array("Content-type: application/json;charset='utf-8'", "Accept: application/json", 
    );
$response = curl_https($url, $data, $headers, 5);   

echo $response;   
?>