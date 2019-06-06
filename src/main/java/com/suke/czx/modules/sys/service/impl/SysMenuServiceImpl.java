package com.suke.czx.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suke.czx.common.utils.Constant;
import com.suke.czx.modules.sys.mapper.SysMenuMapper;
import com.suke.czx.modules.sys.entity.SysMenu;
import com.suke.czx.modules.sys.service.SysMenuService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Lazy
@Service
@AllArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper,SysMenu> implements SysMenuService {

	private final SysMenuMapper sysMenuMapper;
	
	/**
	 * 根据父菜单，查询子菜单
	 * @param parentId 父菜单ID
	 * @param menuIdList  用户菜单ID,查询出的菜单必须在该参数下
	 */
	@Override
	public List<SysMenu> queryListParentId(Long parentId, List<Long> menuIdList) {
		List<SysMenu> menuList = queryListParentId(parentId);
		if(menuIdList == null){
			return menuList;
		}
		
		List<SysMenu> userMenuList = new ArrayList<>();
		for(SysMenu menu : menuList){
			if(menuIdList.contains(menu.getMenuId())){
				userMenuList.add(menu);
			}
		}
		return userMenuList;
	}
	
	/**
	 * 根据父菜单，查询子菜单
	 * @param parentId 父菜单ID
	 */
	@Override
	public List<SysMenu> queryListParentId(Long parentId) {
		QueryWrapper<SysMenu> queryWrapper = new QueryWrapper<>();
		queryWrapper
				.eq("parent_id",parentId)
				.orderByAsc("order_num");
		return sysMenuMapper.selectList(queryWrapper);
	}

	@Override
	public List<SysMenu> queryNotButtonList() {
		return sysMenuMapper.queryNotButtonList();
	}

	@Override
	public List<SysMenu> getUserMenuList(Long userId) {
		//系统管理员，拥有最高权限
		if(userId == Constant.SUPER_ADMIN){
			return getAllMenuList(null);
		}
		
		//用户菜单列表
		// [注]:用户id和roleId对应一张表,roleId和该权限可以使用的菜单id列表对应一张表
		List<Long> menuIdList = sysMenuMapper.queryAllMenuId(userId);
		return getAllMenuList(menuIdList);
	}

	/**
	 * 查询用户的权限列表
	 */
	@Override
	public List<SysMenu> queryUserList(Long userId) {
		return sysMenuMapper.queryUserList(userId);
	}

	/**
	 * 获取所有菜单列表
	 */
	private List<SysMenu> getAllMenuList(List<Long> menuIdList){
		//查询根菜单列表
		List<SysMenu> menuList = queryListParentId(0L, menuIdList);
		//递归获取子菜单
		getMenuTreeList(menuList, menuIdList);
		
		return menuList;
	}

	/**
	 * 递归
	 */
	private List<SysMenu> getMenuTreeList(List<SysMenu> menuList, List<Long> menuIdList){
		// [注]:算法:menuIdList不用管,只是一个查询范围限制.遍历menuList,如果是目录,从数据库查询该目录下的所有菜单并set到实体类里的list域(数据库里没有),然后递归
		// [注]:这样结束后,返回的subMenuList里,每个SysMenu都存了自己下一级的目录
		List<SysMenu> subMenuList = new ArrayList<SysMenu>();
		
		for(SysMenu entity : menuList){
			if(entity.getType() == Constant.MenuType.CATALOG.getValue()){//目录
				entity.setList(getMenuTreeList(queryListParentId(entity.getMenuId(), menuIdList), menuIdList));
			}
			subMenuList.add(entity);
		}
		
		return subMenuList;
	}
}
