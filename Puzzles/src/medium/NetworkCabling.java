package medium;

import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Solution4
{

    public void run()
    {
        Scanner in = new Scanner(System.in);
        int N = in.nextInt();
        System.err.println(N);
        Maison[] ms = new Maison[N];

        int maxY = -Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = -Integer.MAX_VALUE;
        int minX = Integer.MAX_VALUE;

        long sumY = 0;
        int meanY = 0;

        for (int i = 0; i < N; i++)
        {
            int X = in.nextInt();
            int Y = in.nextInt();
            System.err.println(X + " " + Y);
            ms[i] = new Maison(X, Y);

            if (Y > maxY)
                maxY = Y;
            if (Y < minY)
                minY = Y;
            if (X > maxX)
                maxX = X;
            if (X < minX)
                minX = X;

            sumY += Y;
        }

        long min = Long.MAX_VALUE;
        long bestY = Integer.MAX_VALUE;

        meanY = (int) (sumY / N);
        sumY = 0;
        for (Maison m : ms)
            sumY += (m.y - meanY) * (m.y - meanY);
        int stdY = (int) Math.sqrt(sumY / ms.length);

        System.err.println(meanY + " " + stdY + " " + minY + " " + maxY);
        if (stdY > 50)
            stdY = 50;
        if (stdY <= 1)
            stdY = 10;
        System.err.println(meanY + " " + stdY + " " + minY + " " + maxY);

        for (int y = meanY - stdY; y <= meanY + stdY; y++)
        {
            long s = 0;
            for (Maison m : ms)
                s += m.getD(y);

            if (s < min)
            {
                min = s;
                bestY = y;
            }

        }

        if (N < 50)
            for (Maison m1 : ms)
            {
                long s = 0;
                for (Maison m : ms)
                    s += m.getD(m1.y);

                if (s < min)
                {
                    min = s;
                    bestY = m1.y;
                }

            }

        System.err.println(bestY + " " + min + " " + minX + " " + maxX);
        long v = (maxX - minX) + min;

        System.out.println(v + "");
    }

    public static void main(String args[])
    {
        new Solution4().run();
    }

    class Maison
    {
        int x;
        int y;

        public Maison(int px, int py)
        {
            this.x = px;
            this.y = py;
        }

        int getD(int py)
        {
            return Math.abs(this.y - py);
        }

    }
}
