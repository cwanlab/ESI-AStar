import hsvis.*;
import java.util.*;

/*

 // constructors
 public Heuristic
       (final DataFile Xt, final DataFile Yt 
           final int k, boolean progress)  
 // access
 public int k()
 public double[] Y_eigenvalues()
 public double[][] Wt()
 public copyof_W(int i) // copy of column i of W

 // parent related
 // returns {H_matrix, HouseQ }
 public Object[] parent_info(final int[] I)

 */

public class Heuristic {

    private boolean progress = false;
    private int k;

    // YY^T ~ UDU^T with ortho U. 
    // X ~ U_x Wx
    // Retain: eigenvalues of Y, P = D^{1/2}UU_x, W_x
    private double[] Y_eigenvalues;
    private double[][] P;
    private double[][] Wt;
    private double trace = 0;
    // private double err_rank = 0; // just for checking lmin

    private String norm = "p:2"; // Frobenius norm

    // access
    public  double[][] Wt() { return(Wt); }
    public  double[] Y_eigenvalues() { return(Y_eigenvalues); }
    public  double trace() { return(trace);} //not really trace. It is the norm of Y.
    // public double err_rank() { return(err_rank);}
    

    public  int k() { return(k); }
    private double[] copyof_W(int i) { // copy of column i of W
	    return(Linalg.copy(Wt[i]));
    }
    // private methods for constructors
    
    // Given: r, maximum rank of Y
    // Given the evd of YY^T: eigenvectors U and eigenvalues l
    // Given the evd of XX^T
    // sets this.Y_eigenvalues, this.P, this.trace
    // return the reduced U_x
    private double[][] setEigens(int r, int rX, final double[] l, final double[][] Ut, final double[] lX, final double[][] UtX) {
  		if(r > l.length) r = l.length;
  		this.Y_eigenvalues = l;
  		for(int i = 0 ; i < r ; i++) {
  		    double li = l[i];
  		    if(li < 0) this.Y_eigenvalues[i] = 0;
          // if (i>=k) this.err_rank += this.Y_eigenvalues[i];

  		    double singularvalues = Math.sqrt(this.Y_eigenvalues[i]);
  		    vector_scale_inplace(Ut[i], singularvalues);
  		}

      this.trace = Norms.error_from_eigenvalues(this.norm, this.Y_eigenvalues);

      if(rX > lX.length) rX = lX.length;
      double[][] reducedUtX = new double[rX][];
      for(int i = 0 ; i < rX ; i++) {
        reducedUtX[i] = UtX[i];
      }

      double[][] P = new double[r][rX];
      for(int i = 0 ; i < r ; i++) {
        for(int j=0; j<rX; j++){
            P[i][j] = Linalg.dot(Ut[i], UtX[j]);
        }
      }
      this.P = P;
        
      return(reducedUtX);
    }

    private void vector_scale_inplace(double[] v, double scale) {
	    for(int i = 0 ; i < v.length ; i++)
	    v[i] = scale*v[i];
    }
    
    public static double[][] select_shallow_copy
    (final int[] I, final double[][] Xt) 
      {
    int k = I.length;
    double[][] St = new double[k][];
    for(int i = 0 ; i < k ; i++) St[i] = Xt[I[i]];
    return(St);
    }

    public static double[][] select_copy(final int[] I, final double[][] Xt) {
      double[][] St = select_shallow_copy(I, Xt);
      return(Linalg.copy(St));
    }

