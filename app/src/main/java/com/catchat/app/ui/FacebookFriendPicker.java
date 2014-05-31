package com.catchat.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.catchat.app.R;
import com.facebook.FacebookException;
import com.facebook.model.GraphUser;
import com.facebook.widget.FriendPickerFragment;
import com.facebook.widget.PickerFragment;

import java.util.List;

public class FacebookFriendPicker extends FragmentActivity {
    FriendPickerFragment mFriendPickerFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_friend_picker);

        if (savedInstanceState == null) {
            final Bundle args = getIntent().getExtras();
            mFriendPickerFragment = new FriendPickerFragment(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.friend_picker_fragment, mFriendPickerFragment)
                    .commit();
        } else {
            mFriendPickerFragment = (FriendPickerFragment) getSupportFragmentManager().findFragmentById(R.id.friend_picker_fragment);
        }

        mFriendPickerFragment.setOnErrorListener(new PickerFragment.OnErrorListener() {
            @Override
            public void onError(PickerFragment<?> fragment, FacebookException error) {
                Log.e("CatChatTag", error.getMessage());
                Toast.makeText(fragment.getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        mFriendPickerFragment.setOnDoneButtonClickedListener(new PickerFragment.OnDoneButtonClickedListener() {
            @Override
            public void onDoneButtonClicked(PickerFragment<?> fragment) {
                List<GraphUser> selection = mFriendPickerFragment.getSelection();

                if(selection.size() > 0) {
                    String id = selection.get(0).getId();

                    Intent i = new Intent();
                    i.putExtra("fbid", id);

                    setResult(RESULT_OK, i);
                }
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            mFriendPickerFragment.loadData(false);
        } catch (Exception ex) {
            Log.e("CatChatTag", ex.getMessage(), ex);
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}