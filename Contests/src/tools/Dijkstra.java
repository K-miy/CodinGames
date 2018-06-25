/**
 *
 */
package tools;

import java.util.ArrayList;

/**
 * MOUHAHAHAHAHAH !
 *
 * @author C. Besse
 */
public class Dijkstra
{
    private int[][] grid;
    private int[][] maze;
    private Position goal;
    private Position start;
    private ArrayList<Position> path = new ArrayList<Position>();

    /**
     * Constructeur
     *
     * @param maze
     * @param start
     * @param goal
     */
    public Dijkstra(int[][] maze, Position start, Position goal)
    {
        this.maze = maze;
        this.grid = new int[maze.length][maze[0].length];
        for (int i = 0; i < this.grid.length; i++)
            for (int j = 0; j < this.grid[0].length; j++)
                if (maze[i][j] == 0)
                    this.grid[i][j] = -1;
                else
                    this.grid[i][j] = Integer.MAX_VALUE;

        this.start = start;
        this.goal = goal;

    }

    private boolean nearEquals(double p_d, double p_d2)
    {
        if (Math.abs(p_d - p_d2) < 0.01)
            return true;
        else
            return false;
    }

    /**
     * Tiens Toé !
     *
     * @return
     */
    public ArrayList<Position> solve()
    {

        this.grid[this.goal.x][this.goal.y] = 0;
        for (Position around : this.goal.casesAutour())
            this.grid[around.x][around.y] = this.goal.distanceL15(around);

        boolean modif = true;
        while ((this.grid[this.start.x][this.start.y] == Integer.MAX_VALUE) && modif)
        {
            modif = false;
            for (int i = 0; i < this.grid.length; i++)
                for (int j = 0; j < this.grid[0].length; j++)
                    if (this.grid[i][j] < Integer.MAX_VALUE && this.grid[i][j] > -1)
                    {
                        Position now = new Position(i, j);
                        for (Position a : now.casesAutour())
                            if (this.maze[a.x][a.y] != 0
                                    && this.grid[a.x][a.y] > this.grid[now.x][now.y] + now.distanceL15(a))
                            {
                                this.grid[a.x][a.y] = this.grid[now.x][now.y] + now.distanceL15(a);
                                modif = true;
                            }
                    }
        }

        if (!modif)
            return this.path;

        Position curr = new Position(this.start);

        int v = this.grid[this.start.x][this.start.y];
        int stopper = 0;
        while (v > 0 && stopper < Math.max(this.grid.length, this.grid[0].length))
        {
            for (Position nex : curr.casesAutour())
            {
                Position next = nex;
                int g = this.grid[next.x][next.y];
                int d = v - curr.distanceL15(next);
                if (this.nearEquals(g, d))
                {
                    this.path.add(next);

                    v -= curr.distanceL15(next);
                    curr = next;
                    break;
                }
            }
            stopper++;
        }

        if (this.path.size() == 1)
            this.path.add(this.goal);

        return this.path;
    }

    /**
     * Redéfinition.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder("");
        for (int i = 0; i < this.maze.length; i++)
        {
            for (int j = 0; j < this.maze[0].length; j++)
                if (this.path.contains(new Position(i, j)))
                    s.append("X");
                else
                    s.append((this.maze[i][j] == 0 ? "0" : "1"));
            s.append("\n");
        }
        s.append("\n");
        for (int i = 0; i < this.maze.length; i++)
        {
            for (int j = 0; j < this.maze[0].length; j++)
                if (this.path.contains(new Position(i, j)))
                    s.append("X");
                else
                    s.append((this.grid[i][j] < 0 ? "0" : (this.grid[i][j] > 10 ? "Z" : (this.grid[i][j]) + "")));
            s.append("\n");
        }

        return s.toString();
    }
}
