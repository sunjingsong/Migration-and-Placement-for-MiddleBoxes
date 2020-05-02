package v4;
import java.util.ArrayList;

public class Cost {
	public void endCosts(FatTree ft) {
		for (int pos: ft.switchesTable) {
			double start = 0, end = 0;
			for (Integer[] item : ft.pairs) {
				
				start += ft.costTable[pos][item[0]] * item[2];
				end   += ft.costTable[pos][item[1]] * item[2];
			}
			ft.start.put(pos, start);
			ft.end.put(pos, end);		
		}
	}
	
	public double calculateTotalCost(FatTree ft) {
		double totalCost = 0.0;
		double totalDistance = 0.0;
		
		totalCost += ft.start.get(ft.VNFs[0]);
		totalCost += ft.end.get(ft.VNFs[ft.VNFs.length - 1]);
		
		if (ft.VNFs.length > 0 && ft.VNFs[0] != 0) {
			for (int i = 0; i < ft.VNFs.length - 1; i++) {
				totalDistance += ft.costTable[ft.VNFs[i]][ft.VNFs[i + 1]];
			}
			
			totalCost += totalDistance * ft.totalFrequency;
			
			
		} 
		return totalCost;
	}
	
	public double migrateCost(FatTree ft, int[] oldVNFs) {
		double distance = 0.0;
		int len = oldVNFs.length;
		for (int i = 0; i < len; i++) {
			distance += ft.costTable[ft.VNFs[i]][oldVNFs[i]];
		}
		double cost = distance * ft.migrateCoefficient;
		return cost;
	}
	
	public double twoNodesMigrateCost(FatTree ft, int node1, int node2) {
		double distance = ft.costTable[node1][node2];
		double cost = distance * ft.migrateCoefficient;
		return cost;
	}
	
	public double printMigrateCost(FatTree ft, int[] oldVNFs) {
		double totalCost = 0.0;
		int len = oldVNFs.length;
		for (int i = 0; i < len; i++) {
			double cost =  ft.costTable[ft.VNFs[i]][oldVNFs[i]]* ft.migrateCoefficient;
			System.out.println("The migration cost of " + ft.indexNodeMap.get(ft.VNFs[i]) + " from " + ft.indexNodeMap.get(oldVNFs[i])
					+ " to " + ft.indexNodeMap.get(ft.VNFs[i]) + " is " + cost);
			totalCost += cost;
		}
		System.out.println("The total migration cost is: " + totalCost);
		return totalCost;
	}
}
