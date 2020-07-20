package cc.makepower.cc_door_face.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import cc.makepower.cc_door_face.R;

public class CCDoorFaceBleOpenWaitDialog extends Dialog {
    TextView ble_State;
    ImageView img_UserFaceImg;

    public CCDoorFaceBleOpenWaitDialog(@NonNull Context context) {
        super(context);
        //获取当前布局的Window
        Window window = getWindow();
        //设置无标题栏
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_bleopen);
        ble_State=findViewById(R.id.ble_State);
        dialogTitleLineColor(this);

        setCanceledOnTouchOutside(true);

    }

   public void setState(String state){
        ble_State.setText(state);
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
