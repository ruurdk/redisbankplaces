package com.redislabs.demos.redisbankplaces;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import redis.clients.jedis.JedisPool;

@SpringBootApplication
@Configuration
@Slf4j
public class RedisbankPlacesApplication {

/*
	JedisConnectionFactory jedisConnectionFactory() {
		JedisConnectionFactory factory = new JedisConnectionFactory();
		return factory;
	}
*/


	@Value("${spring.redis.host}")
	String host;
	@Value("${spring.redis.port}")
	int port;

	@Bean
	JedisPool jedisPool() {
		log.info("Connecting JedisPool to Redis " + host + ":"+port);
		return new JedisPool(host, port);
	}


/*	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		final RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
		template.setConnectionFactory(jedisConnectionFactory());
		template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));
		return template;
	}
*/
	public static void main(String[] args) {
		SpringApplication.run(RedisbankPlacesApplication.class, args);
	}


}
