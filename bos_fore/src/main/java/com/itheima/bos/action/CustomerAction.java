package com.itheima.bos.action;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Controller;

import com.itheima.bos.utils.MailUtils;
import com.itheima.bos.utils.SmsUtils;
import com.itheima.crm.domain.Customer;

@Controller
@Scope("prototype")
@ParentPackage("json-default")
@Namespace("/")
public class CustomerAction extends BaseAction<Customer>{

	@Autowired
	@Qualifier("jmsQueueTemplate")
	private JmsTemplate jmsTemplate;
	
	// 因为没有账号所有模拟短信发送,虽然代码逻辑相同但是最后的结果直接使用000/XXX来结束
	@Action(value="customer_sendSMS")
	public String sendSMS() throws IOException {
		
		// 生成验证码
		String randomCode = RandomStringUtils.randomNumeric(4);
		System.out.println("验证码是:" + randomCode);
		
		// 将生成的验证码存入到我们的session中
		ServletActionContext.getRequest().getSession().setAttribute(model.getTelephone(), randomCode);
		
		// 编辑短信的内容
		final String content = "尊敬的用户您好,您的验证码为" + randomCode + "服务电话:";
		
		// 调用SMS服务发送短信
		// String result = SmsUtils.sendSmsByHTTP(model.getTelephone(), content);
		/*String result = "000/XXXXX";
		if(result.startsWith("000")) {
			// 发送成功
			return NONE;
		} else {
			throw new RuntimeException("短信发送失败,短信码" + result);
		}*/
		// 调用MQ服务，发送一条消息
		jmsTemplate.send("bos_sms",  new MessageCreator() {
			
			@Override
			public Message createMessage(Session session) throws JMSException {
				MapMessage mapMessage = session.createMapMessage();
				mapMessage.setString("telephone", model.getTelephone());
				mapMessage.setString("msg", content);
				return mapMessage;
			}
		});
		
		return NONE;
	}
	
	private String checkCode;
	
	public void setCheckCode(String checkCode) {
		this.checkCode = checkCode;
	}
	
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Action(value="customer_regist", results = 
		{@Result(name="success",type="redirect",location="signup-success.html"),
		@Result(name="input",type="redirect",location="signup.html")})
	public String regist() {
		
		// 判断输入的验证码和我们 在缓存中存储的验证码是否相同
		String checkCodeSession = (String) ServletActionContext.getRequest().getSession()
				.getAttribute(model.getTelephone());
		if(checkCodeSession == null || !checkCodeSession.equals(checkCode)) {
			// 验证码错误
			System.out.println("验证码错误");
			return INPUT;
		}
		
		// 使用WebService创建连接
		WebClient.create("http://localhost:9001/crm_management/services/"
				+ "customerService/customer").type(MediaType.APPLICATION_JSON).post(model);
		
		System.out.println("用户注册成功");
		// 发送一封激活邮件
		// 生成激活码
		String activecode = RandomStringUtils.randomNumeric(32);

		// 将激活码保存到redis，设置24小时失效
		/*redisTemplate.opsForValue().set(model.getTelephone(), activecode, 24,
				TimeUnit.HOURS);*/

		// 调用MailUtils发送激活邮件
		String content = "尊敬的客户您好，请于24小时内，进行邮箱账户的绑定，点击下面地址完成绑定:<br/><a href='"
				+ MailUtils.activeUrl + "?telephone=" + model.getTelephone()
				+ "&activecode=" + activecode + "'>速运快递邮箱绑定地址</a>";
		MailUtils.sendMail("速运快递激活邮件", content, model.getEmail());
		return SUCCESS;
	}
	
	// 属性驱动
	private String activecode;

	public void setActivecode(String activecode) {
		this.activecode = activecode;
	}
	
	@Action(value="customer_activeMail")
	public String activeMail() throws IOException {
		
		System.out.println("邮箱在这里进行验证");
		ServletActionContext.getResponse().setContentType(
				"text/html;charset=utf-8");
		// 判断激活码是否有效
		String activecodeRedis = redisTemplate.opsForValue().get(
				model.getTelephone());
		if (activecodeRedis == null || !activecodeRedis.equals(activecodeRedis)) {
			// 激活码无效
			ServletActionContext.getResponse().getWriter()
					.println("激活码无效，请登录系统，重新绑定邮箱！");
		} else {
			// 激活码有效
			// 防止重复绑定
			// 调用CRM webService 查询客户信息，判断是否已经绑定
			Customer customer = WebClient
					.create("http://localhost:9001/crm_management/services"
							+ "/customerService/customer/telephone/"
							+ model.getTelephone())
					.accept(MediaType.APPLICATION_JSON).get(Customer.class);
			if(customer != null) {
				if (customer.getType() == null || customer.getType() != 1) {
					// 没有绑定,进行绑定
					WebClient.create(
							"http://localhost:9001/crm_management/services"
									+ "/customerService/customer/updatetype/"
									+ model.getTelephone()).get();
					ServletActionContext.getResponse().getWriter()
							.println("邮箱绑定成功！");
				} else {
					// 已经绑定过
					ServletActionContext.getResponse().getWriter()
							.println("邮箱已经绑定过，无需重复绑定！");
				}
	
				// 删除redis的激活码
				redisTemplate.delete(model.getTelephone());
			}
		}
		
		return NONE;
	}
}
