package cn.alsaces.translator.fragment;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cn.alsaces.translator.R;
import cn.alsaces.translator.database.DatabaseHelper;
import cn.alsaces.translator.util.NewWordUtil;

public class NewWordFragment extends Fragment {
    // View和Context
    private View view;
    private Context context;

    // 数据库
    private SQLiteDatabase sqLiteWritableDB;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new_word, container, false);
        context = view.getContext();

        // 初始化
        initDatabase();
        initView();

        return view;
    }

    // 初始化Database
    private void initDatabase() {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        sqLiteWritableDB = databaseHelper.getWritableDatabase();
    }

    // 初始化View
    private void initView() {
        LinearLayout newWordSetLinearLayout = view.findViewById(R.id.new_word_set_linear_layout);

        // 显示生词记录
        NewWordUtil.showNewWordRecord(context, newWordSetLinearLayout, sqLiteWritableDB);
    }
}
