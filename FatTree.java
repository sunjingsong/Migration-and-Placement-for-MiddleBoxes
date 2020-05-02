package v4;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Random;

public class FatTree {
	// Data fields
	// The structure of the tree
	public ArrayList<String> coreSwitches; // core switches
	public ArrayList<String> aggrSwitches; // aggregation switches
	public ArrayList<String> edgeSwitches; // edge switches
	public ArrayList<String> allSwitches; // allSwitches = core + aggregation + edge
	public ArrayList<String> allPositions; // core + aggregation + edge + hosts
	public ArrayList<String> pods; // pods
	public ArrayList<String> hosts; // physical machines
	public HashMap<String, String> links; // the link between two nodes
	public int numberOfPorts;   // number of ports
	public int numberOfPods;    // Pod number in Fat Tree
	public int coreSwitchNumber; // The number of core switches
	public int aggrSwitchNumber; // The number of aggregation switches
	public int edgeSwitchNumber; // The number of edge switches
	public int hostNum;          // The number of hosts
	
	// The services of the tree
	public ArrayList<Integer[]> pairs;  // the communication pairs
	public int[] VNFs;   // middle boxes
	public double migrateCoefficient;   // migrate coefficient
	public int totalFrequency;      // the total communication frequency along the VNFs chain
	
	public HashMap<Integer, Double> start; // {Possible first VNF position: total cost of hosts->1st VNF}
	public HashMap<Integer, Double> end;   // {Possible last VNF position: total cost of last VNF->hosts}
	public HashMap<String, Double> distance; // {node1 + node2: the distance of two nodes}
	public double[][] costTable;
	public int[] switchesTable;
	public HashMap<Integer, String> indexNodeMap;
	public HashMap<String, Integer> nodeIndexMap;
	public HashMap<String, String> hostEdgeMap;
	
	/** Initialize the topology */
	public FatTree() {
		// Initial variables
		initialVariables();
		createTopo(); // Create a topology tree
		generate_VM_pairs(); // Generate VM pairs
		Distance ds = new Distance(this); // Calculate the distance
		generateVNFs(); // Generate VNFs
		// setMigrationCoefficient(); // set migration coefficient
	}
	
	public void initialVariables() {
		this.coreSwitches  = new ArrayList(); // core switches
		this.aggrSwitches  = new ArrayList();// aggregation switches
		this.edgeSwitches  = new ArrayList(); // edge switches
		this.allSwitches   = new ArrayList(); // core + aggregation + edge
		this.allPositions  = new ArrayList(); // core + aggregation + edge + hosts
		this.pods          = new ArrayList(); // pods
		this.hosts         = new ArrayList(); // physical machines
		this.links         = new HashMap(); // the link between two nodes
		this.indexNodeMap  = new HashMap();
		this.nodeIndexMap  = new HashMap();     
		this.hostEdgeMap   = new HashMap();
		
		// The services of the tree
		migrateCoefficient = 0.0;   // migrate coefficient
		totalFrequency     = 0;      // the total communication frequency along the VNFs chain
		
		start              = new HashMap(); // {Possible first VNF position: total cost of hosts->1st VNF}
		end                = new HashMap();   // {Possible last VNF position: total cost of last VNF->hosts}
		distance           = new HashMap(); // {node1 + node2: the distance of two nodes}
	}
	
