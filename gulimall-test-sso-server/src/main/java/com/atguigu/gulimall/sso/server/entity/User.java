package com.atguigu.gulimall.sso.server.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class User {
	private String username;

	private String password;

	private String redirect_url;
}
