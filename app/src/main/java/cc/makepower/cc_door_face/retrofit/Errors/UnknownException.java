package cc.makepower.cc_door_face.retrofit.Errors;

public class UnknownException extends ServerErrorException {


    public UnknownException(int resultCode, String resultMessage) {
        super(resultCode, resultMessage);
    }
}
