import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class BackToCode
{

    public void run()
    {
        Scanner in = new Scanner(System.in);
        int opponentCount = in.nextInt(); // Opponent count

        AI me = new AI();
        AI[] opp = new AI[opponentCount];
        for (int i = 0; i < opponentCount; i++)
            opp[i] = new AI();

        Screen s = new Screen();

        // game loop
        while (true)
        {
            int gameRound = in.nextInt(); // gameRound
            int x = in.nextInt(); // Your x position
            int y = in.nextInt(); // Your y position
            int backInTimeLeft = in.nextInt(); // Remaining back in time

            me.upd(x, y, backInTimeLeft);

            for (int i = 0; i < opponentCount; i++)
                // X,Y and remaining back in time.
                opp[i].upd(in.nextInt(), in.nextInt(), in.nextInt());

            String[] sc = new String[20];
            for (int i = 0; i < 20; i++)
                sc[i] = in.next(); // One line of the map ('.' = free, '0' = you, otherwise the id of the opponent)

            s.upd(gameRound, sc);

            Position p = me.decide(s);

            System.out.println(p.toInvString());

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            // System.err.println(x + "," + y + "," + backInTimeLeft);
            // action: "x y" to move or "BACK rounds" to go back in time
            // if (x == 34)
            // System.out.println("34 19");
            // else if (y == 0)
            // System.out.println("34 0");
            // else
            // System.out.println("17 0");
        }
    }

    public static void main(String args[])
    {
        new BackToCode().run();
    }

    class AI
    {
        public int x;
        public int y;
        public boolean back;

        public Position decide(Screen p_s)
        {
            // TODO Auto-generated method stub
            return null;
        }

        public void upd(int px, int py, int pb)
        {
            this.x = px;
            this.y = py;
            this.back = (pb == 1);
        }

    }

    class Dijkstra
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

    enum Direction
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

    class Position
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

        public String toInvString()
        {
            return "" + this.y + " " + this.x + "";
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

    class Screen
    {
        public int gr;
        public int[][] s = new int[20][35];

        public int[] scores = new int[4];

        public void upd(int p_gameRound, String[] p_sc)
        {
            this.gr = p_gameRound;
            for (int l = 0; l < 20; l++)
            {
                char[] line = p_sc[l].toCharArray();
                for (int c = 0; c < 35; c++)
                {
                    if (line[c] == '.')
                        this.s[l][c] = 0;
                    else
                        this.s[l][c] = (Integer.parseInt(line[c] + "") + 1);

                    if (this.s[l][c] != 0)
                        this.scores[this.s[l][c] - 1]++;
                }
            }

        }

    }

}
