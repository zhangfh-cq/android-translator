<?php

/**
 * 合法英语单词检查POST接口
 * JSON请求示例：{"auth_key":"123456789","word":"hello"}
 * JSON正确响应示例：{"error_code":0,"error_msg":null,legal:true}
 * JSON错误响应示例：{"error_code":50003,"error_msg":"请求数据格式不正确！"}
 */

include_once('../../translator.php');
include_once('./modules/database_manager.php');

// 设置顶层错误和异常处理器
Translator::setTopHandler();

// 获取请求内容
$request = json_decode(file_get_contents('php://input'));
// 本地模拟请求
//$request = json_decode('{"auth_key":"'.Translator::$LEGAL_WORD_AUTH_KEY.'","word":"hello"}');
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
} else if (!property_exists($request, 'auth_key') || $request->auth_key !== Translator::$LEGAL_WORD_AUTH_KEY) {
    $rep_data['error_code'] = 50004;
    $rep_data['error_msg'] = '参数auth_key不正确！';
    $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
    echo $rep_json;
    Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
    exit();
} else if (!property_exists($request, 'word')) {
    $rep_data['error_code'] = 50005;
    $rep_data['error_msg'] = '参数word不正确！';
    $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
    echo $rep_json;
    Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
    exit();
} else {
    // 实例化数据库对象
    $database_manager = new DataBaseManager(Translator::$DATABASE_HOSTNAME, Translator::$DATABASE_USERNAME,
        Translator::$DATABASE_PASSWORD, Translator::$DATABASE_NAME, Translator::$DATABASE_PORT);
    try {
        $database_manager->connect();
        $request->word = $database_manager->real_escape_string($request->word);
        $sql = "SELECT * FROM `EnWords` WHERE word=" . "'" . $request->word . "'";
        $query_result = $database_manager->query($sql);
        if ($query_result->num_rows !== 0) {
            $rep_data['legal'] = true;
        } else {
            $rep_data['legal'] = false;
        }
        $database_manager->close();

        $rep_data['error_code'] = 0;
        $rep_data['error_msg'] = null;
        $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
        echo $rep_json;
        Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
    } catch (Exception $e) {
        $rep_data['error_code'] = 50006;
        $rep_data['error_msg'] = '单词合法性检查功能出现异常！';
        $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
        echo $rep_json;
        Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
        Translator::log($e->getMessage() . ' at file:' . $e->getFile() . ' on line:' . $e->getLine(), Translator::$ERROR_LOG_NAME);
    }
}
