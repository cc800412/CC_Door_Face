package cc.makepower.cc_door_face.retrofit;

import java.util.Map;

import cc.makepower.cc_door_face.bean.ResultBean;
import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by bll on 2020/8/24 0024.
 */
public interface RetApi {
    @POST(Url.URL_ROOT_ + "rest/v1/buildRemotedoor")
    @FormUrlEncoded
    Observable<ResultBean> buildRemotedoor(@FieldMap Map<String, Object> stringObjectMap);
}
