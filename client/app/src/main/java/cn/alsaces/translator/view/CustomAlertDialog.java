package cn.alsaces.translator.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


import cn.alsaces.translator.R;

public class CustomAlertDialog extends AlertDialog {
    // 控件
    private TextView titleTextView;
    private ProgressBar progressBar;
    private TextView messageTextView;
    private Button positiveButton;
    private Button negativeButton;

    // 控件参数
    private String customTitle;
    private boolean hasProgressBar;
    private String customMessage;
    private String customPositiveButtonName;
    private String customNegativeButtonName;
    private View.OnClickListener customPositiveOnClickListener;
    private View.OnClickListener customNegativeOnClickListener;


    public CustomAlertDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_alert_dialog);

        initView();
    }

    // 初始化View
    private void initView() {
        titleTextView = (TextView) findViewById(R.id.custom_dialog_title_text_view);
        progressBar = (ProgressBar) findViewById(R.id.custom_dialog_progress_bar);
        messageTextView = (TextView) findViewById(R.id.custom_dialog_message_text_view);
        positiveButton = (Button) findViewById(R.id.custom_dialog_positive_button);
        negativeButton = (Button) findViewById(R.id.custom_dialog_negative_button);
    }

     // 自定义监听器
    public interface CustomOnClickListener {
        void onClick(View v);
    }

    // 设置自定义标题
    public CustomAlertDialog setCustomTitle(String customTitle) {
        this.customTitle = customTitle;
        return this;
    }

    // 设置自定义进度条
    public CustomAlertDialog setCustomProgressBar(boolean hasProgressBar) {
        this.hasProgressBar = hasProgressBar;
        return this;
    }

    // 设置自定义消息
    public CustomAlertDialog setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
        return this;
    }

    // 设置自定义积极性质按钮
    public CustomAlertDialog setCustomPositiveButton(String customPositiveButtonName, CustomOnClickListener customOnClickListener) {
        this.customPositiveButtonName = customPositiveButtonName;

        this.customPositiveOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customOnClickListener != null) {
                    customOnClickListener.onClick(v);
                } else {
                    // 没有监听器
                }
                cancel();
            }
        };

        return this;
    }

    // 设置自定义消极性质按钮
    public CustomAlertDialog setCustomNegativeButton(String customNegativeButtonName, CustomOnClickListener customOnClickListener) {
        this.customNegativeButtonName = customNegativeButtonName;

        this.customNegativeOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customOnClickListener != null) {
                    customOnClickListener.onClick(v);
                } else {
                    // 没有监听器
                }
                cancel();
            }
        };

        return this;
    }

    // 显示
    @Override
    public void show() {
        super.show();

        setCustomData();
    }

    // 设置控件参数
    private void setCustomData() {
        if (customTitle != null) {
            titleTextView.setText(customTitle);
        } else {
            titleTextView.setVisibility(View.GONE);
        }

        if (hasProgressBar) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }

        if (customMessage != null) {
            messageTextView.setText(customMessage);
        } else {
            messageTextView.setVisibility(View.GONE);
        }

        if (customPositiveButtonName != null) {
            positiveButton.setText(customPositiveButtonName);
            positiveButton.setOnClickListener(customPositiveOnClickListener);
        } else {
            positiveButton.setVisibility(View.GONE);
        }

        if (customNegativeButtonName != null) {
            negativeButton.setText(customNegativeButtonName);
            negativeButton.setOnClickListener(customNegativeOnClickListener);
        } else {
            negativeButton.setVisibility(View.GONE);
        }
    }
}

