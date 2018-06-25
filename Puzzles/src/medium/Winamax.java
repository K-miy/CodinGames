package medium;

import java.util.LinkedList;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Solution1
{
    LinkedList<Card> deck1 = new LinkedList<>();
    LinkedList<Card> deck2 = new LinkedList<>();

    public int tour = 0;

    public long timeS = System.currentTimeMillis();

    public void fight()
    {
        if (this.deck1.isEmpty())
            throw new TwoException(this.tour);

        if (this.deck2.isEmpty())
            throw new OneException(this.tour);

        this.tour++;

        // step 1 :
        Card c1 = this.deck1.pollFirst();
        Card c2 = this.deck2.pollFirst();

        if (c1.compareTo(c2) == 0)
            // step 2 : war
            this.war(c1, c2, new LinkedList<>(), new LinkedList<>());
        else if (c1.compareTo(c2) < 0)                               // c1 < c2
        {
            this.deck2.addLast(c1);
            this.deck2.addLast(c2);
        }
        else// c2 < c1
        {
            this.deck1.addLast(c1);
            this.deck1.addLast(c2);
        }

        // if (this.tour > 130)
        // throw new TwoException(1262);

    }

    public void run()
    {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(); // the number of cards for player 1
        for (int i = 0; i < n; i++)
            this.deck1.add(new Card(in.next())); // the n cards of player 1
        int m = in.nextInt(); // the number of cards for player 2
        for (int i = 0; i < m; i++)
            this.deck2.add(new Card(in.next())); // the m cards of player 2

        System.err.println(this.tour + "|P1 : " + this.deck1);
        System.err.println(this.tour + "|P2 : " + this.deck2);

        try
        {
            while (true)
                this.fight();
        }
        catch (RuntimeException e)
        {
            System.out.println(e.getMessage());
        }

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

    }

    public void war(Card p_c1, Card p_c2, LinkedList<Card> subdeck1, LinkedList<Card> subdeck2)
    {
        // if (!subdeck1.isEmpty())
        {
            System.err.println(this.tour + "|P1 : " + p_c1 + " - " + subdeck1 + " -- " + this.deck1);
            System.err.println(this.tour + "|P2 : " + p_c2 + " - " + subdeck2 + " -- " + this.deck2);
        }

        subdeck1.addLast(p_c1);
        subdeck2.addLast(p_c2);

        for (int i = 0; i < 3; i++)
        {
            if (this.deck1.isEmpty() || this.deck2.isEmpty())
                throw new PATException(this.tour);

            subdeck1.addLast(this.deck1.pollFirst());
            subdeck2.addLast(this.deck2.pollFirst());
        }

        if (this.deck1.isEmpty() || this.deck2.isEmpty())
            throw new PATException(this.tour);

        // step 1 :
        Card c1 = this.deck1.pollFirst();
        Card c2 = this.deck2.pollFirst();

        if (c1.compareTo(c2) == 0)
            // step 2 : war
            this.war(c1, c2, subdeck1, subdeck2);
        else if (c1.compareTo(c2) < 0)                                // c1 < c2
        {
            subdeck1.addLast(c1);
            subdeck2.addLast(c2);
            this.deck2.addAll(subdeck1);
            this.deck2.addAll(subdeck2);
        }
        else// c2 < c1
        {
            subdeck1.addLast(c1);
            subdeck2.addLast(c2);
            this.deck1.addAll(subdeck1);
            this.deck1.addAll(subdeck2);
        }

    }

    public static void main(String args[])
    {
        new Solution1().run();
    }

    class Card implements Comparable<Card>
    {
        public final int v;
        public final String s;

        public Card(String ss)
        {
            this.s = ss.substring(0, ss.length() - 1);
            switch (ss.substring(0, ss.length() - 1))
            {
                case "2":
                case "3":
                case "4":
                case "5":
                case "6":
                case "7":
                case "8":
                case "9":
                case "10":
                    this.v = Integer.parseInt(ss.substring(0, ss.length() - 1));
                    break;
                case "J":
                    this.v = 11;
                    break;
                case "Q":
                    this.v = 12;
                    break;
                case "K":
                    this.v = 13;
                    break;
                case "A":
                    this.v = 14;
                    break;
                default:
                    this.v = 0;
                    break;

            }
        }

        @Override
        public int compareTo(Card c)
        {
            return this.v - c.v;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return this.s;
        }

    }

    class OneException extends RuntimeException
    {
        public OneException(int gr)
        {
            super("1 " + gr);
        }

    }

    class PATException extends RuntimeException
    {
        public PATException(int gr)
        {
            super("PAT");
        }
    }

    class TwoException extends RuntimeException
    {
        public TwoException(int gr)
        {
            super("2 " + gr);
        }
    }

}
