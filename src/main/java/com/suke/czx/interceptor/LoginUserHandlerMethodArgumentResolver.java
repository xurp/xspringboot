package com.suke.czx.interceptor;

import com.suke.czx.common.annotation.LoginUser;
import com.suke.czx.modules.sys.entity.SysUser;
import com.suke.czx.modules.sys.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 有@LoginUser注解的方法参数，注入当前登录用户
 * @author czx
 * @email object_czx@163.com
 * @date 2017-03-23 22:02
 */
// [注]:implements HandlerMethodArgumentResolver这种叫自定义解析器
@Component
public class LoginUserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Autowired
    private SysUserService sysUserService;
    // [注]:本项目使用可能有问题,实际用法类似:只要方法参数是SysUser的并且加了LoginUser注解,就调用resolveArgument方法,返回的Object就是user(注入了)
    /*@GetMapping("info")
    public UserEntity userInfo(@LoginUser SysUser user){
        return user;
    }*/

    // [注]:用于判定是否需要处理该参数分解，返回true为需要，并会去调用下面的方法resolveArgument
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(SysUser.class) && parameter.hasParameterAnnotation(LoginUser.class);
    }

    // [注]:resolveArgument：真正用于处理参数分解的方法，返回的Object就是controller方法上的形参对象
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,NativeWebRequest request, WebDataBinderFactory factory) throws Exception {
        //获取用户ID
        Object object = request.getAttribute(AuthorizationInterceptor.USER_KEY, RequestAttributes.SCOPE_REQUEST);
        if(object == null){
            return null;
        }

        //获取用户信息
        SysUser user = sysUserService.getById((Long)object);

        return user;
    }
}
