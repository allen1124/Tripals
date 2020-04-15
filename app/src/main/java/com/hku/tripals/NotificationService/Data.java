package com.hku.tripals.NotificationService;

public class Data {
    private String chat_id;
    private String chat_name;
    private String chat_icon;
    private String chat_type;
    private String chat_participants;

    private int icon;
    private String body;
    private String title;
    private String sent;

    public Data(String chat_id, String chat_name, String chat_icon, String chat_type, String chat_participants, int icon, String body, String title, String sent) {
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

    public Data() {
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
