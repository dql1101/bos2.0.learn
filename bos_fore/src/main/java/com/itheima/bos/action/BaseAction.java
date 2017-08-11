package com.itheima.bos.action;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

public abstract class BaseAction<T> extends ActionSupport implements ModelDriven<T>{

	protected T model;
	
	@Override
	public T getModel() {
		return model;
	}

	public BaseAction() {
		Type genericSuperclass = this.getClass().getGenericSuperclass();
		ParameterizedType parameterizedType = (ParameterizedType)genericSuperclass;
		Class<T> modelClass  = (Class<T>)parameterizedType.getActualTypeArguments()[0];
		try {
			model = modelClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			System.out.println("构造模型失败");
		}
	}

}
