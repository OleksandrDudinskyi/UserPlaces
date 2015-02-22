package com.dudinskyi.userplaces;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

/**
 * Settings activity
 *
 * @author Oleksandr Dudinskyi (dudinskyj@gmail.com)
 */
public class SettingsActivity extends Activity implements Button.OnClickListener {
    public static final String NEARBY_SEARCH = "NEARBY_SEARCH";
    public static final String TEXT_SEARCH = "TEXT_SEARCH";
    public static final String SEARCH_TYPE_KEY = "SEARCH_TYPE_KEY";
    private RadioGroup mSearchType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        mSearchType = (RadioGroup) findViewById(R.id.search_type);
        Button btnGoBack = (Button) findViewById(R.id.btn_go_back);
        btnGoBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int selectedId = mSearchType.getCheckedRadioButtonId();
        Intent intent = new Intent();
        if (selectedId == R.id.nearby_search) {
            intent.putExtra(SEARCH_TYPE_KEY, NEARBY_SEARCH);
        } else {
            intent.putExtra(SEARCH_TYPE_KEY, TEXT_SEARCH);
        }
        setResult(RESULT_OK, intent);
        finish();
    }
}
