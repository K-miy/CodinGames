package easy;

import java.util.HashMap;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Solution7
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        int L = in.nextInt();
        in.nextLine();
        int H = in.nextInt();
        in.nextLine();
        String T = in.nextLine();

        // Init map
        HashMap<Character, String[]> letters = new HashMap<Character, String[]>();
        for (char c = 'A'; c <= 'Z'; c++)
            letters.put(c, new String[H]);
        letters.put('?', new String[H]);

        // full map
        for (int i = 0; i < H; i++)
        {
            String ROW = in.nextLine();
            int index = 0;
            for (char c = 'A'; c <= 'Z'; c++)
            {
                index = c - 'A';
                letters.get(c)[i] = ROW.substring(index * L, (index + 1) * L);
            }
            index++;
            // letters.get('?')[i] = ROW.substring(ROW.length()-L-1,ROW.length()-1);
            letters.get('?')[i] = ROW.substring(index * L, (index + 1) * L);
        }

        // Test map
        /*
         * for(char c = 'A'; c <= 'Z'; c++) { for (int i = 0; i < H; i++) { System.err.println(letters.get(c)[i]); }
         * System.err.println("----------------------------"); } for (int i = 0; i < H; i++) {
         * System.err.println(letters.get('?')[i]); } System.err.println("----------------------------");
         */

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        String[] result = new String[H];
        for (int i = 0; i < H; i++)
            result[i] = "";

        for (char c : T.toCharArray())
        {
            char cU = Character.toUpperCase(c);
            if (!letters.containsKey(cU))
                cU = '?';

            for (int i = 0; i < H; i++)
                result[i] += letters.get(cU)[i];
        }

        for (int i = 0; i < H; i++)
            System.out.println(result[i]);

    }
}
