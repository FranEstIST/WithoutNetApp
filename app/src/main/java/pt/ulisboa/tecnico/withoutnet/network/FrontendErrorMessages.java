package pt.ulisboa.tecnico.withoutnet.network;

public class FrontendErrorMessages {
    public static final String JSON_ERROR = "Json error";
    public static final String VOLLEY_ERROR = "Json error";

    public static String fromStatusCode(int statusCode) {
        return "Unknown error";
    }
}
