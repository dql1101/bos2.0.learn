package com.itheima.crm.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.itheima.crm.domain.Customer;

public interface CustomerService {

	// 查询所有未关联的客户
	@Path("/noaccesscus")
	@GET
	@Produces({ "application/xml", "application/json" })
	public List<Customer> findNoAccessCus();

	// 查询所有已关联客户
	@Path("/accesscus/{fixedAreaId}")
	@GET
	@Produces({ "application/xml", "application/json" })
	public List<Customer> findAccessCus(@PathParam("fixedAreaId") String fixedAreaId);

	// 将客户添加到定去ID,将所有的客户ID拼成一个字符串
	@Path("/addcustofixedarea")
	@PUT
	public void addCusToFixedArea(@QueryParam("customerIdStr") String customerIdStr,
			@QueryParam("fixedAreaId") String fixedAreaId);
	
	// 前台界面的用户注册
	@Path("/customer")
	@POST
	@Consumes({ "application/xml", "application/json" })
	public void regist(Customer customer);

	// 邮件激活用户时判断用户是否已经已经存入数据库
	@Path("/customer/telephone/{telephone}")
	@GET
	@Produces({ "application/xml", "application/json" })
	public Customer findByTelephone(@PathParam("telephone") String telephone);
	
	// 进行绑定激活操作
	@Path("/customer/updatetype/{telephone}")
	@GET
	public void updateType(@PathParam("telephone") String telephone);
	
	// 用户登录
	@Path("/customer/login")
	@GET
	@Produces({ "application/xml", "application/json" })
	public Customer login(@QueryParam("telephone") String telephone, @QueryParam("password") String password);

	// 根据订单的地址获取对应的定区id
	@Path("/customer/findFixedAreaIdByAddress/{address}")
	@GET
	@Produces({ "application/xml", "application/json" })
	public String findFixedAreaIdByAddress(@PathParam("address") String address);
}
