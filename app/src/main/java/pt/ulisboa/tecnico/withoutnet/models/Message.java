package pt.ulisboa.tecnico.withoutnet.models;

import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Message {
    private short length;

    private long timestamp;

    private MessageType messageType;

    private int sender;

    private int receiver;

    private byte[] payload;

    public Message(long timestamp, int messageTypeInt, int sender, int receiver, byte[] payload) {
        this.length = (short) (13 + payload.length);

        this.timestamp = timestamp;

        try {
            this.messageType = MessageType.values()[Integer.valueOf(messageTypeInt)];
        } catch (NumberFormatException e) {
            // TODO: Treat this exception
        }

        this.sender = sender;
        this.receiver = receiver;
        this.payload = payload;
    }

    public Message(long timestamp, MessageType messageType, int sender, int receiver, byte[] payload) {
        this.timestamp = timestamp;
        this.messageType = messageType;
        this.sender = sender;
        this.receiver = receiver;
        this.payload = payload;
    }

    public Message(byte[] messageByteArray) {
        // Since Arduino Nanos are little endian and Android is big endian
        // these byte arrays need to be flipped to be stored and flipped again
        // when being transferred to an Arduino Nano

        byte[] lengthByteArray = Arrays.copyOfRange(messageByteArray, 0, 2);
        byte[] timestampByteArray = Arrays.copyOfRange(messageByteArray, 2, 6);
        byte[] messageTypeByteArray = Arrays.copyOfRange(messageByteArray, 6, 7);
        byte[] senderByteArray = Arrays.copyOfRange(messageByteArray, 7, 11);
        byte[] receiverByteArray = Arrays.copyOfRange(messageByteArray, 11, 15);
        // The last byte in the message should be 0, therefore it should be excluded from the payload
        byte[] payloadByteArray = Arrays.copyOfRange(messageByteArray, 15, messageByteArray.length - 1);

        this.length = (short) byteArrayToIntRev(lengthByteArray);

        this.timestamp = byteArrayToIntRev(timestampByteArray);

        try {
            this.messageType = MessageType.values()[Integer.valueOf(byteArrayToIntRev(messageTypeByteArray))];
        } catch (ArrayIndexOutOfBoundsException e1) {
            //TODO: Throw an exception here
            e1.printStackTrace();
        }

        this.sender = byteArrayToIntRev(senderByteArray);
        this.receiver = byteArrayToIntRev(receiverByteArray);
        this.payload = payloadByteArray;
    }

    /*public Message(String messageString) {
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
    }*/

    short byteArrayToShort(byte[] byteArray) {
        short shortValue = 0;
        for (byte b : byteArray) {
            shortValue = (short) ((shortValue << 8) + (b & 0xFF));
        }
        return shortValue;
    }

    short byteArrayToShortRev(byte[] byteArray) {
        short intValue = 0;

        for (int i = byteArray.length - 1; i >= 0 ; i--) {
            intValue = (short) ((intValue << 8) + (byteArray[i] & 0xFF));
        }
        return intValue;
    }

    int byteArrayToInt(byte[] byteArray) {
        int intValue = 0;
        for (byte b : byteArray) {
            intValue = (intValue << 8) + (b & 0xFF);
        }
        return intValue;
    }

    int byteArrayToIntRev(byte[] byteArray) {
        int intValue = 0;

        for (int i = byteArray.length - 1; i >= 0 ; i--) {
            intValue = (intValue << 8) + (byteArray[i] & 0xFF);
        }
        return intValue;
    }

    long byteArrayToLong(byte[] byteArray) {
        long longValue = 0;
        for (byte b : byteArray) {
            longValue = (longValue << 8) + (b & 0xFF);
        }
        return longValue;
    }

    long byteArrayToLongRev(byte[] byteArray) {
        long longValue = 0;
        for (int i = byteArray.length - 1; i >= 0 ; i--) {
            longValue = (longValue << 8) + (byteArray[i] & 0xFF);
        }
        return longValue;
    }

    public short getLength() {
        return length;
    }

    public void setLength(short length) {
        this.length = length;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public int getReceiver() {
        return receiver;
    }

    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public String byteArrayToString() {
        byte[] messageByteArray = this.toByteArray();
        String messageByteArrayString = "";

        for(byte messageByte : messageByteArray) {
            messageByteArrayString += messageByte + " # ";
        }

        return messageByteArrayString;
    }

    public byte[] toByteArray() {
        // Since Arduino Nanos are little endian and Android is big endian
        // these byte arrays need to be flipped to be stored and flipped again
        // when being transferred to an Arduino Nano

        byte[] lengthByteArray = shortToByteArrayRev(this.length);
        byte[] timestampByteArray = intToByteArrayRev((int) this.timestamp);
        byte[] messageTypeByteArray = messageTypeToByteArrayRev(this.messageType);
        byte[] senderByteArray = intToByteArrayRev(this.sender);
        byte[] receiverByteArray = intToByteArrayRev(this.receiver);

        return concatByteArrays(lengthByteArray,
                timestampByteArray,
                messageTypeByteArray,
                senderByteArray,
                receiverByteArray,
                this.payload);

    }

    private byte[] concatByteArrays(byte[]... byteArrays) {
        Iterator<byte[]> byteArrayIterator = Arrays.stream(byteArrays).iterator();

        if(!byteArrayIterator.hasNext()) {
            return null;
        }

        byte[] resultingByteArray = byteArrayIterator.next();

        while(byteArrayIterator.hasNext()) {
            byte[] arrayToConcatOne = resultingByteArray;
            byte[] arrayToConcatTwo = byteArrayIterator.next();

            resultingByteArray = Arrays.copyOf(arrayToConcatOne, arrayToConcatOne.length + arrayToConcatTwo.length);
            System.arraycopy(arrayToConcatTwo, 0, resultingByteArray, arrayToConcatOne.length, arrayToConcatTwo.length);
        }

        return resultingByteArray;
    }

    private byte[] messageTypeToByteArray(MessageType value) {
        int ordinalValue = value.ordinal();
        byte[] bytes = new byte[1];
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            bytes[length - i - 1] = (byte) (ordinalValue & 0xFF);
            ordinalValue >>= 8;
        }

        return bytes;
    }

    private byte[] shortToByteArray(short value) {
        byte[] bytes = new byte[Short.BYTES];
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            bytes[length - i - 1] = (byte) (value & 0xFF);
            value >>= 8;
        }

        return bytes;
    }

    private byte[] intToByteArray(int value) {
        byte[] bytes = new byte[Integer.BYTES];
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            bytes[length - i - 1] = (byte) (value & 0xFF);
            value >>= 8;
        }

        return bytes;
    }

    private byte[] shortToByteArrayRev(short value) {
        byte[] bytes = new byte[Short.BYTES];
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) (value & 0xFF);
            value >>= 8;
        }

        return bytes;
    }

    private byte[] intToByteArrayRev(int value) {
        byte[] bytes = new byte[Integer.BYTES];
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) (value & 0xFF);
            value >>= 8;
        }

        return bytes;
    }

    private byte[] messageTypeToByteArrayRev(MessageType value) {
        switch(value) {
            case DATA:
                return new byte[] {0};
            case ACK:
                return new byte[] {1};
            default:
                return new byte[] {-1};
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "" + this.length
                + "#" + this.timestamp
                + "#" + this.messageType.ordinal()
                + "#" + this.sender
                + "#" + this.receiver
                + "#" + byteArrayToIntRev(this.payload);
    }
}

enum MessageType {
    DATA,
    ACK
}
