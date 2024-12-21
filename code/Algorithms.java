import hsvis.*;
import java.util.*;


/*
  search_info = {goal_node, post_bound}

  public static String known_algorithms()

  // public static Object[] run()
 */

public class Algorithms {
    
    public static String known_algorithms() {
		String algs ="xy";
		return(algs);
    }
	

    // return value:
    //  search_info ={goal_node, post_bound}
    private static Object[]  astar_variant
	(String alg,
	 int k,
	 int n,
	 int[] range,
	 double eps_wAStar,
	 final int[] iselect,
	 Heuristic H,
	 boolean p,
	 String anytime_str,
	 double e_all,
	 HashMap<String, ArrayList> neighboring_map
	 )
    {

		AnyTime AT = new AnyTime(anytime_str);
		if(!AT.useAnyTime()) AT = null;

		// search
		AStarAlgorithm astar = new AStarAlgorithm(H,k,n,range,p,AT,eps_wAStar, e_all, neighboring_map); 
		Object[] search_info = astar.search_iteratively(iselect);
		
		return(search_info);
    }

    // return search_info =
    //         {goal_node, post_bound}
    public static Object[] run
	(String alg,
	 int k,
	 int n,
	 int[] range,
	 double eps_wAStar,
	 final int[] iselect,
	 Heuristic H,
	 boolean p,
	 String anytime_str,
	 double e_all,
	 HashMap<String, ArrayList> neighboring_map
	 )
    {
		// k < X.rows
		Object[] search_info = astar_variant(alg, k, n, range, eps_wAStar, iselect, H, p, anytime_str,e_all,neighboring_map);
		return(search_info);
    }
}