    // constructors
    public Heuristic(final DataFile Xt, final DataFile Yt,
			 final int k, boolean progress, String norm)
    {
      this.k = k;
      this.norm = norm;
      // EVD for Yt
	    int max_rank = Yt.rows();
      if (Yt.cols() < max_rank) max_rank = Yt.cols();
      // max_rank = 500; //if dimensionality reduction is desired.
	    AStar.progress(progress, "started computing eigens X");

	    // Object[] eigensystem = DataFileDominantEigens.eigensystem
	    //    (Yt, max_rank, 
  	  //    DataFileDominantEigens.default_p(), 
  	  //    null,
  	  //    DataFileDominantEigens.default_iters(), 
  	  //    DataFileDominantEigens.default_R(), 
  	  //    DataFileDominantEigens.default_z_epsilon(), 
  	  //    false);

      Object[] eigensystem = DataFileDominantEigens.eigensystem
         (Yt, max_rank, 
         10, 
         null,
         2, 
         DataFileDominantEigens.default_R(), 
         DataFileDominantEigens.default_z_epsilon(), 
         false);

	    double[] l = (double[]) eigensystem[0]; // eigenvalues
	    double[][] Ut = (double[][]) eigensystem[1]; // eigenvectors
	    AStar.progress(progress, "finished computing eigens, rank:"+l.length);
	
      // EVD for Xt
      int max_rank_X = Xt.rows();
      if (Xt.cols() < max_rank_X) max_rank_X = Xt.cols();
      // max_rank_X = 500;
      AStar.progress(progress, "started computing eigens L");

      // Object[] eigensystemX = DataFileDominantEigens.eigensystem
      //     (Xt, max_rank_X, 
      //      DataFileDominantEigens.default_p(), 
      //      null,
      //      DataFileDominantEigens.default_iters(), 
      //      DataFileDominantEigens.default_R(), 
      //      DataFileDominantEigens.default_z_epsilon(), 
      //      false);

      Object[] eigensystemX = DataFileDominantEigens.eigensystem
          (Xt, max_rank_X, 
           10, 
           null,
           2, 
           DataFileDominantEigens.default_R(), 
           DataFileDominantEigens.default_z_epsilon(), 
           false);

      double[] lX = (double[]) eigensystemX[0]; // eigenvalues
      double[][] UtX = (double[][]) eigensystemX[1]; // eigenvectors
      AStar.progress(progress, "finished computing eigens, rank:"+lX.length);

      double[][] reducedUtX = setEigens(max_rank, max_rank_X, l, Ut, lX, UtX);
      // global Y_eigenvalues/P were set
    	
      int n = Xt.rows();
      double[][] Wt = new double[n][];
      for(int i = 0 ; i < n ; i++) {
          // X = U Sigma Vt = U W.  x(i) = U W(i). Wt(i) = Ut xt(i)
          SparseVector si = Xt.nextSparseVector();
          Wt[i] = SparseVector.dot(reducedUtX, si);
      }
      this.Wt = Wt;
      
    	Yt.rewind();
      Xt.rewind();
    	
    	this.progress = progress;
    }
    
    //=====================================================
    // compute heuristics
    private double[][] H_matrix_from_I(final int[] I) {
  		double[][] St = select_copy(I, this.Wt);
  		return(H_matrix_from_St(St));
    }

    private double[][] H_matrix_from_St(double[][]St) {
  		double[][] Qt = HouseOrthoBasis.Qt_OrthoBasis_destroy_input(St);
  		return(H_matrix_from_Qt(Qt));
    }

    private double[][] H_matrix_from_Qt (final double[][] Qt){
		  // Qt has orthonormal vectors
		  // H = D^2 - sum_j sj sj', sj = Pqj
      
      double[] d = this.Y_eigenvalues;
		  int r = d.length;
		  double[][] H = new double[r][r];

		  if(Qt.length > 0){
  			for(int j = 0 ; j < Qt.length ; j++) {
  			    double[] qj = Qt[j];
  			    double[] sj = P_dot_vector(qj);
  			    Linalg.incrementOuterLower(H, sj);
  			}
  			for(int i = 0 ; i < r ; i++)
  			    for(int j = 0 ; j <= i ; j++)
  				    H[i][j] = -H[i][j];
  			Linalg.ReflectLower(H); //reflect lower half B to upper half.
	    }

		  for(int i = 0 ; i < r ; i++)
		    H[i][i] += d[i];
		  return(H);
    }
    private double[] P_dot_vector(double[] v){
    	int r= this.P.length;
    	double[] ret = new double[r];
    	for(int i=0;i<r;i++){
    		ret[i]= Linalg.dot(this.P[i], v);
    	}
    	return(ret);
    }
    
    public double[] eigenvalues_selection(final int[] parent_selection) {
      int parent_ki = parent_selection.length;

      double[][] St = select_copy(parent_selection, this.Wt);

      ArrayList<double[]> HQ = HouseOrthoBasis.H_OrthoBasis_destroy_input(St);
      double[][] Qt = HouseFactored.Q1t(HQ);
          
      double[][] parent_H_matrix = H_matrix_from_Qt(Qt);
      
      double[] l = new double[parent_H_matrix.length];
      Eigens.eigens(l, parent_H_matrix); // here, operate in place, then parent_H_matrix is eigenvectors
      return(l);
    }

