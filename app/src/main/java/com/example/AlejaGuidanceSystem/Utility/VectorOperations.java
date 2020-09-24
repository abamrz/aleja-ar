package com.example.AlejaGuidanceSystem.Utility;

import android.util.Log;

import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VectorOperations {
	public static float[] v3diff(float[] a, float[] b) {
		return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
	}

	public static float[] v3add(float[] a, float[] b) {
		return new float[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
	}

	public static float[] v4add(float[] a, float[] b) {
		return new float[]{a[0] + b[0], a[1] + b[1], a[2] + b[2], a[3] + b[3]};
	}

	public static float[] v3div(float[] a, float f) {
		return new float[]{a[0] / f, a[1] / f, a[2] / f};
	}

	public static float[] v4div(float[] a, float f) {
		return new float[]{a[0] / f, a[1] / f, a[2] / f, a[3] / f};
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
		node.setWorldRotation(VectorOperations.quaternionFromArray(pose.getRotationQuaternion()));
		node.setWorldPosition(VectorOperations.vectorFromArray(pose.getTranslation()));
	}

	public static Pose averagePoses(List<Pose> poses) {
		List<Integer> num = new ArrayList<Integer>();
		for (int i = 0; i < poses.size(); i++) num.add(1);

		while (poses.size() > 1) {
			ArrayList<Pose> newPoses = new ArrayList<>();
			ArrayList<Integer> newNum = new ArrayList<>();

			for (int i = 0; i < poses.size(); i += 2) {
				int i1 = i + 0;
				int i2 = i + 1;

				if (i2 < poses.size()) {
					Pose interpolated = Pose.makeInterpolated(poses.get(i1), poses.get(i2), (float) num.get(i1) / ((float) num.get(i1) + (float) num.get(i2)));
					newPoses.add(interpolated);
					newNum.add(num.get(i1) + num.get(i2));
				} else {
					newPoses.add(poses.get(i1));
					newNum.add(num.get(i1));
				}
			}

			poses = newPoses;
			num = newNum;
		}

		return poses.get(0);
	}
}
