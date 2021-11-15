package cn.alsaces.translator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.alsaces.translator.R;

public class LanguageAdapter extends BaseAdapter {
    // 语言清单
    private final ArrayList<HashMap<String, String>> langArrayList;

    // 视图填充器
    private final LayoutInflater layoutInflater;

    public LanguageAdapter(Context context, ArrayList<HashMap<String, String>> langArrayList) {
        this.layoutInflater = LayoutInflater.from(context);
        this.langArrayList = langArrayList;
    }

    // 获取语言数量
    @Override
    public int getCount() {
        return langArrayList.size();
    }

    // 获取语言
    @Override
    public HashMap<String, String> getItem(int position) {
        return langArrayList.get(position);
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
        langTextView.setText(getItem(position).entrySet().iterator().next().getKey());
        return convertView;
    }
}
