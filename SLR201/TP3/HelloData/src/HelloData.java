public class HelloData implements java.io.Serializable{
    private String message;
    private transient String transientMessage;
 
    public HelloData(String message, String transietMessage) {
        this.message = message;
        this.transientMessage = transientMessage;
    }

    public String getMessage() {
        return message;
    }

    public String getTransientMessage () {
        return transientMessage;
    }

    public void setMessage (String message) {
        this.message = message;
    }

    public void setTransientMessage (String transientMessage) {
        this.transientMessage = transientMessage;
    }

}
