package medium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Solution
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        int L = in.nextInt();
        int H = in.nextInt();

        String[] n = new String[H];
        for (int i = 0; i < H; i++)
            n[i] = in.next();

        HashMap<Integer, String> numbers = new HashMap<>();
        HashMap<String, Integer> literals = new HashMap<>();
        for (int j = 0; j < 20; j++)
            numbers.put(j, "");

        for (String l : n)
            for (int j = 0; j < 20; j++)
                numbers.put(j, numbers.get(j) + l.substring(j * L, j * L + L));

        // System.err.println(numbers);

        for (Integer i : numbers.keySet())
            literals.put(numbers.get(i), i);

        // System.err.println(literals);

        int S1 = in.nextInt();
        int N1 = S1 / H;
        String[] n1 = new String[N1];
        for (int j = 0; j < N1; j++)
        {
            n1[j] = "";
            for (int i = 0; i < H; i++)
                n1[j] += in.next();
        }

        // System.err.println(Arrays.toString(n1));

        int S2 = in.nextInt();
        int N2 = S2 / H;
        String[] n2 = new String[N2];
        for (int j = 0; j < N2; j++)
        {
            n2[j] = "";
            for (int i = 0; i < H; i++)
                n2[j] += in.next();
        }

        String operation = in.next();

        long nombre1 = 0;
        for (int j = 0; j < N1; j++)
            nombre1 += Math.pow(20, N1 - j - 1) * literals.get(n1[j]);

        long nombre2 = 0;
        for (int j = 0; j < N2; j++)
            nombre2 += Math.pow(20, N2 - j - 1) * literals.get(n2[j]);

        System.err.print("-- " + nombre1 + operation + nombre2);

        long res = 0;
        switch (operation)
        {
            case "+":
                res = nombre1 + nombre2;
                break;
            case "*":
                res = nombre1 * nombre2;
                break;
            case "-":
                res = nombre1 - nombre2;
                break;
            case "/":
                res = nombre1 / nombre2;
                break;
            default:
                System.err.println("FUCK OFF !");
        }
        System.err.println(" = " + res);

        ArrayList<Integer> nn = new ArrayList<>();
        while (res > 0)
        {
            int c = (int) (res % 20);
            nn.add(c);

            res = res / 20;
        }

        if (nn.isEmpty())
            nn.add(0);

        System.err.println(nn);

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        for (int i = nn.size() - 1; i >= 0; i--)
        {
            String s = numbers.get(nn.get(i));
            for (int j = 0; j < H; j++)
                System.out.println(s.substring(j * L, j * L + L));
        }

    }
}
