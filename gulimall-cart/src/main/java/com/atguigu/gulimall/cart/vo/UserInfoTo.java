package com.atguigu.gulimall.cart.vo;

import lombok.Data;
import lombok.ToString;

/**
 * <p>Title: UserInfoVo</p>
 * Description：每条线程中的用户数据
 * date：2021/03/19 22:34
 */
@Data
@ToString
public class UserInfoTo {

	/**
	 * 存储已登录用户在数据库中的ID
	 */
	private Long userId;

	/**
	 * 存储用户名
	 */
	private String username;

	/**
	 * 配分一个临时的user-key
	 */
	private String userKey;

	/**
	 * 判断是否是临时用户
	 */
	private boolean tempUser = false;
}
