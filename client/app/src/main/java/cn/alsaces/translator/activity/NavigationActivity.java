package cn.alsaces.translator.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import cn.alsaces.translator.R;
import cn.alsaces.translator.util.BarUtil;
import cn.alsaces.translator.fragment.NewWordFragment;
import cn.alsaces.translator.fragment.PhotoFragment;
import cn.alsaces.translator.fragment.SettingFragment;
import cn.alsaces.translator.fragment.TextFragment;
import cn.alsaces.translator.fragment.VoiceFragment;

public class NavigationActivity extends AppCompatActivity {
    // Context和Activity
    private Context context;
    private AppCompatActivity appCompatActivity;

    // Fragment
    private TextFragment textFragment;
    private VoiceFragment voiceFragment;
    private PhotoFragment photoFragment;
    private NewWordFragment newWordFragment;
    private SettingFragment settingFragment;

    // 控件
    private RadioGroup navigationRadioGroup;
    private RadioButton textRadioButton;

    // 首次返回的时刻
    private long firstBackPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        context = this;
        appCompatActivity = this;

        OptimizeBar();
        initFragment();
        initView();
        initListener();

        // 默认为文本翻译
        textRadioButton.setChecked(true);
    }

    // 优化Bar
    private void OptimizeBar() {
        BarUtil.removeActionBar(appCompatActivity);
        BarUtil.setStatusBarColor(appCompatActivity, BarUtil.StatusBarMode.Light, 0xFAFAFA);
    }

    // 初始化Fragment
    private void initFragment() {
        textFragment = new TextFragment();
        voiceFragment = new VoiceFragment();
        photoFragment = new PhotoFragment();
        newWordFragment = new NewWordFragment();
        settingFragment = new SettingFragment();
    }

    // 初始化View
    private void initView() {
        navigationRadioGroup = findViewById(R.id.nav_radio_group);
        textRadioButton = findViewById(R.id.text_radio_button);
    }

    // 初始化Listener
    private void initListener() {
        navigationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                switch (checkedId) {
                    case R.id.text_radio_button:
                        fragmentTransaction.replace(R.id.nav_container_frame_layout, textFragment);
                        break;
                    case R.id.voice_radio_button:
                        fragmentTransaction.replace(R.id.nav_container_frame_layout, voiceFragment);
                        break;
                    case R.id.photo_radio_button:
                        fragmentTransaction.replace(R.id.nav_container_frame_layout, photoFragment);
                        break;
                    case R.id.new_word_radio_button:
                        fragmentTransaction.replace(R.id.nav_container_frame_layout, newWordFragment);
                        break;
                    case R.id.setting_radio_button:
                        fragmentTransaction.replace(R.id.nav_container_frame_layout, settingFragment);
                }
                // 提交Fragment事务
                fragmentTransaction.commit();
            }
        });
    }

    // 两次返回退出
    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - firstBackPressedTime) < 2000) {
            super.onBackPressed();
        } else {
            firstBackPressedTime = System.currentTimeMillis();
            Toast.makeText(context, "再次返回退出", Toast.LENGTH_SHORT).show();
        }
    }
}
