package cc.makepower.cc_door_face.retrofit;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cc.makepower.blesdk.BuildConfig;
import cc.makepower.cc_door_face.bean.ResultBean;
import cc.makepower.cc_door_face.bean.VarListBean;
import cc.makepower.cc_door_face.retrofit.cookies.CookiesManager;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestDataSource {
    private static final int DEFAULT_TIMEOUT = 30;//超时时间30s
    private static final int DEFAULT_PAGESIZE = 20;//默认一页的数量
    static RestDataSource appservice;
    RestApi restApi;
    Gson gsonBuilder;

    public static RestDataSource getInstance(Context context) {
        if (appservice == null) {
            appservice = new RestDataSource(context, "");
        }
        return appservice;
    }

    OkHttpClient client;

    public RestDataSource(Context context, final String token) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {

                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        //请求头里添加部分参数  服务器可以从请求头中拿到想到的数据
                        Request.Builder requestBuilder = original.newBuilder()
                                .header("token", TextUtils.isEmpty(token) ? "" : token)//token
//                                .header("token", "9b98656786f2a41561c9db3d1a6c645caafedadc9bbb0b23a1bcc72f102b9f29")//token
                                .header("clientType", "2");//客户端类型，2代表是android端请求
                        return chain.proceed(requestBuilder.build());
                    }
                })
//                .addInterceptor(new MoreBaseUrlInterceptor())
                .cookieJar(new CookiesManager(context));//cookies持久化
        if (BuildConfig.DEBUG) {
            HttpLoggerInterceptor loggerInterceptor = new HttpLoggerInterceptor();
            loggerInterceptor.setLevel(HttpLoggerInterceptor.Level.BODY);
            builder.addInterceptor(loggerInterceptor);//日志输出
        }
        client = builder.build();


        gsonBuilder = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .setVersion(1.0)
                .create();
        initRetrofit(context);

    }

    public void initRetrofit(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Url.BASE_URL_HTTP + Url.BASE_URL_DOMAIN_DEFAULT + File.pathSeparator
                        +Url.BASE_URL_PORT_DEFAULT+ File.separator)
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(client)
                .build();

        restApi = retrofit.create(RestApi.class);
    }

    /**
     * 请求头
     *
     * @param context
     * @param token
     */
    public void addHeaders(Context context, final String token) {
        appservice = new RestDataSource(context, token);
    }

    /**
     * 获取门锁列表
     *
     * @return
     */
    public Observable<VarListBean> fetchDoorList(int pageIndex) {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("pageSize", 20);
        stringObjectMap.put("pageIndex", pageIndex);
        return restApi.fetchDoorList(stringObjectMap)
                .throttleFirst(5, TimeUnit.SECONDS)
                .map(new HttpResultInterceptorFunc<VarListBean>());
    }


    /**
     * 远程开门
     *
     * @param stationCode
     * @param deviceID
     * @return
     */
    public Observable<ResultBean> buildRemotedoor(String stationCode, String deviceID) {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("deviceID", deviceID);
        return restApi.buildRemotedoor(stringObjectMap)
                .throttleFirst(5, TimeUnit.SECONDS);
    }
    /**
     * 下载
     *
     * @param url
     * @return
     */
    public Observable<ResponseBody> downLoadFile(String url) {

        return restApi.downLoadFile(url)
                .throttleFirst(5, TimeUnit.SECONDS);
    }

}