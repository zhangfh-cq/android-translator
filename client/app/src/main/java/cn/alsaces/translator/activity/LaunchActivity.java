package cn.alsaces.translator.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

import cn.alsaces.translator.R;
import cn.alsaces.translator.Translator;
import cn.alsaces.translator.util.BarUtil;
import cn.alsaces.translator.util.SPUtil;

public class LaunchActivity extends AppCompatActivity {
    // context和Activity
    private Context context;
    private AppCompatActivity appCompatActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        context = this;
        appCompatActivity = this;

        OptimizeBar();
        initConfig();
        jumpActivity();
    }

    // 优化Bar
    private void OptimizeBar() {
        BarUtil.removeActionBar(appCompatActivity);
        BarUtil.removeStatusBar(appCompatActivity);
    }

    // 初始化设置
    private void initConfig() {
        SPUtil.getBoolean(Translator.CONFIG_NAME, Translator.VOICE_AUTO_TTS_CONFIG_NAME, true, context);
        SPUtil.getString(Translator.CONFIG_NAME, Translator.ENG_AUTO_TTS_TYPE_CONFIG_NAME, "US", context);
        SPUtil.getBoolean(Translator.CONFIG_NAME, Translator.AUTO_ADD_NEW_WORD_CONFIG_NAME, true, context);
    }

    // 跳转界面
    private void jumpActivity() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(appCompatActivity, NavigationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        };

        Timer timer = new Timer();
        timer.schedule(timerTask, 2000);
    }
}