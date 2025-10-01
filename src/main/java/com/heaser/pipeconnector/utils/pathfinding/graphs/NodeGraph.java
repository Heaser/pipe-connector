package com.heaser.pipeconnector.utils.pathfinding.graphs;

import com.heaser.pipeconnector.utils.NodeParameter;
import com.heaser.pipeconnector.utils.pathfinding.PathfindingUtils;
import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Kruskal's algorithm
public class NodeGraph {
    public static class Edge implements Comparable<Edge> {
        public BlockPos nodePosition, nextNodePosition;
        public double cost;

        Edge(BlockPos nodePosition, BlockPos nextNodePosition, double cost) {
            this.nodePosition = nodePosition;
            this.nextNodePosition = nextNodePosition;
            this.cost = cost;
        }

        @Override
        public int compareTo(Edge other) {
            return Double.compare(this.cost, other.cost);
        }
    }

    private List<BlockPos> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    public NodeGraph(List<NodeParameter> nodeParameters) {
        nodes = nodeParameters.stream().map((node) -> node.getRelativePosition()).toList();
        edges = new ArrayList<>();
        for (BlockPos nodePosition : nodes) {
            for (BlockPos nextNodePosition : nodes) {
                if (nextNodePosition != null && !nodePosition.equals(nextNodePosition)) {
                    int cost = PathfindingUtils.getCost(nodePosition, nextNodePosition);
                    edges.add(new Edge(nodePosition, nextNodePosition, cost));
                }
            }
        }
    }

    static class UnionFind {
        private int[] parent, rank;

        UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
            }
        }

        int find(int p) {
            if (p != parent[p]) {
                parent[p] = find(parent[p]);
            }
            return parent[p];
        }

        void union(int p, int q) {
            int rootP = find(p), rootQ = find(q);
            if (rootP == rootQ) return;

            if (rank[rootP] < rank[rootQ]){
                parent[rootP] = rootQ;
            }
            else if (rank[rootP] > rank[rootQ]) {
                parent[rootQ] = rootP;
            }
            else {
                parent[rootQ] = rootP;
                rank[rootP]++;
            }
        }
    }

    public List<Edge> createPath() {
        List<Edge> result = new ArrayList<>();
        Collections.sort(edges);

        UnionFind uf = new UnionFind(nodes.size());
        for (Edge edge : edges) {
            int nodeIndex = nodes.indexOf(edge.nodePosition);
            int nextNodeIndex = nodes.indexOf(edge.nextNodePosition);
            if (uf.find(nodeIndex) != uf.find(nextNodeIndex)) {
                result.add(edge);
                uf.union(nodeIndex, nextNodeIndex);
            }
        }
        return result;
    }
}