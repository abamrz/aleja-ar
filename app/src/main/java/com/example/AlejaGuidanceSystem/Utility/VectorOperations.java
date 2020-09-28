package com.example.AlejaGuidanceSystem.Utility;

import android.util.Log;

import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.schemas.lull.Quat;

import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

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

	public static float[] createQuaternionFromAxisAngle(float xx, float yy, float zz, float a)
	{
		// Here we calculate the sin( theta / 2) once for optimization
		float factor = (float) Math.sin( a / 2.0 );

		// Calculate the x, y and z of the quaternion
		float x = xx * factor;
		float y = yy * factor;
		float z = zz * factor;

		// Calcualte the w value by cos( theta / 2 )
		float w = (float)Math.cos( a / 2.0 );

		return new float[]{x, y, z, w};
	}


	public static float[] quatFromMatrix3f(SimpleMatrix m) {
		assert(!isReflectionMatrix(m));

		return quatFromMatrix3f(
				(float) m.get(0, 0),
				(float) m.get(0, 1),
				(float) m.get(0, 2),
				(float) m.get(1, 0),
				(float) m.get(1, 1),
				(float) m.get(1, 2),
				(float) m.get(2, 0),
				(float) m.get(2, 1),
				(float) m.get(2, 2)
		);
	}

	private static float[] quatFromMatrix3f(float m00, float m01, float m02, float m10, float m11,
										   float m12, float m20, float m21, float m22) {
		// Use the Graphics Gems code, from
		// ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z
		// *NOT* the "Matrix and Quaternions FAQ", which has errors!

		// the trace is the sum of the diagonal elements; see
		// http://mathworld.wolfram.com/MatrixTrace.html
		float t = m00 + m11 + m22;
		float x,y,z,w;

		// we protect the division by s by ensuring that s>=1
		if (t >= 0) { // |w| >= .5
			float s = (float)Math.sqrt(t + 1); // |s|>=1 ...
			w = 0.5f * s;
			s = 0.5f / s; // so this division isn't bad
			x = (m21 - m12) * s;
			y = (m02 - m20) * s;
			z = (m10 - m01) * s;
		} else if ((m00 > m11) && (m00 > m22)) {
			float s = (float)Math.sqrt(1.0f + m00 - m11 - m22); // |s|>=1
			x = s * 0.5f; // |x| >= .5
			s = 0.5f / s;
			y = (m10 + m01) * s;
			z = (m02 + m20) * s;
			w = (m21 - m12) * s;
		} else if (m11 > m22) {
			float s = (float)Math.sqrt(1.0f + m11 - m00 - m22); // |s|>=1
			y = s * 0.5f; // |y| >= .5
			s = 0.5f / s;
			x = (m10 + m01) * s;
			z = (m21 + m12) * s;
			w = (m02 - m20) * s;
		} else {
			float s = (float)Math.sqrt(1.0f + m22 - m00 - m11); // |s|>=1
			z = s * 0.5f; // |z| >= .5
			s = 0.5f / s;
			x = (m02 + m20) * s;
			y = (m21 + m12) * s;
			w = (m10 - m01) * s;
		}
		return new float[]{x,y,z,w};
	}

	public static SimpleMatrix vec3(double x, double y, double z) {
		SimpleMatrix sm = new SimpleMatrix(3, 1);
		sm.set(0, x);
		sm.set(1, y);
		sm.set(2, z);
		return sm;
	}

	public static class TransformationResult {
		public SimpleMatrix R;
		public SimpleMatrix t;

		public SimpleMatrix applyTo(SimpleMatrix v3) {
			return R.mult(v3).plus(t);
		}
	}

	public static TransformationResult findGoodTransformation(List<SimpleMatrix> xi, List<SimpleMatrix> pi) {
		assert(xi.size() == pi.size());

		SimpleMatrix ux = new SimpleMatrix(3, 1);
		SimpleMatrix up = new SimpleMatrix(3, 1);

		for(int i = 0; i < xi.size(); i++) {
			ux = ux.plus(xi.get(i));
			up = up.plus(pi.get(i));
		}

		ux = ux.divide((double)xi.size());
		up = up.divide((double)xi.size());

		SimpleMatrix sum = new SimpleMatrix(3, 3);
		for(int i = 0; i < pi.size(); i++) {
			SimpleMatrix xiv = xi.get(i).minus(ux);
			SimpleMatrix piv = pi.get(i).minus(up);
			sum = sum.plus(xiv.mult(piv.transpose()));
		}

		SimpleSVD<SimpleMatrix> svd = sum.svd();
		System.out.println(Arrays.toString(svd.getSingularValues()));
		SimpleMatrix R = svd.getU().mult(svd.getV().transpose());
		SimpleMatrix t = ux.minus(R.mult(up));

		TransformationResult tr = new TransformationResult();
		tr.R = R;
		tr.t = t;
		return tr;
	}

	public static SimpleMatrix crossProduct(SimpleMatrix a, SimpleMatrix b) {
		double ax = a.get(0);
		double ay = a.get(1);
		double az = a.get(2);

		double bx = b.get(0);
		double by = b.get(1);
		double bz = b.get(2);

		return vec3(
				ay * bz - az * by,
				az * bx - ax * bz,
				ax * by - ay * bx
		);
	}

	public static boolean isReflectionMatrix(SimpleMatrix m) {
		SimpleMatrix x = m.extractVector(false, 0);
		SimpleMatrix y = m.extractVector(false, 1);
		SimpleMatrix z = m.extractVector(false, 2);

		SimpleMatrix xy = crossProduct(x, y);

		return xy.elementMult(z).elementSum() > 0 ? false : true;
	}


	public static Pose poseFromTransformationResult(TransformationResult tr) {
		float[] translation = new float[] { (float)tr.t.get(0), (float)tr.t.get(1), (float)tr.t.get(2)  };

		SimpleMatrix forward = vec3(0, 0, -1);
		SimpleMatrix up = vec3(0, 1, 0);

		float[] forward1 = v3matTo3f(tr.R.mult(forward));
		float[] up1 = v3matTo3f(tr.R.mult(up));

		Quaternion quat = Quaternion.lookRotation(vectorFromArray(forward1), vectorFromArray(up1));
//		Vector3 a = Quaternion.rotateVector(quat, Vector3.up());
//		Log.d("Trans2Test", a.toString() + " " + Arrays.toString(up1));
//		Vector3 b = Quaternion.rotateVector(quat, Vector3.forward());
//		Log.d("Trans2Test", a.toString() + " " + Arrays.toString(forward1));
		float[] quatArray = new float[] { quat.x, quat.y, quat.z, quat.w };
		return Pose.makeTranslation(translation).compose(Pose.makeRotation(quatArray));
	}

	public static float[] v3matTo3f(SimpleMatrix v3) {
		return new float[] { (float)v3.get(0), (float)v3.get(1), (float)v3.get(2) };
	}

}
