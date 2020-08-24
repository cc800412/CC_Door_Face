package cc.makepower.cc_door_face.retrofit;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cc.makepower.blesdk.BuildConfig;
import cc.makepower.cc_door_face.bean.ResultBean;
import cc.makepower.cc_door_face.retrofit.cookies.CookiesManager;
import cc.makepower.cc_door_face.utils.Map2JsonTool;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by bll on 2020/8/24 0024.
 */
public class RetDataSource {
    private static final int DEFAULT_TIMEOUT = 30;//超时时间30s
    private static final int DEFAULT_PAGESIZE = 20;//默认一页的数量
    static RetDataSource appservice;
    RetApi retApi;
    Gson gsonBuilder;

    public static RetDataSource getInstance(Context context) {
        if (appservice == null) {
            appservice = new RetDataSource(context, "");
        }
        return appservice;
    }

    OkHttpClient client;

    public RetDataSource(Context context, final String token) {
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
                .baseUrl(RetUrl.BASE_URL_HTTP + RetUrl.BASE_URL_DOMAIN_DEFAULT + File.pathSeparator
                        +RetUrl.BASE_URL_PORT_DEFAULT+ File.separator)
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(client)
                .build();

        retApi = retrofit.create(RetApi.class);
    }

    /**
     * 请求头
     *
     * @param context
     * @param token
     */
    public void addHeaders(Context context, final String token) {
        appservice = new RetDataSource(context, token);
    }

    /**
     *远程开门
     * @param stationCode
     * @param deviceID
     * @return
     */
    public Observable<ResultBean> buildRemotedoor(String stationCode, String deviceID) {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("deviceID", deviceID);
        return retApi.buildRemotedoor(stringObjectMap)
                .throttleFirst(5, TimeUnit.SECONDS);
    }
}
