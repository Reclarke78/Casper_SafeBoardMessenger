package ru.olgathebest.casper;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.SimpleFormatter;

import static ru.olgathebest.casper.R.id.content;

/**
 * Created by Ольга on 05.12.2016.
 */

public class MessageAdapter extends BaseAdapter {
    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;
    public static final int READ = 2;
    public static final int UNREAD = 3;
    private List<Pair<Message, Integer>> messages;
    private LayoutInflater layoutInflater;

    public MessageAdapter(Activity activity) {
        layoutInflater = activity.getLayoutInflater();
        messages = new ArrayList<Pair<Message, Integer>>();
    }

    public void addMessage(Message message, int direction) {
        messages.add(new Pair(message, direction));
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Message getItem(int i) {
        return messages.get(i).first;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int i) {
        return messages.get(i).second;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        int direction = getItemViewType(i);

        //show message on left or right, depending on if
        //it's incoming or outgoing
        if (convertView == null) {
            int res = 0;
            if (direction == DIRECTION_INCOMING) {
                res = R.layout.msg_left;
            } else if (direction == DIRECTION_OUTGOING) {
                res = R.layout.msg_right;
            }
            convertView = layoutInflater.inflate(res, viewGroup, false);
        }

        String message = messages.get(i).first.getText();
        TextView txtMessage = (TextView) convertView.findViewById(R.id.txtMessage);
        txtMessage.setText(message);
        TextView txtInfo = (TextView) convertView.findViewById(R.id.txtInfo);
        SimpleDateFormat dt = new SimpleDateFormat("hh:mm");
        txtInfo.setText(dt.format(messages.get(i).first.getTime()));
        Log.d("About to change color ", messages.get(i).first.getStatus().toString());
        RelativeLayout msgBackground = (RelativeLayout) convertView.findViewById(R.id.content);
        if (messages.get(i).first.getStatus() == StatusMsg.Seen) {
            msgBackground.setBackgroundColor(Color.parseColor("#F7F7F7"));
            Log.d("Color changed", "2");
        }
        return convertView;
    }
}
