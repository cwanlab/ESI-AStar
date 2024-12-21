import hsvis.*;
import java.io.*;
import java.util.*;

/*
  Some usefull functions
 */

public class Helper {

    public static boolean isInset(int idx, ArrayList<Integer> subset){
    	for(int i=0;i<subset.size();i++)
    		if (subset.get(i) == idx) return true;
    	return false;
    }

    public static boolean isInset(int idx, int[] subset){
    	for(int i=0;i<subset.length;i++)
    		if (subset[i] == idx) return true;
    	return false;
    }

    public static void println(String comment, final int[] S){
	    if( comment != null) System.out.print(comment + " ");
	    System.out.print("(");
	    int size = S.length;
	    for(int i = 0 ; i < size-1 ; i++) 
	      System.out.print(S[i] + ", ");
	    if(size > 0) System.out.print(S[size-1]);
	    System.out.print(")");
	    System.out.print("\n");
    }

    public static void println(String comment, final double[] S){
	    if( comment != null) System.out.print(comment + " ");
	    System.out.print("(");
	    int size = S.length;
	    for(int i = 0 ; i < size-1 ; i++) 
	      System.out.print(S[i] + ", ");
	    if(size > 0) System.out.print(S[size-1]);
	    System.out.print(")");
	    System.out.print("\n");
    }


    public static int[] getSlice(int[] array, int startIndex, int endIndex) {   
			// Get the slice of the Array   
			int[] slicedArray = new int[endIndex - startIndex];   
			//copying array elements from the original array to the newly created sliced array  
			for (int i = 0; i < slicedArray.length; i++)   
			{   
			slicedArray[i] = array[startIndex + i];   
			}   
			//returns the slice of an array  
			return slicedArray;
		}

		public static int[] conbineSet_unique(int[] array1, int[] array2){
			int[] newset = new int[array1.length + array2.length];
    	System.arraycopy(array1, 0, newset, 0, array1.length);
    	int count = array1.length;
    	for(int i=0;i<array2.length;i++){
    		if (!Helper.isInset(array2[i], array1)){
    			newset[count]=array2[i];
    			count += 1;
    		}
    	}
    	return getSlice(newset, 0, count);
		}
    
}

