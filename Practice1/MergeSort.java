import java.io.*;

/**
 * MergeSort
 * Created by Gerry on 05/03/2017.
 * Copyright © 2016 Gerry. All rights reserved.
 */

public class MergeSort {
    static final int MAX_LENGTH = 1000005;
    static void mergeSortRecursive(int[] arr, int[] result, int start, int end) {
       if (start >= end) 
           return;
       int len = end - start, mid = (len >> 1) + start;
       int start1 = start, end1 = mid;
       int start2 = mid + 1, end2 = end;
       mergeSortRecursive(arr, result, start1, end1);
       mergeSortRecursive(arr, result, start2, end2);
       int ptr = start;
       while (start1 <= end1 && start2 <= end2)
           result[ptr++] = arr[start1] < arr[start2] ? arr[start1++] : arr[start2++];
       while (start1 <= end1)
           result[ptr++] = arr[start1++];
       while (start2 <= end2)
           result[ptr++] = arr[start2++];
       for (int i = start; i <= end; i++)
           arr[i] = result[i];
    }

    public static void mergeSort(int[] arr, final int length) {
        int[] result = new int[length];
        mergeSortRecursive(arr, result, 0, length-1);
    }
    
    public static void main(String[] args) {
        System.out.println("Java");
        
        int[] arr = new int[MAX_LENGTH];
        int num, len = 0;

        // File io
        BufferedReader br = null;
        PrintWriter pw = null;

        // Read data from file
        try {
            br = new BufferedReader(new FileReader("not_sorted.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                 num = Integer.parseInt(line);
                 arr[len++] = num;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Prompt user
        System.out.println("Sorting...");

        // Merge sort
        mergeSort(arr, len);

        // Write sorted data to file
        try {
            pw = new PrintWriter(new FileWriter("sorted_java.txt"));
            for (int i = 0; i < len; i++) {
                pw.println(arr[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Close
        try {
            br.close();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Prompt user
        System.out.println("Done!");
    }
}
