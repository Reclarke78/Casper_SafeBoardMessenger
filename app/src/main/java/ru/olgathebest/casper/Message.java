package ru.olgathebest.casper;

import java.util.Date;

import static ru.olgathebest.casper.R.string.to;

/**
 * Created by Ольга on 08.12.2016.
 */

public class Message {
    private String to;
    private String from;
    private String id;
    private String text;
    private Date time; //время когда отправили
    private StatusMsg status;
public Message(String id, String to, String from, String text, Date time, StatusMsg status){
    this.to = to;
    this.id = id;
    this.from = from;
    this.text = text;
    this.time = time;
    this.status = status;
}
}
