package v4;
import java.util.*;

public class Distance {
	public Distance(FatTree ft) {
		int i = 0;
		int j = 0;
		for (String node1: ft.allPositions) {
			for (String node2: ft.allPositions) {
				ft.distance.put(node1 + node2, calculateDistance(node1, node2));
				ft.costTable[i][j++] = calculateDistance(node1, node2);
			}
			i++;
			j = 0;
			
		}
	}
	
	public double calculateDistance(String node1, String node2) {
		// same nodes
        if (node1 == node2) {
        	return 0;
        } else if (!isNode(node1) || !isNode(node2)) {
        	return -1;
        } 
       
       // Both nodes are PMs
        if (node1.substring(0,2).equals("pm") && node2.substring(0,2).equals("pm")) {
        	String[] data1 = node1.substring(2).split("_");
        	String[] data2 = node2.substring(2).split("_");
        	
        	// Same pod
        	if (data1[0].equals(data2[0])) {
        		// Same Edge
        		if (data1[1].equals(data2[1])) {
        			return 2;
        		}
        		// Different Edges
        		else {
        			return 4;
        		}	
        	} else { // Different Pods
        		return 6;	
        	}
        	
        } else {
        	// One is PM
        	if (node1.substring(0,2).equals("pm") || node2.substring(0,2).equals("pm")) {
        		String pm = node1.substring(0,2).equals("pm")?node1:node2;
        		String sw = node1.substring(0,2).equals("pm")?node2:node1;
        		
        		String[] data1 = pm.substring(2).split("_");
        		// Edge Switches
        		if (sw.substring(0,2).equals("es")) {
        			String[] data2 = sw.substring(2).split("_");
        			// Same Pod
        			if (data1[0].equals(data2[0])) {
        				// Same Edge
        				if (data1[1].equals(data2[1])) {
        					return 1;
        				} else {
        					// Different Edge
        					return 3;
        				}
        			} else { // different pod
        				return 5;
        			}
        		} else if (sw.substring(0,2).equals("as")) { // Aggregation Switches
        			String[] data2 = sw.substring(0,2).split("_");
        			// Same pod
        			if (data1[0] == data2[0]) {
        				return 2;
        			} else {
        				// Different pod
        				return 4;
        			}
        		} else {
        			// Core Switches
        			return 3;
        		}
        	} else {
        		// Both are switches
        		
        		// One is core switch
        		if (node1.substring(0,2).equals("cs") || node2.substring(0,2).equals("cs")) {
        			String core = node1.substring(0,2).equals("cs")?node1:node2;
        			String sw   = node1.substring(0,2).equals("cs")?node2:node1;
        			
        			if (sw.substring(0,2).equals("cs")) {// Both are core switches
        				return 2;
        			} else if (sw.substring(0,2).equals("as")) {// Aggregation switch
        				return 1;
        			} else {// Edge Switch
        				return 2;
        			}
        		} else if (node1.substring(0,2).equals("as") || node2.substring(0,2).equals("as")) {
        			// One is aggregation switch
        			String aggr = node1.substring(0,2).equals("as")? node1: node2;
        			String sw   = node1.substring(0,2).equals("as")? node2: node1;
        			
        			// Both are aggregation switches
        			if (sw.substring(0,2).equals("as")) {
        				String[] data1 = node1.substring(2).split("_");
        				String[] data2 = node2.substring(2).split("_");
        				
        				// Same Pod
        				if (data1[0].equals(data2[0])) {
        					return 2;
        				} else { // Different pod
        					int d1 = Integer.parseInt(data1[1]);
        					int d2 = Integer.parseInt(data2[1]);
        					if (d1 % 2 == d2 % 2) {
        						return 2;
        					} else {
        						return 4;
        					}
        				}
        			} else { // One is edge switch
        				String[] data1 = node1.substring(2).split("_");
        				String[] data2 = node2.substring(2).split("_");
        				// Same Pod
        				if (data1[0].equals(data2[0])) {
        					return 1;
        				} else { // Different Pod
        					return 4;
        				}
        			}
        			
        			
        		} else { // One is edge switch, here we just consider edge-edge
        			String[] data1 = node1.substring(2).split("_");
        			String[] data2 = node2.substring(2).split("_");
        			// Same Pod
        			if (data1[0].equals(data2[0])) {
        				return 2;
        			} else {
        				return 4;
        			}
        			
        		}
        	}
        }
	}
	
	public boolean isNode(String node) {
	    return node.substring(0,2).equals("pm") || node.substring(0,2).equals("cs") ||
        		node.substring(0,2).equals("as") || node.substring(0,2).equals("es");
	}
	
	public static void main(String[] args) {
		System.out.println("Hello");
		FatTree ft = new FatTree();
		System.out.println(ft);
		Distance dis = new Distance(ft);
		for(Map.Entry<String, Double> entry: ft.distance.entrySet()) {
			System.out.println(entry);
		}
	}

}
