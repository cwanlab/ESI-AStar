import hsvis.*;
import java.util.*;


/*
    Choose k from initial_range
    
    // constructor
    public AStarAlgorithm
	(final Heuristic H, final int k, int n, final int[] range,boolean progress, AnyTime AT, double eps)
 
    // return value: 
    //   search_info ={goal_node, post_bound}

    // public functions
    public Object[] search_iteratively(final int[] iselect)
 */

public class AStarAlgorithm {
    final private Heuristic H;
    final private int k;
    final private int n;
    private int[] initial_range;
    final private boolean progress;
    final AnyTime AT;
    private double eps;
    private double e_all;
    private	 HashMap<String, ArrayList> neighboring_map;

    // FRINGE and CLOSED
    private Fringe FRINGE = null;
    private Closed CLOSED = null;

    ArrayList<Double> errslist = new ArrayList<Double> ();
    ArrayList<Double> boundslist = new ArrayList<Double> ();//additive bound
    ArrayList<Double> timeslist = new ArrayList<Double> ();
    ArrayList<Double> boundsRlist = new ArrayList<Double> ();//relative bound for Frobenius norm.

    // constructor
    public AStarAlgorithm
	(final Heuristic H, final int k, int n, final int[] range,boolean progress, AnyTime AT, double eps, double e_all, 
		HashMap<String, ArrayList> neighboring_map)
    {
	this.H = H;
	this.k = k;
	this.n = n;
	this.progress = progress;
	this.initial_range = range;
	this.AT = AT;
	this.eps = eps;
	this.e_all = e_all;
	this.neighboring_map = neighboring_map;
    }


    private void add_clusternode(final Node node1, final Node node2) {
    	int[] selection1 = node1.selection();
    	int[] selection2 = node2.selection();
        
        // new set
    	int[] newset = new int[selection1.length + selection2.length];
    	System.arraycopy(selection1, 0, newset, 0, selection1.length);
    	for(int i=0;i<selection2.length;i++){
    		if (Helper.isInset(selection2[i], selection1))
    			return;
    		newset[selection1.length+i]=selection2[i];
    	}

    	// new pSet
    	ArrayList<Integer> new_pSet = new ArrayList<>(node1.pSet);
    	new_pSet.add(node2.pSet.get(0));

    	addchild(newset, H, new_pSet, node1.c+1);
    }

    private void addchild (final int[] child_subset, final Heuristic H, ArrayList<Integer> child_pSet, int c){    
    	boolean open = this.CLOSED.add(child_subset);
	    if (open){
	    	double[] fg = H.subsetfg(child_subset);
	    	// fg can be null
		    if(fg != null) {
			    Node node = new Node(child_subset, fg);
			    node.pSet = child_pSet;
			    node.c = c;
			    this.FRINGE.add(node);
		    }
	    }
    }
    
    
    private void add_children(final Node node, int[] iselect) {
    	int[] parent = node.selection();

		// add neighbor children
        for (int idx : node.selection()){
        	// already added before
        	if (Helper.isInset(idx, node.pSet)) continue;
        	if (Helper.isInset(idx, iselect)) continue;

			String key=Integer.toString(idx);
			ArrayList<Integer> neighbors = neighboring_map.get(key);

            // child set
			int[] child_set =new int[parent.length + neighbors.size()];
			System.arraycopy(parent, 0, child_set, 0, parent.length);
			int count = 0;
			for(int i=0;i<neighbors.size();i++){
				int tmp = neighbors.get(i);
				if (!Helper.isInset(tmp, child_set)) {
					child_set[parent.length+count]=tmp;
					count += 1;
				}
			}
			child_set = Helper.getSlice(child_set, 0, parent.length + count); 

            // child pset
            ArrayList<Integer> child_pSet =  (ArrayList<Integer>) node.pSet.clone();
            child_pSet.add(idx);

			addchild(child_set, H, child_pSet, node.c);
		}
    }

