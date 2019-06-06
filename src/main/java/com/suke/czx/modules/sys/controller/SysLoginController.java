package com.suke.czx.modules.sys.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.code.kaptcha.Producer;
import com.suke.czx.common.base.AbstractController;
import com.suke.czx.common.utils.Constant;
import com.suke.czx.common.utils.R;
import com.suke.czx.modules.sys.entity.SysUser;
import com.suke.czx.modules.sys.service.SysUserService;
import com.suke.czx.modules.sys.service.SysUserTokenService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 登录相关
 * 
 * @author czx
 * @email object_czx@163.com
 * @date 2019年4月18日 下午8:15:31
 */
@Slf4j
@RestController
@AllArgsConstructor
public class SysLoginController extends AbstractController {

	private final Producer producer;
	private final SysUserService sysUserService;
	private final SysUserTokenService sysUserTokenService;
	private final RedisTemplate redisTemplate;

	/**
	 * 验证码
	 */
	// [注]:@SneakyThrows是注解在方法上,省略方法名后面的 throws exceptions,这会自动检测要throws的异常,比如这里的IO.但似乎不是特别推荐用这个注解
	@SneakyThrows
	@RequestMapping("/sys/code/{time}")
	public void captcha(@PathVariable("time") String time, HttpServletResponse response){
		// [注]:这里的time是前台vue js生成的随机数.本方法的路径在ShiroConfig里配置为匿名可访问
		response.setHeader("Cache-Control", "no-store, no-cache");
		response.setContentType("image/jpeg");

		//生成文字验证码
		String text = producer.createText();
		log.info("==================验证码:{}====================",text);
		//生成图片验证码
		BufferedImage image = producer.createImage(text);
		// [注]:将生成的验证码text存在redis里并限制60秒过期
		redisTemplate.opsForValue().set(Constant.NUMBER_CODE_KEY + time,text,60, TimeUnit.SECONDS);

		ServletOutputStream out = response.getOutputStream();
		ImageIO.write(image, "jpg", out);
		IOUtils.closeQuietly(out);
	}
	/**
	 * 短信验证码
	 */
	@RequestMapping("/mobile/code/{number}")
	public Map<String, Object> mobile(@PathVariable("number") String number){

		QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("mobile",number);
		SysUser user = sysUserService.getOne(queryWrapper);

		//账号不存在
		if(user == null) {
			return R.error("手机号码未注册");
		}

		// [注]:在下面把手机号以60秒存在redis里,避免手机验证码重复发送
		String mobile = (String) redisTemplate.opsForValue().get(Constant.MOBILE_CODE_KEY + number);
		if(!StrUtil.isEmpty(mobile)){
			return R.error("验证码未失效");
		}

		// [注]:生成4位验证码,这是hutool里的工具类
		String code = RandomUtil.randomNumbers(Constant.CODE_SIZE);
		log.info("==================短信验证码:{}====================",code);
		//redis 60秒
		redisTemplate.opsForValue().set(Constant.MOBILE_CODE_KEY + number,code,60, TimeUnit.SECONDS);
		// [注]:这里调用短信服务去发送,具体API未实现

		return R.ok("验证码发送成功");
	}

	/**
	 * 密码登录
	 */
	@RequestMapping(value = "/sys/login", method = RequestMethod.POST)
	public Map<String, Object> login(String username, String password, String captcha,String randomStr){

		// [注]:将参数captcha即验证码与redis里存的对比
		String code_key = (String) redisTemplate.opsForValue().get(Constant.NUMBER_CODE_KEY + randomStr);
		if(StrUtil.isEmpty(code_key)){
			return R.error("验证码过期");
		}

		if(!captcha.equalsIgnoreCase(code_key)){
			return R.error("验证码不正确");
		}

		//用户信息
		QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("username",username);
		SysUser user = sysUserService.getOne(queryWrapper);

		//账号不存在、密码错误
		// [注]:本系统似乎没注册功能,所以这里后面的密码算法是事先约定好的.至于注册估计参考下Shiro的注册方式就行了
		// [注]:也可以参考SysUserServiceImpl.save
		if(user == null || !user.getPassword().equals(new Sha256Hash(password, user.getSalt()).toHex())) {
			return R.error("账号或密码不正确");
		}

		//账号锁定
		// [注]:给user设置状态是常见设计
		if(user.getStatus() == 0){
			return R.error("账号已被锁定,请联系管理员");
		}

		//生成token，并保存到数据库
		// [注]:这里的token是配合shiro做权限的,token存在表里,并且发请求时会根据用户存在header里,这样在方法里根据token获取用户信息
		// [注]:每次用户登录,会把新的token写在数据库里,同时把token写在R里返回给前台,前台ajax获取token后存在localstorage里(common.js)
		// [注]:在common.js里有ajax全部配置,每次ajax请求都会把token发过去
		R r = sysUserTokenService.createToken(user.getUserId());
		return r;
	}

	/**
	 * 手机号码登录
	 */
	@RequestMapping(value = "/mobile/login", method = RequestMethod.POST)
	public Map<String, Object> mobileLogin(String mobile, String code){

		String code_key = (String) redisTemplate.opsForValue().get(Constant.MOBILE_CODE_KEY + mobile);
		if(StrUtil.isEmpty(code_key)){
			return R.error("验证码过期");
		}

		if(!code.equalsIgnoreCase(code_key)){
			return R.error("验证码不正确");
		}

		//用户信息
		QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("mobile",mobile);
		SysUser user = sysUserService.getOne(queryWrapper);

		//账号不存在
		if(user == null) {
			return R.error("账号不存在");
		}

		//账号锁定
		if(user.getStatus() == 0){
			return R.error("账号已被锁定,请联系管理员");
		}

		//生成token，并保存到数据库
		R r = sysUserTokenService.createToken(user.getUserId());
		return r;
	}


	/**
	 * 退出
	 */
	@RequestMapping(value = "/sys/logout", method = RequestMethod.POST)
	public R logout() {
		sysUserTokenService.logout(getUserId());
		return R.ok();
	}

	/**
	 * 未认证
	 */
	// [注]:这个是配合ShiroConfig的,访问了不该访问的页面就跳转到401
	@RequestMapping(value = "/sys/unauthorized", method = RequestMethod.POST)
	public R unauthorized() {
		return R.error(HttpStatus.SC_UNAUTHORIZED, "unauthorized");
	}


	
}