	/** Create a fat tree based on the number of ports from user input */
	public void createTopo() {
		Scanner scan = new Scanner(System.in);
		System.out.println("Please Enter The Number of Ports: ");
		this.numberOfPorts = scan.nextInt();
		while (this.numberOfPorts % 2 == 1) {
			System.out.println("Illegal input, please enter the number of ports: ");
			this.numberOfPorts = scan.nextInt();
		}
	    
		int k = this.numberOfPorts;
		
		this.numberOfPods = k;
		this.coreSwitchNumber = (int)Math.pow(k/2, 2);
		this.aggrSwitchNumber = (int)(k * k / 2);
		this.edgeSwitchNumber = (int)(k * k / 2);
		this.hostNum          = (int)(k * Math.pow(k / 2, 2));
		
		// Generate core switches
		for (int i = 0; i < this.coreSwitchNumber; i++) {
			this.coreSwitches.add("cs" + i);
			this.allSwitches.add("cs" + i);
			this.allPositions.add("cs" + i);
		}
			
		// Traversal each pod
		for (int j = 0; j < this.numberOfPods; j++) {
			this.pods.add("po" + j);
			// Aggregation switches
			for (int l = 0; l < (int)(this.aggrSwitchNumber / this.numberOfPods); l++) {
				String aggrSwitch = "as" + j + "_" +  l;
				this.aggrSwitches.add(aggrSwitch);
				this.allSwitches.add(aggrSwitch);
				this.allPositions.add(aggrSwitch);
				// Each pod contains k / 2 aggregation switches
				for (int m =  (int)(k * l / 2); m < (int)(k * (l + 1) / 2); m++) {
					this.links.put(aggrSwitch, this.coreSwitches.get(m));
				}
			}
			
			// Edge switches
			for (int l = 0; l < (int)(this.edgeSwitchNumber / this.numberOfPods); l++) {
				String edgeSwitch = "es" + j + "_" +  l;
				this.edgeSwitches.add(edgeSwitch);
				this.allSwitches.add(edgeSwitch);
				this.allPositions.add(edgeSwitch);
				// Each pod contains k / 2 edge switches
				for (int m =  (int)(this.edgeSwitchNumber * j / this.numberOfPods); 
						m < (int)(this.edgeSwitchNumber * (j + 1) / this.numberOfPods); m++) {
					this.links.put(edgeSwitch, this.aggrSwitches.get(m));
				}
				
				// Physical Machines
				for (int m = 0; m < (int)(this.hostNum / this.numberOfPods / (this.edgeSwitchNumber / this.numberOfPods));
						m++){
					String host = "pm" + j + "_" + l + "_" + m;
					this.hosts.add(host);
					this.allPositions.add(host);
					this.links.put(edgeSwitch, host);
					this.hostEdgeMap.put(host, edgeSwitch);
				}
			}		
		}
		
		int index = 0;
		for (String pos: this.allPositions) {
			this.indexNodeMap.put(index, pos);
			this.nodeIndexMap.put(pos, index);
			index++;
		}
		
		index = 0;
		this.switchesTable = new int[this.allSwitches.size()];
		for (String swit: this.allSwitches) {
			this.switchesTable[index++] = nodeIndexMap.get(swit);
		}
		
		this.costTable = new double[this.allPositions.size()][this.allPositions.size()];
			
		
	}
	
	public void generate_VM_pairs() {
		Scanner scan = new Scanner(System.in);
		System.out.println("Input the number of VM pairs: ");
		int numberOfPairs = scan.nextInt();
		this.pairs = new ArrayList();
	
		
		int size = this.hosts.size();
		int number25 = (int)(numberOfPairs * 0.25);
		int number80 = (int)(numberOfPairs * 0.80);
		int number95 = (int)(numberOfPairs * 0.95);
		int totalNumber = 0;
		Random rand = new Random();
		
		while (numberOfPairs > 0) {
			totalNumber += 1;
			int i = rand.nextInt(size - 1);
			String host1 = this.hosts.get(i);
			int j = rand.nextInt(size - 1);
			String host2 = this.hosts.get(j);
			
			if (i != j) {
				while (i == j || totalNumber <= number80 && 
						!this.hostEdgeMap.get(host1).equals(this.hostEdgeMap.get(host2))) {
					i = rand.nextInt(size - 1);
					host1 = this.hosts.get(i);
					j = rand.nextInt(size - 1);
					host2 = this.hosts.get(j);	
				}
				
				while (i == j || totalNumber > number80 && 
						this.hostEdgeMap.get(host1).equals(this.hostEdgeMap.get(host2))) {
					i = rand.nextInt(size - 1);
					host1 = this.hosts.get(i);
					j = rand.nextInt(size - 1);
					host2 = this.hosts.get(j);	
				}
				
				int frequency = 0;
				if (totalNumber <= number25) {
					frequency = rand.nextInt(300);
					this.totalFrequency += frequency;
				} else if (totalNumber <= number95) {
					frequency = rand.nextInt(700 - 300) + 300;
					this.totalFrequency += frequency;
				} else {
					frequency = rand.nextInt(1001 - 700) + 700;
				}
				this.pairs.add(new Integer[]{this.nodeIndexMap.get(host1), this.nodeIndexMap.get(host2), frequency});
				numberOfPairs -= 1;
			}
			
			
		}
		
	}
	
