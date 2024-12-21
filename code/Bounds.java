import hsvis.*;
import java.util.*;

/*
    public static Node f_min_node(final Fringe fringe)
 */

public class Bounds {

    private static boolean isInSubset(int value, int[] target_selection) {
    	/*
    	check if the value is in target_selection.
    	*/
    	for(int i = 0; i < target_selection.length;i++){
    		if(target_selection[i] == value) return(true);
    	}
    	return(false);
    }

    private static boolean isSubset(int[] selection, int[] target_selection) {
    	/*
    	check if selection is the subset of target_selection.
    	True: if every element in selection is in target_selection
    	False: otherwise.
    	*/
    	for(int i = 0; i < selection.length; i++){
    		if (!isInSubset(selection[i], target_selection)) return(false);
    	}
    	return(true);
    }

    public static Node f_min_node_subset(Fringe fringe, Node goal_node) {
	    /* 
	       find the node with minimum l value and the node should not be the subset of the goal node
	    */
	    if(fringe == null) return(null);

	    int[] goal_seleciton = goal_node.selection();
		
		Iterator<Node> iterator = fringe.iterator();
		double f_min = Double.POSITIVE_INFINITY;
		Node f_min_node = null;
		while(iterator.hasNext()) {
		    Node n = iterator.next();
		    double[] fg = n.fg();
		    double f = fg[0];
		    if(f < f_min && !isSubset(n.selection(), goal_seleciton)) {
				f_min = f;
				f_min_node = n;
		    }
		}
		return(f_min_node);
    }

    public static Node f_min_node(Fringe fringe, double error) {
    /* 
       1. find the node with minimum f value. 
       2. remove nodes with f value >= error.
    */
	if(fringe == null) return(null);
	ArrayList<Node> removelist = new ArrayList<Node> ();
	Iterator<Node> iterator = fringe.iterator();
	double f_min = Double.POSITIVE_INFINITY;
	Node f_min_node = null;
	while(iterator.hasNext()) {
	    Node n = iterator.next();
	    double[] fg = n.fg();
	    double f = fg[0];
	    if(f < f_min) {
			f_min = f;
			f_min_node = n;
	    }
	    // if(f>=error){
	    // 	removelist.add(n);
	    // }
	}

	// for(Node n : removelist) {
	// 	fringe.remove(n);
	// }
	return(f_min_node);
    }

    public static Node u_max_node(Fringe fringe) {
    /* 
       1. find the node with minimum u value. 
    */
	if(fringe == null) return(null);
	
	Iterator<Node> iterator = fringe.iterator();
	double u_max = 0;
	Node u_max_node = null;
	while(iterator.hasNext()) {
	    Node n = iterator.next();
	    double[] fg = n.fg();
	    double u = fg[1];
	    if(u > u_max) {
			u_max = u;
			u_max_node = n;
	    }
	}
	return(u_max_node);
    }
    
}
