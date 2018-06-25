
package hard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Player
{

    static Position EXIT = null;

    public static void main(String args[])
    {

        Scanner in = new Scanner(System.in);
        int W = in.nextInt(); // number of columns.
        int H = in.nextInt(); // number of rows.
        in.nextLine();

        IndiMap m = new IndiMap(W, H);
        IndiMap.r = new boolean[H][W];

        for (int i = 0; i < H; i++)
        {
            String LINE = in.nextLine(); // represents a line in the grid and contains W integers. Each integer
                                         // represents one room of a given type.
            String[] l = LINE.split(" ");
            for (int j = 0; j < l.length; j++)
            {
                int v = Integer.parseInt(l[j]);
                m.t[i][j] = Tile.get(v);
                IndiMap.r[i][j] = v > 0;
            }
        }
        int ex = in.nextInt(); // the coordinate along the X axis of the exit

        EXIT = new Position(H - 1, ex);

        System.err.println(m + "\n " + EXIT);

        // game loop
        while (true)
        {
            int c = in.nextInt();
            int l = in.nextInt();
            String POSI = in.next();

            Direction dI = Direction.N;
            switch (POSI)
            {
                case "TOP":
                    dI = Direction.N;
                    break;
                case "LEFT":
                    dI = Direction.W;
                    break;
                case "RIGHT":
                    dI = Direction.E;
                    break;
                default:
                    dI = null;

            }

            int R = in.nextInt(); // the number of rocks currently in the grid.

            Position[] rocks = new Position[R];
            Direction[] rd = new Direction[R];
            for (int i = 0; i < R; i++)
            {
                int cr = in.nextInt();
                int lr = in.nextInt();
                rocks[i] = new Position(lr, cr);
                String POSR = in.next();

                switch (POSR)
                {
                    case "TOP":
                        rd[i] = Direction.N;
                        break;
                    case "LEFT":
                        rd[i] = Direction.W;
                        break;
                    case "RIGHT":
                        rd[i] = Direction.E;
                        break;
                    default:
                        rd[i] = null;
                }
            }

            Position p = new Position(l, c);
            System.err.println(p + " / " + dI);

            State s = new State(m, p, dI, rocks, rd);
            Action a = (Action) solve(s);
            System.out.println(a);

            // One line containing on of three commands: 'X Y LEFT', 'X Y RIGHT' or 'WAIT'
            // System.out.println("WAIT");
        }
    }

    public enum Act
    {
        RIGHT, LEFT, WAIT;
    }

    public static class Action extends BFSAction
    {
        Position t = null;
        Act a = null;

        public Action()
        {
            this.a = Act.WAIT;
        }

        /**
         * Constructeur
         *
         * @param p_t
         * @param p_a
         */
        public Action(Position p_t, Act p_a)
        {
            this.t = p_t;
            this.a = p_a;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return (this.t != null ? this.t.y + " " + this.t.x : "") + this.a + "";
        }

        @Override
        public int cost()
        {
            return 0;
        }

    }

    public static class State extends BFSState
    {
        IndiMap map = null;
        Position posI = null;
        Direction dirI = null;
        Position[] rocks = null;
        Direction[] rd = null;

        public State(IndiMap p_m, Position p_Indi, Direction p_dI, Position[] p_r, Direction[] p_dr)
        {
            this.map = p_m;
            this.posI = p_Indi;
            this.dirI = p_dI;
            this.rocks = p_r;
            this.rd = p_dr;
        }

        @Override
        public BFSState next(BFSAction p_a)
        {
            Action a = (Action) p_a;
            IndiMap nextMap = this.map.clone();
            if (a.t != null)
                nextMap.change(a);
            Position nextPosI = nextMap.next(this.posI, this.dirI);
            Direction nextDirI = this.posI.relativeDir(nextPosI);

            List<Position> nR = new ArrayList<>(Arrays.asList(this.rocks));
            List<Direction> nD = new ArrayList<>(Arrays.asList(this.rd));

            for (int i = 0; i < this.rocks.length; i++)
            {
                Position nextPosR = nextMap.next(this.rocks[i], this.rd[i]);
                if (nextPosR != null)
                {
                    Direction nextDirR = this.rocks[i].relativeDir(nextPosR);

                    nR.add(nextPosR);
                    nD.add(nextDirR);
                }
            }

            Position[] nextPosR = nR.toArray(new Position[0]);
            Direction[] nextDirR = nD.toArray(new Direction[0]);

            return new State(nextMap, nextPosI, nextDirI, nextPosR, nextDirR);
        }

        @Override
        public List<BFSAction> actions()
        {
            ArrayList<BFSAction> as = new ArrayList<>();

            as.add(new Action()); // wait action
            // pour toutes les tuiles mobiles au meme niveau que Indi ou en dessous, on ajoute
            // OU
            // On trouve la prochaine tuile problématique seulement ? ca suffit ?

            // Find problematic tile
            Position p = this.posI;
            Position next = this.map.next(p, this.dirI);
            Direction dnext = p.relativeDir(next);
            while (next != null && !next.equals(EXIT))
            {
                p = next;
                next = this.map.next(p, dnext);
                dnext = p.relativeDir(next);
            }
            // p contains the problematic tile position if next == null
            if (next == null)
            {
                as.add(new Action(next, Act.RIGHT));
                as.add(new Action(next, Act.LEFT));
            }
            // Sinon cet état mène au but.
            return as;
        }

        @Override
        public boolean isEndState()
        {
            return this.posI.equals(EXIT);
        }

        @Override
        public int heuristic()
        {
            // Find problematic tile
            Position p = this.posI;
            int i = 1;
            Position next = this.map.next(p, this.dirI);
            Direction dnext = p.relativeDir(next);
            while (next != null && !next.equals(EXIT))
            {
                p = next;
                next = this.map.next(p, dnext);
                dnext = p.relativeDir(next);
                i++;
            }

            if (next == null)
                return 0;
            else
                return 100 - i;

        }

        @Override
        public int lowerBound()
        {
            return 0;
        }

    }

    /**
     * Classe représentant une énumération de directions.
     * <ul>
     * <li>Nord</li>
     * <li>Sud</li>
     * <li>Est</li>
     * <li>Ouest</li>
     * </ul>
     *
     * @author Camille Besse
     */
    public static enum Direction
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

    public static enum Distance
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

    public static class IndiMap
    {
        Tile[][] t;
        static boolean[][] r;

        public IndiMap(int w, int h)
        {
            this.t = new Tile[h][w];
        }

        @Override
        public IndiMap clone()
        {
            IndiMap m = new IndiMap(this.t[0].length, this.t.length);
            for (int i = 0; i < this.t.length; i++)
                for (int j = 0; j < this.t[0].length; j++)
                    m.t[i][j] = this.t[i][j];
            return m;
        }

        public void change(Action a)
        {
            Position p = a.t;
            Tile tt = this.t[p.x][p.y];
            switch (a.a)
            {
                case LEFT:
                    this.t[p.x][p.y] = tt.rotateLeft();
                    break;
                case RIGHT:
                    this.t[p.x][p.y] = tt.rotateRight();
                    break;
                case WAIT:
                default:
                    return;
            }
        }

        public Position next(Position p, Direction d)
        {
            Direction out = this.t[p.y][p.x].towards(d);
            if (out == null)
                return null;
            return p.relativePos(out);
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            for (Tile[] line : this.t)
            {
                for (Tile e : line)
                    sb.append(e + " ");
                sb.append("\n");
            }

            return sb.toString();
        }
    }

    public static class Position
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

    enum Tile
    {
        P, C, // PLAIN, CROSS
        S_H, S_V,// STRAINGHT Hori and Vert
        R_NW_SE, R_NE_SW, // Double rounded
        T_N, T_E, T_S, T_W, // T-shape
        R_NW, R_NE, R_SE, R_SW; // Single Rounded;

        public Tile rotateLeft()
        {
            switch (this)
            {
                case S_H:
                    return S_V;
                case S_V:
                    return S_H;
                case R_NW_SE:
                    return R_NE_SW;
                case R_NE_SW:
                    return R_NW_SE;
                case T_N:
                    return T_W;
                case T_E:
                    return T_N;
                case T_S:
                    return T_E;
                case T_W:
                    return T_S;
                case R_NW:
                    return R_SW;
                case R_NE:
                    return R_NW;
                case R_SE:
                    return R_NE;
                case R_SW:
                    return R_SE;
                case C:
                default: // Plain
                    return this;
            }
        }

        public Tile rotateRight()
        {
            switch (this)
            {
                case S_H:
                    return S_V;
                case S_V:
                    return S_H;
                case R_NW_SE:
                    return R_NE_SW;
                case R_NE_SW:
                    return R_NW_SE;
                case T_N:
                    return T_E;
                case T_E:
                    return T_S;
                case T_S:
                    return T_W;
                case T_W:
                    return T_N;
                case R_NW:
                    return R_NE;
                case R_NE:
                    return R_SE;
                case R_SE:
                    return R_SW;
                case R_SW:
                    return R_NW;
                case C:
                default: // Plain
                    return this;
            }

        }

        public Direction towards(Direction in)
        {
            switch (this)
            {
                case C:
                    if (in.equals(Direction.N) || in.equals(Direction.W) || in.equals(Direction.E))
                        return Direction.S;
                    break;
                case T_N:
                case S_H:
                    if (in.equals(Direction.W) || in.equals(Direction.E))
                        return in.inverse();
                    break;
                case S_V:
                    if (in.equals(Direction.N))
                        return Direction.S;
                    break;
                case R_NW_SE:
                    if (in.equals(Direction.N))
                        return Direction.W;
                    if (in.equals(Direction.E))
                        return Direction.S;
                    break;
                case R_NE_SW:
                    if (in.equals(Direction.N))
                        return Direction.E;
                    if (in.equals(Direction.W))
                        return Direction.S;
                    break;
                case T_E:
                    if (in.equals(Direction.N) || in.equals(Direction.E))
                        return Direction.S;
                    break;
                case T_S:
                    if (in.equals(Direction.W) || in.equals(Direction.E))
                        return Direction.S;
                    break;
                case T_W:
                    if (in.equals(Direction.N) || in.equals(Direction.W))
                        return Direction.S;
                    break;
                case R_NW:
                    if (in.equals(Direction.N))
                        return Direction.W;
                    break;
                case R_NE:
                    if (in.equals(Direction.N))
                        return Direction.E;
                    break;
                case R_SE:
                    if (in.equals(Direction.E))
                        return Direction.S;
                    break;
                case R_SW:
                    if (in.equals(Direction.W))
                        return Direction.S;
                    break;

                default: // Plain
            }
            return null;
        }

        public static Tile get(int p_v)
        {
            if (p_v < 0)
                p_v *= -1;

            return Tile.values()[p_v];
        }

    }

    public static BFSAction solve(BFSState p_init)
    {

        TreeMap<BFSState, Integer> open = new TreeMap<>();

        for (BFSAction a : p_init.actions())
        {
            BFSState next = p_init.nextState(a);
            open.put(next, a.cost() + next.heuristic());
        }

        int lowerBound = p_init.lowerBound();

        while (!open.isEmpty() && !open.firstKey().isEndState())
        {
            BFSState current = open.pollFirstEntry().getKey();
            // db("BFS2 : " + Arrays.toString(this.generateActions(current.s)));
            for (BFSAction a : current.actions())
            {
                BFSState next = current.nextState(a);
                int c_lower = next.lowerBound();
                if (c_lower >= lowerBound)
                {
                    open.put(next, c_lower);
                    lowerBound = c_lower;
                }
            }
        }

        if (!open.isEmpty())// DONC open.firstKey().isEndState()
        {
            BFSState b = open.firstKey();
            if (b.prev == null)
                System.err.println("--- > " + b);
            else
                System.err.println(b + "---+>" + b.prev);

            while (b.prev.prev != null)
                b = b.prev;

            return b.prevA;
        }
        else
            System.err.println("I'm probably SCREWED ... ");

        return null;

    }

    public static abstract class BFSAction
    {
        public abstract int cost();
    }

    public static abstract class BFSState
    {
        BFSState prev = null;
        BFSAction prevA = null;

        public abstract BFSState next(BFSAction p_a);

        public BFSState nextState(BFSAction p_a)
        {
            BFSState next = this.next(p_a);
            next.prev = this;
            next.prevA = p_a;
            return next;
        }

        public abstract List<BFSAction> actions();

        public abstract boolean isEndState();

        public abstract int heuristic();

        public abstract int lowerBound();

    }

}
