package cc.makepower.cc_door_face.retrofit;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.concurrent.TimeUnit;

import cc.makepower.blesdk.BuildConfig;
import cc.makepower.cc_door_face.bean.BindDeviceResult;
import cc.makepower.cc_door_face.bean.PostResultBean;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class DownLoadPermissionDataSource {
    private static final int DEFAULT_TIMEOUT = 30;//超时时间30s
    private static final int DEFAULT_PAGESIZE = 20;//默认一页的数量
    static DownLoadPermissionDataSource appservice;
    DownLoadPermissionRestApi restApi;
    Gson gsonBuilder;

    public static DownLoadPermissionDataSource getInstance(Context context) {
        if (appservice == null) {
            appservice = new DownLoadPermissionDataSource(context);
        }
        return appservice;
    }

    public DownLoadPermissionDataSource(Context context) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)//设置超时时间
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
//                .cookieJar(new CookiesManager(context));//cookies持久化


//        mConfigPref = new ConfigPref(context);
        if (BuildConfig.DEBUG) {
            HttpLoggerInterceptor loggerInterceptor = new HttpLoggerInterceptor();
            loggerInterceptor.setLevel(HttpLoggerInterceptor.Level.BODY);
            builder.addInterceptor(loggerInterceptor);//日志输出
        }
        OkHttpClient client = builder.build();


        gsonBuilder = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .setVersion(1.0)
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Url.BASE_URL_HTTP + Url.BASE_URL_DOMAIN_DEFAULT + File.pathSeparator +"8007" + File.separator)
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(client)
                .build();

        restApi = retrofit.create(DownLoadPermissionRestApi.class);
    }


    /**
     * 绑定设备
     * @param deviceID
     * @param panelId
     * @return
     */
    public Observable<PostResultBean<BindDeviceResult>> fetchDoorPermission(String deviceID, String panelId) {

        return restApi.fetchDoorPermission()
                .throttleFirst(5, TimeUnit.SECONDS)
                .map(new HttpResultInterceptorFunc<PostResultBean<BindDeviceResult>>());
    }

}