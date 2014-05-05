package com.catchat.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CatImagePickerActivity extends Activity implements AdapterView.OnItemClickListener {

    private GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cat_image_picker);

        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setHorizontalSpacing(2);
        mGridView.setVerticalSpacing(2);
        mGridView.setAdapter(new SimpleAdapter(this, buildCatList(), R.layout.image, new String[]{"img"}, new int[]{R.id.imageview}));
        mGridView.setOnItemClickListener(this);
    }

    private List<HashMap<String, Integer>> buildCatList() {
        List<HashMap<String, Integer>> list = new ArrayList<HashMap<String, Integer>>();

        for (int i = 0; i < 20; i++) {
            HashMap<String, Integer> hm = new HashMap<String, Integer>();
            hm.put("img", R.drawable.office_cat);
            list.add(hm);
        }
        return list;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        HashMap<String, Integer> map = (HashMap<String, Integer>) adapterView.getAdapter().getItem(i);

        Intent intent = new Intent();
        intent.putExtra("imageid", map.get("img"));
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
