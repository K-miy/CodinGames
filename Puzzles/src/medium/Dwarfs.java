package medium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Solution7
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(); // the number of relationships of influence
        HashMap<Integer, ArrayList<Integer>> adj = new HashMap<>();

        for (int i = 0; i < n; i++)
        {
            int x = in.nextInt(); // a relationship of influence between two people (x influences y)
            int y = in.nextInt();

            if (adj.get(x) == null)
                adj.put(x, new ArrayList<>());
            if (adj.get(y) == null)
                adj.put(y, new ArrayList<>());

            adj.get(x).add(y);
            System.err.println(x + " -> " + y);

        }
        int size = adj.size();

        int depth = 0;
        HashSet<Integer> leaf = new HashSet<>();
        HashSet<Integer> all = new HashSet<>();
        System.err.println(adj);

        while (!adj.keySet().isEmpty())
        {
            // Find all leafs:
            for (Integer i : adj.keySet())
                if (adj.get(i).size() == 0)
                    leaf.add(i);

            adj.keySet().removeAll(leaf);

            for (Integer i : adj.keySet())
                adj.get(i).removeAll(leaf);

            depth++;
            all.addAll(leaf);
            leaf.clear();
            System.err.println("Current : " + all.size() + "/" + size + " -- " + depth);
            System.err.println(adj);
        }

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        // The number of people involved in the longest succession of influences
        System.out.println(depth);
    }
}
