package v4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ShortestPathDP {
	public int operation;
	public Cost cost;
	public int[] originalVNFs;
	public int[] optimalVNFs;
	public double originalCost;
	public double optimalCost;
	public double[][] graph;
	public double minCostStart = Double.MAX_VALUE;
	public double minCostEnd = Double.MAX_VALUE;

	
	public ShortestPathDP(int operation, Cost cost) {
		this.operation = operation;
		this.cost      = cost;
		if (operation == 0) {
			System.out.println("Shortest Path Dynamic To Placement");
		} else {
			System.out.println("Shortest Path Dynamic Shortest Path To Migration");
		}
	}
	
	public double execute(FatTree ft,  int[] originalVNFs, 
		int[] optimalVNFs, double originalCost, double optimalCost) {
		this.originalVNFs = originalVNFs;
		this.optimalVNFs  = optimalVNFs;
		this.originalCost = originalCost;
		this.optimalCost  = optimalCost;
		createGraph(ft);
		findTwoEnds(ft);
		shortestPath(ft);
		return this.optimalCost;
	}
	
	public void shortestPath(FatTree ft) {
		int u = ft.VNFs[0]; 
		int v = ft.VNFs[ft.VNFs.length - 1]; 
		int k = ft.VNFs.length - 1;
		int len = graph.length;
		
		double[][][][] dp = new double[graph.length][graph.length][k + 1][4];
		
		for (int e = 0; e <= k; e++) {// edges
			for (int i = 0; i < len; i++) {// sources
				for (int j = 0; j < len; j++) {// destinations
					dp[i][j][e] = new double[] {i, j, 0, Double.MAX_VALUE};
					
					// Base cases
					if (e == 0 && i == j) {
						dp[i][j][e][2] = (int)dp[i][j][e][2] | 1 << j;
						dp[i][j][e][3] = 0;
					}
					if (e == 1) {
						dp[i][j][e][2] = (int)dp[i][j][e][2] | 1 << i;
						dp[i][j][e][2] = (int)dp[i][j][e][2] | 1 << j;
						dp[i][j][e][3] = graph[i][j];
					}
					
					if (e > 1) {
						for (int a = 0; a < len; a++) {
							if (i != a && j != a && dp[i][j][e - 1][3] != Double.MAX_VALUE) {
								double migrateCost = this.operation == 0? 
										0: cost.twoNodesMigrateCost(ft, this.originalVNFs[e], ft.switchesTable[a]);
								// dp[0][j][e - 1]: 0->j with e - 1 edges, dp[1][j][e - 1]: 1->j, 2->j, ..., len - 1->j
								if (dp[i][j][e][3] > graph[i][a] + dp[a][j][e - 1][3] + migrateCost &&
										((int)dp[a][j][e - 1][2] & (1 << i)) == 0) {
									int bit = (int)dp[a][j][e - 1][2] | (1 << i);
									dp[i][j][e] =  new double[]{a, j, bit, graph[i][a] + dp[a][j][e - 1][3] + migrateCost};
								}
								
							}
						}
					}
				}
			}
		}
		//System.out.println("Value is " + dp[u][v][k][3]);
		this.optimalCost = dp[u][v][k][3] + this.minCostStart + this.minCostEnd;
		int i = u;
		int j = v;
		int e = k;
		while (e != 1) {
		
			i = (int)dp[i][j][e][0];
			j = (int)dp[i][j][e][1];
			ft.VNFs[e] = ft.switchesTable[i];
			e--;
			
		}
	}
	
	
	public void findTwoEnds(FatTree ft) {
		// Find start point with smallest cost
		int posStart = 0;
		for (Map.Entry<Integer, Double> entry: ft.start.entrySet()) {
			if (entry.getValue() < this.minCostStart) {
				posStart = entry.getKey();
				this.minCostStart = entry.getValue();
			}
		}
		ft.VNFs[0] = posStart;
				
				
		// Find end point with smallest cost
		int posEnd = 0;
		for (Map.Entry<Integer, Double> entry: ft.end.entrySet()) {
			if (entry.getValue() < this.minCostEnd && !entry.getKey().equals(posStart)) {
				posEnd = entry.getKey();
				this.minCostEnd = entry.getValue();
		    }
	    }
		ft.VNFs[ft.VNFs.length - 1] = posEnd;
		
	}
	public void createGraph(FatTree ft) {
		graph = new double[ft.switchesTable.length][ft.switchesTable.length];
		for (int i = 0; i < ft.switchesTable.length; i++) {
			for (int j = 0; j < ft.switchesTable.length; j++) {
				graph[i][j] = ft.costTable[ft.switchesTable[i]][ft.switchesTable[j]] * ft.totalFrequency;
			}
		}
	}

}
