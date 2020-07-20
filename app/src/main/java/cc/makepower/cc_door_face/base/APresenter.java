package cc.makepower.cc_door_face.base;

import android.content.Context;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

import androidx.annotation.NonNull;
import cc.makepower.cc_door_face.retrofit.Errors.RxJava2NullException;
import cc.makepower.cc_door_face.retrofit.Errors.ServerErrorException;
import cc.makepower.cc_door_face.retrofit.Errors.UnknownException;
import cc.makepower.cc_door_face.retrofit.RestDataSource;
import io.reactivex.disposables.Disposable;
import retrofit2.HttpException;


/**
 * @author: ATEX(YGQ)
 * @description:这里添加描述
 * @projectName: MakepeoDo
 * @date: 2016-10-11
 * @time: 16:56
 */
public  abstract class APresenter<T> implements LifecyclePresenter<T> {

    protected T mView;
    protected RestDataSource restDataSource;



    protected Disposable subscription;

    public void setSubscription(Disposable subscription) {
        this.subscription = subscription;
    }

    public Context context;
    public APresenter(Context context) {
        this.context=context;
        restDataSource=RestDataSource.getInstance(context);
    }

    /**
     * 根据捕获的错误返回相应的提示
     * @param e
     * @return
     */
    public String showError(Throwable e){
        e.printStackTrace();
        if(e instanceof UnknownException){
            if (((UnknownException) e).code()==5||((UnknownException) e).code()==6){
                return ((UnknownException) e).code() + ":" + ((UnknownException) e).message()+","+"请重新登陆";
            }else {
                return ((UnknownException) e).code() + ":" + ((UnknownException) e).message();
            }
        }
        if (e instanceof RxJava2NullException){
            return "";
        }
        if(e instanceof ServerErrorException){
            return ((ServerErrorException) e).code()+":"+((ServerErrorException) e).message();
        }
        if (e instanceof RuntimeException) {

            return "运行异常"+e.getMessage();
        }
        if (e instanceof ConnectException) {
            return "网络连接异常";
        }
        if (e instanceof SocketTimeoutException) {
            return "网络连接超时";
        }
        if (e instanceof HttpException && ((HttpException) e).response().code() != HttpURLConnection.HTTP_OK){

            return "http异常,错误码:"+ ((HttpException) e).response().code();
        }

        return "未知错误:"+e.toString();
    }
    public String showErrorMessage(Throwable e){
        if(e instanceof UnknownException){
            return ((UnknownException) e).message();
        }  if(e instanceof ServerErrorException){
            return ((ServerErrorException) e).message();
        }
        if (e instanceof RuntimeException) {
            return "运行异常";
        }
        if (e instanceof ConnectException) {
            return "网络连接异常";
        }
        if (e instanceof SocketTimeoutException) {
            return "网络连接超时";
        }
        if (e instanceof HttpException && ((HttpException) e).response().code() != HttpURLConnection.HTTP_OK){
            return "网络请求异常";
        }

        return "未知错误";
    }
    @Override
    public void attachView(@NonNull T v) {
        this.mView = v;
    }



    @Override
    public void detachView() {
//        this.mView = null;//不将view置为null  看看
//        unSubscribe();
    }




    public void unSubscribe(){
        if (subscription!=null){
            subscription.dispose();
        }
    }
}
