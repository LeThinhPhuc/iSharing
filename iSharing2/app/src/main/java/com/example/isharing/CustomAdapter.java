package com.example.isharing;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private ArrayList<String> mUserList;

    public CustomAdapter(Context context, ArrayList<String> userList) {
        super(context, 0, userList);
        mContext = context;
        mUserList = userList;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_layout, parent, false);
        }

        String user = mUserList.get(position);
        TextView textViewName = convertView.findViewById(R.id.textViewName);
        Button buttonFollow = convertView.findViewById(R.id.buttonFollow);

        textViewName.setText(user);

        // Check if user is already followed
        final boolean[] isFollowing = {false};

        if (!isFollowing[0]) {
            buttonFollow.setText(R.string.button_follow);
            buttonFollow.setBackgroundColor(Color.BLUE);
        } else {
            buttonFollow.setText(R.string.button_following);
            buttonFollow.setBackgroundColor(Color.GREEN);
        }

        buttonFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle follow state
                // Update UI accordingly
                if (!isFollowing[0]) {
                    buttonFollow.setText(R.string.button_following);
                    buttonFollow.setBackgroundColor(Color.GREEN);
                } else {
                    buttonFollow.setText(R.string.button_follow);
                    buttonFollow.setBackgroundColor(Color.BLUE);
                }
                isFollowing[0] = !isFollowing[0];
            }
        });

        return convertView;
    }
}
