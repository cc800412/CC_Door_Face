package cc.makepower.cc_door_face.retrofit;

import cc.makepower.cc_door_face.bean.ResultBean;
import cc.makepower.cc_door_face.retrofit.Errors.UnknownException;
import io.reactivex.functions.Function;

/**
 * 通用数据解析器
 * Created by atex on 16/4/26.
 */
public class HttpResultInterceptorFunc<T> implements Function<ResultBean<T>, T> {

    @Override
    public T apply(ResultBean<T> tResult) throws Exception {
        if (tResult.getCode() != ResultBean.Code.SUCCESS) {
            if (tResult.getCode() == ResultBean.Code.LOGIN_TIMEOUT) {
//                EventBus.getDefault().post(new EventJPush(JPushReceiver.LOGIN_TIMEOUT,tResult.getMessage(),null));
            }
            throw new UnknownException(tResult.getCode(), tResult.getMessage());
        }
        return tResult.getData();
    }
}