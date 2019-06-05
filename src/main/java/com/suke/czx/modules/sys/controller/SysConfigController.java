package com.suke.czx.modules.sys.controller;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.suke.czx.common.annotation.SysLog;
import com.suke.czx.common.base.AbstractController;
import com.suke.czx.common.utils.R;
import com.suke.czx.common.validator.ValidatorUtils;
import com.suke.czx.modules.sys.entity.SysConfig;
import com.suke.czx.modules.sys.service.SysConfigService;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * 系统参数信息
 * 
 * @author czx
 * @email object_czx@163.com
 * @date 2019年4月18日 下午6:55:53
 */
@RestController
@RequestMapping("/sys/config")
@AllArgsConstructor
public class SysConfigController extends AbstractController {
	private final SysConfigService sysConfigService;
	
	/**
	 * 所有配置列表
	 */
	@RequestMapping("/list")
	@RequiresPermissions("sys:config:list")
	public R list(@RequestParam Map<String, Object> params){
		//查询列表数据
		QueryWrapper<SysConfig> queryWrapper = new QueryWrapper<>();
		if(MapUtil.getStr(params,"key") != null){
			queryWrapper
					.like("remark",MapUtil.getStr(params,"key"));
		}
		IPage<SysConfig> sysConfigList = sysConfigService.page(mpPageConvert.<SysConfig>pageParamConvert(params),queryWrapper);

		return R.ok().put("page", mpPageConvert.pageValueConvert(sysConfigList));
	}
	
	
	/**
	 * 配置信息
	 */
	@RequestMapping("/info/{id}")
	@RequiresPermissions("sys:config:info")
	public R info(@PathVariable("id") Long id){
		SysConfig config = sysConfigService.getById(id);
		
		return R.ok().put("config", config);
	}
	
	/**
	 * 保存配置
	 */
	@SysLog("保存配置")
	@RequestMapping("/save")
	@RequiresPermissions("sys:config:save")
	public R save(@RequestBody SysConfig config){
		// [注]:先验证再保存
		ValidatorUtils.validateEntity(config);

		sysConfigService.save(config);
		
		return R.ok();
	}
	
	/**
	 * 修改配置
	 */
	@SysLog("修改配置")
	@RequestMapping("/update")
	@RequiresPermissions("sys:config:update")
	public R update(@RequestBody SysConfig config){
		ValidatorUtils.validateEntity(config);
		
		sysConfigService.updateById(config);
		
		return R.ok();
	}
	
	/**
	 * 删除配置
	 */
	@SysLog("删除配置")
	@RequestMapping("/delete")
	@RequiresPermissions("sys:config:delete")
	public R delete(@RequestBody Long[] ids){
		// [注]:这里的Serializable是因为mybatisplus的removeById封装的参数必须是Serializable的
		sysConfigService.removeById((Serializable)Arrays.asList(ids));
		return R.ok();
	}

}
