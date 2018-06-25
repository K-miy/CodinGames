package medium;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Solution5
{

    public static int[] lookandsay(int[] number)
    {
        ArrayList<Integer> result = new ArrayList<>();

        int repeat = number[0];
        int times = 1;

        for (int i = 1; i <= number.length; i++)
            if (i == number.length)
            {
                result.add(times);
                result.add(repeat);
            }
            else
            {
                int actual = number[i];
                if (actual != repeat)
                {
                    result.add(times);
                    result.add(repeat);
                    times = 1;
                    repeat = actual;
                }
                else
                    times += 1;
            }

        int[] r = new int[result.size()];

        for (int i = 0; i < r.length; i++)
            r[i] = result.get(i);

        return r;
    }

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        int R = in.nextInt();
        int L = in.nextInt();

        int[] s = new int[] { R };

        while (L > 1)
        {
            s = lookandsay(s);
            L--;
        }

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");
        StringBuilder sb = new StringBuilder();
        for (int element : s)
            sb.append(" " + element);

        System.out.println(sb.substring(1));
    }
}
