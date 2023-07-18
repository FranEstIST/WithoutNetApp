package pt.ulisboa.tecnico.withoutnet.models;

import android.util.Log;

import androidx.annotation.NonNull;

public class Message {
    private long id;

    private long timestamp;

    private MessageType messageType;

    private String sender;

    private String receiver;

    // TODO: Change this type to a more appropriate one (maybe use reflection)
    private String content;

    public Message(long id, long timestamp, int messageTypeInt, String sender, String receiver, String content) {
        this.id = id;
        this.timestamp = timestamp;

        try {
            this.messageType = MessageType.values()[Integer.valueOf(messageTypeInt)];
        } catch (NumberFormatException e) {
            // TODO: Treat this exception
        }

        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    public Message(long id, long timestamp, MessageType messageType, String sender, String receiver, String content) {
        this.id = id;
        this.timestamp = timestamp;
        this.messageType = messageType;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    public Message(String messageString) {
        String messageStringComponents[] = messageString.split("#");

        if(messageStringComponents.length != 6) {
            //TODO: Throw an exception here
        }

        try {
            this.id = Long.valueOf(messageStringComponents[0]);
            this.messageType = MessageType.values()[Integer.valueOf(messageStringComponents[1])];
            this.timestamp = Long.valueOf(messageStringComponents[2]);
        } catch (NumberFormatException e) {
            //TODO: Throw an exception here
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e1) {
            //TODO: Throw an exception here
            e1.printStackTrace();
        }

        this.sender = messageStringComponents[3];
        this.receiver = messageStringComponents[4];
        this.content = messageStringComponents[5];
    }

    public long getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public int getMessageTypeAsInt() {
        return messageType.ordinal();
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @NonNull
    @Override
    public String toString() {
        return "" + this.id
                + "#" + this.messageType.ordinal()
                + "#" + this.timestamp
                + "#" + this.sender
                + "#" + this.receiver
                + "#" + this.content;
    }
}

enum MessageType {
    DATA,
    ACK
}
