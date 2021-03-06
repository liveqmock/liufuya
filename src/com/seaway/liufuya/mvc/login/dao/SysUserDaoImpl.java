package com.seaway.liufuya.mvc.login.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.pager.Pager;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import com.seaway.liufuya.BasicDao;
import com.seaway.liufuya.mvc.crm.memberinfo.dao.MemberInfoManager;
import com.seaway.liufuya.mvc.login.model.Role;
import com.seaway.liufuya.mvc.login.model.SysUser;
import com.seaway.liufuya.mvc.system.user.data.SysUserRole;


@IocBean
public class SysUserDaoImpl extends BasicDao  implements Serializable{
	private static final Log log = Logs.get();
	
	private Dao dao = null;

	private static final long serialVersionUID = 1L;
	
	public SysUserDaoImpl(){}
	
	public SysUserDaoImpl(Dao dao) {
		this.dao = dao;
		super.dao = dao;
	}
	
	
	/**
	 * 根据登陆名和密码查当前用户
	 * 
	 * @param login_name  用户输入用户名
	 * @param log_pwd     用户输入密码，没有做 MD5 加密
	 * @return
	 */
	public SysUser findSysUser(String login_name,String log_pwd) {
		Cnd condition = Cnd.where("loginName", "=", login_name)
				.and("logPwd", "=", log_pwd)
				.and("status", "=", "1");

		return findByCondition(SysUser.class, condition);
	}

	/**
	 * 添加登陆日记 ,目前没有这个表
	 * 
	 * @param map
	 * @return
	 */
//	 public void insertLoginLog(Map<String,Object> map) throws Exception{
//	 return this.getSqlSessionTemplate().insert(this.nameSpace +
//	 ".insertLoginLog", map);
//	 }

	/**
	 * 根据usercode修改用户登陆密码
	 * 
	 * editPassword.jsp 页面中的 ，真实姓名、密码、邮箱、电话 四个内容进行修改
	 * 
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public boolean updateLoginPwd(SysUser sysUser){
		return this.update(sysUser);
	}
	
	/**
	 * 查询所有角色
	 */
	public List<Role> getAllRoles() {
		Cnd condition = Cnd.where("status", "=", "1");
		return search(Role.class, condition);
	}

	/**
	 * 新增系统用户
	 * 
	 * @param sysUser
	 */
	public void saveSysUser(SysUser sysUser) {
		this.save(sysUser);
	}
	
	/**
	 * 新增系统用户与角色关联表
	 * 
	 * @param sysUser
	 */
	public void saveSysUserRole(SysUserRole sysUser) {
		this.save(sysUser);
	}
	
	/**
	 * 根据 userCode 查询当前用户的 roleCode
	 * 
	 * @param sysUserId
	 * @return
	 */
	public String findRoleCodeByUserCode(String userCode) {
		Cnd condition = Cnd.where("user_code", "=", userCode).and("status",
				"=", "1");
		SysUserRole sysUserRole = findByCondition(SysUserRole.class, condition);
		if (sysUserRole != null) {
			return sysUserRole.getRole_code();
		}else{
			return null;
		}
		
	}
	
	

	/**
	 * 根据id修改用户
	 * 
	 * @param sysUser
	 * @return
	 */
	public boolean updateSysUserById(SysUser sysUser) {
		return this.update(sysUser);
	}

	/**
	 * 根据id查询用户
	 * 
	 * @param sysUserId
	 * @return
	 */
	public List<SysUser> getSysUserById(String sysUserId) {
		// select * from SYS_USER where user_code=#{id} and STATUS='1'
		Cnd condition = Cnd.where("userCode", "=", sysUserId).and("status",
				"=", "1");
		return search(SysUser.class, condition);
	}

	/**
	 * 查询当前所有管理员 用户数量
	 * 
	 * @return
	 */
	public int getTotalCount() {
		Cnd condition = Cnd.where("status", "=", 1);
		return this.searchCount(SysUser.class, condition);
	}

