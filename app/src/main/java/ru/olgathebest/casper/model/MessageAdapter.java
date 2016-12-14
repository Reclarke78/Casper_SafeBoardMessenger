package ru.olgathebest.casper.model;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import ru.olgathebest.casper.R;
import ru.olgathebest.casper.utils.Coding;

/**
 * Created by Ольга on 05.12.2016.
 */

public class MessageAdapter extends BaseAdapter {
    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;
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
        String type = messages.get(i).first.getType();
        ImageView image = (ImageView) convertView.findViewById(R.id.imgMessage);
        TextView txtMessage = (TextView) convertView.findViewById(R.id.txtMessage);
        TextView txtInfo = (TextView) convertView.findViewById(R.id.txtInfo);
        TextView txtStatus = (TextView) convertView.findViewById(R.id.txtStatus);
        if (type.equals("1")) {
            byte[] bits = Coding.hexToBytes(messages.get(i).first.getText());
            Log.d("imageadd", bits.length + "");
            Bitmap bitmap = BitmapFactory.decodeByteArray(bits, 0, bits.length);
            //Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher);
            image.setImageBitmap(bitmap);
            txtMessage.setText("");
            SimpleDateFormat dt = new SimpleDateFormat("HH:mm");
            txtInfo.setText(dt.format(messages.get(i).first.getTime()));
            if (messages.get(i).first.getStatus()!=null&& txtStatus!=null )
            txtStatus.setText(messages.get(i).first.getStatus().toString());
        } else {
            image.setImageBitmap(null);
            txtMessage.setText(message);
            SimpleDateFormat dt = new SimpleDateFormat("HH:mm");
            txtInfo.setText(dt.format(messages.get(i).first.getTime()));
        }
        RelativeLayout msgBackground = (RelativeLayout) convertView.findViewById(R.id.content);
        if (messages.get(i).first.getStatus() == StatusMsg.Delivered) {
            msgBackground.setBackgroundColor(Color.parseColor("#B6D0D2"));
            if (txtStatus!=null )
            txtStatus.setText("Delivered");
        }
        if (messages.get(i).first.getStatus() == StatusMsg.Seen) {
            msgBackground.setBackgroundColor(Color.parseColor("#F7F7F7"));
            if (txtStatus!=null )
            txtStatus.setText("Seen");
        }


        return convertView;
    }
}
