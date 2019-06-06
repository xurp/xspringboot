package com.suke.czx.modules.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suke.czx.modules.sys.mapper.SysRoleMenuMapper;
import com.suke.czx.modules.sys.entity.SysRoleMenu;
import com.suke.czx.modules.sys.service.SysRoleMenuService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



/**
 * 角色与菜单对应关系
 * 
 * @author czx
 * @email object_czx@163.com
 * @date 2016年9月18日 上午9:44:35
 */
@Service
@AllArgsConstructor
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper,SysRoleMenu> implements SysRoleMenuService {

	private final SysRoleMenuMapper sysRoleMenuMapper;

	// [注]:这个肯定要加事务,但它是在另一个事务方法SysRoleServiceImpl#save里被调用的.
	// [注]:PROPAGATION_REQUIRED支持当前事务，如果当前没有事务，就新建一个事务。这是最常见的选择，也是spring默认的事务的传播。
	// [注]:save调用saveOrUpdate,因为是默认设置,所以在一个事务里,这两个大小方法会同时提交,同时回滚
	@Override
	@Transactional
	public void saveOrUpdate(Long roleId, List<Long> menuIdList) {
		//先删除角色与菜单关系
		sysRoleMenuMapper.deleteById(roleId);

		if(menuIdList.size() == 0){
			return ;
		}

		//保存角色与菜单关系
		Map<String, Object> map = new HashMap<>();
		map.put("roleId", roleId);
		map.put("menuIdList", menuIdList);
		sysRoleMenuMapper.saveUserMenu(map);
	}

	/**
	 * 根据角色ID，获取菜单ID列表
	 */
	@Override
	public List<Long> queryMenuIdList(Long roleId) {
		return sysRoleMenuMapper.queryMenuIdList(roleId);
	}

}
