package com.catchat.app.ui.sendingmessage;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.catchat.app.R;
import com.catchat.app.ui.progress.CatProgressDialog;
import com.catchat.app.ui.progress.OnCatImageDecodingComplete;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class CatImagePickerActivity extends Activity implements AdapterView.OnItemClickListener, OnCatImageDecodingComplete {

    private GridView mGridView;
    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cat_image_picker);

        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setHorizontalSpacing(2);
        mGridView.setVerticalSpacing(2);
        mGridView.setAdapter(new CatAdapter(this));
        mGridView.setOnItemClickListener(this);

        mProgressDialog = CatProgressDialog.show(CatImagePickerActivity.this, getString(R.string.retrieving_cat_pics));

        pullLatestCatPics();
    }

    private void pullLatestCatPics() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CatImage");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> catImages, ParseException e) {
                if (e == null) {
                    if(catImages == null || catImages.size() <= 0) {
                        mProgressDialog.dismiss();
                    } else {
                        updateAdapter(catImages);
                    }
                } else {
                    mProgressDialog.dismiss();
                    Log.e("CatChatTag", "Error pulling cat pics: " + e.getMessage(), e);
                }
            }
        });
    }

    private void updateAdapter(List<ParseObject> catImages) {
        CatAdapter adapter = (CatAdapter) mGridView.getAdapter();
        new DecodeBitmapsAsyncTask(adapter, catImages, this).execute();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        CatImage c = (CatImage) adapterView.getAdapter().getItem(i);

        Intent intent = new Intent();
        intent.putExtra("imageid", c.objectId);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onDecodingComplete() {
        mProgressDialog.dismiss();
    }

    private class CatImage
    {
        public String objectId;
        public Bitmap bitmap;
    }

    private class CatAdapter extends BaseAdapter {
        private final LayoutInflater mViewInflater;
        private final List<CatImage> mImages = new ArrayList<CatImage>();

        public CatAdapter(Context c) {
            mViewInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mImages.size();
        }

        @Override
        public Object getItem(int i) {
            return mImages.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final CatImage catImage = mImages.get(i);
            final ImageView imageview;
            if (view == null) {
                view = mViewInflater.inflate(R.layout.image, null);
                imageview = (ImageView) view.findViewById(R.id.imageview);
                view.setTag(imageview);
            } else {
                imageview = (ImageView) view.getTag();
            }

            imageview.setImageBitmap(catImage.bitmap);
            return view;
        }

        public void addImage(CatImage c) {
            mImages.add(c);
        }
    }

    private class DecodeBitmapsAsyncTask extends AsyncTask<Void, Void, Void> {
        private final CatAdapter mAdapter;
        private final List<ParseObject> mCatImages;
        private final OnCatImageDecodingComplete mListener;

        public DecodeBitmapsAsyncTask(CatAdapter adapter, List<ParseObject> catImages, OnCatImageDecodingComplete listener) {
            mAdapter = adapter;
            mCatImages = catImages;
            mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for(ParseObject o : mCatImages) {
                ParseFile file = o.getParseFile("image");
                try {
                    Bitmap bmp = BitmapFactory.decodeByteArray(file.getData(), 0, file.getData().length);

                    CatImage c = new CatImage();
                    c.bitmap = bmp;
                    c.objectId = o.getObjectId();

                    mAdapter.addImage(c);
                } catch (ParseException e) {
                    Log.d("CatChatTag", "Failed to decode bitmap: " + o, e);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.notifyDataSetChanged();
            mListener.onDecodingComplete();
        }
    }
}