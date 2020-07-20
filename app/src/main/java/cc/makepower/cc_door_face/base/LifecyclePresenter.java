package cc.makepower.cc_door_face.base;

import androidx.annotation.NonNull;

public interface LifecyclePresenter<T> {

    /**
     * BasePresenter生命周期的开始
     *
     * @param v
     */
    void attachView(@NonNull T v);

    /**
     * BasePresenter生命周期的结束
     */
    void detachView();
}