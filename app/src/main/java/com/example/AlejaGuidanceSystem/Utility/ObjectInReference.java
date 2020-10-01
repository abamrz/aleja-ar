package com.example.AlejaGuidanceSystem.Utility;

import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;

public class ObjectInReference {
	private Pose poseInReference;
	private AnchorNode node;


	public ObjectInReference(AnchorNode node, Pose p) {
		this.node = node;
		this.poseInReference = p;
	}

	public Pose getPoseInReference() {
		return poseInReference;
	}

	public AnchorNode getNode() {
		return node;
	}

	public void setPoseInReference(Pose poseInReference) {
		this.poseInReference = poseInReference;
	}

	public void setNode(AnchorNode node) {
		this.node = node;
	}

	public void recalculatePosition(Pose referenceToWorld) {
		Pose nodePose = referenceToWorld.compose(this.poseInReference);
		VectorOperations.applyPoseToAnchorNode(this.node, nodePose);
	}

	public void setEnable(boolean enable){
		if (enable){
			this.node.getScene().addChild(this.node);
		}
		else {
			this.node.getScene().removeChild(this.node);
		}
	}
}
