<?php

/**
 * 图片翻译POST接口
 * 请求URL：/index.php?auth_key=123456789&from=auto&to=en
 * POST Body：img[图片]
 * JSON正确响应示例：{"error_code":"0","error_msg":"success","data":{"from":"zh","to":"en",
 * "content":[{"src":"404 ","dst":"four hundred and four","rect":"6 40 86 40","lineCount":1,
 * "points":[{"x":2,"y":39},{"x":94,"y":38},{"x":95,"y":80},{"x":2,"y":82}],"erasedImg":""}],
 * "sumSrc":"404 ","sumDst":"four hundred and four","erasedImg":""}}
 * JSON错误响应示例：{"error_code":50003,"error_msg":"请求数据格式不正确！"}
 */

require_once('../../translator.php');
require_once('./modules/img_receiver.php');
require_once('./modules/img_compressor.php');
require_once('./modules/img_translator.php');

// 设置顶层错误和异常处理器
Translator::setTopHandler();

// 记录接收的请求
Translator::log('收到请求的参数：' . $_SERVER['REQUEST_URI'], Translator::$ACCESS_LOG_NAME);

// 检查接收的数据
if (!array_key_exists('auth_key', $_REQUEST) || $_REQUEST['auth_key'] !== Translator::$IMAGE_AUTH_KEY) {
    $rep_data['error_code'] = 50003;
    $rep_data['error_msg'] = '参数auth_key不正确！';
    $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
    echo $rep_json;
    Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
    exit();
} else if (!array_key_exists('from', $_REQUEST)) {
    $rep_data['error_code'] = 50004;
    $rep_data['error_msg'] = '参数from不正确！';
    $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
    echo $rep_json;
    Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
    exit();
} else if (!array_key_exists('to', $_REQUEST)) {
    $rep_data['error_code'] = 50005;
    $rep_data['error_msg'] = '参数to不正确！';
    $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
    echo $rep_json;
    Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
    exit();
} else {
    if (!empty($_FILES)) {
        try {
            $img_name = null;
            $compressed_img_name = null;
            // 接收图片
            $img_receiver = new ImgReceiver();
            $img_name = $img_receiver->receiveImg(Translator::$IMAGE_PATH, 'img');

            // 压缩图片
            $img_compressor = new ImgCompressor(Translator::$IMAGE_PATH . $img_name, 1);
            $img_compressor->compressImg(Translator::$IMAGE_PATH . 'compressed_' . $img_name);
            $compressed_img_name = 'compressed_' . $img_name;

            // 图片翻译
            $img_translator = new ImgTranslator(Translator::$BAIDU_APP_ID, Translator::$BAIDU_SEC_KEY, Translator::$IMAGE_BAIDU_API_URL);
            try {
                $baidu_rep = $img_translator->translate(Translator::$IMAGE_PATH . $compressed_img_name, $_REQUEST['from'], $_REQUEST['to']);
                $baidu_rep = json_encode($baidu_rep, JSON_UNESCAPED_UNICODE);
                echo $baidu_rep;
                Translator::log('响应数据：' . $baidu_rep, Translator::$ACCESS_LOG_NAME);
            } catch (Exception $e) {
                $rep_data['error_code'] = 50006;
                $rep_data['error_msg'] = '翻译功能出现异常！';
                $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
                echo $rep_json;
                Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
                Translator::log($e->getMessage() . ' at file:' . $e->getFile() . ' on line:' . $e->getLine(), Translator::$ERROR_LOG_NAME);
                exit();
            }
        } catch (Exception $e) {
            $rep_data['error_code'] = 50007;
            $rep_data['error_msg'] = '图片操作异常！';
            $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
            echo $rep_json;
            Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
            Translator::log($e->getMessage() . ' at file:' . $e->getFile() . ' on line:' . $e->getLine(), Translator::$ERROR_LOG_NAME);
            exit();
        }
    } else {
        $rep_data['error_code'] = 50008;
        $rep_data['error_msg'] = '上传的图片为空！';
        $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
        echo $rep_json;
        Translator::log('响应数据：' . $rep_json, Translator::$ACCESS_LOG_NAME);
        exit();
    }

}


