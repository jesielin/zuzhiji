package com.zzj.zuzhiji.network;

import com.google.gson.Gson;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.entity.AdvertResult;
import com.zzj.zuzhiji.network.entity.LoginResult;
import com.zzj.zuzhiji.network.entity.MessageResult;
import com.zzj.zuzhiji.network.entity.NewsResult;
import com.zzj.zuzhiji.network.entity.RegisterResult;
import com.zzj.zuzhiji.network.entity.ReplyItem;
import com.zzj.zuzhiji.network.entity.ServiceItem;
import com.zzj.zuzhiji.network.entity.SetInfoResult;
import com.zzj.zuzhiji.network.entity.SocialTotal;
import com.zzj.zuzhiji.network.entity.StudioItem;
import com.zzj.zuzhiji.network.entity.Tech;
import com.zzj.zuzhiji.network.entity.UserInfoResult;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by shawn on 2017-02-07.
 */

public class Network {


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

    //获取单例
    public static Network getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Observable<List<NewsResult>> getNews(String type) {
        return compose(normalHttpService.getNews(type, sign));
    }

    public Observable<Object> addFriend(String ownerUUID, String friendUUID) {
        return compose(normalHttpService.addFriend(ownerUUID, friendUUID, sign));
    }

    public Observable<RegisterResult> register(String loginName, String verifyCode, String type, String nickName) {
        return compose(normalHttpService.register(loginName, verifyCode, type, nickName, sign));
    }

    public Observable<LoginResult> login(String loginName, String identifyCode) {
        return compose(normalHttpService.login(loginName, identifyCode, sign));
    }

    public Observable<SocialTotal> getSocialItems(String userUUID, int page, int rows) {
        return compose(normalHttpService.getSocialItems(userUUID, page, rows, sign));
    }

    public Observable<Object> postSocial(RequestBody uuid, RequestBody message, RequestBody ownerNickName, MultipartBody.Part[] part) {
        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), sign);
        return compose(normalHttpService.sendMoment(uuid, message, part, ownerNickName, description));
    }

    public Observable<Object> sendComment(String momentsID, String ownerUUID, String commenterUUID, String friendUUID, String message
            , String commenterNickName, String friendNickName) {

        return compose(normalHttpService.sendComment(momentsID, ownerUUID, commenterUUID, friendUUID, message, commenterNickName, friendNickName, sign));
    }

    public Observable<List<Tech>> searchTech(int currentPage, int size, String title, String owner) {
        return compose(normalHttpService.searchTechs(currentPage, size, title, owner, sign));
    }

    public Observable<Object> delFriend(String ownerUUID, String friendUUID) {
        return compose(normalHttpService.delFriend(ownerUUID, friendUUID, sign));
    }

    public Observable<List<StudioItem>> getAllStudio() {
        return compose(normalHttpService.getAllStudio(sign));
    }

    public Observable<SocialTotal> getUserSocialItems(String uuid, int page, int rows) {
        return compose(normalHttpService.getUserMoment(uuid, page, rows, sign));
    }

    public Observable<List<Tech>> getRecommendTech(int size) {
        return compose(normalHttpService.getRecommendTechs(size, sign));
    }

    public Observable<SetInfoResult> setUserInfo(RequestBody uuid, RequestBody nickName, RequestBody sex, RequestBody studio, MultipartBody.Part avator) {

        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), sign);
//        if (studio == null) {
//            DebugLog.e("without studio");
            return compose(normalHttpService.setUserinfo(uuid, nickName, sex, avator, description));
//        }
//        else {
//            DebugLog.e("with studio");
//            return compose(normalHttpService.setUserinfoWithStudio(uuid, nickName, sex, studio, avator, description));
//        }
    }

    public Observable<Object> sendSms(String mobile) {
        return compose(smsHttpService.sendSms(mobile, sign));
    }

    public Observable<List<MessageResult>> getMyFriendship(String uuid) {
        return compose(normalHttpService.getMyFriendship(uuid, sign));
    }

    public Observable<List<AdvertResult>> getAdvert(String position) {
        return compose(normalHttpService.getIndexAdvert(position, sign));
    }

    public Observable<UserInfoResult> getUserInfo(String uuid) {
        return compose(normalHttpService.getUserInfo(uuid, sign));
    }

    public Observable<List<List<ReplyItem>>> queryMyReplyComments(String uuid) {
        return compose(normalHttpService.queryMyReplyComments(uuid, sign));
    }

    public Observable<List<ServiceItem>> getService(String uuid) {
        return compose(normalHttpService.getService(uuid, sign));
    }

    public Observable<Object> reserv(String uuid, String techId, String startDate, String service) {
        return compose(normalHttpService.reserv(uuid, techId, startDate, service, sign));
    }

    private <T> Observable<T> compose(Observable<HttpResult<T>> o) throws ApiException {
        return o.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new HttpResultFunc<T>())
                .observeOn(AndroidSchedulers.mainThread())
                ;
    }

    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final Network INSTANCE = new Network();
    }

    /**
     * 用来统一处理Http的resultCode,并将HttpResult的Data部分剥离出来返回给subscriber
     *
     * @param <T> Subscriber真正需要的数据类型，也就是Data部分的数据类型
     */
    private class HttpResultFunc<T> implements Func1<HttpResult<T>, T> {

        @Override
        public T call(HttpResult<T> httpResult) {
            DebugLog.e("result:" + new Gson().toJson(httpResult));
            if (httpResult == null || "error".equals(httpResult.result))
                throw new ApiException(httpResult == null ? "网络错误!" : httpResult.msg);
            return httpResult.data;
        }
    }


}