	public void generateVNFs() {
		// randomly generate VNFs based on the user input
		Scanner scan = new Scanner(System.in);
		System.out.println("Input the number of VNFs: ");
		int numberOfVNFs = scan.nextInt();
		this.VNFs = new int[numberOfVNFs];
		if (numberOfVNFs > this.allSwitches.size()) {
			System.out.println("Too many VNFs without enough positions to put");
		}
		int size = this.allSwitches.size();
		HashSet<Integer> used = new HashSet();
		
		int k = 0; // The index of VNFs
		while (numberOfVNFs > 0) {
			Random rand = new Random();
			String tmp = this.allSwitches.get(rand.nextInt(size));
			int index = nodeIndexMap.get(tmp);
			if (!used.contains(index)) {
				this.VNFs[k] = index;
				used.add(index);
				k += 1;
				size -= 1;
				numberOfVNFs -= 1;
			}
		}		
	}
	
	public void setMigrationCoefficient() {
		Scanner scan = new Scanner(System.in);
		System.out.println("Input the migration coefficient: ");
		this.migrateCoefficient = scan.nextDouble();
	}
	
	public String toString() {
		String str = "===================================================\n";
		str += "The Structure of the fat tree is: \n";
		int num = 0;
		str += "Core Switches: {\n";
		str += "\t";
		for (String coreSwitch: this.coreSwitches) {
			num++;
			str += coreSwitch + "   ";
			if (num == 10) {
				num = 0;
				str += "\n";
				str += "\t";
			}
		}
		str += "\n}\n\n";
		
		num = 0;
		str += "PODS: {\n";
		str += "\t";
		for (String pod: this.pods) {
			num++;
			str += pod + "   ";
			if (num == 10) {
				num = 0;
				str += "\n";
				str += "\t";
			}
		}
		str += "\n}\n\n";
		
		num = 0;
		str += "Aggregation Switches: {\n";
		str += "\t";
		for (String aggrSwitch: this.aggrSwitches) {
			num++;
			str += aggrSwitch + "   ";
			if (num == 10) {
				num = 0;
				str += "\n";
				str += "\t";
			}
		}
		str += "\n}\n\n";
		
		num = 0;
		str += "Edge Switches: {\n";
		str += "\t";
		for (String edgeSwitch: this.edgeSwitches) {
			num++;
			str += edgeSwitch + "   ";
			if (num == 10) {
				num = 0;
				str += "\n";
				str += "\t";
			}
		}
		str += "\n}\n\n";
		
		num = 0;
		str += "Physical Machines: {\n";
		str += "\t";
		for (String host: this.hosts) {
			num++;
			str += host + "   ";
			if (num == 10) {
				num = 0;
				str += "\n";
				str += "\t";
			}
		}
		str += "\n}\n\n";
		
		
		str += "===================================================\n";
		str += "The VM pairs shows as below(Frequency 0~300: 25%; 300~700: 70%; 700~1000: 5%):\n";
		for (Integer[] obj: this.pairs) {
			str += "[" + indexNodeMap.get(obj[0]) + ", " + indexNodeMap.get(obj[1]) + 
					", Frequency: " + obj[2] + "]\n";
		}
		
		str += "===================================================\n";
			
		return str;
	}
	
	public static void main(String[] args) {
		FatTree ft = new FatTree();
		System.out.println(ft);
	}
	
}
