package cc.makepower.cc_door_face.retrofit;

import java.util.List;
import java.util.Map;

import cc.makepower.cc_door_face.bean.ResultBean;
import cc.makepower.cc_door_face.bean.VarListBean;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;

/**
 * author : atex
 * e-mail : xxx@xx
 * time   : 2018/09/08
 * desc   :
 * version: 1.0
 */

public interface RestApi {
    /*
   下载文件
    */
    @Streaming
    @GET
    Observable<ResponseBody> downLoadFile(@retrofit2.http.Url String fileUrl);

    @GET("face/face-permission")
    Observable<ResultBean<List<String>>> fetchFaceList(@QueryMap Map<String, Object> stringObjectMap);

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("/face/face-pushLog")
    Observable<ResultBean<Boolean>> pushOpenLog(@Body RequestBody info);
}

