package com.itheima.redis.test;

import org.junit.Test;

import redis.clients.jedis.Jedis;

public class RedisTest {
	
	@Test
	public void test1() {
		Jedis jedis = new Jedis("localhost");
		jedis.set("name", "longaotian");
		String value = jedis.get("name");
		System.out.println(value);
	}
}
