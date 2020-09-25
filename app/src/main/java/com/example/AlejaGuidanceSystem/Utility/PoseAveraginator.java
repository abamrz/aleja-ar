package com.example.AlejaGuidanceSystem.Utility;

import com.google.ar.core.Pose;

import java.util.ArrayList;
import java.util.LinkedList;

public class PoseAveraginator {
	private LinkedList<Pose> list = new LinkedList<>();
	private int maxSize;
	private Long lastRecorded = null;

	public PoseAveraginator(int size) {
		this.maxSize = size;
	}

	public Pose add(Pose newPose) {
		/*if(lastRecorded != null && lastRecorded < System.currentTimeMillis() + 20) {
			this.list.removeLast();
			this.list.add(newPose);
			return VectorOperations.averagePoses(list);
		}*/

		lastRecorded = System.currentTimeMillis();

		list.add(newPose);
		if (list.size() > maxSize)
			list.removeFirst();

		return VectorOperations.averagePoses(list);
	}
}
