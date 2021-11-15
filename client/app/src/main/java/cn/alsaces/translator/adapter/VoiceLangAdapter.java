package cn.alsaces.translator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.translate.asr.data.Language;

import cn.alsaces.translator.R;

public class VoiceLangAdapter extends BaseAdapter {
    // 常量
    private static final String VOICE_TIP_TEXT = "(语音)";

    // 语言
    private final Language[] voiceLangArray;

    // 视图填充器
    private final LayoutInflater layoutInflater;


    public VoiceLangAdapter(Context context, Language[] voiceLangArray) {
        this.layoutInflater = LayoutInflater.from(context);
        this.voiceLangArray = voiceLangArray;
    }

    // 获取语言数量
    @Override
    public int getCount() {
        return voiceLangArray == null ? 0 : voiceLangArray.length;
    }

    // 获取语言
    @Override
    public Language getItem(int position) {
        return voiceLangArray[position];
    }

    // 获取语言ID
    @Override
    public long getItemId(int position) {
        return position;
    }

    // 获取View
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_language_choose, parent, false);
        } else {
            // 已有View
        }

        TextView langTextView = convertView.findViewById(R.id.spinner_language_text_view);

        String languageName = "";
        if (getItem(position).isTtsAvailable()) {
            languageName = getItem(position).getLanguage() + VOICE_TIP_TEXT;
        } else {
            languageName = getItem(position).getLanguage();
        }
        langTextView.setText(languageName);

        return convertView;
    }
}
