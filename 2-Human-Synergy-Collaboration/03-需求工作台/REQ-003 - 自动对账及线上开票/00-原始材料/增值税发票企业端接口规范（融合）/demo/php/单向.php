
<?php
/** curl 获取 https 请求  
* @param String $url 请求的url  
* @param Array $data 要发送的数据 
* @param Array $header 请求时发送的header  
* @param int $timeout 超时时间，默认60s
*/   
function curl_https($url, $data, $header=array(), $timeout=60){
    $ch = curl_init();   
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false); // 跳过证书检查   
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2); // 从证书中检查SSL加密算法是否存在   
    curl_setopt($ch, CURLOPT_URL, $url);   
    curl_setopt($ch, CURLOPT_HTTPHEADER, $header);   
    curl_setopt($ch, CURLOPT_POST, true);   
    curl_setopt($ch, CURLOPT_POSTFIELDS, $data);   
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);   
    curl_setopt($ch, CURLOPT_TIMEOUT, $timeout);   
    $response = curl_exec($ch);   
    if($error=curl_error($ch)){   
        die($error);   
    }   
    curl_close($ch);   
    return $response;   
  
}    
$url = 'https://dev.fapiao.com:19444/fpt-rhqz/prepose';   
$data = '{
    "interface":{
        "globalInfo":{
            "appId":"b80d652bf2065668c28d3374cf937d850ad5f4fcff9dabf51affeab393664fe3",
            "interfaceId":"",
            "interfaceCode":"GP_LGXXCX",
            "requestCode":"DZFPQZ",
            "requestTime":"2020-07-21 09:58:53",
            "responseCode":"DS",
            "dataExchangeId":"DZFPQZDFXJ10012020072198A6123D0"
        },
        "returnStateInfo":{
            "returnCode":"",
            "returnMessage":""
        },
        "data":{
            "dataDescription":{
                "zipCode":"0"
            },
            "content":"ewoJIlJFUVVFU1RfQ09NTU9OX0xHWFhDWCI6IHsKCQkiTlNSU0JIIjogIjExMDEwOTUwMDMyMTY1NCIsCgkJIlNCTFgiOiAiNCIsCgkJIlNCQkgiOiAiIiwKCQkiRlBMWERNIjogIjAyNiIsCgkJIktQWkRETSI6ICIxNjU0ZHozIiwKCQkiRlBaVCI6ICIxIiwKCQkiTEdRWFgiOiAiMCIKCX0KfQ==",
            "contentKey":"3CwCUHfeA+Y5XpGfSohRyiBigTn4s9ygS0kjRaJEU0XbWorKoHx8YbGGeYveB8ELSdCrXDulRQxoFltJA9l2AfRsh3yxtaSiTPlAxMclUQ0="
        }
    }
}';   
$headers = array("Content-type: application/json;charset='utf-8'", "Accept: application/json", 
    );
$response = curl_https($url, $data, $headers, 5);   

echo $response;   
?>