package v4;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashSet;

public class Exhaustive {
	public int operation;
	public Cost cost;
	public int[] originalVNFs;
	public int[] optimalVNFs;
	public double originalCost;
	public double optimalCost;
	
	public Exhaustive(int operation, Cost cost) {
		this.operation = operation;
		this.cost      = cost;
		if (operation == 0) {
			System.out.println("Exhaustive Method To Placement");
		} else {
			System.out.println("Exhaustive Method To Migration");
		}
		
	}
	
	public double execute(FatTree ft,  int[] originalVNFs, 
			int[] optimalVNFs, double originalCost, double optimalCost) {
		
		this.originalVNFs = originalVNFs;
		this.optimalVNFs  = optimalVNFs;
		this.originalCost = originalCost;
		this.optimalCost  = optimalCost;
		
		int index = 0;
		HashSet<Integer> used = new HashSet();
		help(ft, index, used);
		return this.optimalCost;
	}
	
	public void help(FatTree ft, int index, HashSet<Integer> used) {
		if (index == this.optimalVNFs.length) {
			optimize(ft);
			return;
		}
		
		for (int i = 0; i < ft.switchesTable.length; i++) {
			if (!used.contains(i)) {
				ft.VNFs[index] = ft.switchesTable[i];
				used.add(i);
				help(ft, index + 1, used);
				used.remove(i);
			}
		}
	}
	
	public void optimize(FatTree ft) {
		double operationCost = this.operation == 0? 0: cost.migrateCost(ft, this.originalVNFs);
		double positionCost  = cost.calculateTotalCost(ft);
		double cost          = positionCost + operationCost;
		
		if (cost < this.optimalCost) {
			this.optimalCost = cost;
			for (int i = 0; i < this.optimalVNFs.length; i++) {
				this.optimalVNFs[i] = ft.VNFs[i];
			}
		}
	}
	
	public static void main(String[] args) {
	}

}
