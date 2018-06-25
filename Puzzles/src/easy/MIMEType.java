package easy;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Solution8
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        String MESSAGE = in.nextLine();

        // Conversion du message en binaire:
        String bin = "";
        for (char c : MESSAGE.toCharArray())
        {
            int ci = c;
            String t = Integer.toBinaryString(ci);
            while (t.length() < 7)
                t = "0" + t;
            bin += t;
        }
        System.err.println(bin);

        // Séparation en blocs de 0/1
        ArrayList<Integer> l = new ArrayList<Integer>();
        l.add(0);
        char current = bin.toCharArray()[0];
        int i = 0;

        for (char c : bin.toCharArray())
        {
            if (current != c)
            {
                current = c;
                l.add(0);
                i++;
            }
            l.set(i, l.get(i) + 1);
        }
        System.err.println(l);

        // Conversion en unary
        String u = "";
        current = bin.toCharArray()[0];

        for (int k = 0; k < l.size(); k++)
        {
            int nb = l.get(k);
            switch (current)
            {
                case '0':
                    u += "00 ";
                    current = '1';
                    break;
                case '1':
                    u += "0 ";
                    current = '0';
                    break;
            }
            for (int j = 0; j < nb; j++)
                u += "0";
            if (k < l.size() - 1)
                u += " ";
        }

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        System.out.println(u);
    }
}
