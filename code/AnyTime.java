import hsvis.*;
import java.io.*;
import java.util.*;

/*
  initstring format is one of the following:
  no // don't use AnyTime
  s:number // time in seconds
  m:number time // in minutes
  h:number // time in hours

    // constructor
    public AnyTime(String initstring)

    public static String describe()
    public boolean useAnyTime()
    public boolean earlyTermination()
    public Node bestInFringe(final Fringe fringe, int k)
 */

public class AnyTime {
    final boolean useAnyTime;
    final double maxtime_seconds;

    static Node solution=null;
    static double bound = Double.POSITIVE_INFINITY;
    static double err = Double.POSITIVE_INFINITY;

    public static void setSolution(Node node, double b){
    	solution = node;
    	err = node.fg()[0];
    	if (b >= 0){
    		bound = b;
    	}
    }
    public static Node getSolution(){
    	return solution;
    }
    public static double getSolutionBound(){
    	return bound;
    }
    public static double getSolutionErr(){
    	return err;
    }
    public static void setSolutionBound(double b){
    	bound = b;
    }

    public static String describe() {
		String s = "";
		s += "no // don't use AnyTime";
		s += ", s:t // seconds";
		s += ", m:t // minutes";
		s += ", h:t // hours";
		return(s);
    }

    // constructor
    public AnyTime(String initstring) {
	if(initstring.equalsIgnoreCase("no")) {
	    this.useAnyTime = false;
	    this.maxtime_seconds = 0; // never used
	}
	else {
	    this.useAnyTime = true;
	    String[] instructions = initstring.split(":");
	    if(instructions.length != 2)
		Errors.errexit
		    ("Expecting letter:number."
		     + " Wrong instructions format for AnyTime: "+initstring
		     + "\n" + describe());
	    String flag = instructions[0];
	    char f = flag.charAt(0);
	    double t = Double.parseDouble(instructions[1]);

	    switch(f) {
	    case 's' : this.maxtime_seconds = t; break;
	    case 'm' : this.maxtime_seconds = t*60; break;
	    case 'h' : this.maxtime_seconds = t*60*60; break;
	    default : 
		Errors.errexitNoStack ("AnyTime unknown format: " + flag
				       + "\n" + describe());
		this.maxtime_seconds = 0; // never reached
		break;
	    }
	}
	System.out.println("timelimit:"+this.maxtime_seconds+"s.");
    }

    // access
    public boolean useAnyTime() {return(this.useAnyTime);}

    // real stuff
    public boolean earlyTermination() {
	return(AStar.elapsed() >= this.maxtime_seconds);
    }

 //    public Node bestInFringe(final Fringe fringe, int k) {
	// Iterator<Node> iterator = fringe.iterator();
	// double f_min = Double.POSITIVE_INFINITY;
	// Node f_min_node = null;
	// while(iterator.hasNext()) {
	//     Node n = iterator.next();
	//     if(n.selection_length() == k) {
	// 	double[] fg = n.fg();
	// 	double f = fg[0];
	// 	if(f < f_min) {
	// 	    f_min = f;
	// 	    f_min_node = n;
	// 	}
	//     }
	// }
	// return(f_min_node);
 //    }
    
}

