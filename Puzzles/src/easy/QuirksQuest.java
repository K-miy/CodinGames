package easy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Player1
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);

        // game loop
        while (true)
        {

            ArrayList<Integer> l = new ArrayList<>();
            ArrayList<Integer> k = new ArrayList<>();

            for (int i = 0; i < 8; i++)
            {
                int mountainH = in.nextInt(); // represents the height of one mountain, from 9 to 0.
                l.add(mountainH);
                k.add(mountainH);
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            Collections.sort(k);
            int max = k.get(k.size() - 1);
            int i = l.indexOf(max);

            System.out.println("" + i); // The number of the mountain to fire on.
        }
    }
}
