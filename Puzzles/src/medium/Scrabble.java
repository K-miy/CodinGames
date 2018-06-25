package medium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Solution9
{
    static HashMap<Character, Integer> sc = new HashMap<>();

    public void run()
    {
        Scanner in = new Scanner(System.in);

        int N = in.nextInt();
        in.nextLine();

        char[] one = { 'e', 'a', 'i', 'o', 'n', 'r', 't', 'l', 's', 'u' };
        char[] two = { 'd', 'g' };
        char[] three = { 'b', 'c', 'm', 'p' };
        char[] four = { 'f', 'h', 'v', 'w', 'y' };
        char[] five = { 'k' };
        char[] height = { 'j', 'x' };
        char[] ten = { 'q', 'z' };

        for (char c : one)
            sc.put(c, 1);
        for (char c : two)
            sc.put(c, 2);
        for (char c : three)
            sc.put(c, 3);
        for (char c : four)
            sc.put(c, 4);
        for (char c : five)
            sc.put(c, 5);
        for (char c : height)
            sc.put(c, 8);
        for (char c : ten)
            sc.put(c, 10);

        HashMap<Word, Integer> dict = new HashMap<>();
        ArrayList<Word> wordOrder = new ArrayList<>();

        for (int i = 0; i < N; i++)
        {
            String W = in.nextLine();
            Word ww = new Word(W);
            dict.put(ww, ww.n);
            wordOrder.add(ww);
            // System.err.println(ww);
        }

        System.err.println("------------------");
        String LETTERS = in.nextLine();

        Word l = new Word(LETTERS);

        PriorityQueue<Word> p = new PriorityQueue<>();
        HashSet<Integer> values = new HashSet<>();

        for (Word s : wordOrder)
            if (l.contains(s) && !(values.contains(s.n)))
            {
                // System.err.println(s + " -> " + l.contains(s) + " - " + !values.contains(s.n));
                p.add(s);
                values.add(s.n);
            }

        System.err.println(values.size());
        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        System.out.println(p.peek());
    }

    public static void main(String args[])
    {
        new Solution9().run();
    }

    private static Integer score(String p_w)
    {
        int score = 0;
        for (char c : p_w.toCharArray())
            score += sc.get(c);
        return score;
    }

    class Word implements Comparable<Word>
    {
        final int n;
        final String w;

        HashMap<Character, Integer> count = new HashMap<>();

        public Word(String p_w)
        {
            this.w = p_w;
            this.n = score(p_w);
            this.count(p_w);
        }

        @Override
        public int compareTo(Word p_a)
        {
            return -1 * (this.n - p_a.n);
        }

        public boolean contains(Word p_w)
        {
            boolean contain = true;

            for (Character c : p_w.count.keySet())
            {
                int a = 0;
                if (this.count.get(c) != null)
                    a = this.count.get(c);

                contain &= a >= p_w.count.get(c);
            }
            return contain;
        }

        private void count(String p_w)
        {
            for (char c : p_w.toCharArray())
                if (this.count.get(c) == null)
                    this.count.put(c, 1);
                else
                    this.count.put(c, this.count.get(c) + 1);
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return this.w;
        }

    }

}
