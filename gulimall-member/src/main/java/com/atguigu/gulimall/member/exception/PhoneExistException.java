package com.atguigu.gulimall.member.exception;


/**
 * @author Cai Peishen
 * @email peishen.cai@foxmail.com
 * @date 2021-03-09 21:42:08
 */
public class PhoneExistException extends RuntimeException {
	public PhoneExistException() {
		super("手机号存在");
	}
}