    private void addrootchild (final int[] child_subset, int xi, final Heuristic H){

    	boolean open = this.CLOSED.add(child_subset);
	    if (open){
	    	double[] fg = H.subsetfg(child_subset);
	    	// fg can be null
		    if(fg != null) {
			    Node node = new Node(child_subset, fg);
			    node.pSet.add(xi);
			    node.c = 1;
			    this.FRINGE.add(node);
		    }
	    }
    }
    private void add_root_nodes(final int[] iselect) {
    	System.out.println("_____add root nodes");
    	System.out.println("total number of sources:"+neighboring_map.keySet().size());
    	
		for (String key : neighboring_map.keySet()){ // go over all sources
			int idx=Integer.parseInt(key);
			if (Helper.isInset(idx, iselect)) continue;

			ArrayList<Integer> neighbors = neighboring_map.get(key);

			int[] neighbors_subset =new int[iselect.length+neighbors.size()];

			// add iselect
			int count = 0;
			for (int i=0; i<iselect.length; i++) {
				if (!Helper.isInset(iselect[i], neighbors_subset)) {
					neighbors_subset[count] = iselect[i];
					count += 1;
				}
			}
			// add neighbors
			for(int i=0;i<neighbors.size();i++){
				int tmp = neighbors.get(i);
				if (Helper.isInset(tmp, iselect)) continue;
				neighbors_subset[count]=tmp;
				count+=1;
			}
            neighbors_subset = Helper.getSlice(neighbors_subset, 0, count);

			addrootchild(neighbors_subset, idx, H);
		}
		System.out.println("_____end add root nodes");
    }


    //---------------------------------------------
    // Return: goal_node or null
    private Node search_once(final int[] iselect) {
		
		if(this.CLOSED == null && this.FRINGE == null) {
		    this.CLOSED = new Closed(this.n);
		    this.FRINGE = new Fringe();
		}
		int c_max = 1;

		// add root nodes
		add_root_nodes(iselect);
		
		while(!this.FRINGE.isEmpty_T()) {
		    Node node = this.FRINGE.poll_T(); //node is removed from top of heap
		    node.println("Selected from fringe:");
		    
		    if(node.selection().length == k) {// goal node found
			    return(node); 
		    } 
		    if(node.selection().length > k) {
		    	System.out.println("length > k");
		    	return(node);
		    }

		    // not a goal node
			add_children(node, iselect);
		     
	        if (this.FRINGE.size()%2000 == 0) {
	        	System.gc();
	        }
		}
		System.out.println("F is empty! --> there is no area with k="+k);
		return(null); // goal node not found
    }
    //---------------------------------------------
    
    // Update the solution and compute the bound
    private void setSolution(Node goal_node){
    	double goal_node_value = goal_node.fg()[0];
    	if (goal_node_value > this.AT.getSolutionErr()) return;
        
        
        // compute the bound
        double bound_additive = -1;
        double bound_relative = -1; //only for Frobenius norm.

        if (this.FRINGE.isEmpty_T()){
            bound_additive = 0;
            bound_relative = 0;
        } else {

        	Node f_min_node = Bounds.f_min_node(this.FRINGE, goal_node.fg()[0]);
        	// Node f_min_node = Bounds.f_min_node_subset(this.FRINGE, goal_node);
        	double f_min = f_min_node.fg()[0];
        	if(f_min < e_all) f_min = e_all;
        	f_min_node.println("f_min_node");


        	// System.out.println("lmin node:"+Arrays.toString(f_min_node.selection())+ " "+f_min_node.fg()[0]+" "+e_all);

        	if (f_min >= goal_node.fg()[0]){
        		bound_additive = 0;
        		bound_relative = 0;
        	} 
        }
        // double post_bound_rank = (this.H.trace() - goal_node.fg()[0])/(this.H.trace() - this.H.err_rank());
        // post_bound_rank = 1 - post_bound_rank;

        this.AT.setSolution(goal_node, bound_additive);

        errslist.add(100*this.AT.getSolutionErr()/this.H.trace());
		boundslist.add(100*(this.AT.getSolutionBound()/this.H.trace()));
		timeslist.add(AStar.elapsed());
		boundsRlist.add(100*bound_relative);
		System.out.println("finish setting solutions");
    }

    // return value:
    // search_info = {goal_node, post_bound}
    public Object[] search_iteratively(final int[] iselect) 
    {
	    
        Node goal_node = null;
        
        this.FRINGE = null;
		this.CLOSED = null;
		new Node(this.eps);
	    int[] current_root = iselect;
	    int iter = 0;
	    while (true){

	    	//Step 1: Search a solution
	    	goal_node = search_once(current_root);

	    	if(goal_node != null){ // found a new solution
	    		goal_node.println("new solution:");
                setSolution(goal_node);
                System.out.println("---end");
	    	} else {//time is up or wrong cases.
	    		break;
	    	}

	    	if (AT == null) break;

            iter++;
            System.gc();
	    }

	    // update bound
	    if (this.AT.getSolution() != null)
	       setSolution(this.AT.getSolution());

		return(new Object[]
		    {this.AT.getSolution(),
		     this.AT.getSolutionBound()
		     });
    }
}

