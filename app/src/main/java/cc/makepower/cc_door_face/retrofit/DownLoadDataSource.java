package cc.makepower.cc_door_face.retrofit;

import android.app.Activity;
import android.content.Context;

import cc.makepower.cc_door_face.retrofit.download.CL_DownLoadListener;
import cc.makepower.cc_door_face.retrofit.download.Cl_DownLoadInterceptor;
import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * author : atex
 * e-mail : xxx@xx
 * time   : 2018/04/13
 * desc   :
 * version: 1.0
 */

public class DownLoadDataSource {
    private RestApi restApi;
    Retrofit retrofit;
    static DownLoadDataSource appservice;

    public static DownLoadDataSource getInstance(Context context) {
        if (appservice == null) {
            appservice = new DownLoadDataSource();
        }
        return appservice;
    }

    Retrofit.Builder builder;

    public DownLoadDataSource() {
        builder = new Retrofit.Builder()
                .baseUrl(Url.BASE_URL_HTTP + "www.baidu.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
    }

//

    /**
     * 下载文件
     *
     * @param url
     * @param cl_downLoadListener
     * @return
     */
    public Observable<ResponseBody> downLoad(Activity activity, String url, CL_DownLoadListener cl_downLoadListener) {
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Cl_DownLoadInterceptor(activity, cl_downLoadListener)).build();
        retrofit = builder.client(client)
                .build();
        restApi = retrofit.create(RestApi.class);
        cl_downLoadListener.onStart();
        return restApi.downLoadFile(url);
    }

}
