package v4;
import java.util.ArrayList;
import java.util.Scanner;
/* Controller class */
public class Controller {
	public int operation;
	public FatTree ft;
	public Distance dis;
	public Cost cost;
	public int[] originalVNFs;
	public int[] optimalVNFs;
	public double originalCost;
	public double optimalCost;
	
	public Controller() {
		this.operation = input();
		this.ft = new FatTree();
		if (this.operation == 1) {
			this.ft.setMigrationCoefficient();
		}
		this.dis = new Distance(this.ft);
		this.cost = new Cost();
		this.originalVNFs = new int[ft.VNFs.length];
		this.optimalVNFs  = new int[ft.VNFs.length];	
		System.out.println(this.ft);
		
		cost.endCosts(this.ft);
		this.originalCost = cost.calculateTotalCost(this.ft);
		this.optimalCost = originalCost;
		
		// Copy initial ft VNFs to originalVNFs and optimalVNFs
		for (int i = 0; i < this.ft.VNFs.length; i++) {
			this.originalVNFs[i] = this.ft.VNFs[i];
			this.optimalVNFs[i] = this.ft.VNFs[i];
		}
		
		// migrationAndPlacement(this.ft, this.operation);
	}
	
	public void exhaustiveAlgorithm() {
		double tmp = this.optimalCost;
		Exhaustive ex = new Exhaustive(this.operation, this.cost);
		beforeOptimizing(ft);
		this.optimalCost = ex.execute(ft, this.originalVNFs, this.optimalVNFs, this.originalCost, this.optimalCost);
		afterOptimizing(ft);
		this.optimalCost = tmp;
		for (int i = 0; i < this.originalVNFs.length; i++) {
			this.optimalVNFs[i] = this.originalVNFs[i];
		}
	}
	
	public void DPAlgorithm() {
		double tmp = this.optimalCost;
		ShortestPathDP spd = new ShortestPathDP(this.operation, this.cost);
		beforeOptimizing(ft);
		this.optimalCost = spd.execute(ft, this.originalVNFs, this.optimalVNFs, this.originalCost, this.optimalCost);
		afterOptimizing(ft);
		this.optimalCost = tmp;
		for (int i = 0; i < this.originalVNFs.length; i++) {
			this.optimalVNFs[i] = this.originalVNFs[i];
		}
	}
	
	public void executeMigrationAndPlacement() {
		Scanner scan = new Scanner(System.in);
		System.out.println("Please select the alogrithm(0-exahaustive, 1-DP, 2-both): ");
		int select = scan.nextInt();
		if (select == 0) {
			exhaustiveAlgorithm();
		} else if (select == 1) {
			DPAlgorithm();
		} else {
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			exhaustiveAlgorithm();
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			DPAlgorithm();
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		}
		
		
	}
	public int input() {
		Scanner scan = new Scanner(System.in);
		System.out.println("What operation do you want? 0-placement, 1-migration");
		return scan.nextInt();
	}
	
	public void beforeOptimizing(FatTree ft) {
		if (this.operation == 0) return;
		String str = "Before Migration:\n";
		str += "The random position of VNF is: \n";
		for (int i = 0; i < this.originalVNFs.length; i++) {
			str += "VNF" + i +  ": " + ft.indexNodeMap.get(this.originalVNFs[i]) + "\n";
		}
		str += "The original communication cost is " + this.originalCost + "\n";
		str += "----------------------------------------------\n";
		System.out.println(str);
	}
	
	public void afterOptimizing(FatTree ft) {
		String str = "";
		if (this.operation == 0) {
			str += "After Placement:\n";
		} else {
			str += "After Migration:\n";
		}
		str += "The optimize VNFs chain is as below:\n";
		for (int i = 0; i < this.optimalVNFs.length; i++) {
			str += "VNF" + i +  ": " + ft.indexNodeMap.get(this.optimalVNFs[i]) + "\n";
		}
		System.out.print(str);
		double migrateCost = this.operation == 0? 0: cost.printMigrateCost(ft, this.originalVNFs);
		System.out.println("The communication cost is: " + (this.optimalCost - migrateCost));
		System.out.println("----------------------------------------------");
	}
	
	public static void main(String[] args) {
		System.out.println("Project for simulating VNFs migration and placement in FatTree");
		System.out.println("==============================================================");
		Controller c = new Controller();
		c.executeMigrationAndPlacement();
	}
}
