package com.example.AlejaGuidanceSystem;

import com.example.AlejaGuidanceSystem.graph.Node;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;


public class ARGraph extends SimpleWeightedGraph<Node, DefaultWeightedEdge> {

	public ARGraph() {
		super(DefaultWeightedEdge.class);
	}

	public ARGraph(ARGraph src) {
		this();
		Graphs.addGraph(this, src);
	}


	public GraphPath<Node, DefaultWeightedEdge> calculateShortestPath( Node sink) {


		return null;
	}


	public static class NearestPointInfo {
		float distance;
		DefaultWeightedEdge bestEdge;
		float interpolatingFactor;
		float[] nearestPosition;
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
