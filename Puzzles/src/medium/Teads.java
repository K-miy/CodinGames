package medium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Solution3
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(); // the number of adjacency relations

        HashMap<Integer, ArrayList<Integer>> adj = new HashMap<>();
        System.err.println(n + "");

        for (int i = 0; i < n; i++)
        {
            int xi = in.nextInt(); // the ID of a person which is adjacent to yi
            int yi = in.nextInt(); // the ID of a person which is adjacent to xi

            if (adj.get(xi) == null)
                adj.put(xi, new ArrayList<>());

            if (adj.get(yi) == null)
                adj.put(yi, new ArrayList<>());

            adj.get(xi).add(yi);
            adj.get(yi).add(xi);
            System.err.println(xi + " " + yi);
        }

        int size = adj.size();
        int lastRemove = -1;

        int depth = 0;
        HashSet<Integer> leaf = new HashSet<>();
        HashSet<Integer> all = new HashSet<>();
        System.err.println(adj);

        while (!adj.keySet().isEmpty() && adj.keySet().size() != 1)
        {
            // Find all leafs:
            for (Integer i : adj.keySet())
                if (adj.get(i).size() == 1)
                {
                    leaf.add(i);
                    lastRemove = i;
                }

            adj.keySet().removeAll(leaf);

            for (Integer i : adj.keySet())
                adj.get(i).removeAll(leaf);

            depth++;
            all.addAll(leaf);
            leaf.clear();
            System.err.println("Current : " + all.size() + "/" + size + " -- " + depth);
            System.err.println(adj);
        }

        if (adj.keySet().size() == 1)
            lastRemove = (int) adj.keySet().toArray()[0];

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        // The minimal amount of steps required to completely propagate the advertisement
        System.out.println("" + lastRemove);
    }
}
