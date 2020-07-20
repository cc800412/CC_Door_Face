package cc.makepower.cc_door_face.retrofit.download;

import android.app.Activity;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * author : atex
 * e-mail : xxx@xx
 * time   : 2018/04/13
 * desc   :
 * version: 1.0
 */

public class Cl_DownLoadInterceptor implements Interceptor {


    private Activity activity;
    private CL_DownLoadListener cl_downLoadListener;

    public Cl_DownLoadInterceptor(Activity activity, CL_DownLoadListener cl_downLoadListener) {
        this.cl_downLoadListener = cl_downLoadListener;
        this.activity = activity;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response=chain.proceed(chain.request());
        return response.newBuilder().body(new Cl_ResponseBody(activity,cl_downLoadListener,response.body())).build();
    }
}
