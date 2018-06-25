package tools;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public class AStar
{

    /**
     * A* Algorithm
     *
     * @param p_o
     *            Origin
     * @param p_g
     *            Goal
     * @param p_map
     *            Map : A 2D Grid
     * @return A path from Origin to Goal, origin <b>NOT</b> included, goal included. Empty if there is no path from
     *         origin to goal.
     */
    public static List<Position> aStar(Position p_o, Position p_g, int[][] p_map)
    {
        LinkedList<Position> path = new LinkedList<>();

        TreeMap<AStar2DNode, Integer> open = new TreeMap<>();
        HashSet<AStar2DNode> close = new HashSet<>();
        open.put(new AStar2DNode(p_o, 0, 0), 0);

        // Find path to goal
        while (!open.isEmpty() && !open.firstKey().p.equals(p_g))
        {
            AStar2DNode current = open.pollFirstEntry().getKey();
            close.add(current);

            for (Position p : current.p.casesAutour())
                if (p_map[p.x][p.y] == 0)
                {
                    int cost = current.g + 1;
                    AStar2DNode neighbor = new AStar2DNode(p, cost, p.distanceL1(p_g));
                    neighbor.prev = current;

                    if (open.containsKey(neighbor) && (open.get(neighbor) - neighbor.h) > neighbor.g)
                        open.remove(neighbor);

                    if (!open.containsKey(neighbor) && !close.contains(neighbor))
                        open.put(neighbor, neighbor.h + neighbor.g);
                }
        }

        // reconstruct path if possible
        if (!open.isEmpty() && open.firstKey().p.equals(p_g))
        {
            AStar2DNode current = open.firstKey();
            while (!current.p.equals(p_o))
            {
                path.addFirst(current.p);
                current = current.prev;
            }
        }

        return path;
    }

    public static class AStar2DNode implements Comparable<AStar2DNode>
    {
        Position p;
        int g;
        int h;
        AStar2DNode prev = null;

        public AStar2DNode(Position p_p, int p_g, int p_h)
        {
            this.p = p_p;
            this.g = p_g;
            this.h = p_h;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.p == null) ? 0 : this.p.hashCode());
            return result;
        }

        /**
         * USELESS because TreeMap use ONLY compareTo !.
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (this.getClass() != obj.getClass())
                return false;
            AStar2DNode other = (AStar2DNode) obj;
            if (this.p == null)
            {
                if (other.p != null)
                    return false;
            }
            else if (!this.p.equals(other.p))
                return false;
            return true;
        }

        @Override
        public int compareTo(AStar2DNode p_o)
        {
            if (((this.g + this.h) - (p_o.g + p_o.h)) == 0)
                return 1;
            else
                return (this.g + this.h) - (p_o.g + p_o.h);

        }
    }

}
