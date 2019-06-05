package com.suke.czx.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.suke.czx.common.exception.RRException;
import com.suke.czx.modules.sys.mapper.SysConfigMapper;
import com.suke.czx.modules.sys.entity.SysConfig;
import com.suke.czx.modules.sys.service.SysConfigService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper,SysConfig> implements SysConfigService {

	private final SysConfigMapper sysConfigMapper;
	// [注]:写数据库加事务,即使一句话也可以加
	@Override
	@Transactional
	public boolean save(SysConfig config) {
		sysConfigMapper.insert(config);
		return true;
	}

	@Transactional
	public void update(SysConfig config) {
		sysConfigMapper.updateById(config);
	}

	@Override
	@Transactional
	public void updateValueByKey(String key, String value) {
		UpdateWrapper<SysConfig> wrapper = new UpdateWrapper<>();
		wrapper.eq("config_key",key)
				.eq("config_value",value);
		// [注]:这里的build用的好
		baseMapper.update(SysConfig.builder().configKey(key).configValue(value).build(),wrapper);
	}
	// [注]:这里是不是写错了,config没用

	@Transactional
	public void deleteBatch(Long[] ids) {
		for(Long id : ids){
			SysConfig config = baseMapper.selectById(id);
		}
		sysConfigMapper.deleteById(ids);
	}

	@Override
	public String getValue(String key) {
		SysConfig config = baseMapper.selectOne(Wrappers.<SysConfig>query().lambda().eq(SysConfig::getConfigKey,key));
		return config == null ? null : config.getConfigValue();
	}
	// [注]:用gson把字符串转实体类
	@Override
	public <T> T getConfigObject(String key, Class<T> clazz) {
		String value = getValue(key);
		if(StringUtils.isNotBlank(value)){
			return new Gson().fromJson(value, clazz);
		}

		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new RRException("获取参数失败");
		}
	}
}
