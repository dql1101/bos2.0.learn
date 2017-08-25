package com.itheima.crm.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itheima.crm.dao.CustomerRepository;
import com.itheima.crm.domain.Customer;
import com.itheima.crm.service.CustomerService;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	private CustomerRepository customerRepository;
	
	// 找到所有没有和fixedArea关联的customer
	@Override
	public List<Customer> findNoAccessCus() {
		return customerRepository.findByFixedAreaIdIsNull();
	}

	@Override
	public List<Customer> findAccessCus(String fixedAreaId) {
		return customerRepository.findByFixedAreaId(fixedAreaId);
	}

	@Override
	public void addCusToFixedArea(String customerIdStr, String fixedAreaId) {
		
		// 先清空未关联
		customerRepository.clearFixedAreaId(fixedAreaId);
		
		// 如果是空那我们就需要直接结束方法来
		if("null".equals(customerIdStr)) {
			return ;
		}
		
		
		String[] cusStr = customerIdStr.split(",");
		for (String cusId : cusStr) {
			Integer id = Integer.parseInt(cusId);
			customerRepository.update(fixedAreaId, id);
		}
	}

	// 用户注册
	@Override
	public void regist(Customer customer) {
		customerRepository.save(customer);
	}

	// 根据电话查询用户
	@Override
	public Customer findByTelephone(String telephone) {
		return customerRepository.findByTelephone(telephone);
	}

	// 激活用户
	@Override
	public void updateType(String telephone) {
		customerRepository.updateType(telephone);
	}

	// 用户登录
	@Override
	public Customer login(String telephone, String password) {
		return customerRepository.findByTelephoneAndPassword(telephone, password);
	}

	@Override
	public String findFixedAreaIdByAddress(String address) {
		return customerRepository.findFixedAreaIdByAddress(address);
	}

}
