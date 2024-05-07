package pt.ulisboa.tecnico.withoutnet.models;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Entity(primaryKeys = {"timestamp", "messageType", "sender", "receiver"})
public class Message implements Serializable {
    private short length;

    @NonNull
    private long timestamp;

    @NonNull
    private MessageType messageType;

    @NonNull
    private int sender;

    @NonNull
    private int receiver;

    private byte[] payload;

    private boolean inServer;

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

    public Message(short length, long timestamp, int messageTypeInt, int sender, int receiver, String payload) {
        this.length = length;

        this.timestamp = timestamp;

        try {
            this.messageType = MessageType.values()[Integer.valueOf(messageTypeInt)];
        } catch (NumberFormatException e) {
            // TODO: Treat this exception
        }

        this.sender = sender;
        this.receiver = receiver;
        this.payload = Base64.getDecoder().decode(payload);

        this.inServer = false;
    }

    public Message(long timestamp, MessageType messageType, int sender, int receiver, byte[] payload) {
        this.length = (short) (13 + payload.length);
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

        this.inServer = false;
    }

    public Message(ArrayList<byte[]> chunks) {
        this.length = 0;

        for(int i = 0; i < chunks.size(); i++) {
            byte[] chunk = chunks.get(i);

            byte[] lengthByteArray = Arrays.copyOfRange(chunk, 0, 2);

            short chunkLength = (short) byteArrayToIntRev(lengthByteArray);
            this.length += chunkLength;

            int payloadChunkStart;

            if(i == 0) {
                byte[] timestampByteArray = Arrays.copyOfRange(chunk, 2, 6);
                byte[] messageTypeByteArray = Arrays.copyOfRange(chunk, 6, 7);
                byte[] senderByteArray = Arrays.copyOfRange(chunk, 7, 11);
                byte[] receiverByteArray = Arrays.copyOfRange(chunk, 11, 15);

                this.timestamp = byteArrayToIntRev(timestampByteArray);

                try {
                    this.messageType = MessageType.values()[Integer.valueOf(byteArrayToIntRev(messageTypeByteArray))];
                } catch (ArrayIndexOutOfBoundsException e1) {
                    //TODO: Throw an exception here
                    e1.printStackTrace();
                }

                this.sender = byteArrayToIntRev(senderByteArray);
                this.receiver = byteArrayToIntRev(receiverByteArray);

                payloadChunkStart = 15;
            } else {
                payloadChunkStart = 2;
            }

            byte[] payloadChunkByteArray = Arrays.copyOfRange(chunk, payloadChunkStart, chunkLength + 2);

            if(i == 0) {
                this.payload = payloadChunkByteArray;
            } else {
                this.payload = concatByteArrays(this.payload, payloadChunkByteArray);
            }
        }
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

    public Message getAckMessage() {
        return new Message(this.timestamp, MessageType.ACK, this.receiver, this.sender, new byte[] {});
    }

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

    public int getMessageTypeAsInt() {
        return this.messageType.ordinal();
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

    public String getPayloadAsByteString() {
        return Base64.getEncoder().encodeToString(payload);
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public boolean isInServer() {
        return inServer;
    }

    public void setInServer(boolean inServer) {
        this.inServer = inServer;
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

    public ArrayList<byte[]> toChunks() {
        // TODO: Add final chunk, whose length should be 0,
        // to tell the receiving node that the message is complete

        //int numChunks = (this.length - (2 + 4 + 1 + 4 + 4))/20 +
        //int numChunks = this.length/20 + (this.length%20 == 0 ? 0 : 1);
        int numChunks = this.length/18 + (this.length%18 == 0 ? 0 : 1);
        // short totalLength = (short) (this.length + 2*numChunks);
        byte[] messageByteArray = this.toByteArray();
        ArrayList<byte[]> chunks = new ArrayList<>();

        for(int currentChunk = 1; currentChunk <= numChunks + 1; currentChunk++) {
            byte[] chunkByteArray;

            if(currentChunk <= numChunks) {
                short messageOffset = (short) (2 + 18 * (currentChunk - 1));

            /*short remainingChunksLength = (short) (totalLength - 20 * (currentChunk - 1));
            short chunkLength = 20 > remainingChunksLength ? remainingChunksLength : 20;*/

                short remainingMessageLength = (short) (this.length - 18 * (currentChunk - 1));
                short chunkLength = 18 > remainingMessageLength ? remainingMessageLength : 18;

                byte[] chunkLengthByteArray = shortToByteArrayRev(chunkLength);

                byte[] chunkPayloadByteArray = Arrays.copyOfRange(messageByteArray,
                        messageOffset,
                        messageOffset + chunkLength);

                /*byte[] chunkPayloadByteArray = Arrays.copyOfRange(messageByteArray,
                    20 * (currentChunk - 1),
                    20 * currentChunk);*/

                chunkByteArray = concatByteArrays(chunkLengthByteArray, chunkPayloadByteArray);
            } else {
                chunkByteArray = shortToByteArrayRev((short) 0);
            }

            if(chunkByteArray.length < 20) {
                byte[] paddingByteArray = new byte[20 - chunkByteArray.length];
                Arrays.fill(paddingByteArray, (byte) 0x0);
                chunkByteArray = concatByteArrays(chunkByteArray, paddingByteArray);
            }

            chunks.add(chunkByteArray);
        }

        return chunks;
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
                + "#" + byteArrayToIntRev(this.payload)
                + "#" + (inServer ? "In Server" : "Not in server");
    }
}