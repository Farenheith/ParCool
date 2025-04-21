package com.alrex.parcool.utilities;

public class MathUtil {
	public static float squaring(float value) {
		return value * value;
	}

	public static float lerp(float start, float end, float factor) {
		return start + (end - start) * factor;
	}

	public static float normalizeDegree(float angle) {
		return (float) (angle - 360f * Math.floor((angle + 180f) / 360f));
	}
}
