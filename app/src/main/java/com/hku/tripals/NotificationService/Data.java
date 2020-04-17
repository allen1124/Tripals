package com.hku.tripals.NotificationService;

public class Data {
    // New Message Notification
    private String chat_id;
    private String chat_name;
    private String chat_icon;
    private String chat_type;
    private String chat_participants;

    // Event Notification
    private String event_id;

    private String type_code;
    private int icon;
    private String body;
    private String title;
    private String sent;

    // New Message Notification
    public Data(String type_code, String chat_id, String chat_name, String chat_icon, String chat_type, String chat_participants, int icon, String body, String title, String sent) {
        this.type_code = type_code;
        this.chat_id = chat_id;
        this.chat_name = chat_name;
        this.chat_icon = chat_icon;
        this.chat_type = chat_type;
        this.chat_participants = chat_participants;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sent = sent;
    }

    //Event Notification
    public Data(String type_code, String event_id, int icon, String body, String title, String sent) {
        this.type_code = type_code;
        this.event_id = event_id;
        this.icon = icon;
        this.body = body;
        this.title = title;
        this.sent = sent;
    }

    public Data() {
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public String getType_code() {
        return type_code;
    }

    public void setType_code(String type_code) {
        this.type_code = type_code;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getChat_name() {
        return chat_name;
    }

    public void setChat_name(String chat_name) {
        this.chat_name = chat_name;
    }

    public String getChat_icon() {
        return chat_icon;
    }

    public void setChat_icon(String chat_icon) {
        this.chat_icon = chat_icon;
    }

    public String getChat_type() {
        return chat_type;
    }

    public void setChat_type(String chat_type) {
        this.chat_type = chat_type;
    }

    public String getChat_participants() {
        return chat_participants;
    }

    public void setChat_participants(String chat_participants) {
        this.chat_participants = chat_participants;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }
}
