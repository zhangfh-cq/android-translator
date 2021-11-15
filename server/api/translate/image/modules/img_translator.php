<?php

class ImgTranslator
{
    private $app_id;
    private $sec_key;
    private $api_url;
    private $salt;
    private $cuid;
    private $mac;

    public function __construct($app_id, $sec_key, $api_url)
    {
        $this->app_id = $app_id;
        $this->sec_key = $sec_key;
        $this->api_url = $api_url;
        $this->cuid = 'APICUID';
        $this->mac = 'MAC';
    }

    public function translate($image_path, $from, $to)
    {
        $this->salt = rand(100000000, 999999999);
        $sign = md5($this->app_id . md5(file_get_contents($image_path)) . $this->salt . $this->cuid . $this->mac . $this->sec_key);
        $url = $this->api_url . '?appid=' . $this->app_id . '&from=' . $from . '&to=' . $to . '&salt=' . $this->salt . '&cuid=' . $this->cuid . '&mac=' . $this->mac . '&sign=' . $sign;
        $header = array(
            'Content-Type' => 'multipart/form-data',
        );
        $sendData = array(
            'image' => '@' . realpath($image_path) . ';type=image/jpeg',
        );
        if (class_exists('\CURLFile')) {
            $sendData['image'] = new \CURLFile(realpath($image_path));
        }

        $ch = curl_init();
        curl_setopt($ch, CURLOPT_HTTPHEADER, $header);
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $sendData);
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_TIMEOUT, 10);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false); // 信任任何证书
        $result = curl_exec($ch);
        $http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        if ($result === false || $http_code !== 200) {
            throw new Exception('图片发送失败！');
        } else {
            curl_close($ch);
            return json_decode($result);
        }
    }
}
