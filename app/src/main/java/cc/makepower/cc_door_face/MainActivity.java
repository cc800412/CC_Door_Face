package cc.makepower.cc_door_face;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import butterknife.BindView;
import cc.makepower.blesdk.SdkMethodInterCallBack;
import cc.makepower.blesdk.bean.BleInfoEntity;
import cc.makepower.blesdk.bean.LockStateEntity;
import cc.makepower.blesdk.bean.LogEntity;
import cc.makepower.blesdk.bean.ResultBean;
import cc.makepower.cc_door_face.base.APresenter;
import cc.makepower.cc_door_face.base.BaseActivity;
import cc.makepower.cc_door_face.bean.BindDeviceResult;
import cc.makepower.cc_door_face.bean.FacePreviewInfo;
import cc.makepower.cc_door_face.bean.RequestFeatureStatus;
import cc.makepower.cc_door_face.camera.CameraHelper;
import cc.makepower.cc_door_face.camera.CameraPreviewListener;
import cc.makepower.cc_door_face.camera.DrawHelper;
import cc.makepower.cc_door_face.camera.DrawInfo;
import cc.makepower.cc_door_face.camera.FaceHelper;
import cc.makepower.cc_door_face.camera.FaceListener;
import cc.makepower.cc_door_face.camera.FaceServer;
import cc.makepower.cc_door_face.dialog.CCDoorFaceBleOpenWaitDialog;
import cc.makepower.cc_door_face.dialog.CCDoorFaceCheckIngFaceDialog;
import cc.makepower.cc_door_face.utils.ConfigUtil;
import cc.makepower.cc_door_face.utils.DeviceUtils;
import cc.makepower.cc_door_face.utils.LocalShareInfoData;
import cc.makepower.cc_door_face.utils.TextToSpeechUtils;
import cc.makepower.cc_door_face.view.FaceRectView;
import cc.makepower.sdk.zje.BleBase;
import cn.jpush.android.api.JPushInterface;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class MainActivity extends BaseActivity implements MainContract.View {

    @BindView(R.id.face_rect_view)
    FaceRectView faceRectView;
    @BindView(R.id.texture_preview)
    TextureView previewView;
    @BindView(R.id.progressFaceNum)
    ProgressBar progressFaceNum;
    @BindView(R.id.tv_DoorPermission)
    TextView tv_DoorPermission;

    MainPresenter mainPresenter;
    private int displayOrientationTemp;
    private int cameraIdTemp;
    private static final int MAX_DETECT_NUM = 1;
    /**
     * 当FR成功，活体未成功时，FR等待活体的时间
     */
    private static final int WAIT_LIVENESS_INTERVAL = 50;
    private CameraHelper cameraHelper;
    private DrawHelper drawHelper;
    private Camera.Size previewSize;
    /**
     * 优先打开的摄像头
     */
    private Integer cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    //    private FaceEngine faceEngine;
    private FaceHelper faceHelper;
    private int afCode = -1;
    String deviceId;
    String deviceBleName;

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected APresenter injectPresenter() {
        mainPresenter = new MainPresenter(MainActivity.this);
        return mainPresenter;
    }

    @Override
    protected void afterOnCreateView(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Activity启动后就锁定为启动时的方向
//        switch (getResources().getConfiguration().orientation) {
//            case Configuration.ORIENTATION_PORTRAIT:
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                break;
//            case Configuration.ORIENTATION_LANDSCAPE:
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                break;
//            default:
//                break;
//        }
        deviceId = LocalShareInfoData.getInstance().getBindDeviceId(MainActivity.this);
        deviceBleName = LocalShareInfoData.getInstance().getdBindeviceBlueToothName(MainActivity.this);
        tv_DoorPermission.setText(LocalShareInfoData.getInstance().getBindDeviceName(MainActivity.this));
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA
                    , Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            mainPresenter.initFaceSdk();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mainPresenter.initFaceSdk();
                } else {
                    TextToSpeechUtils.getInstance(MainActivity.this).speek("权限被拒绝");
                }
                break;
        }
    }

    @Override
    public void initFaceSdkCallBack(boolean result) {
        if (result) {
            if (FaceServer.getInstance().init(this)) {
                initCamera();
            } else {
                TextToSpeechUtils.getInstance(MainActivity.this).speek("引擎初始化失败");
                onBackPressed();
            }
        }
    }


    private void initCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final FaceListener faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {

            }

            //请求FR的回调
            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId) {
                //FR成功
                if (faceFeature != null) {
                    if (mainPresenter.getLivenessMap().get(requestId) != null && mainPresenter.getLivenessMap().get(requestId) == LivenessInfo.ALIVE) {

                        mainPresenter.searchFace(faceHelper, faceFeature, requestId, deviceId);
                    } //活体检测未出结果，延迟100ms再执行该函数
                    else if (mainPresenter.getLivenessMap().get(requestId) != null && mainPresenter.getLivenessMap().get(requestId) == LivenessInfo.UNKNOWN) {
                        mainPresenter.getGetFeatureDelayedDisposables().add(Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
                                .subscribe(new Consumer<Long>() {
                                    @Override
                                    public void accept(Long aLong) {
                                        onFaceFeatureInfoGet(faceFeature, requestId);
                                    }
                                }));
                    }
                    //活体检测失败
                    else {

                        mainPresenter.getRequestFeatureStatusMap().put(requestId, RequestFeatureStatus.NOT_ALIVE);
                    }

                }
                //FR 失败
                else {

                    mainPresenter.getRequestFeatureStatusMap().put(requestId, RequestFeatureStatus.FAILED);
                }
            }

        };


        CameraPreviewListener cameraListener = new CameraPreviewListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation) {
                cameraIdTemp = cameraId;
                displayOrientationTemp = displayOrientation;


                previewSize = camera.getParameters().getPreviewSize();
                drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
                        , cameraId);

                faceHelper = new FaceHelper.Builder()
                        .faceEngine(FaceServer.getInstance().faceEngine)
                        .frThreadNum(MAX_DETECT_NUM)
                        .previewSize(previewSize)
                        .faceListener(faceListener)
                        .currentTrackId(ConfigUtil.getTrackId(MainActivity.this.getApplicationContext()))
                        .build();
            }


            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                if (faceRectView != null) {
                    faceRectView.clearFaceInfo();
                }
                //这些条件下 人脸识别没有意义
                if (TextUtils.isEmpty(deviceId) || progressFaceNum.getProgress() != 0
                        || (ccDoorFaceBleOpenWaitDialog != null && ccDoorFaceBleOpenWaitDialog.isShowing())) {
                    return;
                }
                List<DrawInfo> drawInfoList = new ArrayList<>();
                List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21);

                if (facePreviewInfoList != null && faceRectView != null && drawHelper != null) {
                    for (int i = 0; i < facePreviewInfoList.size(); i++) {
                        String name = faceHelper.getName(facePreviewInfoList.get(i).getTrackId());
                        Rect rect = adjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect(),
                                previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientationTemp
                                , cameraIdTemp
                        );
//                        if (rect.width() > dip2px(MainActivity.this, 250)) {
                        drawInfoList.add(new DrawInfo(rect, GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE, LivenessInfo.UNKNOWN,
                                name == null ? "" : name));
//                        }

                    }
                    drawHelper.transFormationDraw(faceRectView, drawInfoList);
                }
                mainPresenter.clearLeftFace(facePreviewInfoList);

                if (drawInfoList != null && drawInfoList.size() > 0 && previewSize != null) {

                    for (int i = 0; i < facePreviewInfoList.size(); i++) {
                        if (drawInfoList.get(0).getRect().width() > dip2px(MainActivity.this, 150)
                        ) {
                            mainPresenter.getLivenessMap().put(facePreviewInfoList.get(i).getTrackId(), facePreviewInfoList.get(i).getLivenessInfo().getLiveness());
                            /**
                             * 对于每个人脸，若状态为空或者为失败，则请求FR（可根据需要添加其他判断以限制FR次数），
                             * FR回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer)}中回传
                             */
                            if (mainPresenter.getRequestFeatureStatusMap().get(facePreviewInfoList.get(i).getTrackId()) == null
                                    || mainPresenter.getRequestFeatureStatusMap().get(facePreviewInfoList.get(i).getTrackId()) == RequestFeatureStatus.FAILED) {
                                mainPresenter.getRequestFeatureStatusMap().put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
                                faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
                            }
                        }

                    }
                }
            }

            @Override
            public void onCameraClosed() {

            }

            @Override
            public void onCameraError(Exception e) {

            }

            @Override
            public void onCameraConfiguraChanged(int cameraID, int displayOrientation) {
                if (drawHelper != null) {
                    drawHelper.setCameraDisplayOrientation(displayOrientation);
                }

            }
        };
        cameraHelper = new CameraHelper.Builder()
                .metrics(metrics)
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(cameraID != null ? cameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
        if (cameraHelper != null) {
            cameraHelper.start();
        }

        eventBusJpush(null);
        //设置极光推送
//        Set<String> hashSet=new HashSet<>();
//        hashSet.add(DeviceUtils.getUniqueId(MainActivity.this));
//        Log.d("测试极光", DeviceUtils.getUniqueId(MainActivity.this));
//        JPushInterface.addTags(MainActivity.this,0,hashSet);
        JPushInterface.setAlias(MainActivity.this, 0, DeviceUtils.getUniqueId(MainActivity.this));
        Log.d("UniqueId",DeviceUtils.getUniqueId(MainActivity.this));

        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = btManager.getAdapter();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
        } else {
            //去打开蓝牙
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }
    }

    @Override
    public void eventBusJpush(Bundle bundle) {
//        mainPresenter.fetchFaceList(1,deviceId);
        mainPresenter.fetchFaceList(1,DeviceUtils.getUniqueId(MainActivity.this));
    }

    CCDoorFaceCheckIngFaceDialog ccDoorFaceCheckIngFaceDialog;

    @Override
    public void searchFaceFindUserCallBack(boolean result, float similar, String compareResult) {
        if (result) {
            if (ccDoorFaceCheckIngFaceDialog == null || !ccDoorFaceCheckIngFaceDialog.isShowing()) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 5000);
                showCCDoorFaceCheckIngFaceDialog(similar, new File(FaceServer.ROOT_PATH + File.separator + FaceServer.SAVE_IMG_DIR + File.separator + compareResult + ".jpg"));
            }
        }

    }

    @Override
    public void fetchFaceListCallBack(boolean result, List<String> facePaths) {
        if (result) progressFaceNum.setMax(facePaths.size());
    }

    @Override
    public void pushOpenLogCallBack(boolean result) {
        if (result) showToast("日志上传成功！");
    }

    @Override
    public void downLoadPermissionCallBack(boolean result, int surplusNum) {
        if (surplusNum == 0) {
            FaceServer.getInstance().initFaceList(MainActivity.this);
        }
        progressFaceNum.setProgress(surplusNum);
        if (surplusNum == 0){
            showToast("下载完成！");
        }
    }


    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (ccDoorFaceCheckIngFaceDialog.isShowing()) {
                ccDoorFaceCheckIngFaceDialog.dismiss();
            }
        }
    };

    public void showCCDoorFaceCheckIngFaceDialog(float setSimilar, String img) {
        ccDoorFaceCheckIngFaceDialog = new CCDoorFaceCheckIngFaceDialog(MainActivity.this);
        ccDoorFaceCheckIngFaceDialog.setSimilar(setSimilar);
        ccDoorFaceCheckIngFaceDialog.setUserFaceImg(img);

        ccDoorFaceCheckIngFaceDialog.show();
    }

    public void showCCDoorFaceCheckIngFaceDialog(float setSimilar, File img) {
        ccDoorFaceCheckIngFaceDialog = new CCDoorFaceCheckIngFaceDialog(MainActivity.this);
        ccDoorFaceCheckIngFaceDialog.setSimilar(setSimilar);
        ccDoorFaceCheckIngFaceDialog.setUserFaceImg(img);
        ccDoorFaceCheckIngFaceDialog.show();
    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    /**
     * 销毁引擎
     */
    private void unInitEngine() {
        if (afCode == ErrorInfo.MOK) {
        }
    }

    @Override
    protected void onDestroy() {
        TextToSpeechUtils.getInstance(MainActivity.this).shutdown();
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }
        //faceHelper中可能会有FR耗时操作仍在执行，加锁防止crash
        if (faceHelper != null) {
            synchronized (faceHelper) {
                unInitEngine();
            }
            ConfigUtil.setTrackId(this, faceHelper.getCurrentTrackId());
            faceHelper.release();
        } else {
            unInitEngine();
        }
        if (mainPresenter.getGetFeatureDelayedDisposables() != null) {
            mainPresenter.getGetFeatureDelayedDisposables().dispose();
            mainPresenter.getGetFeatureDelayedDisposables().clear();
        }
        FaceServer.getInstance().unInit();

        super.onDestroy();
    }


    /*******************远程开门失败 开始走本地的蓝牙连接************/
    BluetoothAdapter mBluetoothAdapter;
    BleBase bleBase;
    CCDoorFaceBleOpenWaitDialog ccDoorFaceBleOpenWaitDialog;

    public void showCCDoorFaceCheckIngFaceDialog(String state) {
        ccDoorFaceBleOpenWaitDialog = new CCDoorFaceBleOpenWaitDialog(MainActivity.this);
        ccDoorFaceBleOpenWaitDialog.setState(state);
        ccDoorFaceBleOpenWaitDialog.show();
    }

    @Override
    public void buildRemotedoorCalBack(boolean result) {
        //远程开门失败 采用蓝牙开门
        if (!result && !TextUtils.isEmpty(deviceBleName)) {
            handler.removeCallbacks(runnable);
            if (ccDoorFaceCheckIngFaceDialog.isShowing()) {
                ccDoorFaceCheckIngFaceDialog.dismiss();
            }
            BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = btManager.getAdapter();
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                //开始搜索蓝牙
                showCCDoorFaceCheckIngFaceDialog("蓝牙搜索中...");
                startSweepBle();
            } else {
                //去打开蓝牙
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBtIntent);
            }
        }
    }

    BluetoothLeScanner bluetoothLeScanner;
    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();

    private void startSweepBle() {
        if (mBluetoothAdapter.isEnabled()) {
            bluetoothDeviceList.clear();
            if (bluetoothLeScanner == null) {
                bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            }
            /**
             * 5.0以上换搜索方式
             */

            bluetoothLeScanner.startScan(scanCallback);
            searchBleDeivce.postDelayed(removeCallbacks, 10000L);//每次搜索10秒
        } else {
            getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        }
    }

    private Handler searchBleDeivce = new Handler();
    private Runnable removeCallbacks = () -> stopSweepBle();

    private void stopSweepBle() {
        if (mBluetoothAdapter.isEnabled()) {
            handler.removeCallbacks(removeCallbacks);
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }

    boolean isConnect = false;
    /**
     * 5.0以上的 搜索回调
     */
    private ScanCallback scanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();

            if (!bluetoothDeviceList.contains(device) && device != null && device.getName() != null) {  //判断是否已经添加
                bluetoothDeviceList.add(device);
                if (device.getName().equalsIgnoreCase(deviceBleName) && !isConnect) {
                    isConnect = true;
                    stopSweepBle();
                    if (ccDoorFaceBleOpenWaitDialog.isShowing())
                        ccDoorFaceBleOpenWaitDialog.setState("建立开门连接中...");
                    bleBase = new BleBase(sdkMethodInterCallBack);
                    bleBase.bleConnect(device, MainActivity.this, "2VA6aWoEzD9jlivH", "FFFFFFFFFFFFFFFF", "0001", false);
//                    bleBase.bleConnect(device, MainActivity.this, "HQzd6rMsW29C1tyu", "FFFFFFFFFFFFFFFF", "0001", false);
                }
            }

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            stopSweepBle();
        }
    };

    SdkMethodInterCallBack sdkMethodInterCallBack = new SdkMethodInterCallBack() {
        @Override
        public void bleConnectCallBack(ResultBean resultBean) {
            Log.d("蓝牙连接测试", "bleConnectCallBack");
            if (resultBean.isRet()) {
                bleBase.getLockCode();
            } else {
                isConnect = false;

                    ccDoorFaceBleOpenWaitDialog.dismiss();
                TextToSpeechUtils.getInstance(MainActivity.this).speek("连接失败");

            }
        }

        @Override
        public void disConnectCallBack(ResultBean resultBean) {
            Log.d("蓝牙连接测试", "disConnectCallBack");
            isConnect = false;
                ccDoorFaceBleOpenWaitDialog.dismiss();
        }

        @Override
        public void getLockCodeCallBack(ResultBean<String> resultBean) {
            Log.d("蓝牙连接测试", "getLockCodeCallBack");
            if (resultBean.isRet()) {
                Date date = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.MINUTE, -5);
                Calendar calendarEnd = Calendar.getInstance();
                calendarEnd.setTime(date);
                calendarEnd.add(Calendar.MINUTE, 5);
//                if (ccDoorFaceBleOpenWaitDialog.isShowing())
//                    ccDoorFaceBleOpenWaitDialog.setState("开门中...");
                bleBase.openLock(resultBean.getObj(), calendar.getTime(), calendarEnd.getTime());
//                openDoorTimerOut.removeCallbacks(openDoorTimeroutRunable);
//                openDoorTimerOut.postDelayed(openDoorTimeroutRunable,5000);
            } else {
                isConnect = false;
//                if (ccDoorFaceBleOpenWaitDialog.isShowing())
//                    ccDoorFaceBleOpenWaitDialog.setState("获取设备Id失败开门失败");
                TextToSpeechUtils.getInstance(MainActivity.this).speek("获取设备Id失败开门失败");
                bleBase.disConnect();
            }
        }

        @Override
        public void getKeyCodeCallBack(ResultBean<String> resultBean) {

        }

        @Override
        public void getBleInfoCallBack(ResultBean<BleInfoEntity> resultBean) {

        }

        @Override
        public void getLockStateCallBack(ResultBean<LockStateEntity> resultBean) {

        }

        @Override
        public void setBleClockCallBack(ResultBean resultBean) {

        }

        @Override
        public void openLockCallBack(ResultBean resultBean) {
            Log.d("蓝牙连接测试", "openLockCallBack");
            showToast(resultBean.isRet() ? "开门成功" : "开门失败");
            TextToSpeechUtils.getInstance(MainActivity.this).speek(resultBean.isRet() ? "开门成功" : "开门失败");
            isConnect = false;
            bleBase.disConnect();
        }

        @Override
        public void setTaskCallBack(ResultBean resultBean) {

        }

        @Override
        public void initKeyCallBack(ResultBean<String> resultBean) {

        }

        @Override
        public void initLockCodeCallBack(ResultBean<String> resultBean) {

        }

        @Override
        public void readLogCallBack(ResultBean<List<LogEntity>> resultBean) {

        }

        @Override
        public void removeLogCallBack(ResultBean resultBean) {

        }
    };
}
