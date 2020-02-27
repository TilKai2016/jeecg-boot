package org.jeecg.modules.system.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.aill.sso.oauth.client.pojo.bo.OAuth2AccessToken;
import com.aill.sso.oauth.client.pojo.dto.TkeyToken;
import com.aill.sso.oauth.client.service.TkeyService;
import com.aill.sso.oauth.client.utils.CodecUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.shiro.vo.DefContants;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysLoginModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysLogService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author scott
 * @since 2018-12-17
 */
@RestController
@RequestMapping("/sys")
@Api(tags="用户登录")
@Slf4j
public class LoginController {
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysBaseAPI sysBaseAPI;
	@Autowired
	private ISysLogService logService;
	@Autowired
    private RedisUtil redisUtil;
	@Autowired
    private ISysDepartService sysDepartService;
	@Autowired
	private TkeyService tkeyService;

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ApiOperation("登录接口")
	public Result<JSONObject> login(@RequestBody SysLoginModel sysLoginModel) {
		Result<JSONObject> result = new Result<JSONObject>();
		String username = sysLoginModel.getUsername();
		String password = sysLoginModel.getPassword();
		SysUser sysUser = sysUserService.getUserByName(username);
		if(sysUser==null) {
			result.error500("该用户不存在");
			sysBaseAPI.addLog("登录失败，用户名:"+username+"不存在！", CommonConstant.LOG_TYPE_1, null);
			return result;
		}else {
			//密码验证
			String userpassword = PasswordUtil.encrypt(username, password, sysUser.getSalt());
			String syspassword = sysUser.getPassword();
			if(!syspassword.equals(userpassword)) {
				result.error500("用户名或密码错误");
				return result;
			}
			//生成token
			String token = JwtUtil.sign(username, syspassword);
			redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
			 //设置超时时间
			redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME/1000);
			
			//获取用户部门信息
			JSONObject obj = new JSONObject();
			List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
			obj.put("departs",departs);
			if(departs==null || departs.size()==0) {
				obj.put("multi_depart",0);
			}else if(departs.size()==1){
				sysUserService.updateUserDepart(username, departs.get(0).getOrgCode());
				obj.put("multi_depart",1);
			}else {
				obj.put("multi_depart",2);
			}
			obj.put("token", token);
			obj.put("userInfo", sysUser);
			result.setResult(obj);
			result.success("登录成功");
			sysBaseAPI.addLog("用户名: "+username+",登录成功！", CommonConstant.LOG_TYPE_1, null);
		}
		return result;
	}
	
	/**
	 * 退出登录
	 * @param
	 * @return
	 */
	@RequestMapping(value = "/logout")
	public Result<Object> logout(HttpServletRequest request,HttpServletResponse response) {
		//用户退出逻辑
		Subject subject = SecurityUtils.getSubject();
		LoginUser sysUser = (LoginUser)subject.getPrincipal();
		sysBaseAPI.addLog("用户名: "+sysUser.getRealname()+",退出成功！", CommonConstant.LOG_TYPE_1, null);
		log.info(" 用户名:  "+sysUser.getRealname()+",退出成功！ ");
	    subject.logout();

	    String token = request.getHeader(DefContants.X_ACCESS_TOKEN);
	    //清空用户Token缓存
	    redisUtil.del(CommonConstant.PREFIX_USER_TOKEN + token);
	    //清空用户权限缓存：权限Perms和角色集合
	    redisUtil.del(CommonConstant.LOGIN_USER_CACHERULES_ROLE + sysUser.getUsername());
	    redisUtil.del(CommonConstant.LOGIN_USER_CACHERULES_PERMISSION + sysUser.getUsername());
		return Result.ok("退出登录成功！");
	}

	/**
	 * 接收 code，然后换取 token
	 */
	@SneakyThrows
	@RequestMapping(value = "/codeCallback", method = RequestMethod.GET)
	public void codeCallback(final HttpServletRequest request, final HttpServletResponse response, @RequestParam(value = "redirect_uri", required = true) String redirectUri) {
		String code = request.getParameter("code");

		if (StringUtils.isBlank(code)) {
			return;
		}

		getAccessToken(request, response, code);

		// 重定向到原请求地址
		redirectUri = CodecUtil.decodeURL(redirectUri);
		response.sendRedirect(redirectUri);
	}

	/**
	 * 获取访问量
	 * @return
	 */
	@GetMapping("loginfo")
	public Result<JSONObject> loginfo() {
		Result<JSONObject> result = new Result<JSONObject>();
		JSONObject obj = new JSONObject();
		//update-begin--Author:zhangweijian  Date:20190428 for：传入开始时间，结束时间参数
		// 获取一天的开始和结束时间
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        Date dayStart = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date dayEnd = calendar.getTime();
		// 获取系统访问记录
		Long totalVisitCount = logService.findTotalVisitCount();
		obj.put("totalVisitCount", totalVisitCount);
		Long todayVisitCount = logService.findTodayVisitCount(dayStart,dayEnd);
		obj.put("todayVisitCount", todayVisitCount);
		Long todayIp = logService.findTodayIp(dayStart,dayEnd);
		//update-end--Author:zhangweijian  Date:20190428 for：传入开始时间，结束时间参数
		obj.put("todayIp", todayIp);
		result.setResult(obj);
		result.success("登录成功");
		return result;
	}
	
	/**
	 * 登陆成功选择用户当前部门
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/selectDepart", method = RequestMethod.PUT)
	public Result<?> selectDepart(@RequestBody SysUser user) {
		String username = user.getUsername();
		String orgCode= user.getOrgCode();
		this.sysUserService.updateUserDepart(username, orgCode);
		return Result.ok();
	}

	//=====================================私有方法 start=====================================

	private void getAccessToken(final HttpServletRequest request, final HttpServletResponse response, String code) {
		OAuth2AccessToken oauthToken = tkeyService.getAccessToken(code);
		String accessToken = oauthToken.getAccessToken();

		TkeyToken tkeyToken = new TkeyToken();
		tkeyToken.setAccessToken(accessToken);
		tkeyToken.setRefreshToken(oauthToken.getRefreshToken());
		tkeyToken.setAttributes(tkeyService.getUserProfile(oauthToken));

		HttpSession session = request.getSession();
		session.setAttribute("userinfo", tkeyToken);
	}

	//=====================================私有方法  end=====================================

}
