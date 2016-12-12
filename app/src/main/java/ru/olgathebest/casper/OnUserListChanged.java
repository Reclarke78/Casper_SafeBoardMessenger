package ru.olgathebest.casper;

import java.util.Observer;

/**
 * Created by Ольга on 06.12.2016.
 */

public interface OnUserListChanged {
    void onUserListChanged(final String [] users, String [] keys);
}
