<?php

/**
 * APP更新POST接口
 * 请求示例：{"auth_key":"123456789"}
 * 响应正确示例：{"error_code":0,"error_msg":null,"version":1,"log":"示例更新日志","url":"https:\/\/example.com"}
 * 响应错误示例：{"error_code":50003,"error_msg":"请求数据格式不正确！"}
 */

require_once('../translator.php');

// 设置顶层错误和异常处理器
Translator::setTopHandler();

// 获取请求内容
$request = json_decode(file_get_contents('php://input'));
// 本地模拟请求
//$request = json_decode('{"auth_key":"'.Translator::$UPDATE_AUTH_KEY.'"}');
// 记录接收的请求
Translator::log('收到请求：' . json_encode($request, JSON_UNESCAPED_UNICODE), Translator::$ACCESS_LOG_NAME);

// 检查接收的数据
if ($request === null || !is_object($request)) {
    $rep_data['error_code'] = 50003;
    $rep_data['error_msg'] = '请求数据格式不正确！';
    $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
    echo $rep_json;
    Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
    exit();
} else if (!property_exists($request, 'auth_key') || $request->auth_key !== Translator::$UPDATE_AUTH_KEY) {
    $rep_data['error_code'] = 50004;
    $rep_data['error_msg'] = '参数auth_key不正确！';
    $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
    echo $rep_json;
    Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
    exit();
} else {
    $rep_data['error_code'] = 0;
    $rep_data['error_msg'] = null;
    $rep_data['version'] = Translator::$NEWEST_VERSION;
    $rep_data['log'] = Translator::$UPDATE_LOG;
    $rep_data['url'] = Translator::$DOWNLOAD_URL;
    $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
    echo $rep_json;
    Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
}
