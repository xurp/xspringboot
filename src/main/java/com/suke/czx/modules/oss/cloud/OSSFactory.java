package com.suke.czx.modules.oss.cloud;

import com.suke.czx.common.utils.Constant;
import com.suke.czx.common.utils.SpringContextUtils;
import com.suke.czx.modules.sys.service.SysConfigService;

import org.springframework.beans.factory.annotation.Autowired;

import com.suke.czx.common.utils.ConfigConstant;

/**
 * 文件上传Factory
 * @author czx
 * @email object_czx@163.com
 * @date 2017-03-26 10:18
 */
public final class OSSFactory {
    private static SysConfigService sysConfigService;

    static {
    	//[注]:本类不归spring管,所以可能要用SpringContextUtils获取spring里的bean(service)了
        OSSFactory.sysConfigService = (SysConfigService) SpringContextUtils.getBean("sysConfigService");
    }

    public static CloudStorageService build(){
        // [注]:从sys_config表里获取CLOUD_STORAGE_CONFIG_KEY的config value(json):获取云存储配置信息
        CloudStorageConfig config = sysConfigService.getConfigObject(ConfigConstant.CLOUD_STORAGE_CONFIG_KEY, CloudStorageConfig.class);
        // [注]:根据type(1)选择storageService

        if(config.getType() == Constant.CloudService.QINIU.getValue()){
            return new QiniuCloudStorageService(config);
        }else if(config.getType() == Constant.CloudService.ALIYUN.getValue()){
            return new AliyunCloudStorageService(config);
        }else if(config.getType() == Constant.CloudService.QCLOUD.getValue()){
            return new QcloudCloudStorageService(config);
        }else if(config.getType() == Constant.CloudService.MINIO.getValue()){
            return new MinioCloudStorageService(config);
        }
        return null;
    }

}
