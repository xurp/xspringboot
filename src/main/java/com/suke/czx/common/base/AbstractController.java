package com.suke.czx.common.base;

import com.suke.czx.common.utils.MPPageConvert;
import com.suke.czx.modules.sys.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller公共组件
 * 
 * @author czx
 * @email object_czx@163.com
 * @date 2016年11月9日 下午9:42:26
 */

public abstract class AbstractController {
	//[注]:用作mybatisPlus的Page和hashmap的转换
	@Autowired
	protected MPPageConvert mpPageConvert;

	//[注]:用shiro获取user
	protected SysUser getUser() {
		return (SysUser) SecurityUtils.getSubject().getPrincipal();
	}

	protected Long getUserId() {
		return getUser().getUserId();
	}
}
