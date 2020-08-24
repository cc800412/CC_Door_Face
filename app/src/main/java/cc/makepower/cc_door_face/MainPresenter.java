package cc.makepower.cc_door_face;

import android.content.Context;
import android.text.TextUtils;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cc.makepower.cc_door_face.base.APresenter;
import cc.makepower.cc_door_face.bean.BindDeviceResult;
import cc.makepower.cc_door_face.bean.CompareResult;
import cc.makepower.cc_door_face.bean.FacePreviewInfo;
import cc.makepower.cc_door_face.bean.PostResultBean;
import cc.makepower.cc_door_face.bean.RequestFeatureStatus;
import cc.makepower.cc_door_face.bean.ResultBean;
import cc.makepower.cc_door_face.bean.UserFacePermission;
import cc.makepower.cc_door_face.camera.FaceHelper;
import cc.makepower.cc_door_face.camera.FaceServer;
import cc.makepower.cc_door_face.utils.DeviceUtils;
import cc.makepower.cc_door_face.utils.TextToSpeechUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

class MainPresenter extends APresenter<MainContract.View> implements MainContract {
    public static final String APP_ID = "BRSbMCi9Y1VX1TT9G2vV5vijWDn22pR4Ttzaik62umhz";
    //    public static final String APP_ID = "BRSbMCi9Y1VX1TT9G2vV5viN21zWj2nRD72NgDvGJ5rb";
    public static final String SDK_KEY = "5BAJFgZWsvDVh5ATMkox4FMqH2ZTNXxQVBjgGhxjtfaJ";
    private static final float SIMILAR_THRESHOLD = 0.8F;

    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();
    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();

    private List<CompareResult> compareResultList = new ArrayList<>();

    public ConcurrentHashMap<Integer, Integer> getRequestFeatureStatusMap() {
        return requestFeatureStatusMap;
    }

    public ConcurrentHashMap<Integer, Integer> getLivenessMap() {
        return livenessMap;
    }

    public CompositeDisposable getGetFeatureDelayedDisposables() {
        return getFeatureDelayedDisposables;
    }

    public MainPresenter(Context context) {
        super(context);
    }


