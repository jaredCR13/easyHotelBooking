package hotelbookingcommon.domain;


import java.io.Serializable;

public class Request{
    private String action;
    private Object data;

    public Request() {}

    public Request(String action, Object data) {
        this.action = action;
        this.data = data;
    }

    public String getAction() {
        return action;
    }

    public Object getData() {
        return data;
    }
}

