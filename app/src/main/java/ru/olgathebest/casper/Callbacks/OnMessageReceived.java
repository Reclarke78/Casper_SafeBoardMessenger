package ru.olgathebest.casper.callbacks;

import ru.olgathebest.casper.model.Message;

/**
 * Created by Ольга on 11.12.2016.
 */

public interface OnMessageReceived {
    void onMessageReceived(Message message);
}
