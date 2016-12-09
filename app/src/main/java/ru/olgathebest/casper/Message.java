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
    public String getTo(){
        return this.to;
    }
    public void setTo(String to){
        this.to = to;
    }
    public String getFrom(){
        return this.from;
    }
    public void setFrom(String from){
        this.from = from;
    }
    public String getId(){
        return this.id;
    }
    public void setId(String id){
        this.id = id;
    }
    public String getText(){
        return this.text;
    }
    public void setText(String text){
        this.text = text;
    }
    public Date getTime(){
        return this.time;
    }
    public StatusMsg getStatus(){
        return this.status;
    }
    public void setStatus(StatusMsg status){
        this.status = status;
    }
}
