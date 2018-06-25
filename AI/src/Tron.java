import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Tron
{
    static Random RAND = new Random();
    static final boolean DEBUG = true;
    static final boolean RELEASE = false;
    static final int NB_MOTOS = 2;

    static int MY_ID = 0;

    public static void ass(String p_s)
    {
        if (!RELEASE)
            System.err.println(" === ERROR === : " + p_s);
    }

    public static void db(String p_s)
    {
        if (DEBUG && !RELEASE)
            System.err.println("Dbg : " + p_s);
    }

    public void run()
    {
        Scanner in = new Scanner(System.in);

        Grid g = new Grid();
        Moto[] motos = new Moto[NB_MOTOS];

        for (int i = 0; i < NB_MOTOS; i++)
            // motos[i] = new Moto(i, new StrMonteCarloTreeSearch(i));
            // motos[i] = new Moto(i, new StrWallHugger(i));
            // motos[i] = new Moto(i, new StrCouard(i));
            motos[i] = new Moto(i, new StrMiniMaxCutter(i));

        State s = new State(g, motos);

        int turn = 0;

        // game loop
        while (true)
        {
            int N = in.nextInt(); // total number of players (2 to 4).
            int P = in.nextInt(); // your player number (0 to 3).

            MY_ID = P;
            for (int i = 0; i < N; i++)
            {
                int oY = in.nextInt(); // starting X coordinate of lightcycle (or -1)
                int oX = in.nextInt(); // starting Y coordinate of lightcycle (or -1)
                int nY = in.nextInt(); // new X coordinate of lightcycle (can be == X0 if you play before this player)
                int nX = in.nextInt(); // new Y coordinate of lightcycle (can be == Y0 if you play before this player)
                db("upd : " + i + "-> (" + oX + "," + oY + ") -> (" + nX + "," + nY + ")");
                s.upd(turn, i, oX, oY, nX, nY);
            }

            // System.err.println(g);

            Direction d = motos[P].decide(turn, s);

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            // A single line with UP, DOWN, LEFT or RIGHT
            System.out.println(d.toAction());
            turn++;
        }
    }

    public static void main(String args[])
    {
        new Tron().run();
    }

    public static void fillArea(int x, int y, int original, int fill, int[][] arr)
    {
        int maxX = arr.length - 1;
        int maxY = arr[0].length - 1;
        int[][] stack = new int[(maxX + 1) * (maxY + 1)][2];
        int index = 0;

        stack[0][0] = x;
        stack[0][1] = y;
        arr[x][y] = fill;

        while (index >= 0)
        {
            x = stack[index][0];
            y = stack[index][1];
            index--;

            if ((x > 0) && (arr[x - 1][y] == original))
            {
                arr[x - 1][y] = fill;
                index++;
                stack[index][0] = x - 1;
                stack[index][1] = y;
            }

            if ((x < maxX) && (arr[x + 1][y] == original))
            {
                arr[x + 1][y] = fill;
                index++;
                stack[index][0] = x + 1;
                stack[index][1] = y;
            }

            if ((y > 0) && (arr[x][y - 1] == original))
            {
                arr[x][y - 1] = fill;
                index++;
                stack[index][0] = x;
                stack[index][1] = y - 1;
            }

            if ((y < maxY) && (arr[x][y + 1] == original))
            {
                arr[x][y + 1] = fill;
                index++;
                stack[index][0] = x;
                stack[index][1] = y + 1;
            }
        }
    }

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
    public static List<Position> aStar(Position p_o, Position p_g, Grid p_map)
    {
        Tron pl = new Tron();
        LinkedList<Position> path = new LinkedList<>();

        TreeMap<AStar2DNode, Integer> open = new TreeMap<>();
        HashSet<AStar2DNode> close = new HashSet<>();
        open.put(pl.new AStar2DNode(p_o, 0, 0), 0);

        // Find path to goal
        while (!open.isEmpty() && !open.firstKey().p.equals(p_g))
        {
            AStar2DNode current = open.pollFirstEntry().getKey();
            close.add(current);

            for (Position p : current.p.casesAutour())
                if (p_map.isAccessible(p))
                {
                    int cost = current.g + 1;
                    AStar2DNode neighbor = pl.new AStar2DNode(p, cost, p.distanceL1(p_g));
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

    public class AStar2DNode implements Comparable<AStar2DNode>
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
         * Redéfinition.
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
            return (this.g + this.h) - (p_o.g + p_o.h);
        }
    }

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

    public enum Direction
    {
        /**
         * définitions de l'énumération. type de directions.
         */
        N, S, E, W;

        /**
         * Renvoie la direction inverse à p_dir
         *
         * @param p_dir
         * @return
         */
        public Direction inverse()
        {
            switch (this)
            {
                case N:
                    return S;
                case S:
                    return N;
                case W:
                    return E;
                case E:
                    return W;
                default:
                    return null;
            }
        }

        public String toAction()
        {
            switch (this)
            {
                case N:
                    return "UP";
                case S:
                    return "DOWN";
                case W:
                    return "LEFT";
                case E:
                    return "RIGHT";
                default:
                    return "";
            }
        }

        /**
         * Redéfinition pour débugage de la méthode toString.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            switch (this)
            {
                case N:
                    return "N";
                case S:
                    return "S";
                case W:
                    return "O";
                case E:
                    return "E";
                default:
                    return "";
            }
        }
    }

    enum Distance
    {
        MANHATTAN, CHEBYSHEV, EUCLIDEAN;

        /**
         * ¸ Dit si oui ou non, deux positions sont à une certain range selon un mesure donnée.
         *
         * @param p_p1
         *            Position 1
         * @param p_p2
         *            Position 2
         * @param p_d
         *            La mesure choisie
         * @param p_r
         *            la distance
         * @return vrai si p1 est à moins que p_r de p2 selon la mesure choisie
         */
        public boolean inside(Position p_p1, Position p_p2, int p_r)
        {
            switch (this)
            {
                case MANHATTAN:
                    return p_p1.distanceL1(p_p2) <= p_r;
                case CHEBYSHEV:
                    return p_p1.distanceL15(p_p2) <= p_r;
                default:
                    return p_p1.distanceL2(p_p2) <= p_r;
            }
        }
    }

    public class Grid
    {
        public static final int VIDE = 8;

        public final static int MY = 30;
        public final static int MX = 20;

        int[][] g = new int[MX][MY];
        Position[] m = new Position[NB_MOTOS];

        public Grid()
        {
            for (int x = 0; x < MX; x++)
                for (int y = 0; y < MY; y++)
                    this.g[x][y] = VIDE;
        }

        @Override
        public Grid clone()
        {
            Grid gg = new Grid();
            for (int x = 0; x < MX; x++)
                for (int y = 0; y < MY; y++)
                    gg.g[x][y] = this.g[x][y];

            for (int i = 0; i < this.m.length; i++)
                gg.m[i] = this.m[i];

            return gg;
        }

        public boolean isAccessible(Position p_p)
        {
            return this.isInside(p_p) && this.g[p_p.x][p_p.y] == VIDE;
        }

        public boolean isInside(Position p_p)
        {
            return p_p.x >= 0 && p_p.x < MX && p_p.y >= 0 && p_p.y < MY;
        }

        @Override
        public String toString()
        {
            StringBuilder s = new StringBuilder();
            s.append("|");
            for (int x = 0; x < MY; x++)
                s.append("_");
            s.append("|\n|");

            for (int x = 0; x < MX; x++)
            {
                for (int y = 0; y < MY; y++)
                    if (this.g[x][y] != VIDE)
                        s.append("" + this.g[x][y]);
                    else
                        s.append(" ");
                s.append("|\n|");
            }

            for (int x = 0; x < MY; x++)
                s.append("_");

            s.append("|\n");

            return s.toString();
        }

        public void upd(int p_turn, int nb, int ox, int oy, int nx, int ny)
        {
            if (ox == -1) // nb is dead;
            {
                for (int x = 0; x < MX; x++)
                    for (int y = 0; y < MY; y++)
                        if (this.g[x][y] == nb)
                            this.g[x][y] = VIDE;
                this.m[nb] = new Position(-1, -1);
            }
            else
            {
                // incohérence mais pas les premiers tours qui sont l'init de la position des motos
                if (this.g[ox][oy] != nb && p_turn > this.m.length)
                    System.err.println("Erreur de saisie précédente ? --- LOOK INTO IT !");

                this.g[nx][ny] = nb;
                this.m[nb] = new Position(nx, ny);
            }

        }

        public int whosThere(Position p_p)
        {
            return this.g[p_p.x][p_p.y];
        }

    }

    public abstract class IStrategy
    {
        int id = -1;

        public IStrategy(int p_id)
        {
            this.id = p_id;
        }

        public abstract Direction decide(int p_turn, State p_s);
    }

    public class Moto
    {
        int id;
        Position p;
        IStrategy s;

        public Moto(int p_id, IStrategy p_s)
        {
            this.id = p_id;
            this.s = p_s;
        }

        @Override
        public Moto clone()
        {
            Moto m = new Moto(this.id, this.s);
            m.p = this.p;
            return m;
        }

        public Direction decide(int p_turn, State p_s)
        {
            return this.s.decide(p_turn, p_s);
        }

        public boolean isDead()
        {
            return this.p.equals(new Position(-1, -1));
        }

        @Override
        public String toString()
        {
            return "[" + this.id + ":" + this.p + "]";
        }

        public void upd(int nx, int ny)
        {
            this.p = new Position(nx, ny);
        }
    }

    public class Position
    {
        /**
         * Position en x
         */
        final public int x;

        /**
         * ¸ Position en y
         */
        final public int y;

        /**
         * Constructeur d'initialisation. Initialise un objet Position avec sa position en x et en y
         *
         * @param p_X
         *            position en x
         * @param p_Y
         *            position en y
         */
        public Position(int p_X, int p_Y)
        {
            this.x = p_X;
            this.y = p_Y;
        }

        /**
         * Constructeur par copie. Initialise un objet Position avec la position en x et en y
         *
         * @param p_p
         *            position
         */
        public Position(Position p_p)
        {
            this.x = p_p.x;
            this.y = p_p.y;
        }

        /**
         * Retourne l'ensemble des positions autour de la position courante.
         *
         * @return
         */
        public HashSet<Position> casesAutour()
        {
            HashSet<Position> s = new HashSet<Position>();

            for (Direction d : Direction.values())
                s.add(this.relativePos(d));

            return s;
        }

        /**
         * Renvoie la distance (de Manhattan) entre deux position
         *
         * @param p_rel
         *            La position relative
         * @return la distance de Manhattan entre la position courante et la position relative
         */
        public int distanceL1(Position p_o)
        {
            return Math.abs(this.x - p_o.x) + Math.abs(this.y - p_o.y);
        }

        /**
         * Renvoie la distance de Chebyshev entre deux positions
         *
         * @param p_rel
         *            La position relative
         * @return la distance de Chebyshev calculée par max(abs(x1-x2),abs(y1-y2))
         */

        public int distanceL15(Position p_rel)
        {
            return Math.max(Math.abs(this.x - p_rel.x), Math.abs(this.y - p_rel.y));
        }

        /**
         * Retourne la distance L2 (euclidienne) entre deux positions
         *
         * @param p_rel
         *            La position relative
         * @return la distance euclidienne entre la position courante et la position relative
         */
        public double distanceL2(Position p_rel)
        {
            return Math.sqrt(Math.pow(this.x - p_rel.x, 2) + Math.pow(this.y - p_rel.y, 2));
        }

        /**
         * Renvoie la distance (Infinity) entre deux position
         *
         * @param p_rel
         *            La position relative
         * @return la distance L-infty entre la position courante et la position relative
         */
        public int distanceLI(Position p_o)
        {
            return Math.max(Math.abs(this.x - p_o.x), Math.abs(this.y - p_o.y));
        }

        /**
         * Redéfinition.
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

            Position other = (Position) obj;
            if (this.x != other.x)
                return false;
            if (this.y != other.y)
                return false;
            return true;
        }

        /**
         * Calcule les positions dans un certain range selon une certaine mesure.
         *
         * @param p_r
         *            le range
         * @param p_d
         *            la mesure
         * @return un ensemble de positions
         */
        private HashSet<Position> getInRange(int p_r, Distance p_d)
        {
            HashSet<Position> s = new HashSet<Position>();

            for (int x = 0; x <= p_r; x++)
            {
                int y = 0;
                Position p = new Position(this.x + x, this.y + y);

                while (p_d.inside(this, p, p_r))
                {
                    s.add(p);
                    s.add(new Position(this.x - x, this.y + y));
                    s.add(new Position(this.x + x, this.y - y));
                    s.add(new Position(this.x - x, this.y - y));
                    p = new Position(this.x + x, this.y + ++y);
                }
            }
            return s;
        }

        /**
         * Calcule les position dans le range d'une certaine distance de Manhattan
         *
         * @param p_r
         * @return
         */
        public HashSet<Position> getInRangeL1(int p_r)
        {

            return this.getInRange(p_r, Distance.MANHATTAN);
        }

        /**
         * Calcule les position dans le range d'une certaine distance Euclidienne
         *
         * @param p_r
         * @return
         */
        public HashSet<Position> getInRangeL2(int p_r)
        {
            return this.getInRange(p_r, Distance.EUCLIDEAN);
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
            result = prime * result + this.x;
            result = prime * result + this.y;
            return result;
        }

        /**
         * Retourne la direction relative d'une position par rapport à la position courante.
         *
         * @param p_Pos
         *            La position dont on cherche la direction
         * @return La direction relative à p_Pos
         */
        public Direction relativeDir(Position p_Pos)
        {
            if (this.equals(p_Pos))
                throw new IllegalArgumentException(
                        "Il n'est pas possible de calculer la direction de la position courante.");
            double theta = (Math.atan2(p_Pos.y - this.y, p_Pos.x - this.x));
            if (theta < 0)
                theta += Math.PI * 2;

            // System.out.println(theta);
            Direction[] dir = { Direction.S, Direction.E, Direction.N, Direction.W, Direction.S };

            int i = 0;
            double search = Math.PI / 8;
            while (theta > search)
            {
                i++;
                search += Math.PI / 2;
            }

            return dir[i];
        }

        /**
         * Retourne une nouvelle position relative selon la direction passée en paramètre.
         *
         * @param p_Direction
         *            une direction donnée
         * @return newPos une nouvelle position
         */
        public Position relativePos(Direction p_Direction)
        {
            Position newPos = null;

            if (p_Direction == null)
                return this;

            switch (p_Direction)
            {
                case N:
                    newPos = new Position(this.x - 1, this.y);
                    break;
                case S:
                    newPos = new Position(this.x + 1, this.y);
                    break;
                case E:
                    newPos = new Position(this.x, this.y + 1);
                    break;
                case W:
                    newPos = new Position(this.x, this.y - 1);
                    break;
                default:
            }
            return newPos;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "" + this.x + " " + this.y + "";
        }

    }

    public class MinMaxSearchNode
    {
        HashMap<Direction, MinMaxSearchNode> nx = new HashMap<>();
        double v = Double.NEGATIVE_INFINITY;
        Direction best = null;
        State s;

        public MinMaxSearchNode(State p_s)
        {
            this.s = p_s;
        }

        public void expand(int p_id)
        {
            List<Direction> possible = this.s.getAcc(p_id);
            if (possible.isEmpty())
                if (p_id == MY_ID)
                    this.v = -100;
                else
                    this.v = 100;
            else
                for (Direction d : possible)
                    this.nx.put(d, new MinMaxSearchNode(this.s.next(0, p_id, d)));
        }

        public void value(int p_id)
        {

            db("Value in :: " + "(" + this.nx.isEmpty() + ")" + this + "");
            if (this.nx.isEmpty()) // Leafs
            {
                Position mp = this.s.getPos(MY_ID);
                Position op = this.s.getPos(1 - MY_ID);
                if (aStar(mp, op, this.s.g).isEmpty())
                { // Then we are sperated
                    Grid gc = this.s.g.clone();

                    fillArea(mp.x, mp.y, Grid.VIDE, 10 + MY_ID, gc.g);
                    fillArea(op.x, op.y, Grid.VIDE, 10 + 1 - MY_ID, gc.g);

                    int mc = 0, oc = 0;
                    for (int x = 0; x < Grid.MX; x++)
                        for (int y = 0; y < Grid.MY; y++)
                        {
                            if (gc.g[x][y] == 10 + MY_ID)
                                mc++;
                            if (gc.g[x][y] == 10 + 1 - MY_ID)
                                oc++;
                        }

                    this.v = 12 + ((float) Math.abs(mc - oc)) / ((float) Math.max(mc, oc)) * 86;
                    if (oc > mc)
                        this.v *= -1;
                    // the result should be between -12..-99 when the opponent has more space
                    // and between 12..99 when we have more space !
                }
                else
                    this.v = 0;

                db("Leaf :: " + this + "");
            }
            else if (p_id == MY_ID) // my turn
            {
                this.v = Double.NEGATIVE_INFINITY;
                // Max over subnodes
                for (Entry<Direction, MinMaxSearchNode> n : this.nx.entrySet())
                    if (n.getValue().v > this.v)
                    {
                        this.v = n.getValue().v;
                        this.best = n.getKey();
                    }
            }
            else // others turn
            {
                this.v = Double.POSITIVE_INFINITY;
                // Min over subnodes
                for (Entry<Direction, MinMaxSearchNode> n : this.nx.entrySet())
                    if (n.getValue().v < this.v)
                    {
                        this.v = n.getValue().v;
                        this.best = n.getKey();
                    }
            }

            // db("Value out :: " + this + "");
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "[ v=" + this.v + ", best=" + this.best + ", nx=" + this.nx + "]";
        }

    }

    public class State
    {
        Grid g;
        Moto[] m;

        public State(Grid p_g, Moto[] p_motos)
        {
            this.g = p_g;
            this.m = p_motos;
        }

        public ArrayList<Direction> getAcc(int nb)
        {
            ArrayList<Direction> possible = new ArrayList<>();
            Position p = this.getPos(nb);

            for (Direction d : Direction.values())
                if (this.g.isAccessible(p.relativePos(d)))
                    possible.add(d);
            return possible;
        }

        public Position getPos(int nb)
        {
            if (!this.m[nb].p.equals(this.g.m[nb]))
                ass("/!\\/!\\/!\\ State Coherence Error /!\\/!\\/!\\");

            return this.g.m[nb];
        }

        public State next(int p_t, int p_id, Direction p_act)
        {

            // Clone all motos
            Moto[] mc = new Moto[this.m.length];
            for (int i = 0; i < this.m.length; i++)
                mc[i] = this.m[i].clone();

            State n = new State(this.g.clone(), mc);

            Position op = mc[p_id].p;
            Position np = op.relativePos(p_act);

            if (!this.g.isAccessible(np))
                db(op + "/" + p_act + "->" + np);

            n.upd(p_t + 1, p_id, op.x, op.y, np.x, np.y);

            return n;
        }

        @Override
        public String toString()
        {
            return Arrays.toString(this.m) + "\n" + this.g;
        }

        public void upd(int p_turn, int nb, int ox, int oy, int nx, int ny)
        {
            this.g.upd(p_turn, nb, ox, oy, nx, ny);
            this.m[nb].upd(nx, ny);

            // Assertion :
            if (p_turn > this.m.length)
                for (Moto mm : this.m)
                    if (!mm.p.equals(this.g.m[mm.id]))
                        ass("/!\\/!\\/!\\ State Coherence Error /!\\/!\\/!\\");
        }
    }

    public class StrCouard extends IStrategy
    {

        public StrCouard(int p_id)
        {
            super(p_id);
        }

        @Override
        public Direction decide(int p_t, State p_s)
        {
            Direction dir = Direction.N;

            TreeMap<Double, Direction> t = new TreeMap<>();

            Position p = p_s.getPos(this.id);
            Position op = p_s.getPos(1 - this.id);

            for (Direction d : p_s.getAcc(this.id))
                t.put(p.relativePos(d).distanceL2(op), d);

            System.err.println(t);

            if (!t.isEmpty())
                return t.lastEntry().getValue();
            else
                return dir;

        }
    }

    public class StrMiniMaxCutter extends StrWallHugger
    {

        public StrMiniMaxCutter(int p_id)
        {
            super(p_id);
            // this.TIME_LEFT = 10;
            // this.TRAJ_LENGTH = 100;
        }

        @Override
        public Direction decide(int p_t, State p_s)
        {
            Direction dir = super.decide(p_t, p_s);
            Position meP = p_s.getPos(this.id);
            Position oP = p_s.getPos(1 - this.id);

            // Distance with other player is less than 5
            if (meP.distanceL1(oP) < 5) // do minimax
            {
                MinMaxSearchNode s = new MinMaxSearchNode(p_s);
                s.expand(this.id);
                for (MinMaxSearchNode ss : s.nx.values())
                {
                    ss.expand(1 - this.id);
                    for (MinMaxSearchNode sss : ss.nx.values())
                    {
                        sss.expand(this.id);
                        for (MinMaxSearchNode ssss : sss.nx.values())
                            ssss.value(Grid.VIDE);

                        sss.value(this.id);
                    }
                    ss.value(1 - this.id);
                }
                s.value(this.id);

                db(s + "");
                dir = s.best;
            }
            else
            {
                // Cut the grid according to possible dirs

                // and flood them to count the possible points

            }

            return dir;
        }
    }

    public class StrMonteCarloTreeSearch extends StrRandomAccessible
    {
        public int TIME_LEFT = 98;

        public int TRAJ_LENGTH = 200;

        public StrMonteCarloTreeSearch(int p_id)
        {
            super(p_id);
        }

        @Override
        public Direction decide(int p_t, State p_s)
        {
            Direction dir = super.decide(p_t, p_s);

            long timer_start = System.currentTimeMillis();

            int[] val = new int[Direction.values().length];

            ArrayList<Direction> possible = p_s.getAcc(this.id);

            StrRandomAccessible str = new StrRandomAccessible(this.id);
            StrRandomAccessible advStr = new StrRandomAccessible(1 - this.id);

            while (System.currentTimeMillis() - timer_start < this.TIME_LEFT)
                for (Direction d : possible)
                {
                    int vald = 0;

                    Direction fd = d;
                    State ns = p_s;

                    while (fd != null && vald < this.TRAJ_LENGTH)
                    {
                        vald++;

                        ns = ns.next(p_t, this.id, fd);

                        Direction advD = advStr.decide(p_t, ns);

                        if (advD == null)
                            vald = this.TRAJ_LENGTH;
                        else
                            ns = ns.next(p_t + vald, 1 - this.id, advD);

                        fd = str.decide(p_t, ns);
                    }

                    val[d.ordinal()] += vald;
                }

            TreeMap<Integer, Direction> t = new TreeMap<>();

            for (Direction d : possible)
                t.put(val[d.ordinal()], d);

            System.err.println(t);

            if (!t.isEmpty())
                return t.lastEntry().getValue();
            else
                return dir;
        }

    }

    public class StrRandomAccessible extends IStrategy
    {
        public StrRandomAccessible(int p_id)
        {
            super(p_id);
        }

        @Override
        public Direction decide(int p_t, State p_s)
        {
            Direction dir = Direction.N;

            if (this.id == -1)
            {
                ass("Deciding for no moto ! How the FUCK !");
                return dir;
            }

            List<Direction> possible = p_s.getAcc(this.id);

            // System.err.println("p : " + p_s.getPos(this.id) + " -> " + possible);

            Collections.shuffle(possible);

            if (possible.isEmpty())
                // db("I'm f*ck*ng BLOCKED !");
                return null;

            return possible.get(0);
        }

    }

    public class StrWallHugger extends IStrategy
    {
        final List<Direction> ORDER = Arrays.asList(Direction.values());

        public StrWallHugger(int p_id)
        {
            super(p_id);
            Collections.shuffle(this.ORDER);
        }

        @Override
        public Direction decide(int p_t, State p_s)
        {
            Direction d = this.ORDER.get(0);
            for (Direction c : this.ORDER)
            {
                Position p = p_s.getPos(this.id).relativePos(c);
                if (p_s.g.isAccessible(p))
                    for (Position pp : p.casesAutour())
                        if (!p_s.g.isAccessible(pp))
                            d = c;
            }
            return d;
        }
    }

    public class Vector2D extends Point2D.Double
    {

        /*
         * (non-Javadoc)
         *
         * @see java.awt.geom.Point2D.Double#Point2D.Double()
         */
        public Vector2D()
        {
            super();
        }

        /*
         * (non-Javadoc)
         *
         * @see java.awt.geom.Point2D.Double#Point2D.Double()
         */
        public Vector2D(double x, double y)
        {
            super(x, y);
        }

        /**
         * Copy constructor
         */
        public Vector2D(Vector2D v)
        {
            this.x = v.x;
            this.y = v.y;
        }

        /** Product of components of the vector: compenentProduct( <x y>) = x*y. */
        public double componentProduct()
        {
            return this.x * this.y;
        }

        /** Componentwise product: <this.x*rhs.x, this.y*rhs.y> */
        public Vector2D componentwiseProduct(Vector2D rhs)
        {
            return new Vector2D(this.x * rhs.x, this.y * rhs.y);
        }

        /**
         * Since Vector2D works only in the x-y plane, (u x v) points directly along the z axis. This function returns
         * the value on the z axis that (u x v) reaches.
         *
         * @return signed magnitude of (this x rhs)
         */
        public double crossProduct(Vector2D rhs)
        {
            return this.x * rhs.y - this.y * rhs.x;
        }

        /** Dot product of the vector and rhs */
        public double dotProduct(Vector2D rhs)
        {
            return this.x * rhs.x + this.y * rhs.y;
        }

        /**
         * @return the radius (length, modulus) of the vector in polar coordinates
         */
        public double getR()
        {
            return Math.sqrt(this.x * this.x + this.y * this.y);
        }

        /**
         * @return the angle (argument) of the vector in polar coordinates in the range [-pi/2, pi/2]
         */
        public double getTheta()
        {
            return Math.atan2(this.y, this.x);
        }

        /**
         * An alias for getR()
         *
         * @return the length of this
         */
        public double length()
        {
            return this.getR();
        }

        /** The difference of the vector and rhs: this - rhs */
        public Vector2D minus(Vector2D rhs)
        {
            return new Vector2D(this.x - rhs.x, this.y - rhs.y);
        }

        /** The sum of the vector and rhs */
        public Vector2D plus(Vector2D rhs)
        {
            return new Vector2D(this.x + rhs.x, this.y + rhs.y);
        }

        /** Product of the vector and scalar */
        public Vector2D scalarMult(double scalar)
        {
            return new Vector2D(scalar * this.x, scalar * this.y);
        }

        /*
         * (non-Javadoc)
         *
         * @see java.awt.geom.Point2D.Double#setLocation(double, double)
         */
        public void set(double x, double y)
        {
            super.setLocation(x, y);
        }

        /**
         * Sets the vector given polar arguments.
         *
         * @param r
         *            The new radius
         * @param t
         *            The new angle, in radians
         */
        public void setPolar(double r, double t)
        {
            super.setLocation(r * Math.cos(t), r * Math.sin(t));
        }

        /** Sets the vector's radius, preserving its angle. */
        public void setR(double r)
        {
            double t = this.getTheta();
            this.setPolar(r, t);
        }

        /** Sets the vector's angle, preserving its radius. */
        public void setTheta(double t)
        {
            double r = this.getR();
            this.setPolar(r, t);
        }

        /** Polar version of the vector, with radius in x and angle in y */
        public Vector2D toPolar()
        {
            return new Vector2D(Math.sqrt(this.x * this.x + this.y * this.y), Math.atan2(this.y, this.x));
        }

        /** Rectangular version of the vector, assuming radius in x and angle in y */
        public Vector2D toRect()
        {
            return new Vector2D(this.x * Math.cos(this.y), this.x * Math.sin(this.y));
        }

        @Override
        public String toString()
        {
            return "<" + this.x + ", " + this.y + ">";
        }

        /**
         * Returns a new vector with the same direction as the vector but with length 1, except in the case of zero
         * vectors, which return a copy of themselves.
         */
        public Vector2D unitVector()
        {
            if (this.getR() != 0)
                return new Vector2D(this.x / this.getR(), this.y / this.getR());
            return new Vector2D(0, 0);
        }
    }
}
