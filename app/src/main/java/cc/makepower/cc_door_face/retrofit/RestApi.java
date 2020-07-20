package cc.makepower.cc_door_face.retrofit;

import java.util.Map;

import cc.makepower.cc_door_face.bean.ResultBean;
import cc.makepower.cc_door_face.bean.VarListBean;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
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
    @GET(Url.URL_ROOT_ + "rest/v1/queryRemoteDoorList")
    Observable<ResultBean<VarListBean>> fetchDoorList(@QueryMap Map<String, Object> stringObjectMap);


    @POST(Url.URL_ROOT_ + "rest/v1/buildRemotedoor")
    @FormUrlEncoded
    Observable<ResultBean> buildRemotedoor(@FieldMap Map<String, Object> stringObjectMap);



    /*
   下载文件
    */
    @Streaming
    @GET
    Observable<ResponseBody> downLoadFile(@retrofit2.http.Url String fileUrl);
}

