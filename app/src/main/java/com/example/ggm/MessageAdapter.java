package com.example.ggm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class MessageAdapter extends ArrayAdapter<Message> {

    private Context context;
    private ArrayList<Message> messagesList;

    public MessageAdapter(Context context, ArrayList<Message> messagesList) {
        super(context, 0, messagesList);
        this.context = context;
        this.messagesList = messagesList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        }

        TextView textViewSender = convertView.findViewById(R.id.textViewSender);
        TextView textViewText = convertView.findViewById(R.id.textViewText);

        Message message = messagesList.get(position);

        textViewSender.setText(message.getMessageId());
        textViewText.setText(message.getReceiverId());

        return convertView;
    }
}
