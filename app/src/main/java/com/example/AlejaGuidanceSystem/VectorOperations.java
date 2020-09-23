package com.example.AlejaGuidanceSystem;

import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

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

	public static float nearestPointToLine(float[] start, float[] end, float[] point) {
		float[] dir = v3diff(end, start);
		float len = v3length(dir);
		dir = v3normalize(dir);
		float[] dirToPoint = v3diff(point, start);
		return v3dot(dir, dirToPoint) / len;
	}

	public static float[]v3interpolate(float[] start, float[] end, float f) {
		return v3add(v3mulf(start, 1 - f), v3mulf(end, f));
	}

	public static Vector3 vectorFromArray(float[] v) {
		return  new Vector3(v[0], v[1], v[2]);
	}

	public static Quaternion quaternionFromArray(float[] v) {
		return  new Quaternion(v[0], v[1], v[2], v[3]);
	}

	public static void applyPoseToAnchorNode(AnchorNode node, Pose pose) {
		node.setWorldPosition(VectorOperations.vectorFromArray(pose.getTranslation()));
		node.setWorldRotation(VectorOperations.quaternionFromArray(pose.getRotationQuaternion()));

	}
}
