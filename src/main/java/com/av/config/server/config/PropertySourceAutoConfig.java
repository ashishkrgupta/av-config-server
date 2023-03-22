package com.av.config.server.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.av.config.server.propertysource.CustomPropertySource;

@Configuration
public class PropertySourceAutoConfig {
	
	@Bean
	@ConditionalOnMissingBean(CustomPropertySource.class)
	CustomPropertySource environmentRepository(MongoTemplate template) {
		return new CustomPropertySource(template);
	}

}
