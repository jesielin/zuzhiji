package com.zzj.zuzhiji.network;

import com.google.gson.Gson;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.download.DownloadProgressInterceptor;
import com.zzj.zuzhiji.network.download.DownloadProgressListener;
import com.zzj.zuzhiji.network.entity.AdvertResult;
import com.zzj.zuzhiji.network.entity.LoginResult;
import com.zzj.zuzhiji.network.entity.MessageResult;
import com.zzj.zuzhiji.network.entity.NewsTotal;
import com.zzj.zuzhiji.network.entity.Notice;
import com.zzj.zuzhiji.network.entity.PItem;
import com.zzj.zuzhiji.network.entity.PaidTotal;
import com.zzj.zuzhiji.network.entity.PayResult;
import com.zzj.zuzhiji.network.entity.RecommendBean;
import com.zzj.zuzhiji.network.entity.RegisterResult;
import com.zzj.zuzhiji.network.entity.RegisterStudioResult;
import com.zzj.zuzhiji.network.entity.ReplyItem;
import com.zzj.zuzhiji.network.entity.ServiceItem;
import com.zzj.zuzhiji.network.entity.SetInfoResult;
import com.zzj.zuzhiji.network.entity.SocialTotal;
import com.zzj.zuzhiji.network.entity.StudioItem;
import com.zzj.zuzhiji.network.entity.Tech;
import com.zzj.zuzhiji.network.entity.UpdateInfo;
import com.zzj.zuzhiji.network.entity.UserInfoResult;
import com.zzj.zuzhiji.util.DebugLog;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by shawn on 2017-02-07.
 */

public class Network {





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
        httpClientBuilder.writeTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        httpClientBuilder.readTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS);





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

    public void downloadApk(String url, final File file, DownloadProgressListener listener,Subscriber subscriber){
        DownloadProgressInterceptor interceptor = new DownloadProgressInterceptor(listener);
        OkHttpClient downloadHttpClientBulder = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(Constant.DEFAULT_TIMEOUT,TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        Retrofit downloadRetrofit = new Retrofit.Builder()
                .client(downloadHttpClientBulder)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(Constant.BASE_URL_DOWNLOAD)
                .build();
        downloadRetrofit.create(HttpService.class)
                .download(url)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(new Func1<ResponseBody, InputStream>() {
                    @Override
                    public InputStream call(ResponseBody responseBody) {
                        return responseBody.byteStream();
                    }
                })
                .observeOn(Schedulers.computation())
                .doOnNext(new Action1<InputStream>() {
                    @Override
                    public void call(InputStream inputStream) {
//                        try {
//                            FileUtils.writeFile(inputStream, file);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            throw new ApiException(e.getMessage());
//                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public Observable<RegisterStudioResult> registerStudio(
            RequestBody loginName,
            RequestBody identifyCode,
            RequestBody title,
            RequestBody summary,
            RequestBody serial,
            RequestBody province,
            RequestBody city,
            RequestBody address,
            RequestBody bankcardno,
            MultipartBody.Part headSculpture,
            MultipartBody.Part license
    ) {
        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), sign);
        return compose(normalHttpService.registerStudio(
                loginName,
                identifyCode,
                title,
                summary,
                serial,
                province,
                city,
                address,
                bankcardno,
                headSculpture,
                license,
                description
        ));
    }

    public Observable<UpdateInfo> update(){
        return compose(normalHttpService.update(sign));
    }

    public Observable<List<PItem>> getArea() {
        return compose(normalHttpService.getArea(sign));
    }

    public Observable<NewsTotal> getNews(String type, int page) {
        return compose(normalHttpService.getNews(type, page, sign));
    }

    public Observable<Object> del(String id, String momentsId) {
        return compose(normalHttpService.del(id, momentsId, sign));
    }

    public Observable<List<Notice>> getNotice(String date) {
        return compose(normalHttpService.getNotice(date, sign));
    }

    public Observable<Object> addFriend(String ownerUUID, String friendUUID) {
        return compose(normalHttpService.addFriend(ownerUUID, friendUUID, sign));
    }

    public Observable<RegisterResult> register(String loginName, String verifyCode, String type, String nickName, String bankcardno) {
        return compose(normalHttpService.register(loginName, verifyCode, type, nickName, bankcardno, sign));
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

    public Observable<List<RecommendBean>> getRecommendTech(int size, String userType) {
        return compose(normalHttpService.getRecommendTechs(size, userType, sign));
    }

    public Observable<PaidTotal> getPaidList(String uuid, int page) {
        return compose(normalHttpService.getPaidList(uuid, page, sign));
    }

    public Observable<List<StudioItem>> getStudioByUser(String uuid) {
        return compose(normalHttpService.getStudioByUser(uuid, sign));
    }

    public Observable<String> isFriend(String userUuid, String techUuid) {
        return compose(normalHttpService.isFriend(userUuid, techUuid, sign));
    }

    public Observable<PayResult> pay(String uuid, String serviceId, String techId, String studioId, String type, String time) {
        return compose(normalHttpService.pay(uuid, serviceId, techId, studioId, type, time));
    }

    public Observable<SetInfoResult> setUserInfo(RequestBody uuid, RequestBody nickName, RequestBody sex, RequestBody summary, MultipartBody.Part avator) {

        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), sign);
//        if (studio == null) {
//            DebugLog.e("without studio");
        return compose(normalHttpService.setUserinfo(uuid, nickName, sex, summary, avator, description));
//        }
//        else {
//            DebugLog.e("with studio");
//            return compose(normalHttpService.setUserinfoWithStudio(uuid, nickName, sex, studio, avator, description));
//        }
    }

    public Observable<Object> joinStudio(String uuid, String studioId) {
        return compose(normalHttpService.joinStudio(uuid, studioId, sign));
    }

    public Observable<List<StudioItem>> getUnjoinStudioByUser(String uuid) {
        return compose(normalHttpService.getUnjoinStudioByUser(uuid, sign));
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
