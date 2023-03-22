package com.av.config.server.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

import com.av.config.server.propertysource.MongoPropertySource;

@Configuration
public class MongoDBEnvironmentRepository implements EnvironmentRepository, Ordered {

	private static final String PROFILE = "profile";
	private static final String APPLICATION = "application";
	private static final String DEFAULT = "default";
	private static final String COLLECTION_NAME = "service_config";
	private static final String COMMON_CONFIG = "common-config";
	private static final String DEFAULT_PROFILE = null;

	@Autowired
	private MongoTemplate template;
	
	private MapFlattener mapFlattener;
	
	private int order = Ordered.LOWEST_PRECEDENCE;


	public MongoDBEnvironmentRepository() {
		this.mapFlattener = new MapFlattener();
	}

	@Override
	public Environment findOne(String applicationName, String profile, String label) {
		String[] profilesArr = StringUtils.commaDelimitedListToStringArray(profile);
		List<String> profiles = new ArrayList<>(Arrays.asList(profilesArr.clone()));
		for (int i = 0; i < profiles.size(); i++) {
			if (DEFAULT.equals(profiles.get(i))) {
				profiles.set(i, DEFAULT_PROFILE);
			}
		}
		profiles.add(DEFAULT_PROFILE); // Default configuration will have 'null' profile
		profiles = sortedUnique(profiles);

		Query query = new Query();
		query.addCriteria(Criteria.where(PROFILE).in(profiles.toArray())).
			addCriteria(Criteria.where(APPLICATION).in(Arrays.asList(applicationName, COMMON_CONFIG)));

		Environment environment;
		try {
			List<MongoPropertySource> sources = template.find(query, MongoPropertySource.class, COLLECTION_NAME);
			sortSourcesByProfile(sources, profiles);
			environment = new Environment(applicationName, profilesArr, label, null, null);
			for (MongoPropertySource propertySource : sources) {
				String prof = propertySource.getProfile() != null ? propertySource.getProfile() : DEFAULT;
				String sourceName = String.format("%s-%s", propertySource.getApplication(), prof);
				Map<String, Object> flatSource = mapFlattener.flatten(propertySource.getSource());
				PropertySource propSource = new PropertySource(sourceName, flatSource);
				environment.add(propSource);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Cannot load environment", e);
		}

		return environment;
	}

	private ArrayList<String> sortedUnique(List<String> values) {
		return new ArrayList<>(new LinkedHashSet<>(values));
	}

	private void sortSourcesByProfile(List<MongoPropertySource> sources, final List<String> profiles) {
		Collections.sort(sources, (MongoPropertySource s1, MongoPropertySource s2) -> {
			int i1 = profiles.indexOf(s1.getProfile());
			int i2 = profiles.indexOf(s2.getProfile());
			return Integer.compare(i1, i2);
		});
	}

	private static class MapFlattener extends YamlProcessor {
		public Map<String, Object> flatten(Map<String, Object> source) {
			return getFlattenedMap(source);
		}
	}

	@Override
	public int getOrder() {
		return order;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
}