    public void initFaceSdk() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                FaceEngine faceEngine = new FaceEngine();
                int activeCode = faceEngine.active(context, APP_ID, SDK_KEY);
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK || activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            mView.initFaceSdkCallBack(true);
                        } else {
                            mView.showToast("人脸引擎激活失败");
                            mView.initFaceSdkCallBack(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showToast(showError(e));
                        mView.initFaceSdkCallBack(false);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList 人脸和trackId列表
     */
    public void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        Set<Integer> keySet = requestFeatureStatusMap.keySet();
        if (compareResultList != null) {
            for (int i = compareResultList.size() - 1; i >= 0; i--) {
                if (!keySet.contains(compareResultList.get(i).getTrackId())) {
                    compareResultList.remove(i);
                }
            }
        }
        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            requestFeatureStatusMap.clear();
            livenessMap.clear();
            return;
        }

        for (Integer integer : keySet) {
            boolean contained = false;
            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                if (facePreviewInfo.getTrackId() == integer) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                requestFeatureStatusMap.remove(integer);
                livenessMap.remove(integer);
            }
        }

    }


    /**
     * @param faceHelper
     * @param frFace
     * @param requestId
     * @param deviceId
     */
    public void searchFace(final FaceHelper faceHelper, final FaceFeature frFace, final Integer requestId, final String deviceId) {
        Observable
                .create(new ObservableOnSubscribe<CompareResult>() {
                    @Override
                    public void subscribe(ObservableEmitter<CompareResult> emitter) {
                        CompareResult compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace);
                        if (compareResult == null) {
                            emitter.onError(null);
                        } else {
                            emitter.onNext(compareResult);
                        }
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CompareResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CompareResult compareResult) {
                        if (compareResult == null || compareResult.getUserName() == null) {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            faceHelper.addName(requestId, "");
                            return;
                        }
//                        Log.d("相似度", compareResult.getSimilar() + "");
                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                            boolean isAdded = false;
                            if (compareResultList == null) {
                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                                faceHelper.addName(requestId, "");
                                return;
                            }
                            for (CompareResult compareResult1 : compareResultList) {
                                if (compareResult1.getTrackId() == requestId) {
                                    isAdded = true;
                                    break;
                                }
                            }
                            if (!isAdded) {
                                //对于多人脸搜索，假如最大显示数量为 MAX_DETECT_NUM 且有新的人脸进入，则以队列的形式移除
                                if (compareResultList.size() >= 1) {
                                    compareResultList.remove(0);
                                }
                                //添加显示人员时，保存其trackId
                                compareResult.setTrackId(requestId);
                                compareResultList.add(compareResult);
                                //用户名称
                                String userInfos[] = compareResult.getUserName().split("_");
                                faceHelper.addName(requestId, userInfos[0]);
                                if (userInfos.length > 2 && compareResult.getUserName().contains("lock")) {
                                    //账号被冻结
                                    mView.showToast("账号被冻结开门失败");
                                    TextToSpeechUtils.getInstance(context).speek("账号被冻结开门失败");
                                } else {
                                    //远程开门

//                                    TextToSpeechUtils.getInstance(context).speek("开门成功");
                                    mView.searchFaceFindUserCallBack(true, compareResult.getSimilar(), compareResult.getUserName());
                                    buildRemotedoor(userInfos[1], "", deviceId, requestId,compareResult.getUserName());
//                                    mView.buildRemotedoorCalBack(false);
                                }
                            }
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
                        } else {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            faceHelper.addName(requestId, "");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void fetchFaceList(int pageIndex, String deviceId){
        unSubscribe();
        restDataSource.fetchFaceList(pageIndex,deviceId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        setSubscription(d);
                    }

                    @Override
                    public void onNext(List<String> strings) {
                        if (strings != null) {
                            facePathList.clear();
                            facePathList.addAll(strings);

                            //清空本地人脸库
                            FaceServer.getInstance().deleteFile(new File(FaceServer.ROOT_PATH+ File.separator+FaceServer.SAVE_FEATURE_DIR));
                            FaceServer.getInstance().deleteFile(new File(FaceServer.ROOT_PATH+ File.separator+FaceServer.SAVE_IMG_DIR));
                            if (facePathList.size()>0){
                                downLoadPermission(facePathList.get(0), false);//这里featureUrl 后台写反了imageUrl
                            }
                            mView.fetchFaceListCallBack(true, strings);
                        } else {
                            mView.fetchFaceListCallBack(false, null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.fetchFaceListCallBack(false, null);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    List<String> facePathList = new ArrayList<>();


    /**
     * 下载相关   下载文件
     *
     * @param url   下载的url
     * @param isImg 图片放在图片文件夹
     */
    public void downLoadPermission(final String url, final boolean isImg) {
        restDataSource.downLoadFile(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String[] urls = url.split("/");
                        writeResponseBodyToDisk(responseBody, FaceServer.getInstance().ROOT_PATH
                                + File.separator
                                + (isImg ? FaceServer.getInstance().SAVE_IMG_DIR :
                                FaceServer.getInstance().SAVE_FEATURE_DIR)
                                + File.separator
                                + urls[urls.length - 1]);
                        if (isImg) {
                            facePathList.remove(0);
                            mView.downLoadPermissionCallBack(true, facePathList.size());
                            if (facePathList.size() != 0) {
                                downLoadPermission(facePathList.get(0), false);
                            }
                        } else {
                            downLoadPermission(facePathList.get(0), true);
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        facePathList.remove(0);
                        if (facePathList.size() != 0) {
                            downLoadPermission(facePathList.get(0), false);
                        }
                        mView.downLoadPermissionCallBack(false, facePathList.size());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }


    private boolean writeResponseBodyToDisk(ResponseBody body, String path) {
        try {
            File futureStudioIconFile = new File(path);
            if (!futureStudioIconFile.getParentFile().exists()) {
                futureStudioIconFile.getParentFile().mkdirs();
            }

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);
                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                }
                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    public void pushOpenLog(String deviceId, String userTag){
        unSubscribe();
        restDataSource.pushOpenLog(deviceId,userTag)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        setSubscription(d);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        mView.pushOpenLogCallBack(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.pushOpenLogCallBack(false);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    private long sendOpenRequestTime;//上一次发起开门请求的时间
    /**
     * 下发远程开门
     *
     * @param userId
     * @param stationCode
     * @param deviceID
     * @param requestId
     */
    public void buildRemotedoor(final String userId, final String stationCode, final String deviceID, final Integer requestId,String userTag) {
        if ((System.currentTimeMillis() - sendOpenRequestTime) > 5000) {
            sendOpenRequestTime = System.currentTimeMillis();
            unSubscribe();
            retDataSource.buildRemotedoor(stationCode, deviceID)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ResultBean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            subscription = d;
                        }

                        @Override
                        public void onNext(ResultBean resultBean) {
                            if (resultBean.getCode() == ResultBean.Code.SUCCESS) {
                                mView.showToast("开门成功");
                                TextToSpeechUtils.getInstance(context).speek("开门成功");
                                pushOpenLog(DeviceUtils.getUniqueId(context),userTag);
                            } else {
                                mView.buildRemotedoorCalBack(false);
                                mView.showToast("开门失败");
                                try {
                                    TextToSpeechUtils.getInstance(context).speek(TextUtils.isEmpty(resultBean.getMessage()) ? "开门失败" : resultBean.getMessage());
                                } catch (Exception e) {
                                    mView.showToast(showError(e));
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            mView.buildRemotedoorCalBack(false);
                            mView.showToast(showError(e));
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }


    }
}