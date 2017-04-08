package com.zzj.zuzhiji.network;

import com.zzj.zuzhiji.network.entity.AdvertResult;
import com.zzj.zuzhiji.network.entity.LoginResult;
import com.zzj.zuzhiji.network.entity.MessageResult;
import com.zzj.zuzhiji.network.entity.NewsResult;
import com.zzj.zuzhiji.network.entity.Notice;
import com.zzj.zuzhiji.network.entity.RegisterResult;
import com.zzj.zuzhiji.network.entity.SetInfoResult;
import com.zzj.zuzhiji.network.entity.SocialTotal;
import com.zzj.zuzhiji.network.entity.StudioItem;
import com.zzj.zuzhiji.network.entity.Tech;
import com.zzj.zuzhiji.network.entity.UserInfoResult;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by shawn on 2017-02-07.
 */

public interface HttpService {



    /**
     * 登录
     *
     * @param loginName
     * @param identifyingCode
     * @param sign
     * @return
     */
    @GET("/login")
    Observable<HttpResult<LoginResult>> login(@Query("loginName") String loginName, @Query("identifyingCode") String identifyingCode, @Query("sign") String sign);

    /**
     * 注册
     *
     * @param loginName
     * @param identifyingCode
     * @param regType         1 技师 0 用户
     * @param sign
     * @return
     */
    @GET("/register")
    Observable<HttpResult<RegisterResult>> register(@Query("loginName") String loginName, @Query("identifyingCode") String identifyingCode, @Query("regType") String regType, @Query("nickname") String nickName, @Query("sign") String sign);

    /**
     * 首页动态
     *
     * @param currentDate 当前日期 2017-02-21
     * @param sign
     * @return
     */

    Observable<HttpResult<List<Notice>>> getNotice(@Query("currentDate") String currentDate, @Query("sign") String sign);

    /**
     * 首页推荐技师
     *
     * @param size 推荐人数控制
     * @param sign
     * @return
     */
    @GET("/getRecommendTechs")
    Observable<HttpResult<List<Tech>>> getRecommendTechs(@Query("size") int size, @Query("sign") String sign);

    /**
     * 首页搜索技师
     *
     * @param currentPage 当前页码 初始1
     * @param size        查询人数控制
     * @param techName    待搜索的技师昵称名称（模糊搜索）
     * @param sign
     * @return
     */
    //searchTechs?currentPage=2&size=1&techName=小&owner=itrqXZ6Q36&sign=123
    @GET("/searchTechs")
    Observable<HttpResult<List<Tech>>> searchTechs(@Query("currentPage") int currentPage, @Query("size") int size, @Query("techName") String techName, @Query("owner") String owner, @Query("sign") String sign);

    /**
     * 更新用户信息
     * uuid 用户唯一标示
     * nickName 用户昵称
     * status 用户状态  0未启用 1启用
     * userType 用户类型 1技师 2普通用户
     * level 用户等级
     * isRecommend 是否推荐技师  1推荐
     * summary 简介
     * headSculpture 头像
     * sex 性别  1男 0女
     *
     * @return
     */
    @Multipart
    @POST("/setUserinfo")
    Observable<HttpResult<SetInfoResult>> setUserinfo(
            @Part("uuid") RequestBody uuid,
            @Part("nickName") RequestBody nickName,
            @Part("sex") RequestBody sex,
            @Part MultipartBody.Part imgs,
            @Part("sign") RequestBody sign

    );

    /**
     * 更新用户信息
     * uuid 用户唯一标示
     * nickName 用户昵称
     * status 用户状态  0未启用 1启用
     * userType 用户类型 1技师 2普通用户
     * level 用户等级
     * isRecommend 是否推荐技师  1推荐
     * summary 简介
     * headSculpture 头像
     * sex 性别  1男 0女
     *
     * @return
     */
    @Multipart
    @POST("/setUserinfo")
    Observable<HttpResult<SetInfoResult>> setUserinfoWithStudio(
            @Part("uuid") RequestBody uuid,
            @Part("nickName") RequestBody nickName,
            @Part("sex") RequestBody sex,
            @Part("studio") RequestBody studio,
            @Part MultipartBody.Part imgs,
            @Part("sign") RequestBody sign

    );


    /**
     * 获取用户信息
     *
     * @param uuid
     * @param sign
     * @return
     */
    @GET("getUserinfo")
    Observable<HttpResult<UserInfoResult>> getUserInfo(@Query("owner") String uuid, @Query("sign") String sign);
    /**
     * 查看朋友圈列表
     *
     * @param userUUID
     * @param page
     * @param rows
     * @param sign
     * @return
     */
    @GET("/getAllMoment")
    Observable<HttpResult<SocialTotal>> getSocialItems(@Query("userUUID") String userUUID, @Query("page") int page, @Query("rows") int rows, @Query("sign") String sign);


