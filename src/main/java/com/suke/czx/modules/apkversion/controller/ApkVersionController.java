package com.suke.czx.modules.apkversion.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.suke.czx.common.annotation.SysLog;
import com.suke.czx.common.base.AbstractController;
import com.suke.czx.common.utils.R;
import com.suke.czx.modules.apkversion.entity.ApkVersion;
import com.suke.czx.modules.apkversion.service.ApkVersionService;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;



/**
 * APK版本管理
 * 
 * @author czx
 * @email object_czx@163.com
 * @date 2019-04-28 15:56:33
 */
@RestController
@AllArgsConstructor
@RequestMapping("/apkversion/apkversion")
public class ApkVersionController  extends AbstractController {
    private final  ApkVersionService apkVersionService;

    /**
     * 列表
     */
    // [注]:这里的@RequiresPermissions是限制权限,apkversion:apkversion:list在sysMenu里,数据库里的sys_menu的APK版本管理的下属查看的perms就是这个
    // [注]:在OAuth2Realm的doGetAuthorizationInfo里调用了shiroService.getUserPermissions(该方法用,分割了数据库的权限列表)获取permSet,再在这里对比.这是专门和shiro配合的
    // [注]:这里的@RequestParam Map<String, Object> params参数是{_search=false, nd=1559807513220, limit=10, page=1, sidx=, order=asc, _=1559807513152}
    // [注]:index.js里通过getMenuList从SysMenuController#nav获得返回的R里的menuList,然后再index.html#L64遍历menuList渲染菜单
    @RequestMapping("/list")
    @RequiresPermissions("apkversion:apkversion:list")
    public R list(@RequestParam Map<String, Object> params){
        //查询列表数据
    	// [注]:QueryWrapper是封装过的mybatis
        QueryWrapper<ApkVersion> queryWrapper = new QueryWrapper<>();
        // [注]:mpPageConvert在AbstractController里,是mybatisPlus的Page和hashmap的转换
        IPage<ApkVersion> sysConfigList = apkVersionService.page(mpPageConvert.<ApkVersion>pageParamConvert(params),queryWrapper);
        // [注]:返回ok,并把page放到页面上
        return R.ok().put("page", mpPageConvert.pageValueConvert(sysConfigList));
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("apkversion:apkversion:info")
    public R info(@PathVariable("id") Long id){
        return R.ok().put("apkVersion", apkVersionService.getById(id));
    }


    /**
     * 新增APK版本管理
     */
    @SysLog("新增APK版本管理数据")
    @RequestMapping("/save")
    @RequiresPermissions("apkversion:apkversion:save")
    public R save(@RequestBody ApkVersion apkVersion){
        apkVersion.setUserId(getUserId());
        apkVersionService.save(apkVersion);
        return R.ok();
    }


    /**
     * 修改
     */
    @SysLog("修改APK版本管理数据")
    @RequestMapping("/update")
    @RequiresPermissions("apkversion:apkversion:update")
    public R update(@RequestBody ApkVersion apkVersion){
        apkVersion.setUpdateTime(new Date());
		apkVersionService.updateById(apkVersion);
        return R.ok();
    }


    /**
     * 删除
     */
    @SysLog("删除APK版本管理数据")
    @RequestMapping("/delete")
    @RequiresPermissions("apkversion:apkversion:delete")
    public R delete(@RequestBody Long[] ids){
		apkVersionService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
	
}
