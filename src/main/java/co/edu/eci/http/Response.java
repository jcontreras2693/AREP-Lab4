package co.edu.eci.http;

public class Response {
    private int status = 200;
    private String contentType = "text/plain";

    public int getStatus() {
        return status;
    }
    public Response status(int status) {
        this.status = status;
        return this;
    }
    public String getContentType() {
        return contentType;
    }
    public Response contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
}
