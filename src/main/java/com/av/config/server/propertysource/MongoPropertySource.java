package com.av.config.server.propertysource;

import java.util.LinkedHashMap;
import java.util.Map;

public class MongoPropertySource {
	
	private String profile;
	private Map<String, Object> source = new LinkedHashMap<>();

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public Map<String, Object> getSource() {
		return source;
	}

	public void setSource(Map<String, Object> source) {
		this.source = source;
	}


}
