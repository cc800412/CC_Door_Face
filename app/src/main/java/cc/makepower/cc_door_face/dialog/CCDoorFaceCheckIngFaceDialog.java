package cc.makepower.cc_door_face.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

import androidx.annotation.NonNull;
import cc.makepower.cc_door_face.R;

public class CCDoorFaceCheckIngFaceDialog extends Dialog {
    TextView tv_Similar;
    ImageView img_UserFaceImg;

    public CCDoorFaceCheckIngFaceDialog(@NonNull Context context) {
        super(context);
        //获取当前布局的Window
        Window window = getWindow();
        //设置无标题栏
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_wait);
        img_UserFaceImg=findViewById(R.id.img_UserFaceImg);
        tv_Similar=findViewById(R.id.tv_Similar);
        dialogTitleLineColor(this);

        setCanceledOnTouchOutside(true);

    }
    public CCDoorFaceCheckIngFaceDialog setSimilar(float similar){
        tv_Similar.setText("相似度:"+(int)(similar*100)+"%");
        return this;
    }
public CCDoorFaceCheckIngFaceDialog setUserFaceImg(String url){

    Glide.with(getContext())
            .load(url)
            .error(R.mipmap.ic_launcher)
            .placeholder(R.mipmap.ic_launcher)
            .centerCrop()
            .dontAnimate()
            .into(img_UserFaceImg);
        return this;
    }public CCDoorFaceCheckIngFaceDialog setUserFaceImg(File file){
        Glide.with(getContext())
                .load(file)
                .error(R.mipmap.ic_launcher)
                .centerCrop()
                .dontAnimate()
                .into(img_UserFaceImg);
        return this;
    }


    /**
     * 去掉dialog顶部蓝颜色线条
     *
     * @param dialog
     */
    private void dialogTitleLineColor(Dialog dialog) {
        try {
            Context context = dialog.getContext();

            int dividerID = context.getResources().getIdentifier("android:id/titleDivider", null, null);
            View divider = findViewById(dividerID);
            divider.setBackgroundColor(Color.TRANSPARENT);
        } catch (Exception e) {
        }
    }
}
