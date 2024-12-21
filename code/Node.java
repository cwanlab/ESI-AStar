import hsvis.*;
import java.util.*;

/*
  // each node has fg, f_prime.
  // f' is computed by f' = f + epsilon g
  // epsilon is static
  // epsilon == -1 means epsilon == infinity (f' = g)
  // f is used for comparing bound

  public static double f_prime(final double[]fg)
  public double f_prime()


  // constructors
  
  // must be called before any other constructor
  public Node(double epsilon)
  
  public Node(final int[]S, final double[] fg)

  // access:
  public int[] selection()
  public int selection_length()
  public double f_prime()
  public double[] fg()
  public double epsilon()

  // printing for debugging
  public void println()
  public void println(String text)
  public static void println(String comment, final ArrayList<Node> L)

 */

public class Node 
    implements Comparator<Node>, Comparable<Node> 
{

    private static double EPSILON = 0;

    private int[] S; // indices of selection
    private double[] fg;
    private double f_prime;
    public ArrayList<Integer> pSet = new ArrayList<Integer>();
    public int c=0;

    public static double f_prime(final double[]fg) {
      return((1-EPSILON)*fg[0] + EPSILON*fg[1]);
    }

    // constructors
    public Node(double epsilon) {this.EPSILON = epsilon;}

    public Node(final int[]S, final double[] fg) {
	    this.S = (S == null) ? new int[0] : S;
	    this.fg = fg;
	    this.f_prime = f_prime(fg);
    }

    // access
    public int selection_length() {return(this.S.length);}
    public int[] selection(){return(this.S);}
    public double f_prime() { return(f_prime); }
    public double[] fg() { return(fg); }
    public double epsilon() { return(this.EPSILON); }

    public void update_f_prime() {
      this.f_prime = f_prime(this.fg);
    }

    // comparator
    public int compare(Node n1, Node n2) {
	    double v1 = n1.f_prime();
	    double v2 = n2.f_prime();
	    // the smaller f' the better
	    if(v1 < v2) return(-1);
	    else if(v1 > v2) return(1);


	    // v1 == v2
	    // the larger |S| the better
	    else if(n1.S.length > n2.S.length) return(-1);
	    else if(n1.S.length < n2.S.length) return(1);
      
	    // length n1.S == length n2.S
	    // the smaller l the better
      else if(n1.fg[0] < n2.fg[0]) return(-1);
      else if(n1.fg[0] > n2.fg[0]) return(1);
      
      // the smaller u the better
      else if(n1.fg[1] < n2.fg[1]) return(-1);
      else if(n1.fg[1] > n2.fg[1]) return(1);

	    return(0);
    }
    public int compareTo(Node n2) {
      // int ret = compare(this, n2);
      // if (this.f_prime()==n2.f_prime()){
      //   this.println("n1");
      //   n2.println("n2");
      //   System.out.println(ret);
      // }
	    return(compare(this, n2));
    }

    /////////////////////////////////////////////////////

    // printing function

    private static void print(String comment, final int[] S){
	    if( comment != null) System.out.print(comment + " ");
	    System.out.print("(");
	    int size = S.length;
	    for(int i = 0 ; i < size-1 ; i++) 
	      System.out.print(S[i] + ", ");
	    if(size > 0) System.out.print(S[size-1]);
	    System.out.print(")");
    }

    public void println() {
    	print("",this.S);
    	System.out.printf(", f=%g, g=%g, f'=%g\n",
    			  this.fg[0], this.fg[1], f_prime()); 
      // System.out.print("pSet:");
      // System.out.println(this.pSet);
    }

    public void println(String comment) {
    	print(comment+":"+this.S.length, this.S);
    	System.out.printf(", f=%g, g=%g, f'=%g\n",
    			  this.fg[0], this.fg[1], f_prime()); 
      System.out.print("pSet:");
      System.out.println(this.pSet);
    }

    public static void println(String comment, final ArrayList<Node> L) {
    	System.out.println(comment);
    	if(L != null)
    	    for(Node n : L) n.println();
    }
}
