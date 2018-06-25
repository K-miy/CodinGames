package medium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

class Node
{
    public int l; // line
    public int c; // col
    public Node r = null; // right
    public Node s = null; // south

    public Node(int x, int y)
    {
        this.l = y;
        this.c = x;
    }

    @Override
    public String toString()
    {
        return this.c + " " + this.l + " " + (this.r != null ? this.r.c : "-1") + " "
                + (this.r != null ? this.r.l : "-1") + " " + (this.s != null ? this.s.c : "-1") + " "
                + (this.s != null ? this.s.l : "-1");
    }
}

/**
 * Don't let the machines win. You are humanity's last hope...
 **/
class Player1
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        int width = in.nextInt(); // the number of cells on the X axis
        in.nextLine();
        int height = in.nextInt(); // the number of cells on the Y axis
        in.nextLine();

        char[][] c = new char[height][width];
        for (int i = 0; i < height; i++)
        {
            String line = in.nextLine(); // width characters, each either 0 or .
            c[i] = line.toCharArray();
        }

        for (int i = 0; i < height; i++)
            System.err.println(Arrays.toString(c[i]));

        Node[] lastYNode = new Node[width];
        Node lastXNode = null;

        ArrayList<Node> l = new ArrayList<Node>();

        for (int y = 0; y < height; y++)
        {
            lastXNode = null;
            for (int x = 0; x < width; x++)
                if (c[y][x] == '0')
                {
                    Node n = new Node(x, y);
                    System.err.println(n);

                    if (lastXNode != null) // Not First on line
                        lastXNode.r = n;

                    System.err.println(lastXNode);
                    lastXNode = n;

                    if (lastYNode[x] != null) // Not first on col
                        lastYNode[x].s = n;

                    lastYNode[x] = n;

                    l.add(n);
                }
        }

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        for (int i = 0; i < l.size(); i++)
            System.out.println(l.get(i));
    }
}
