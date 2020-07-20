package cc.makepower.cc_door_face.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;

import java.util.Locale;

public class TextToSpeechUtils {
    static TextToSpeechUtils textToSpeechUtils;
    public static TextToSpeechUtils getInstance(Context context) {
        if (textToSpeechUtils==null){
            textToSpeechUtils=new TextToSpeechUtils(context);
        }
        return textToSpeechUtils;
    }
    TextToSpeech textToSpeech;
    private TextToSpeechUtils(Context context) {
        speakChina(context);
    }

    private void speakChina(Context context){
        //设置朗读语言
        textToSpeech = new TextToSpeech(context.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status== TextToSpeech.SUCCESS) {
                    //设置朗读语言
                    int supported= textToSpeech.setLanguage(Locale.US);
                }
            }
        });
    }
    private  long lastSpeedTime;//禁止频繁调用
    public void speek(String content){
        if (textToSpeech!=null&& !TextUtils.isEmpty(content)&& System.currentTimeMillis()-lastSpeedTime>3000) {
            lastSpeedTime= System.currentTimeMillis();
            textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    public void shutdown(){
        if (textToSpeech!=null) {
            textToSpeech.stop();
            textToSpeech.shutdown();

        }
    }
}
