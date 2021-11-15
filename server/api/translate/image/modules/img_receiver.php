<?php

class ImgReceiver
{
    public function receiveImg($img_path, $img_key)
    {
        if (!file_exists($img_path)) {
            if (!mkdir(iconv('UTF-8', 'GBK', $img_path), 0755, true)) {
                throw new Exception('创建图片文件夹失败！');
            }
        }

        $img_name = $_FILES[$img_key]['name'];
        $tmp_name = $_FILES[$img_key]['tmp_name'];
        if (move_uploaded_file($tmp_name, $img_path . $img_name)) {
            return $img_name;
        } else {
            throw new Exception('接收图片失败！');
        }
    }
}
