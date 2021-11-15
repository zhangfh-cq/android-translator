<?php

class CommonTranslator
{
    private $app_id;
    private $sec_key;
    private $api_url;
    private $timeout;

    function __construct($app_id, $sec_key, $api_url, $timeout = 10)
    {
        $this->app_id = $app_id;
        $this->sec_key = $sec_key;
        $this->api_url = $api_url;
        $this->timeout = $timeout;
    }

    //翻译
    function translate($query, $from, $to)
    {
        $args = array(
            'q' => $query,
            'appid' => $this->app_id,
            'salt' => rand(10000, 99999),
            'from' => $from,
            'to' => $to,
        );
        $args['sign'] = $this->buildSign($query, $this->app_id, $args['salt'], $this->sec_key);
        $ret= $this->call($this->api_url, $args);
        $ret= json_decode($ret);
        return $ret;
    }

    //加密
    function buildSign($query, $app_id, $salt, $secKey)
    {
        $str = $app_id . $query . $salt . $secKey;
        return md5($str);
    }

    //发起网络请求
    function call($url, $args = null, $method = "post", $headers = array())
    {
        $ret = false;
        $i = 0;
        while ($ret === false) {
            if ($i > 1)
                break;
            if ($i > 0) {
                sleep(1);
            }
            $ret = $this->callOnce($url, $args, $method, false, $headers);
            $i++;
        }
        return $ret;
    }

    function callOnce($url, $args = null, $method = "post", $with_Cookie = false, $headers = array())
    {
        $ch = curl_init();
        if ($method == "post") {
            $data = $this->convert($args);
            curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
            curl_setopt($ch, CURLOPT_POST, 1);
        } else {
            $data = $this->convert($args);
            if ($data) {
                if (stripos($url, "?") > 0) {
                    $url .= "&$data";
                } else {
                    $url .= "?$data";
                }
            }
        }
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_TIMEOUT, $this->timeout);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        if (!empty($headers)) {
            curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        }
        if ($with_Cookie) {
            curl_setopt($ch, CURLOPT_COOKIEJAR, $_COOKIE);
        }
        $r = curl_exec($ch);
        curl_close($ch);
        return $r;
    }

    function convert($args)
    {
        $data = '';
        if (is_array($args)) {
            foreach ($args as $key => $val) {
                if (is_array($val)) {
                    foreach ($val as $k => $v) {
                        $data .= $key . '[' . $k . ']=' . rawurlencode($v) . '&';
                    }
                } else {
                    $data .= "$key=" . rawurlencode($val) . "&";
                }
            }
            return trim($data, "&");
        }
        return $args;
    }
}
