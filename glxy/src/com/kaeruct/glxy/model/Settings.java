package com.kaeruct.glxy.model;

import com.badlogic.gdx.utils.ArrayMap;

public class Settings extends ArrayMap<Settings.Setting, Boolean> {
	public enum Setting {
		TRAILS ("Show Particle Trails", true),
		COLLISION ("Collide / Absorb", true),
		PAUSED ("Paused", false);
		
		public final String description;
		public final boolean defaultVal;
		
		Setting(String d, boolean dv) {
			description = d;
			defaultVal = dv;
		}
	}
	
	public Settings() {
		for (Setting setting : Setting.values()) {
			put(setting, setting.defaultVal);
		}
	}
}