package cc.makepower.cc_door_face.retrofit.download;

import android.app.Activity;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * author : atex
 * e-mail : xxx@xx
 * time   : 2018/04/13
 * desc   :
 * version: 1.0
 */

public class Cl_ResponseBody extends ResponseBody {

    private CL_DownLoadListener cl_downLoadListener;
    private ResponseBody responseBody;
    private Activity context;

    public Cl_ResponseBody(Activity activity, CL_DownLoadListener cl_downLoadListener, ResponseBody responseBody) {
        this.context=activity;
        this.cl_downLoadListener = cl_downLoadListener;
        this.responseBody = responseBody;
    }

    private BufferedSource bufferedSource;

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource==null){
            bufferedSource= Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    public Source source(Source source){

        return new ForwardingSource(source) {
            long totalBytesRead=0L;
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead=super.read(sink,byteCount);

                totalBytesRead+=bytesRead!=-1?bytesRead:0;
                if (null!=cl_downLoadListener){
                    if (totalBytesRead!=0&&context!=null){
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cl_downLoadListener.onChange((int) (totalBytesRead * 100 / responseBody.contentLength()));
                            }
                        });
                    }
                }
                return bytesRead;
            }
        };
    }
}