    ///getAllStudio?sign=123

    /**
     * 获取工作室列表
     * @param sign
     * @return
     */
    @GET("/getAllStudio")
    Observable<HttpResult<List<StudioItem>>> getAllStudio(@Query("sign") String sign);

    /**
     * 发表朋友圈
     *
     * @param uuid
     * @param message
     * @param parts
     * @param ownerNickname
     * @param sign
     * @return
     */
    @Multipart
    @POST("/sendMoment")
    Observable<HttpResult<Object>> sendMoment(@Part("owner") RequestBody uuid,
                                              @Part("message") RequestBody message,
                                              @Part MultipartBody.Part[] parts,
                                              @Part("ownerNickname") RequestBody ownerNickname,
                                              @Part("sign") RequestBody sign);


//    RequestParam(value = "momentsID", required = true) String momentsID,
//    @RequestParam(value = "ownerUUID", required = true) String ownerUUID,
//    @RequestParam(value = "commenterUUID", required = true) String commenterUUID,
//    @RequestParam(value = "friendUUID", required = false) String friendUUID,
//    @RequestParam(value = "message", required = true) String message,
//    @RequestParam(value = "sign", required = true) String sign,
    /**
     * 发表评论
     *
     * @param momentsID     朋友圈id
     * @param ownerUUID     朋友圈发布者id
     * @param commenterUUID 评论者id
     * @param friendUUID    @评论人id  可为空
     * @param message       评论内容
     * @param sign
     * @return
     */

    /**
     * 发表评论
     *
     * @param momentsID         朋友圈id
     * @param ownerUUID         朋友圈发布者id
     * @param commenterUUID     评论者id
     * @param friendUUID        被评论人id 可为空
     * @param message           评论内容
     * @param commenterNickname 评论者昵称
     * @param friendNickname    被评论人昵称
     * @param sign
     * @return
     */
    @GET("sendComment")
    Observable<HttpResult<Object>> sendComment(@Query("momentsID") String momentsID,
                                               @Query("ownerUUID") String ownerUUID,
                                               @Query("commenterUUID") String commenterUUID,
                                               @Query("friendUUID") String friendUUID,
                                               @Query("message") String message,
                                               @Query("commenterNickname") String commenterNickname,
                                               @Query("friendNickname") String friendNickname,
                                               @Query("sign") String sign);

    ///getUserMoment?userUUID=FF3&page=8&rows=3&sign=123

    /**
     * 查看我的朋友圈
     * @param userUUID
     * @param page
     * @param rows
     * @param sign
     * @return
     */
    @GET("getUserMoment")
    Observable<HttpResult<SocialTotal>> getUserMoment(@Query("userUUID") String userUUID, @Query("page") int page, @Query("rows") int rows, @Query("sign") String sign);

    /**
     * 获取短信验证码
     *
     * @param mobile 手机号
     * @param sign
     * @return
     */
    @GET("sendSms")
    Observable<HttpResult<Object>> sendSms(@Query("mobile") String mobile, @Query("sign") String sign);


    /**
     * 获取好友列表
     *
     * @param uuid
     * @param sign
     * @return
     */
    @GET("/getMyFriendship")
    Observable<HttpResult<List<MessageResult>>> getMyFriendship(@Query("ownerUUID") String uuid, @Query("sign") String sign);


    /**
     * 关注技师
     *
     * @param ownerUUID
     * @param friendUUID
     * @param sign
     * @return
     */
    @GET("addFriend")
    Observable<HttpResult<Object>> addFriend(@Query("ownerUUID") String ownerUUID, @Query("friendUUID") String friendUUID, @Query("sign") String sign);


    /**
     * 查询咨询列表
     *
     * @param type 1.文字咨询 2.视频咨询
     * @param sign
     * @return
     */
    @GET("getInformations")
    Observable<HttpResult<List<NewsResult>>> getNews(@Query("type") String type, @Query("sign") String sign);

    //getIndexAdvert?position=1&sign=123

    /**
     * 首页广告轮播图
     *
     * @param position
     * @param sign
     * @return
     */
    @GET("/getIndexAdvert")
    Observable<HttpResult<List<AdvertResult>>> getIndexAdvert(@Query("position") String position, @Query("sign") String sign);

    ///delFriend?ownerUUID=FFF1-33&friendUUID=EEE1&sign=123

    /**
     * 删除好友
     *
     * @param ownerUUID
     * @param friendUUID
     * @param sign
     * @return
     */
    @GET("delFriend")
    Observable<HttpResult<Object>> delFriend(@Query("ownerUUID") String ownerUUID, @Query("friendUUID") String friendUUID, @Query("sign") String sign);

}
