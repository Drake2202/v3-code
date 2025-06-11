/*
 * (c) 2013 InfinityArts
 * All codes are for use only in HiddenProject
 */
package augoeides.dispatcher;

/**
 *
 * @author Mystical
 */
public class RequestException extends Exception {

    private static final long serialVersionUID = 1L;

    private String type = "warning";

    public RequestException() {
    }

    public RequestException(String msg) {
        super(msg);
    }

    public RequestException(String msg, String type) {
        super(msg);

        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
