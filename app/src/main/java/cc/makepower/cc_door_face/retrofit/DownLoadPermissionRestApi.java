package cc.makepower.cc_door_face.retrofit;
import cc.makepower.cc_door_face.bean.BindDeviceResult;
import cc.makepower.cc_door_face.bean.PostResultBean;
import cc.makepower.cc_door_face.bean.ResultBean;
import io.reactivex.Observable;
import retrofit2.http.GET;

public interface DownLoadPermissionRestApi {


    //获取最新的人脸权限
    @GET( "getDoorPermission")
    Observable<ResultBean<PostResultBean<BindDeviceResult>>> fetchDoorPermission();


}
