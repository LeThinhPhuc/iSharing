package com.example.ggm;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MessageAdapter extends ArrayAdapter<Message> {

    private Context context;
    private ArrayList<Message> messagesList;
    private TextToSpeech tts;

    public MessageAdapter(Context context, ArrayList<Message> messagesList, TextToSpeech tts) {
        super(context, 0, messagesList);
        this.context = context;
        this.messagesList = messagesList;
        this.tts = tts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        }

        TextView textViewSender = convertView.findViewById(R.id.textViewSender);
        TextView textViewText = convertView.findViewById(R.id.textViewText);
        Button btnSpeak = convertView.findViewById(R.id.btnSpeak);

        Message message = messagesList.get(position);

        textViewSender.setText(message.getMessageId());
        textViewText.setText(message.getReceiverId());

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.speak(message.getReceiverId(), TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        return convertView;
    }
}
