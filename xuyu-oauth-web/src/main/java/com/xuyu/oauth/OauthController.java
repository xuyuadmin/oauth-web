﻿package com.xuyu.oauth;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.xuyu.base.BaseApiService;
import com.xuyu.utils.HttpClientUtils;
import com.xuyu.utils.WeiXinUtils;


@Controller
public class OauthController extends BaseApiService {

	@Autowired
	private WeiXinUtils weiXinUtils;
	private String errorPage = "errorPage";

	// 生成授权链接
	@RequestMapping("/authorizedUrl")
	public String authorizedUrl() {
		return "redirect:" + weiXinUtils.getAuthorizedUrl();
	}

	// 微信授权回调地址
	@RequestMapping("/callback")
	public String callback(String code, HttpServletRequest request) {
		// 1.使用Code 获取 access_token
		String accessTokenUrl = weiXinUtils.getAccessTokenUrl(code);
		JSONObject resultAccessToken = HttpClientUtils.httpGet(accessTokenUrl);
		boolean containsKey = resultAccessToken.containsKey("errcode");

		if (containsKey) {
			request.setAttribute("errorMsg", "系统错误!");
			return errorPage;
		}
		// 2.使用access_token获取用户信息
		String accessToken = resultAccessToken.getString("access_token");
		String openid = resultAccessToken.getString("openid");
		// 3.拉取用户信息(需scope为 snsapi_userinfo)
		String userInfoUrl = weiXinUtils.getUserInfo(accessToken, openid);
		JSONObject userInfoResult = HttpClientUtils.httpGet(userInfoUrl);
		System.out.println("userInfoResult:" + userInfoResult);
		request.setAttribute("nickname", userInfoResult.getString("nickname"));
		request.setAttribute("city", userInfoResult.getString("city"));
		request.setAttribute("headimgurl", userInfoResult.getString("headimgurl"));
		return "info";
	}

}
