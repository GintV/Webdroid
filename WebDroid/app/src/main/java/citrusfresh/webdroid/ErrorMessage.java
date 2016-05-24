package citrusfresh.webdroid;

/**
 * Created by Eimantas on 2016-05-24.
 */
public class ErrorMessage {
    private int errorID;
    private String errorText;

    public void setErrorID(int errorID) {
        this.errorID = errorID;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public int getErrorID() {

        return errorID;
    }

    public String getErrorText() {
        return errorText;
    }
}
