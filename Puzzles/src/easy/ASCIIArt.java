package easy;

import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Solution6
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        in.nextInt(); // the number of temperatures to analyse
        in.nextLine();
        String temps = in.nextLine(); // the n temperatures expressed as integers ranging from -273 to 5526

        String[] t = temps.split(" ");

        int near0 = Integer.MAX_VALUE;
        for (String s : t)
            if (!s.isEmpty())
            {
                int a = Integer.parseInt(s);
                if (Math.abs(a) < Math.abs(near0))
                    near0 = a;
                if (Math.abs(a) == Math.abs(near0) && a > 0)
                    near0 = a;
            }
        if (near0 == Integer.MAX_VALUE)
            near0 = 0;

        System.out.println(near0 + "");
    }
}