    ////////////////////////////////////////////////////////////
    // ChildrenHeuristic
    // Object[] CH_info = {
    //                     Integer parent_ki, 
    //                     ArrayList<double[]> HQ,
    //                     Rank1Update rank1update,
    //                     Double trace
    //                    }

    public Object[] ChildHeuristic_info(final int[] parent_selection) {
    	// long start_time = System.nanoTime();
    	// long time_ns;

  		int parent_ki = parent_selection.length;

  		double[][] St = select_copy(parent_selection, this.Wt);

  		ArrayList<double[]> HQ = HouseOrthoBasis.H_OrthoBasis_destroy_input(St);
  		double[][] Qt = HouseFactored.Q1t(HQ);
          
  		double[][] parent_H_matrix = H_matrix_from_Qt(Qt);
      
      double[] l = new double[parent_H_matrix.length];
      Eigens.eigens(l, parent_H_matrix); // here, operate in place, then parent_H_matrix is eigenvectors
      
      double trace = 0;
      for(int i = 0 ; i < l.length ; i++) {
            if(l[i] < 0) l[i] = 0;
            trace += l[i];
      }

		Rank1Update rank1update = new Rank1Update(parent_H_matrix, l, true);
    
		// time_ns = System.nanoTime() - start_time;
		// System.out.println("parent:"+(time_ns/1e9));

		// rank1update is in location [2]
		return(new Object[] {parent_ki, HQ, rank1update, trace});
    }

    // returns null if x is linearly dependent on the selection in parent
    final double EPSILON_zero = 1e-10;
    public double[] child_heuristic (final Object[] CH_info, int index) {
  		int parent_ki = (Integer) CH_info[0];
  		ArrayList<double[]> HQ = (ArrayList<double[]>) CH_info[1];
  		Rank1Update rank1update = (Rank1Update) CH_info[2];
      double trace_p = (double) CH_info[3];

  		double[] w = copyof_W(index);

      // long start_time = System.nanoTime();
      // long time_ns;
  		HouseOrthoBasis.reduce_inplace(w, HQ); // w = (1 - QQ^T)w

  		double[] q = w;
  		double norm2q = Linalg.norm2(q);
  		if(norm2q <= EPSILON_zero) return(null);
  		// q != 0, norm2q > 0
  		Linalg.scaleInplace(1.0/Math.sqrt(norm2q), q);

  		double[] s = P_dot_vector(q);

      //all eigenvalues
      double[] evals = rank1update.evalues_A_minus_xxt(s);

      // ui
      double ui =  Norms.error_from_eigenvalues(this.norm, evals);
      if (ui < 0) ui = 0;

      //li
      double li = 0;
      int skip = this.k - parent_ki - 1;
      int n = evals.length - skip;
      if(n > 0){
        double[] l = new double[n];
        for(int i = 0 ; i < n ; i++) l[i] = evals[i+skip];
        li =  Norms.error_from_eigenvalues(this.norm, l);
      }
      if (li < 0) li = 0;

      double[] fg = new double[2];
      fg[0] = li;
      fg[1] = ui;
      
  		return(fg);
    
    }
    //----------
    public double[] subsetfg (int[] S) {
      if (S.length > this.k) return null;
      
      //all eigenvalues
      double[] evals = eigenvalues_selection(S);
      // Helper.println("evals:"+evals.length, evals);

      // ui
      double ui =  Norms.error_from_eigenvalues(this.norm, evals);
      if (ui < 0) ui = 0;

      //li
      double li = 0;
      int skip = this.k - S.length;
      int n = evals.length - skip;
      if (skip<0) li = ui;
      else{
        if(n > 0){
          double[] l = new double[n];
          for(int i = 0 ; i < n ; i++) l[i] = evals[skip+i];
          li =  Norms.error_from_eigenvalues(this.norm, l);
          // Helper.println("l:"+l.length, l);
        }
      }
      if (li < 0) li = 0;

      double[] fg = new double[2];
      fg[0] = li;
      fg[1] = ui;
      if (S.length < this.k & li == ui){
        System.out.println("skip:"+skip+", n:"+n);
        Helper.println("S:"+S.length, S);
      }
      // System.out.println("S:"+S.length + ", k:" + this.k + ", skip:" + skip);
      // Helper.println("fg:", fg);
      return(fg);
    
    }
} 


