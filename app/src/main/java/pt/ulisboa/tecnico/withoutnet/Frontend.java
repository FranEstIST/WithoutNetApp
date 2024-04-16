package pt.ulisboa.tecnico.withoutnet;

import static pt.ulisboa.tecnico.withoutnet.network.FrontendErrorMessages.JSON_ERROR;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.StrictMode;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import pt.ulisboa.tecnico.withoutnet.models.Network;
import pt.ulisboa.tecnico.withoutnet.models.Node;
import pt.ulisboa.tecnico.withoutnet.models.Update;
import pt.ulisboa.tecnico.withoutnet.network.FrontendErrorMessages;

public class Frontend {
    private final String TAG = "Frontend";

    //private String serverURL = "http://10.0.2.2:8080/";     // change accordingly
    private String serverURL = "http://192.168.1.102:8081/";
    //private String serverURL = "http://sigma01.ist.utl.pt:50012/";
    //private String serverURL = "http://10.5.192.102:8081/";

    private RequestQueue requestQueue;
    private static final String BASE_URL = "https://192.168.1.102:8081/";

    private String token;
    private GlobalClass globalClass;

    public Frontend(GlobalClass globalClass) {
        this.globalClass = globalClass;
        this.requestQueue = globalClass.getRequestQueue();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public void sendMessageToServerViaVolley(Message message, FrontendResponseListener responseListener) {
        // This should check whether or not the smartphone is connected to the internet
        if (getConnectionType() == -1) return;

        String url = BASE_URL + "add-message";

        JSONObject jsonRequest = new JSONObject();

        try {
            jsonRequest.put("length", message.getLength());
            jsonRequest.put("timestamp", message.getTimestamp());
            jsonRequest.put("messageType", message.getMessageTypeAsInt());
            jsonRequest.put("sender", message.getSender());
            jsonRequest.put("receiver", message.getReceiver());
            jsonRequest.put("payload", message.getPayloadAsByteString());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Received response (sendMessageToServerViaVolley)");
                responseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Received error");
                responseListener.onError("Error");
            }
        });

        this.requestQueue.add(request);
    }

    public void getAllMessagesInServerViaVolley(FrontendResponseListener responseListener) {
        if (getConnectionType() == -1) return;

        String url = BASE_URL + "get-all-messages";

        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Received response (getAllMessagesInServerViaVolley)");

                try {
                    int statusCode = response.getInt("status");

                    List<Message> receivedMessages = null;

                    // if status is OK, create an ArrayList with all the messages
                    if (statusCode == StatusCodes.OK) {
                        JSONArray messagesJsonArray = response.getJSONArray("messages");

                        if (messagesJsonArray == null)
                            responseListener.onResponse(receivedMessages);

                        receivedMessages = new ArrayList<>();

                        for (int i = 0; i < messagesJsonArray.length(); i++) {
                            // TODO: Fix this

                            JSONObject messageJson = messagesJsonArray.getJSONObject(i);
                            short length = (short) messageJson.getInt("length");
                            long timestamp = messageJson.getLong("timestamp");
                            int messageType = messageJson.getInt("messageType");
                            int sender = messageJson.getInt("sender");
                            int receiver = messageJson.getInt("receiver");
                            String payload = messageJson.getString("payload");

                            Message receivedMessage = new Message(length, timestamp, messageType, sender, receiver, payload);

                            receivedMessages.add(receivedMessage);
                        }

                        responseListener.onResponse(receivedMessages);
                    }
                    responseListener.onError("Error");
                } catch (JSONException e) {
                    Log.e(TAG, "Received error");
                    e.printStackTrace();
                    responseListener.onError("Error");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseListener.onError("Error");
            }
        });

        this.requestQueue.add(request);
    }

    public void sendMessageBatchToServer(List<Message> messageBatch, FrontendResponseListener responseListener) {
        if (getConnectionType() == -1) return;

        String url = BASE_URL + "add-message-batch";

        JSONObject jsonRequest = new JSONObject();

        JSONArray messageBatchJsonArray = new JSONArray();
        try {
            for (Message message : messageBatch) {
                messageBatchJsonArray.put(getMessageJson(message));
            }

            jsonRequest.put("messageBatch", messageBatchJsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
            responseListener.onError(FrontendErrorMessages.JSON_ERROR);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Received response (sendMessageToServerViaVolley)");
                responseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Received error");
                responseListener.onError("Error");
            }
        });

        this.requestQueue.add(request);
    }

    /*public Update getMostRecentUpdateByNodeFromServer(Node node) {
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
    }*/

    public void getAllNodesInServer(FrontendResponseListener responseListener) {
        if(getConnectionType() == -1) {
            responseListener.onError(null);
            return;
        }

        String url = BASE_URL + "get-all-nodes";

        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int status = response.getInt("status");

                    if(status == StatusCodes.OK) {
                        JSONArray nodesJsonArray = response.getJSONArray("nodes");
                        ArrayList<Node> nodes = new ArrayList<>();

                        for(int i = 0; i < nodesJsonArray.length(); i++) {
                            Node node = buildNodeFromJson(nodesJsonArray.getJSONObject(i));
                            nodes.add(node);
                        }

                        responseListener.onResponse(nodes);
                    } else {
                        responseListener.onError(FrontendErrorMessages.fromStatusCode(status));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    responseListener.onError(JSON_ERROR);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                responseListener.onResponse(FrontendErrorMessages.VOLLEY_ERROR);
            }
        });

        this.requestQueue.add(request);
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

    private JSONObject getMessageJson(Message message) throws JSONException {
        JSONObject messageJson = new JSONObject();

        messageJson.put("length", message.getLength());
        messageJson.put("timestamp", message.getTimestamp());
        messageJson.put("messageType", message.getMessageTypeAsInt());
        messageJson.put("sender", message.getSender());
        messageJson.put("receiver", message.getReceiver());
        messageJson.put("payload", message.getPayloadAsByteString());

        return messageJson;
    }

    private Node buildNodeFromJson(JSONObject nodeJson) throws JSONException {
        String uuid = nodeJson.getString("id");
        String commonName = nodeJson.getString("common-name");
        int networkId = nodeJson.getInt("network-id");
        String networkName = nodeJson.getString("network-name");;

        Network network = new Network(networkId, networkName);

        return new Node(uuid, commonName, network);
    }

    public interface FrontendResponseListener {
        public void onResponse(Object response);

        public void onError(String errorMessage);
    }
}
