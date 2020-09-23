package com.example.AlejaGuidanceSystem;

public class VectorOperations {
	public static float[] v3diff(float[] a, float[] b) {
		return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
	}

	public static float[] v3add(float[] a, float[] b) {
		return new float[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
	}

	public static float[] v3div(float[] a, float f) {
		return new float[]{a[0] / f, a[1] / f, a[2] / f};
	}

	public static float[] v3mulf(float[] a, float f) {
		return new float[]{a[0] * f, a[1] * f, a[2] * f};
	}


	public static float v3dot(float[] a, float[] b) {
		return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
	}

	public static float v3dist(float[] a, float[] b) {
		float[] dif = v3diff(a, b);
		return (float) Math.sqrt(v3dot(dif, dif));
	}

	public static float v3length(float[] a) {
		return (float) Math.sqrt(v3dot(a, a));
	}

	public static float[] v3normalize(float[] a) {
		float length = v3length(a);
		return v3div(a, length + 0.001f);
	}
}
