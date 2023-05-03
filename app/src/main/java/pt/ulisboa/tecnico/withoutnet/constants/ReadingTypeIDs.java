package pt.ulisboa.tecnico.withoutnet.constants;

public class ReadingTypeIDs {
    public static final String BATTERY_READING_ID = "2A19";

    public static String getReadingTypeName(String id) {
        switch(id) {
            case BATTERY_READING_ID:
                // TODO: Implement the global context so that this method
                // can return a string defined in the resources
                return "Battery Reading";
            default:
                return "Unknown Type";
        }
    }
}
