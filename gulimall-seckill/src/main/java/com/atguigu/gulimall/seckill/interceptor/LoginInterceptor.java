package com.atguigu.gulimall.seckill.interceptor;

import com.atguigu.common.consant.AuthServerConstant;
import com.atguigu.common.vo.MemberRsepVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Author: Cai Peishen
 * @Date: 2021/4/16 15:43
 * @Description: 登录拦截器
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

	public static ThreadLocal<MemberRsepVo> loginUser = new ThreadLocal<>();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String uri = request.getRequestURI();
		
		// 这个请求直接放行
		boolean match = new AntPathMatcher().match("/kill", uri);
		if(match){
			HttpSession session = request.getSession();
			MemberRsepVo memberRsepVo = (MemberRsepVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
			if(memberRsepVo != null){
				loginUser.set(memberRsepVo);
				return true;
			}else{
				// 没登陆就去登录
				session.setAttribute("msg", AuthServerConstant.NOT_LOGIN);
				response.sendRedirect("http://auth.gulimall.com/login.html");
				return false;
			}
		}
		return true;
	}
}
