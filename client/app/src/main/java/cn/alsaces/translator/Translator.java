package cn.alsaces.translator;


import android.app.Application;

public class Translator extends Application {
    // 必选修改配置项
    public static final String TEXT_TRANS_API_URL = "https://example.com/api/translate/common/";  // 通用翻译API地址
    public static final String TEXT_TRANS_AUTH_KEY = "123456789abc"; // 通用翻译key
    public static final String BAIDU_TRANS_APP_ID = "123456789";  // 百度翻译平台APP ID
    public static final String BAIDU_TRANS_SECRET_KEY = "123456789abc";  // 百度翻译平台密钥
    public static final String IMAGE_TRANS_API_URL = "https://example.com/api/translate/image/";  // 图片翻译API地址
    public static final String IMAGE_TRANS_API_AUTH = "123456789abc";  // 图片翻译Key
    public static final String NEW_WORD_API_URL = "https://example.com/api/legal-word/en/"; // 生词API地址
    public static final String NEW_WORD_AUTH_KEY = "123456789abc"; // 生词Key
    public static final String VERSION_CHECK_API_URL = "https://example.com/api/update/"; // 版本检查API地址
    public static final String VERSION_CHECK_AUTH_KEY = "123456789abc";  // 版本检查Key

    // 可选修改配置项
    public static final String CONFIG_NAME = "Config";
    public static final String VOICE_AUTO_TTS_CONFIG_NAME = "IsVoiceAutoTts";
    public static final String ENG_AUTO_TTS_TYPE_CONFIG_NAME = "EngAutoTtsType";
    public static final String AUTO_ADD_NEW_WORD_CONFIG_NAME = "IsAutoAddNewWord";

    @Override
    public void onCreate() {
        super.onCreate();
    }

}
