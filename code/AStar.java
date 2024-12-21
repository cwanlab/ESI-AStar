import hsvis.*;
import java.io.*;
import java.util.*;

 /*
    Electrophysiological Brain Source Imaging via Combinatorial Search with Provable Optimality
    
    @authors: Guihong Wan, Meng Jiao, Xinglong Ju, Yu Zhang, Haim Schweitzer, Feng Liu
 */

public class AStar {
    private static long START_TIME;

    public static void main(String[] args) {

    ArrayList<DataFile> tmpDF = new ArrayList<DataFile>();
	 
	 try {
	    StandardArgs Args = new StandardArgs();

	    Args.add("help", "flag", "", "Prints usage help");
	    Args.add("progress", "boolean", "yes", "yes or no");
	    Args.add("X", "String", null, "The EEG data");
	    Args.add("L", "String", null, "The leadfield matrix");
	    Args.add("NeighboringTXT", "String", null, "Neighboring file: eg. ESI_Neighboring_l2.txt");
	    Args.add("k", "int", null,
		     "the desired  number of activated sources");
	    Args.add("iselect", 
		     "String", "null", "List of initial selection e.g. idx1:idx2:idx3:idx4");

	    Args.setArgs("AStar", args);


       // Read neighborhood map
       HashMap<String, ArrayList> neighboring_map = new HashMap<String, ArrayList>();

	    String neighboringfilename = Args.stringValue("NeighboringTXT");
	    try(BufferedReader br = new BufferedReader(new FileReader(neighboringfilename))) {
	    	for(String line; (line = br.readLine()) != null; ) {
	    		// key
	    		String[] arrOfStr = line.split(":", 2);
	    		String key = Integer.toString(Integer.parseInt(arrOfStr[0])-1);

            // neighbors
            ArrayList<Integer> neighbors = new ArrayList<Integer>();
	    		String neighbors_str = arrOfStr[1];
	    		String[] neighbors_strs = neighbors_str.split(",");

	    		for (String neighbor : neighbors_strs){
	    		 	neighbors.add(Integer.parseInt(neighbor)-1);

	    		}
	    		neighboring_map.put(key, neighbors);
	    	}
	    } catch (Exception e){

	    }
	    // end reading neighborhood map; output: neighboring_map
	    //-------------------------------------------------------------------------

        
       String alg = "xy"; // fixed in this algorithm
	    double eps_wAStar = 0; // fixed in this algorithm
	    String anytime_str = "no"; // not used in this algorithm
	    String norm = "p:2"; // fixed in this algorithm
	    
	    // read X
	    Object[] X_info = DataFile_cmdline.read_transpose(Args.stringValue("L"), tmpDF);
	    DataFile X = (DataFile)X_info[0];
	    int nX = X.rows();
	    int mX = X.cols();
	    
	    // read Y
	    Object[] Y_info = DataFile_cmdline.read_transpose(Args.stringValue("X"), tmpDF);
	    DataFile Y = (DataFile)Y_info[0];
	    int nY = Y.rows();
	    int mY = Y.cols();

	    System.out.println("mL:nL="+mX+":"+nX);
	    System.out.println("mX:nX="+mY+":"+nY);
	    if( mX != mY)
	    	Errors.errexitNoStack("mX should be same as mY.");
        
       //iselect
	    int[] iselect = RangeList.selection(Args.stringValue("iselect"), nX);
	    if(iselect == null) iselect = new int[0];

	    // k
	    int k = Args.intValue("k");
	    k = k + iselect.length;

	    if(k <= 0)
	    	Errors.errexitNoStack("Nothing to be selected?");
	    if((k+iselect.length) >= nX)
	    	Errors.errexitNoStack("All columns are to be selected?");

	    int[] range = DuplicateRows.uniques(X, null, iselect);

	    boolean p = Args.booleanValue("progress");
	    //////// finished reading arguments ////////////

	    // compute H: initial eigendecomposition
	    START_TIME = System.nanoTime();
		 Heuristic H  = new Heuristic(X, Y, k, p, norm);
		 double evdtime = elapsed();
		 System.gc();

		 //error of all coloumns are selected
		 double e_all = Norms.error_from_eigenvalues(norm, H.eigenvalues_selection(range));
		 System.out.println("e_all columns selected:"+e_all);
		 double e_iselect = Norms.error_from_eigenvalues(norm, H.eigenvalues_selection(iselect));
		 Helper.println("e_iselect:" + e_iselect + ", length_iselect:" +iselect.length, iselect);

		 // search
		 START_TIME = System.nanoTime();// search time
	    Object[] search_info = Algorithms.run(alg, k, nX, range, eps_wAStar, iselect, H, p, anytime_str, e_all, neighboring_map);
	    double searchtime = elapsed();
       //////// finished///////////////////////////////


       //print results
	    System.out.println("=============================================");
	    System.out.println("finish seaching: time = (" + (evdtime+searchtime) +" = evd:" + evdtime + " + search:" + searchtime + ")");
	    if (search_info==null){
	    	System.out.println(" No solutions found!!!");
	    	return;
	    }
	    
	    Node goal_node = (Node) search_info[0];

	    if (goal_node == null) {
	    	return;
	    }
	    double goal_node_value = goal_node.fg()[0]; //error
	    double post_bound = (Double) search_info[1]; //bound
       

	    ArrayList<Integer> new_S = new ArrayList<Integer>();
	    for (int idx : goal_node.selection()){
	    	if (!Helper.isInset(idx, iselect)) new_S.add(idx);
	    }
	    Collections.sort(new_S);

       
       // all selection
       int[] goal_S = goal_node.selection();
	    Arrays.sort(goal_S);
	    Helper.println("solution:"+goal_S.length, goal_S);

        System.out.println("solution error = " + goal_node_value
			 +" (" + (goal_node_value/H.trace())*100+"%)");
        System.out.println("additive bound = " + post_bound
        	+" ("+100*(post_bound/H.trace())+"%)");

	    System.out.println("mL:nL = "+mX+":"+nX);
	    System.out.println("mX:nX = "+mY+":"+nY);
	    System.out.println("=============================================");

	}
	finally {DataFile.temporary_purge(tmpDF);}
   }

    private static double getepsilon(String arg) {
		// epsilon value for wastar.
		double eps = Double.parseDouble(arg);
		if(eps < 0 || eps > 1)
		    Errors.errexit("eps range is [0,1]. It can't be " + eps);
		return(eps);
    }

    public static double elapsed() {
		long time_ns = System.nanoTime() - START_TIME;
		double time_seconds = (double)time_ns/1e9;
		return(time_seconds);
    }

    public static void progress(boolean p, String message) {
		if(p) System.out.println(message + ". " + elapsed() + " seconds");
    }

    //////////////////////////////////////////////////////////////
}

