package cn.alsaces.translator.util;

import java.util.ArrayList;
import java.util.HashMap;

public class LangUtil {
    private static final ArrayList<HashMap<String, String>> TEXT_TRANS_LANG_ARRAY_LIST = new ArrayList<HashMap<String, String>>() {{
        add(getHashMap("自动检测", "auto"));
        add(getHashMap("中文", "zh"));
        add(getHashMap("英语", "en"));
        add(getHashMap("粤语", "yue"));
        add(getHashMap("文言文", "wyw"));
        add(getHashMap("日语", "jp"));
        add(getHashMap("韩语", "kor"));
        add(getHashMap("法语", "fra"));
        add(getHashMap("西班牙语", "spa"));
        add(getHashMap("泰语", "th"));
        add(getHashMap("阿拉伯语", "ara"));
        add(getHashMap("俄语", "ru"));
        add(getHashMap("葡萄牙语", "pt"));
        add(getHashMap("德语", "de"));
        add(getHashMap("意大利语", "it"));
        add(getHashMap("希腊语", "el"));
        add(getHashMap("荷兰语", "nl"));
        add(getHashMap("波兰语", "pl"));
        add(getHashMap("保加利亚语", "bul"));
        add(getHashMap("爱沙尼亚语", "est"));
        add(getHashMap("丹麦语", "dan"));
        add(getHashMap("芬兰语", "fin"));
        add(getHashMap("捷克语", "cs"));
        add(getHashMap("罗马尼亚语", "rom"));
        add(getHashMap("斯洛文尼亚语", "slo"));
        add(getHashMap("瑞典语", "swe"));
        add(getHashMap("匈牙利语", "hu"));
        add(getHashMap("繁体中文", "cht"));
        add(getHashMap("越南语", "vie"));
    }};
    private static final ArrayList<HashMap<String, String>> PHOTO_TRANS_LANG_ARRAY_LIST =new ArrayList<HashMap<String, String>>(){{
        add(getHashMap("自动检测", "auto"));
        add(getHashMap("中文", "zh"));
        add(getHashMap("英语", "en"));
        add(getHashMap("日语", "jp"));
        add(getHashMap("韩语", "kor"));
        add(getHashMap("法语", "fra"));
        add(getHashMap("西班牙语", "spa"));
        add(getHashMap("俄语", "ru"));
        add(getHashMap("葡萄牙语", "pt"));
        add(getHashMap("德语", "de"));
        add(getHashMap("意大利语", "it"));
        add(getHashMap("丹麦语", "dan"));
        add(getHashMap("荷兰语", "nl"));
        add(getHashMap("马来语", "may"));
        add(getHashMap("瑞典语", "swe"));
        add(getHashMap("印尼语", "id"));
        add(getHashMap("波兰语", "pl"));
        add(getHashMap("罗马尼亚语", "rom"));
        add(getHashMap("土耳其语", "tr"));
        add(getHashMap("希腊语", "el"));
        add(getHashMap("匈牙利语", "hu"));
    }};

    private static HashMap<String, String> getHashMap(String key, String value) {
        return new HashMap<String, String>() {{
            put(key, value);
        }};
    }

    public static ArrayList<HashMap<String, String>> getTextualFromLangList(){
        return TEXT_TRANS_LANG_ARRAY_LIST;
    }

    public static ArrayList<HashMap<String, String>> getTextualToLangList(){
        ArrayList<HashMap<String, String>> textToLangArrayList=new ArrayList<HashMap<String, String>>(TEXT_TRANS_LANG_ARRAY_LIST);
        textToLangArrayList.remove(0);
        return textToLangArrayList;
    }

    public static ArrayList<HashMap<String, String>> getPhotographicFromLangList(){
        return PHOTO_TRANS_LANG_ARRAY_LIST;
    }

    public static ArrayList<HashMap<String, String>> getPhotographicToLangList(){
        ArrayList<HashMap<String, String>> photoToLangArrayList =new ArrayList<HashMap<String, String>>(PHOTO_TRANS_LANG_ARRAY_LIST);
        photoToLangArrayList.remove(0);
        return photoToLangArrayList;
    }
}
