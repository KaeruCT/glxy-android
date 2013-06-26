package com.kaeruct.glxy.model;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.graphics.Color;

public class TrailParticle extends Circle {
	public Color color;
	private float rsqrt;
	
	public TrailParticle (float x, float y, float radius, Color color) {
		super(x, y, radius);
		this.color = color;
		rsqrt = (float) Math.sqrt(radius);
	}
	
	public void set(float x, float y, float radius, Color color) {
		set(x, y, radius);
		this.color = color;
	}

	public void shrink() {
		radius -= rsqrt * 0.2f;
	}
}