	/**
	 * 分页查询系统用户
	 */
	public List<SysUser> getAllSysUserList(int startNum, int rows) {

		Pager pager = dao.createPager(startNum, rows);
		// 设置一共可以查询的条数
		pager.setRecordCount(dao.count(SysUser.class,
				Cnd.where("status", "=", 1)));
		List<SysUser> users = dao.query(SysUser.class,
				Cnd.where("status", "=", 1), pager);
		//log.info("分页查询的用户数量  users.size ="+users.size());
		
		return users;
	}
	
	public List<SysUser> getAllSysUserList() {
		List<SysUser> users = dao.query(SysUser.class,
				Cnd.where("status", "=", 1));
		List<SysUser> list=new ArrayList<SysUser>();
		for (SysUser sysUser : users) {
			SysUser sys = ex(sysUser);
			list.add(sys);
		}
		return list;
	}
	
	//---------------------------------------------
	public SysUser ex(SysUser user){
		SysUser sys = new SysUser();
		sys.setId(user.getId());
		sys.setUserCode(user.getUserCode());
		sys.setUserName(user.getUserName());
		sys.setLoginName(user.getLoginName());
		sys.setLogPwd(user.getLogPwd());
		sys.setCreateDate(user.getCreateDate());
		sys.setEmail(user.getEmail());
		sys.setUserPhone(user.getUserPhone());
		sys.setUserType(user.getUserType().equals("1")?"系统用户":"卖家用户");
		sys.setStatus(user.getStatus().equals("1")?"启用":"禁用");
		//根据 user_code 到 sys_user_role 表格里面查询
		String user_code = user.getUserCode();
		Cnd condition1 = Cnd.where("status", "=", "1").and("user_code", "=", user_code);
		SysUserRole userRole = findByCondition(SysUserRole.class, condition1);
		//得到 role_code 。查询 roleName
		Cnd condition2 = Cnd.where("status", "=", "1").and("roleCode", "=", userRole.getRole_code());
		Role role = findByCondition(Role.class, condition2);
		sys.setRoleName(role.getRoleName()); //这个要修改为动态的
		
		return sys;
	}
	
	
	//----------------------------------------------
	/**
	 * 根据loginName查询系统用户
	 */
	public List<SysUser> findSysUserByLoginName(Map<String, Object> map) {
		List<SysUser> users = this.searchByPageLike(SysUser.class, "loginName",
				(String) map.get("loginName"), (Integer) map.get("startNum"),
				(Integer) map.get("rows"));
		return users;
	}
	
	/**
	 * 根据 用户 编码，查询对象
	 * @param user_code
	 * @return
	 */
	public SysUser findSysUserByCode(String user_code) {
		Cnd condition = Cnd.where("userCode", "=", user_code);
		SysUser user = findByCondition(SysUser.class, condition);
		
		return user;
	}

	/**
	 * 新增 用户数据，前，判断用户登录名是否已经存在 不存在，返回 true
	 * 
	 * @param memberlevel
	 */
	public boolean checkSysUserByLoginName(String login_name) {
		boolean flag = false;
		Cnd condition = Cnd.where("loginName", "=", login_name);
		SysUser user = findByCondition(SysUser.class, condition);
		if (user == null) {
			flag = true;
		}
		return flag;
	}
	
	
	/**
	 * 根据loginName查询系统用户的总记录数
	 */
	public int findSysUserByLoginNameCount(Map<String, Object> map) {
		return searchByPageLike(SysUser.class, "loginName",(String) map.get("loginName"));
	}

	/**
	 * 根据loginName验证登录名是否唯一
	 */
	public List<SysUser> checkLoginName(Map<String, Object> map) {
		Cnd condition = Cnd.where("name", "=", map.get("loginName")).and(
				"status", "=", "1");
		return search(SysUser.class, condition);
	}

}
