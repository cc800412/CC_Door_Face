package cc.makepower.cc_door_face.base;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * author : atex
 * e-mail : xxx@xx
 * time   : 2018/09/08
 * desc   :
 * version: 1.0
 */

public abstract class BaseActivity extends AppCompatActivity implements BaseView {


    APresenter aPresenter;

    protected abstract int getLayout();//返回layout布局

    protected abstract APresenter injectPresenter();

    protected abstract void afterOnCreateView(Bundle savedInstanceState);//setContentView执行完之后


    private Unbinder unbinder;

    //onCreate和setContentView之间执行
    protected void beforeOnCreate(Bundle savedInstanceState) {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBar();
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//保持竖屏
        beforeOnCreate(savedInstanceState);
        setContentView(getLayout());
        EventBus.getDefault().register(this);

        unbinder = ButterKnife.bind(this);
        aPresenter = injectPresenter();
        if (aPresenter != null) {
            aPresenter.attachView(this);
        }
        afterOnCreateView(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (unbinder != null) {
            unbinder.unbind();
        }
        if (aPresenter != null) {
            aPresenter.unSubscribe();
        }
        super.onDestroy();
    }


    @Override
    public void showToast(String message) {
        if (!TextUtils.isEmpty(message)) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showToast(int message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSnakeBar(int msg) {

    }

    @Override
    public void showSnakeBar(String msg) {

    }

    private void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0及以上
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4到5.0
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        //修改字体颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//android6.0以后可以对状态栏文字颜色和图标进行修改
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)//非默认值
            getResources();
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public void showProgress(int msg) {

    }

    /**
     * @param ftRect                   FT人脸框
     * @param previewWidth             相机预览的宽度
     * @param previewHeight            相机预览高度
     * @param canvasWidth              画布的宽度
     * @param canvasHeight             画布的高度
     * @param cameraDisplayOrientation 相机预览方向
     * @param cameraId                 相机ID
     * @return 调整后的需要被绘制到View上的rect
     */
    public Rect adjustRect(Rect ftRect, int previewWidth, int previewHeight, int canvasWidth, int canvasHeight, int cameraDisplayOrientation, int cameraId) {
        if (ftRect == null) {
            return null;
        }
        Rect rect = new Rect(ftRect);
        if (canvasWidth < canvasHeight) {
            int t = previewHeight;
            previewHeight = previewWidth;
            previewWidth = t;
        }
        float horizontalRatio;
        float verticalRatio;
        if (cameraDisplayOrientation == 0 || cameraDisplayOrientation == 180) {
            horizontalRatio = (float) canvasWidth / (float) previewWidth;
            verticalRatio = (float) canvasHeight / (float) previewHeight;
        } else {
            horizontalRatio = (float) canvasHeight / (float) previewHeight;
            verticalRatio = (float) canvasWidth / (float) previewWidth;
        }
        rect.left *= horizontalRatio;
        rect.right *= horizontalRatio;
        rect.top *= verticalRatio;
        rect.bottom *= verticalRatio;
        Rect newRect = new Rect();
        switch (cameraDisplayOrientation) {
            case 0:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.left = canvasWidth - rect.right;
                    newRect.right = canvasWidth - rect.left;
                } else {
                    newRect.left = rect.left;
                    newRect.right = rect.right;
                }
                newRect.top = rect.top;
                newRect.bottom = rect.bottom;
                break;
            case 90:
                newRect.right = canvasWidth - rect.top;
                newRect.left = canvasWidth - rect.bottom;
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.top = canvasHeight - rect.right;
                    newRect.bottom = canvasHeight - rect.left;
                } else {
                    newRect.top = rect.left;
                    newRect.bottom = rect.right;
                }
                break;
            case 180:
                newRect.top = canvasHeight - rect.bottom;
                newRect.bottom = canvasHeight - rect.top;
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.left = rect.left;
                    newRect.right = rect.right;
                } else {
                    newRect.left = canvasWidth - rect.right;
                    newRect.right = canvasWidth - rect.left;
                }
                break;
            case 270:
                newRect.left = rect.top;
                newRect.right = rect.bottom;
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.top = rect.left;
                    newRect.bottom = rect.right;
                } else {
                    newRect.top = canvasHeight - rect.right;
                    newRect.bottom = canvasHeight - rect.left;
                }
                break;
            default:
                break;
        }
        return newRect;
    }
//    /**
//     * @param ftRect                   FT人脸框
//     * @param previewWidth             相机预览的宽度
//     * @param previewHeight            相机预览高度
//     * @param canvasWidth              画布的宽度
//     * @param canvasHeight             画布的高度
//     * @param cameraDisplayOrientation 相机预览方向
//     * @param cameraId                 相机ID
//     * @return 调整后的需要被绘制到View上的rect
//     */
//    public Rect adjustRect(Rect ftRect, int previewWidth, int previewHeight, int canvasWidth, int canvasHeight, int cameraDisplayOrientation, int cameraId) {
//        if (ftRect == null) {
//            return null;
//        }
//        Rect rect = new Rect(ftRect);
//        if (canvasWidth < canvasHeight) {
//            int t = previewHeight;
//            previewHeight = previewWidth;
//            previewWidth = t;
//        }
//        float horizontalRatio;
//        float verticalRatio;
//        if (cameraDisplayOrientation == 0 || cameraDisplayOrientation == 180) {
//            horizontalRatio = (float) canvasWidth / (float) previewWidth;
//            verticalRatio = (float) canvasHeight / (float) previewHeight;
//        } else {
//            horizontalRatio = (float) canvasHeight / (float) previewHeight;
//            verticalRatio = (float) canvasWidth / (float) previewWidth;
//        }
//        rect.left *= horizontalRatio;
//        rect.right *= horizontalRatio;
//        rect.top *= verticalRatio;
//        rect.bottom *= verticalRatio;
//        Rect newRect = new Rect();
//        switch (cameraDisplayOrientation) {
//            case 0:
//                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                    newRect.left = canvasWidth - rect.right;
//                    newRect.right = canvasWidth - rect.left;
//                } else {
//                    newRect.left = rect.left;
//                    newRect.right = rect.right;
//                }
//                newRect.top = rect.top;
//                newRect.bottom = rect.bottom;
//                break;
//            case 90:
//                newRect.right = canvasWidth - rect.top;
//                newRect.left = canvasWidth - rect.bottom;
//                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                    newRect.top = canvasHeight - rect.right;
//                    newRect.bottom = canvasHeight - rect.left;
//                } else {
//                    newRect.top = rect.left;
//                    newRect.bottom = rect.right;
//                }
//                break;
//            case 180:
//                newRect.top = canvasHeight - rect.bottom;
//                newRect.bottom = canvasHeight - rect.top;
//                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                    newRect.left = rect.left;
//                    newRect.right = rect.right;
//                } else {
//                    newRect.left = canvasWidth - rect.right;
//                    newRect.right = canvasWidth - rect.left;
//                }
//                break;
//            case 270:
//                newRect.left = rect.top;
//                newRect.right = rect.bottom;
//                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                    newRect.top = rect.left;
//                    newRect.bottom = rect.right;
//                } else {
//                    newRect.top = canvasHeight - rect.right;
//                    newRect.bottom = canvasHeight - rect.left;
//                }
//                break;
//            default:
//                break;
//        }
//        return newRect;
//    }

    @Override
    public void showProgress(String msg) {

    }
    @Override
    public void hideProgress() {

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventBusJpush(Bundle bundle) {

    }

}
