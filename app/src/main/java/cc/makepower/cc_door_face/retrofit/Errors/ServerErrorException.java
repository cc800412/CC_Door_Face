package cc.makepower.cc_door_face.retrofit.Errors;

/**
 * 服务器异常类
 */
public class ServerErrorException extends RuntimeException {
    private int resultCode;
    private String resultMessage;

    public ServerErrorException() {
    }

    public ServerErrorException(int resultCode, String resultMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }

    public int code() {
        return resultCode;
    }

    public String message() {
        return resultMessage;
    }
}
