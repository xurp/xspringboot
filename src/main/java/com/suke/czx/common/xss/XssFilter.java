package com.suke.czx.common.xss;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * XSS过滤
 * @author czx
 * @email object_czx@163.com
 * @date 2017-04-01 10:20
 */
// [注]:实现了Filter,在config里的FilterConfig里注册
public class XssFilter implements Filter {

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
		// [注]:这里调用了XssHttpServletRequestWrapper,纯粹是为了xss过滤的工具类
		XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(
				(HttpServletRequest) request);
		chain.doFilter(xssRequest, response);
	}

	@Override
	public void destroy() {
	}

}