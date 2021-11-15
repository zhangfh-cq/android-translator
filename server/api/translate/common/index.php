<?php

/**
 * 通用翻译POST接口
 * JSON请求示例：{"auth_key":"123456789","query":"你好", "from":"auto","to":"en"}
 * JSON正确响应示例：{"from":"zh","to":"en","trans_result":[{"src":"你好","dst":"Hello"}]}
 * JSON错误响应示例：{"error_code":"58001","error_msg":"INVALID_TO_PARAM"}
 */

include_once("../../translator.php");
include_once("./modules/common_translator.php");

// 设置顶层错误和异常处理器
Translator::setTopHandler();

// 获取请求内容
$request = json_decode(file_get_contents('php://input'));
// 本地模拟请求
//$request = json_decode('{"auth_key":"'.Translator::$COMMON_AUTH_KEY.'","query":"你好", "from":"auto","to":"en"}');
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
} else if (!property_exists($request, 'auth_key') || $request->auth_key !== Translator::$COMMON_AUTH_KEY) {
    $rep_data['error_code'] = 50004;
    $rep_data['error_msg'] = '参数auth_key不正确！';
    $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
    echo $rep_json;
    Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
    exit();
} else if (!property_exists($request, 'query')) {
    $rep_data['error_code'] = 50005;
    $rep_data['error_msg'] = '参数query不正确！';
    $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
    echo $rep_json;
    Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
    exit();
} else if (!property_exists($request, 'from')) {
    $rep_data['error_code'] = 50006;
    $rep_data['error_msg'] = '参数from不正确！';
    $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
    echo $rep_json;
    Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
    exit();
} else if (!property_exists($request, 'to')) {
    $rep_data['error_code'] = 50007;
    $rep_data['error_msg'] = '参数to不正确！';
    $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
    echo $rep_json;
    Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
    exit();
} else {
    // 实例化翻译对象
    $translator = new CommonTranslator(Translator::$BAIDU_APP_ID, Translator::$BAIDU_SEC_KEY, Translator::$COMMON_BAIDU_API_URL);
    try {
        $baidu_rep = $translator->translate($request->query, $request->from, $request->to);
        $baidu_rep = json_encode($baidu_rep, JSON_UNESCAPED_UNICODE);
        echo $baidu_rep;
        Translator::log('响应数据：' . $baidu_rep, Translator::$ACCESS_LOG_NAME);
    } catch (Exception $e) {
        $rep_data['error_code'] = 50008;
        $rep_data['error_msg'] = '翻译功能出现异常！';
        $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
        echo $rep_json;
        Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
        Translator::log($e->getMessage() . ' at file:' . $e->getFile() . ' on line:' . $e->getLine(), Translator::$ERROR_LOG_NAME);
    }
}
