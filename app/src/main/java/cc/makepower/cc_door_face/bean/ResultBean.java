package cc.makepower.cc_door_face.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * json返回的最外层json的格式
 * @author ygq
 *
 * @param <T>
 */
public class ResultBean<T> {
	@Expose
	@SerializedName("resultCode")
	private int code;
	@Expose
	@SerializedName("resultMsg")
	private String message;
	@Expose
	@SerializedName("data")
	private T data;

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public T getData() {
		return data;
	}

	public void setData(T t) {
		this.data = t;
	}

	public static final class Code {
		public static final int SUCCESS = 200;
		public static final int LOGIN_TIMEOUT = 4001;
	}

	@Override
	public String toString() {
		return "Result{" +
				"code=" + code +
				", message='" + message + '\'' +
				", data=" + data +
				'}';
	}
}