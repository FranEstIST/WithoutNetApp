package pt.ulisboa.tecnico.withoutnet;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.StrictMode;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.withoutnet.constants.StatusCodes;
import pt.ulisboa.tecnico.withoutnet.models.Message;
import pt.ulisboa.tecnico.withoutnet.models.Node;
import pt.ulisboa.tecnico.withoutnet.models.Update;

public class Frontend {
    //private String serverURL = "http://10.0.2.2:8080/";     // change accordingly
    //private String serverURL = "http://192.168.1.102:8081/";
    private String serverURL = "http://sigma01.ist.utl.pt:50012/";
    //private String serverURL = "http://10.5.192.102:8081/";

    private String token;
    private GlobalClass globalClass;

    private final String TAG = "FRONTEND";

    public Frontend(GlobalClass globalClass) {
        this.globalClass = globalClass;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public int sendMessageToServer(Message message) {
        // This should check whether or not the smartphone is connected to the internet
        if (getConnectionType() == -1) return -1;

        // Create the update's json object
        JsonObject sendMessageJson = JsonParser.parseString("{}").getAsJsonObject();
        sendMessageJson.addProperty("localId", message.getId());
        sendMessageJson.addProperty("messageType", message.getMessageTypeAsInt());
        sendMessageJson.addProperty("timestamp", message.getTimestamp());
        sendMessageJson.addProperty("sender", message.getSender());
        sendMessageJson.addProperty("receiver", message.getReceiver());
        sendMessageJson.addProperty("content", message.getContent());

        // Send request and extract status code
        JsonObject response = postRequest("add-message", sendMessageJson.toString());

        return response.get("status").getAsInt();
    }

    public List<Message> getAllMessagesInServer() {
        //if (getConnectionType() == -1) return -1;

        // Send request and extract status code
        JsonObject response = postRequest("get-all-messages", "");

        int statusCode = response.get("status").getAsInt();

        List<Message> receivedMessages = null;

        // if status is OK, create an ArrayList with all the acl users
        if (statusCode == StatusCodes.OK) {
            JsonArray messagesJsonArray = response.get("messages").getAsJsonArray();

            if (messagesJsonArray == null) return receivedMessages;  // avoids empty jsons

            receivedMessages = new ArrayList<>();

            for(int i = 0; i < messagesJsonArray.size(); i++) {
                JsonObject messageJson = messagesJsonArray.get(i).getAsJsonObject();
                long id = messageJson.get("id").getAsLong();
                long timestamp = messageJson.get("timestamp").getAsLong();
                int messageType = messageJson.get("messageType").getAsInt();
                String sender = messageJson.get("sender").getAsString();
                String receiver = messageJson.get("receiver").getAsString();
                String content = messageJson.get("content").getAsString();

                Message receivedMessage = new Message(id, timestamp, messageType, sender, receiver, content);

                receivedMessages.add(receivedMessage);
            }
        }

        return receivedMessages;
    }

    public int sendUpdateToServer(Update update) {
        // This should check whether or not the smartphone is connected to the internet
        if (getConnectionType() == -1) return -1;

        // Create the update's json object
        JsonObject sendUpdateJson = JsonParser.parseString("{}").getAsJsonObject();
        sendUpdateJson.addProperty("timestamp", update.getTimestamp());
        sendUpdateJson.add("sender", getNodeJson(update.getSender()));
        sendUpdateJson.addProperty("reading", update.getReading());

        // Send request and extract status code
        JsonObject response = postRequest("add-update", sendUpdateJson.toString());

        return response.get("status").getAsInt();
    }

    public Update getMostRecentUpdateByNodeFromServer(Node node) {
        //if (getConnectionType() == -1) return -1;

        // Create the node's json object
        JsonObject sendUpdateJson = getNodeJson(node);

        // Send request and extract status code
        JsonObject response = getRequest("get-most-recent-update-by-node-id/" + node.getId(), sendUpdateJson.toString());

        int statusCode = response.get("status").getAsInt();

        Update receivedUpdate = null;

        // if status is OK, create an ArrayList with all the acl users
        if (statusCode == StatusCodes.OK) {
            JsonObject updateJson = response.get("update").getAsJsonObject();

            if (updateJson == null) return receivedUpdate;  // avoids empty jsons

            long timestamp = updateJson.get("timestamp").getAsLong();
            String reading = updateJson.get("reading").getAsString();
            Node sender = buildNodefromJson(updateJson.get("sender").getAsJsonObject());

            receivedUpdate = new Update(timestamp, sender, reading);
        }

        return receivedUpdate;
    }

    private int getConnectionType() {
        int result = -1;

        Context context = globalClass.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    result = 0;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    result = 1;
                }
            }
        }

        return result;
    }

    private JsonObject getNodeJson(Node node) {
        JsonObject nodeJson = JsonParser.parseString("{}").getAsJsonObject();
        nodeJson.addProperty("uuid", node.getId());
        nodeJson.addProperty("common-name", node.getCommonName());
        nodeJson.addProperty("reading-type", node.getReadingType());
        return nodeJson;
    }

    private Node buildNodefromJson(JsonObject nodeJson) {
        String uuid = nodeJson.get("uuid").getAsString();
        String commonName = nodeJson.get("commonName").getAsString();
        String readingType = nodeJson.get("readingType").getAsString();
        return new Node(uuid, commonName, readingType);
    }

    private JsonObject getRequest(String endpoint, String data) {
        JsonObject responseJson = JsonParser.parseString("{}").getAsJsonObject();
        responseJson.addProperty("status", -1);

        String endpointURL = this.serverURL + endpoint;

        responseJson.addProperty("status", -1);

        int responseCode;
        String responseString;

        HttpURLConnection connection;

        try {
            connection = (HttpURLConnection) new URL(endpointURL).openConnection();
        } catch (IOException e) {
            System.out.println(String.format("Failed to open a connection with URL '%s'", endpointURL));
            return responseJson;
        }

        try {
            connection.setRequestMethod("GET");
        } catch (ProtocolException e) {
            System.out.println(String.format("Failed to set post method"));
        }

        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        try {
            writeStream(connection.getOutputStream(), data);

            InputStream in = new BufferedInputStream(connection.getInputStream());

            if (connection.getResponseCode() != -1) {
                responseString = readStream(in);
            } else {
                return JsonParser.parseString("{status:-1}").getAsJsonObject();
            }

            System.out.println("RESPONSE: " + responseString);
            return JsonParser.parseString(responseString).getAsJsonObject();
        } catch (IOException e) {
            System.out.println(String.format("Failed to write to the connection"));
            e.printStackTrace();
        }

        return JsonParser.parseString("{status:-1}").getAsJsonObject();
    }

    private JsonObject postRequest(String endpoint, String data) {
        JsonObject responseJson = JsonParser.parseString("{}").getAsJsonObject();
        responseJson.addProperty("status", -1);

        String endpointURL = this.serverURL + endpoint;

        responseJson.addProperty("status", -1);

        int responseCode;
        String responseString;

        HttpURLConnection connection;

        try {
            connection = (HttpURLConnection) new URL(endpointURL).openConnection();
        } catch (IOException e) {
            System.out.println(String.format("Failed to open a connection with URL '%s'", endpointURL));
            return responseJson;
        }

        try {
            connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            System.out.println(String.format("Failed to set post method"));
        }

        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        try {
            writeStream(connection.getOutputStream(), data);

            InputStream in = new BufferedInputStream(connection.getInputStream());

            if (connection.getResponseCode() != -1) {
                responseString = readStream(in);
            } else {
                return JsonParser.parseString("{status:-1}").getAsJsonObject();
            }

            System.out.println("RESPONSE: " + responseString);
            return JsonParser.parseString(responseString).getAsJsonObject();
        } catch (IOException e) {
            System.out.println(String.format("Failed to write to the connection"));
            e.printStackTrace();
        }

        return JsonParser.parseString("{status:-1}").getAsJsonObject();
    }

    private void writeStream(OutputStream os, String data) throws IOException {
        OutputStreamWriter oSW = new OutputStreamWriter(os);
        oSW.write(data);
        oSW.flush();
        oSW.close();
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }
}
