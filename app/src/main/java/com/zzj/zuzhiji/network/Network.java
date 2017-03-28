package com.zzj.zuzhiji.network;

import android.widget.Toast;

import com.google.gson.Gson;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.entity.LoginResult;
import com.zzj.zuzhiji.network.entity.MessageResult;
import com.zzj.zuzhiji.network.entity.NewsResult;
import com.zzj.zuzhiji.network.entity.RegisterResult;
import com.zzj.zuzhiji.network.entity.SetInfoResult;
import com.zzj.zuzhiji.network.entity.SocialTotal;
import com.zzj.zuzhiji.network.entity.Tech;
import com.zzj.zuzhiji.util.DebugLog;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by shawn on 2017-02-07.
 */

public class Network {


    private Retrofit normalRetrofit;
    private Retrofit smsRetrofit;
    private HttpService normalHttpService;
    private HttpService smsHttpService;

    //构造方法私有
    private Network() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        //手动创建一个OkHttpClient并设置超时时间
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(logging);
        httpClientBuilder.connectTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        normalRetrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(Constant.BASE_URL_NOR)
                .build();

        normalHttpService = normalRetrofit.create(HttpService.class);

        smsRetrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(Constant.BASE_URL_SMS)
                .build();

        smsHttpService = smsRetrofit.create(HttpService.class);
    }

    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final Network INSTANCE = new Network();
    }

    //获取单例
    public static Network getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * 用来统一处理Http的resultCode,并将HttpResult的Data部分剥离出来返回给subscriber
     *
     * @param <T> Subscriber真正需要的数据类型，也就是Data部分的数据类型
     */
//    private class HttpResultFunc<T> implements Func1<HttpResult<T>, Observable<T>> {
//        @Override
//        public Observable<T> call(final HttpResult<T> tHttpResult) {
//            return Observable.create(new Observable.OnSubscribe<T>() {
//                @Override
//                public void call(Subscriber<? super T> subscriber) {
//                    DebugLog.e("result:"+new Gson().toJson(tHttpResult));
//                    if (tHttpResult != null) {
//                        if ("error".equals(tHttpResult.result)) {
//                            subscriber.onError(new NetworkException(tHttpResult.msg));
//                            DebugLog.e("error1");
//                        }
//
////                        else if (tHttpResult.data == null) {
////                            subscriber.onError(new NetworkException("子数据为空"));
////                        }
//                        else {
//                            DebugLog.e("next1");
//                            subscriber.onNext(tHttpResult.data);
//                        }
//                    } else {
//                        DebugLog.e("error2");
//                        subscriber.onError(new NetworkException("总数据为空"));
//                    }
//                    DebugLog.e("complete");
//                    subscriber.onCompleted();
//                }
//            });
//        }
//    }


    private static String sign = "123";


    public Observable<List<NewsResult>> getNews(String type) {
        return compose(normalHttpService.getNews(type, sign));
    }

    public Observable<Object> addFriend(String ownerUUID, String friendUUID) {
        return compose(normalHttpService.addFriend(ownerUUID, friendUUID, sign));
    }

    public Observable<RegisterResult> register(String loginName, String verifyCode, String type) {
        return compose(normalHttpService.register(loginName, verifyCode, type, sign));
    }

    public Observable<LoginResult> login(String loginName, String identifyCode) {
        return compose(normalHttpService.login(loginName, identifyCode, sign));
    }

    public Observable<SocialTotal> getSocialItems(String userUUID, int page, int rows) {
        return compose(normalHttpService.getSocialItems(userUUID, page, rows, sign));
    }

    public Observable<Object> postSocial(RequestBody uuid, RequestBody message, MultipartBody.Part[] part) {
        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), sign);
        return compose(normalHttpService.sendMoment(uuid, message, part, description));
    }

    public Observable<Object> sendComment(String momentsID, String ownerUUID, String commenterUUID, String friendUUID, String message) {

        return compose(normalHttpService.sendComment(momentsID, ownerUUID, commenterUUID, friendUUID, message, sign));
    }


    public Observable<List<Tech>> searchTech(int currentPage, int size, String title) {
        return compose(normalHttpService.searchTechs(currentPage, size, title, sign));
    }


    public Observable<SetInfoResult> setUserInfo(RequestBody uuid, RequestBody nickName, RequestBody sex, MultipartBody.Part avator) {

        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), sign);
        return compose(normalHttpService.setUserinfo(uuid, nickName, sex, avator, description));
    }

    public Observable<Object> sendSms(String mobile) {
        return compose(smsHttpService.sendSms(mobile, sign));
    }

    public Observable<List<MessageResult>> getMyFriendship(String uuid) {
        return compose(normalHttpService.getMyFriendship(uuid, sign));
    }


    private <T> Observable<T> compose(Observable<HttpResult<T>> o) throws ApiException {
        return o.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new HttpResultFunc<T>())
                .observeOn(AndroidSchedulers.mainThread())
                ;
    }

    /**
     * 用来统一处理Http的resultCode,并将HttpResult的Data部分剥离出来返回给subscriber
     *
     * @param <T> Subscriber真正需要的数据类型，也就是Data部分的数据类型
     */
    private class HttpResultFunc<T> implements Func1<HttpResult<T>, T> {

        @Override
        public T call(HttpResult<T> httpResult) {
            if (httpResult == null || "error".equals(httpResult.result))
                throw new ApiException(httpResult == null ? "网络错误!" : httpResult.msg);
            return httpResult.data;
        }
    }


}
