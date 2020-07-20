package cc.makepower.cc_door_face.retrofit.cookies;

import android.content.Context;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Cookies管理器
 * 持久化
 * Created by makePower
 */
public class CookiesManager implements CookieJar {
    private final PersistentCookieStore cookieStore;


    public CookiesManager(Context context) {
        this.cookieStore = new PersistentCookieStore(context);
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookies != null && cookies.size() > 0) {
            for (Cookie item : cookies) {
                cookieStore.add(url, item);
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url);
        return cookies;
    }

    public void removeCookiesAll() {
        cookieStore.removeAll();
    }
}