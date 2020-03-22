package com.hku.tripals.model;

public class Message {
    private String senderID;
    private String senderName, senderURL;
    private String msgText;
    private String msgTime;
    private String msgDate;
    private String msgType;

    public Message(){}

    public Message(String senderID, String senderName, String senderURL, String msgText, String time, String date, String msgType) {
        this.senderID = senderID;
        this.senderName = senderName;
        this.senderURL = senderURL;
        this.msgText = msgText;
        this.msgTime = time;
        this.msgDate = date;
        this.msgType = msgType;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderURL() {
        return senderURL;
    }

    public void setSenderURL(String senderURL) {
        this.senderURL = senderURL;
    }

    public String getMsgText() {
        return msgText;
    }

    public void setMsgText(String messageText) {
        this.msgText = messageText;
    }

    public String getMsgTime() {
        return msgTime;
    }

    public void setMsgTime(String msgTime) {
        this.msgTime = msgTime;
    }

    public String getMsgDate() {
        return msgDate;
    }

    public void setMsgDate(String msgDate) {
        this.msgDate = msgDate;
    }

    public String getmsgType() {
        return msgType;
    }

    public void setType(String msgType) {
        this.msgType = msgType;
    }
}
