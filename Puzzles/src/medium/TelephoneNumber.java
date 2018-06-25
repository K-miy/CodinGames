
package medium;

import java.util.HashMap;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Solution6
{

    public void run()
    {
        Scanner in = new Scanner(System.in);
        int N = in.nextInt();

        NumberStorer n = new NumberStorer();
        for (int i = 0; i < N; i++)
        {
            String telephone = in.next();
            System.err.println(telephone);
            n.add(telephone);
        }

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        // The number of elements (referencing a number) stored in the structure.
        System.out.println(n.size + "");
    }

    public static void main(String args[])
    {
        new Solution6().run();
        // Solution s = new Solution();
        // NumberStorer n = s.new NumberStorer();
        // n.add("0412578440");
        // n.add("0412199803");
        // n.add("0468892011");
        // n.add("112");
        // n.add("15");
        // n.add("13");
    }

    class NodeNumber extends NumberStorer
    {
        final int n;

        public NodeNumber(int p_n)
        {
            this.n = p_n;
        }
    }

    class NumberStorer
    {
        protected HashMap<Integer, NodeNumber> nx = new HashMap<>();

        int size = 0;
        private boolean sizeUpd = false;

        public void add(String number)
        {
            if (number.isEmpty())
                return;

            int a = Integer.parseInt(number.substring(0, 1));
            if (this.nx.get(a) == null)
                this.nx.put(a, new NodeNumber(a));

            this.nx.get(a).add(number.substring(1));
            this.sizeUpd = false;
            this.recomputeSize();
        }

        protected void recomputeSize()
        {
            if (!this.sizeUpd)
            {
                int s = this.nx.size();
                for (NodeNumber n : this.nx.values())
                {
                    n.recomputeSize();
                    s += n.size;
                }
                this.size = s;
                this.sizeUpd = true;
            }
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return (this.sizeUpd ? this.size + "+" : this.size + "-") + "[" + this.nx + "]";
        }

    }
}
