package medium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Scanner;

class Dijkstra
{
    public static void computePaths(Vertex source)
    {
        source.minDistance = 0.;

        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty())
        {
            Vertex u = vertexQueue.poll();

            // Visit each edge exiting u
            for (Edge e : u.adjacencies)
            {
                Vertex v = e.target;
                double weight = e.weight;

                double distanceThroughU = u.minDistance + weight;
                if (distanceThroughU < v.minDistance)
                {
                    vertexQueue.remove(v);
                    v.minDistance = distanceThroughU;
                    v.previous = u;
                    vertexQueue.add(v);
                }

            }
        }
    }

    public static ArrayList<Vertex> getShortestPathTo(Vertex target)
    {
        ArrayList<Vertex> path = new ArrayList<Vertex>();
        for (Vertex vertex = target; vertex != null; vertex = vertex.previous)
            path.add(vertex);

        Collections.reverse(path);
        return path;
    }
}

class Edge
{
    public final Vertex target;
    public final double weight;

    public Edge(Vertex argTarget)
    {
        this.target = argTarget;
        this.weight = 1;
    }

    @Override
    public boolean equals(Object p_o)
    {
        if (p_o instanceof Edge)
        {
            Edge e = (Edge) p_o;
            return this.target.id == e.target.id;
        }
        return false;
    }
}

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Player3
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        int N = in.nextInt(); // the total number of nodes in the level, including the gateways
        int L = in.nextInt(); // the number of links
        int E = in.nextInt(); // the number of exit gateways
        System.err.println(L);
        Vertex[] nodes = new Vertex[N];
        for (int i = 0; i < N; i++)
            nodes[i] = new Vertex(i);

        for (int i = 0; i < L; i++)
        {
            int N1 = in.nextInt(); // N1 and N2 defines a link between these nodes
            int N2 = in.nextInt();
            nodes[N1].adjacencies.add(new Edge(nodes[N2]));
            nodes[N2].adjacencies.add(new Edge(nodes[N1]));
        }

        Vertex[] exits = new Vertex[E];
        for (int i = 0; i < E; i++)
        {
            int EI = in.nextInt(); // the index of a gateway node
            exits[i] = nodes[EI];
        }

        // game loop
        while (true)
        {
            String s = "";
            int SI = in.nextInt(); // The index of the node on which the Skynet agent is positioned this turn

            Dijkstra.computePaths(nodes[SI]);
            System.err.println("Skynet on :" + nodes[SI]);

            double dmin = Double.MAX_VALUE;
            ArrayList<Vertex> path = null;
            for (Vertex v : exits)
            {
                System.err.println("Distance to " + v + ": " + v.minDistance);
                ArrayList<Vertex> p = Dijkstra.getShortestPathTo(v);
                System.err.println("Path: " + p);
                if (v.minDistance < dmin)
                {
                    dmin = v.minDistance;
                    path = p;
                }
            }

            if (path.size() <= 2)
            {
                s = path.get(path.size() - 1).id + " " + path.get(path.size() - 2).id;
                path.get(path.size() - 1).adjacencies.remove(new Edge(nodes[path.get(path.size() - 2).id]));
                path.get(path.size() - 2).adjacencies.remove(new Edge(nodes[path.get(path.size() - 1).id]));
            }
            else
            {
                s = path.get(1).id + " " + path.get(2).id;
                path.get(1).adjacencies.remove(new Edge(nodes[path.get(2).id]));
                path.get(2).adjacencies.remove(new Edge(nodes[path.get(1).id]));
            }

            for (Vertex v : nodes)
                v.reset();

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            System.out.println(s); // Example: 0 1 are the indices of the nodes you wish to sever the link between
        }
    }
}

class Vertex implements Comparable<Vertex>
{
    public final int id;
    public ArrayList<Edge> adjacencies = new ArrayList<Edge>();
    public double minDistance = Double.POSITIVE_INFINITY;
    public Vertex previous;

    public Vertex(int p_id)
    {
        this.id = p_id;
    }

    @Override
    public int compareTo(Vertex other)
    {
        return Double.compare(this.minDistance, other.minDistance);
    }

    public void reset()
    {
        this.previous = null;
        this.minDistance = Double.POSITIVE_INFINITY;
    }

    @Override
    public String toString()
    {
        return this.id + "";
    }
}
