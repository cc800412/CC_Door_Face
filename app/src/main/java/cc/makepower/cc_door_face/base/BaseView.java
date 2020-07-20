package cc.makepower.cc_door_face.base;

/**
 * @author: ATEX(YGQ)
 * @description:这里添加描述
 * @projectName: MakepeoDo
 * @date: 2016-10-11
 * @time: 11:32
 */
public interface BaseView {

    void showToast(int message);
    void showToast(String message);

    void showSnakeBar(int msg);
    void showSnakeBar(String msg);

    void showProgress(int message);
    void showProgress(String message);
    void hideProgress();



}
