<?php

class Translator
{
    // 必选修改配置项
    public static $BAIDU_APP_ID = '123456789';  // 百度翻译开放平台APP ID
    public static $BAIDU_SEC_KEY = '123456789abc'; // 百度翻译开放平台密钥

    public static $COMMON_AUTH_KEY = '123456789abc'; // 通用翻译接口使用的Key

    public static $IMAGE_AUTH_KEY = '123456789abc';  // 图片翻译接口使用的Key

    public static $LEGAL_WORD_AUTH_KEY = '123456789abc'; // 合法单词判断接口使用的Key
    public static $DATABASE_HOSTNAME = 'localhost';  // 数据库地址
    public static $DATABASE_USERNAME = 'legal';  // 数据库用户名
    public static $DATABASE_PASSWORD = '123456789abc'; // 数据库密码
    public static $DATABASE_NAME = 'legal_en_word'; // 数据库名
    public static $DATABASE_PORT = 3306; // 数据库端口

    public static $UPDATE_AUTH_KEY = '123456789abc'; // 更新接口使用的Key
    public static $NEWEST_VERSION = 1.0; // 最新版本号
    public static $UPDATE_LOG = '示例更新日志'; // 更新日志
    public static $DOWNLOAD_URL = 'https://example.com/versions/1.0/translator.apk'; // 下载地址


    // 可选修改配置项
    public static $ACCESS_LOG_NAME = 'access.log';
    public static $ERROR_LOG_NAME = 'error.log';
    public static $IMAGE_PATH = './images/'; // 图片保存目录


    // 百度没改就不管
    public static $COMMON_BAIDU_API_URL = 'http://api.fanyi.baidu.com/api/trans/vip/translate'; // 百度翻译开放平台通用翻译接口URL
    public static $IMAGE_BAIDU_API_URL = 'https://fanyi-api.baidu.com/api/trans/sdk/picture'; // 百度翻译开放平台图片翻译接口URL


    // 记录日志
    public static function log($content, $filename, $log_dir = './logs/', $max_size = 104857600)
    {
        $filename = $log_dir . $filename;
        // 检查目录存在
        if (file_exists($log_dir) === false) {
            if (mkdir(iconv("UTF-8", "GBK", $log_dir), 0755, true) === false) {
                error_log('创建日志文件[' . $log_dir . ']失败！');
                return false;
            }
        }
        // 超出大小限制删除日志
        if (file_exists($filename) === true && (abs(filesize($filename)) > $max_size)) {
            if (unlink($filename) === false) {
                error_log('删除日志文件[' . $filename . ']失败！');
                return false;
            }
        }
        // 记录日志
        if (file_put_contents($filename, '[' . date("Y-m-d") . ' ' . date('H:i:s') . ']: '
                . $content . "\n", FILE_APPEND) === false) {
            error_log('记录日志[' . $filename . ']失败！');
            return false;
        }

        return true;
    }

    public static function setTopHandler()
    {
        // 设置顶层异常处理器
        set_exception_handler(function ($e) {
            $rep_data['error_code'] = 50001;
            $rep_data['error_msg'] = '发生未知异常';
            $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
            echo $rep_json;
            self::log('响应数据：' . $rep_json, self::$ACCESS_LOG_NAME);
            self::log($e->getMessage() . ' at file:' . $e->getFile() . ' on line:' . $e->getLine(), self::$ERROR_LOG_NAME);
            exit();
        });
        // 设置顶层错误处理器
        set_error_handler(function ($errorLevel, $errorMsg, $errorFile, $errorLine) {
            $rep_data['error_code'] = 50002;
            $rep_data['error_msg'] = '发生未知错误！';
            $rep_json = json_encode($rep_data, JSON_UNESCAPED_UNICODE);
            echo $rep_json;
            Translator::log('响应数据：' . $rep_json, self::$ACCESS_LOG_NAME);
            Translator::log('Error:[' . $errorLevel . ']' . $errorMsg . ' at file:' . $errorFile . ' on line:' . $errorLine, self::$ERROR_LOG_NAME);
            exit();
        });
    }
}
