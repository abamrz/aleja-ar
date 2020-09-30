package com.example.AlejaGuidanceSystem.Graph;

import com.example.AlejaGuidanceSystem.Utility.VectorOperations;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.Serializable;


public class ARGraph extends SimpleWeightedGraph<Node, DefaultWeightedEdge> implements Serializable {
	private String name = "schlabber";

	public ARGraph() {
		super(DefaultWeightedEdge.class);
	}

	public ARGraph(ARGraph src) {
		this();
		Graphs.addGraph(this, src);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * super.addVertex-method throws a "No loops allowed"-exception if two equal Nodes have an edge between them
	 * this methods avoids two nodes with the same coordinates being added to the graph
	 */
/*	@Override
	public boolean addVertex(Node node) {
		if(super.vertexSet().stream().anyMatch(n -> n.getX() == node.getX() && n.getY() == node.getY()  &&
				n.getZ() == node.getZ())) {
			return false;
		}
		return super.addVertex(node);
	}*/

	/**
	 * if graph does not contain both nodes, the super.addEdge-method will throw an exception.
	 * To avoid this problem, this method checks, whether safe usage is possible.
	 */
/*	@Override
	public DefaultWeightedEdge addEdge(Node source, Node sink) {
		if(this.containsVertex(source) && this.containsVertex(sink)) {
			return super.addEdge(source, sink);
		}
		return null;
	}*/


	public static class NearestPointInfo {
		public float distance;
		public DefaultWeightedEdge bestEdge;
		public float interpolatingFactor;
		public float[] nearestPosition;
	}

	public NearestPointInfo nearestPointInGraph( float[] probePosition) {

		float shortestDist = Float.MAX_VALUE;
		DefaultWeightedEdge bestEdge = null;
		float bestInterpolatingFactor = 0;
		float[] nearestPosition = null;

		for(DefaultWeightedEdge e : this.edgeSet()) {
			Node source = this.getEdgeSource(e);
			Node target = this.getEdgeTarget(e);

			float[] sourcePos = source.getPositionF();
			float[] targetPos = target.getPositionF();

			float f = VectorOperations.nearestPointToLine(sourcePos, targetPos, probePosition);
			if(f < 0) f = 0;
			if(f > 1) f = 1;

			float[] pos = VectorOperations.v3interpolate(sourcePos, targetPos, f);

			float dist = VectorOperations.v3dist(probePosition, pos);
			if(bestEdge == null || dist < shortestDist) {
				bestEdge = e;
				shortestDist = dist;
				nearestPosition = pos;
				bestInterpolatingFactor = f;
			}
		}

		if(nearestPosition == null) return null;

		NearestPointInfo nearestPointInfo = new NearestPointInfo();
		nearestPointInfo.bestEdge = bestEdge;
		nearestPointInfo.distance = shortestDist;
		nearestPointInfo.interpolatingFactor = bestInterpolatingFactor;
		nearestPointInfo.nearestPosition = nearestPosition;
		return nearestPointInfo;
	}
}
