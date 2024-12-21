import hsvis.*;
import java.io.*;
import java.util.*;

/*
  String norm can be one of:
  p:pval // Schattern p norm
  q:qval // Ky Fan q norm
 */

public class Norms {

    public static String describe() {
	String s = "";
	s += "p:pval // Schattern p norm";
	s += ", q:qval // Ky Fan q norm";
	return(s);
    }
    
    public static double error_from_eigenvalues
	(String norm, final double[] evalues)
    {
    	String[] tmp = norm.split(":");
		if(tmp.length != 2) Errors.errexit("unknown norm " + norm);

		if(tmp[0].equalsIgnoreCase("p")) { // Schattern p norm
		    double p = Double.parseDouble(tmp[1]);
		    return(Schattenp(evalues, p));
		}
		else if(tmp[0].equalsIgnoreCase("q")) { //Ky Fan q norm
			double q = Double.parseDouble(tmp[1]);
		    return(KyFanq(evalues, q));
		}
		else { 
		    Errors.errexit("unknown norm " + norm);
		    return(0);
		}
    }

    private static double KyFanq(final double[] eigenvalues, double q) {
	    if(q > eigenvalues.length) q = eigenvalues.length;

		double zero_epsilon = 1e-12;
		double sum = 0;
		for(int i = 0 ; i < q ; i++) {
		    double l = eigenvalues[i];
		    if(l > zero_epsilon) 
			    sum += Math.pow(eigenvalues[i], 0.5); //because the norm is computed from eigenvalues.
		}
		return(sum);
    }

    private static double Schattenp(final double[] eigenvalues, double p) {
		double zero_epsilon = 1e-12;
		double e = p/2.0; //because the norm is computed from eigenvalues
		double sum = 0;
		for(int i = 0 ; i < eigenvalues.length ; i++) {
		    double l = eigenvalues[i];
		    if(l > zero_epsilon) 
			sum += Math.pow(eigenvalues[i], e);
		}
		return(sum);
    }
    
}

