import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class WondevWoman
{

    static Scanner in = new Scanner(System.in);

    // static boolean LOCAL = true;
    static boolean LOCAL = false;
    final static boolean DEBUG = true;
    final static Random RAND = new Random();

    public static int SIZE = 0;

    static int tour = 0;

    // static TeamCoordinator TC;

    public static void sep(Object s)
    {
        if (DEBUG)
            System.err.println(tour + ": " + s);
    }

    public static void main(String args[])
    {
        in = new Scanner(System.in);
        SIZE = in.nextInt();
        int unitsPerPlayer = in.nextInt();

        State s = new State();
        Unit[] my = new Unit[unitsPerPlayer];
        Unit[] them = new Unit[unitsPerPlayer];

        for (int i = 0; i < unitsPerPlayer; i++)
        {
            my[i] = new Unit(0, i);
            them[i] = new Unit(1, i);
        }

        Greedy g = new Greedy(my);

        // game loop
        while (true)
        {
            tour++;

            long start = System.currentTimeMillis();

            String[] row = new String[SIZE];
            for (int i = 0; i < SIZE; i++)
                row[i] = in.next();

            s.upd(tour, row);

            for (int i = 0; i < unitsPerPlayer; i++)
                my[i].upd(tour, in.nextInt(), in.nextInt());
            // int unitX = in.nextInt();
            // int unitY = in.nextInt();

            for (int i = 0; i < unitsPerPlayer; i++)
                them[i].upd(tour, in.nextInt(), in.nextInt());
            // int otherX = in.nextInt();
            // int otherY = in.nextInt();

            s.upd(my, them);

            int legalActions = in.nextInt();

            ArrayList<Action> poss = new ArrayList<>();
            for (int i = 0; i < legalActions; i++)
            {
                String atype = in.next();
                int index = in.nextInt();
                String dir1 = in.next();
                String dir2 = in.next();

                Action a = new Action(my[index], Direction.fromString(dir1), Direction.fromString(dir2));
                if (atype.equals("PUSH&BUILD"))
                    a.t = ActionType.PUSHBUILD;

                poss.add(a);
            }

            Action a = g.decide(s, poss);

            long end = System.currentTimeMillis() - start;

            sep("Time : " + end);

            System.out.println(a);
        }
    }

    public static class Greedy extends IStrategy
    {
        Action lastA = null;

        public Greedy(Unit[] p_my)
        {
            this.setUnit(p_my);
        }

        @Override
        public Action decide(State p_s, ArrayList<Action> p_poss)
        {
            Action a = null;

            if (p_poss.size() > 0)
            {
                TreeSet<Action> set = new TreeSet<>(new ActionComparator(p_s));

                set.addAll(p_poss);

                a = set.first();

                this.lastA = a;
            }

            return a;
        }

    }

    public static class ActionComparator implements Comparator<Action>
    {
        final State init;

        public ActionComparator(State p_s)
        {
            this.init = p_s;
        }

        public static int StateEvaluation(State p_s)
        {
            int v = 0;
            Position unknown = new Position(-1, -1);

            // Gain Points for being higher
            for (Unit u : p_s.i)
                v += p_s.get(u.p) * 15;

            // Gain Points for being able to get high
            for (Unit u : p_s.i)
                for (Position np : u.p.casesAutour())
                    if (p_s.inBound(np) && !p_s.i[1 - u.id].p.equals(np) && p_s.get(np) - p_s.get(u.p) <= 1)
                        v += 4;

            // Lose Points for blocking myself
            for (Unit u : p_s.i)
                for (Position np : u.p.casesAutour())
                    if (!p_s.inBound(np) || p_s.get(np) > 3 || p_s.get(np) - p_s.get(u.p) > 1
                            || p_s.i[1 - u.id].p.equals(np))
                        v -= 2;

            // Lose Points for ennemy being higher
            for (Unit u : p_s.o)
                if (!u.p.equals(unknown))
                    v -= p_s.get(u.p) * 5;

            // Gain Points for preventing ennemy to get high
            for (Unit u : p_s.o)
                if (!u.p.equals(unknown))
                    for (Position np : u.p.casesAutour())
                        if (!p_s.inBound(np) || p_s.get(np) > 3 || p_s.get(np) - p_s.get(u.p) > 1)
                            v += 2;

            return v;
        }

        @Override
        public int compare(Action p_a1, Action p_a2)
        {
            int v1 = StateEvaluation(this.init.nextState(p_a1));
            int v2 = StateEvaluation(this.init.nextState(p_a2));

            return v2 - v1;
        }

    }

    public static class Premier extends IStrategy
    {
        Action lastA = null;

        public Premier(Unit[] p_my)
        {
            this.setUnit(p_my);
        }

        @Override
        public Action decide(State p_s, ArrayList<Action> p_poss)
        {
            Action a = null;
            // sep("Act:" + this.w + " -> " + a);
            sep(p_s);
            for (Action aa : p_poss)
                try
                {
                    // if (this.lastA != null && this.lastA.u.id == aa.u.id)
                    // continue;

                    Position rel = aa.u.p.relativePos(aa.m);

                    // if (p_s.isOccupied(rel))
                    // continue; // PUSH ACTION

                    Position bui = rel.relativePos(aa.b);
                    if (p_s.get(rel) == 3 && p_s.get(bui) != 3)
                    {
                        // sep("getPoint:" + aa.u.p + " + " + aa.m + " -> " + aa.u.p.relativePos(aa.m));
                        a = aa;
                        break;
                    }
                    else if (p_s.get(rel) > p_s.get(aa.u.p) && p_s.get(bui) != 3)
                        // sep("GetUp:" + aa.u.p + " + " + aa.m + " -> " + aa.u.p.relativePos(aa.m));
                        a = aa;
                    else if (p_s.get(rel) == p_s.get(aa.u.p) && p_s.get(bui) != 3)
                        // sep("StayUp:" + aa.u.p + " + " + aa.m + " -> " + aa.u.p.relativePos(aa.m));
                        a = aa;
                }
                catch (ArrayIndexOutOfBoundsException e)
                {
                    sep("OutOfBounds:" + aa.u.p + " + " + aa.m + " -> " + aa.u.p.relativePos(aa.m));
                    a = null;
                }

            if (a == null)
                a = p_poss.get(RAND.nextInt(p_poss.size()));

            this.lastA = a;
            return a;
        }

    }

    public static abstract class IStrategy
    {
        Unit[] u;

        abstract Action decide(State p_s, ArrayList<Action> p_poss);

        public void setUnit(Unit[] p_u)
        {
            this.u = p_u;
        }
    }

    public static class Unit
    {
        final int o;
        final int id;
        int lastUpd = -1;
        Position p;

        private Unit(int p_owner, int p_id)
        {
            this.o = p_owner;
            this.id = p_id;
            this.p = null;
        }

        @Override
        public Unit clone()
        {
            Unit e = new Unit(this.o, this.id);
            e.p = this.p;
            return e;
        }

        void upd(int p_t, int p_x, int p_y)
        {
            this.lastUpd = p_t;
            this.p = new Position(p_x, p_y);
            // sep("upd : " + this);
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
            result = prime * result + this.id;
            result = prime * result + this.lastUpd;
            result = prime * result + this.o;
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
            Unit other = (Unit) obj;
            if (this.id != other.id)
                return false;
            if (this.lastUpd != other.lastUpd)
                return false;
            if (this.o != other.o)
                return false;
            if (this.p == null)
            {
                if (other.p != null)
                    return false;
            }
            else if (!this.p.equals(other.p))
                return false;
            return true;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "U [o=" + this.o + ", id=" + this.id + ", p=" + this.p + ", lT=" + this.lastUpd + "]";
        }

    }

    public enum ActionType
    {
        MOVEBUILD, PUSHBUILD;
    }

    public static class Action
    {
        ActionType t;
        Unit u;
        Direction m;
        Direction b;

        public Action(Unit p_u, Direction p_move, Direction p_build)
        {
            this.t = ActionType.MOVEBUILD;
            this.u = p_u;
            this.m = p_move;
            this.b = p_build;
        }

        @Override
        public String toString()
        {
            switch (this.t)
            {
                case MOVEBUILD:
                    return "MOVE&BUILD " + this.u.id + " " + this.m + " " + this.b;
                case PUSHBUILD:
                    return "PUSH&BUILD " + this.u.id + " " + this.m + " " + this.b;
            }
            return "";
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
            result = prime * result + ((this.b == null) ? 0 : this.b.hashCode());
            result = prime * result + ((this.m == null) ? 0 : this.m.hashCode());
            result = prime * result + ((this.t == null) ? 0 : this.t.hashCode());
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
            Action other = (Action) obj;
            if (this.b != other.b)
                return false;
            if (this.m != other.m)
                return false;
            if (this.t != other.t)
                return false;
            return true;
        }

    }

    public static class State
    {
        int tour = 0;
        int[][] m;

        Unit[] i;
        Unit[] o;

        public State()
        {
            this.m = new int[SIZE][SIZE];
        }

        public boolean inBound(Position p_np)
        {
            return p_np.x > 0 && p_np.y > 0 && p_np.x < SIZE && p_np.y < SIZE;
        }

        public boolean isOccupied(Position p_p)
        {
            for (Unit element : this.o)
                if (element.p.equals(p_p))
                    return true;
            return false;
        }

        public int get(Position p_p)
        {
            return this.m[p_p.y][p_p.x];
        }

        @Override
        public State clone()
        {
            State c = new State();
            c.m = Arrays.stream(this.m).map(x -> x.clone()).toArray(int[][]::new);
            c.i = Arrays.stream(this.i).map(x -> x.clone()).toArray(Unit[]::new);
            c.o = Arrays.stream(this.o).map(x -> x.clone()).toArray(Unit[]::new);
            return c;
        }

        /**
         * Given a State and an action, returns the next state
         *
         * @param p_a
         * @return the enxt State after the action and no following action
         */
        public State nextState(Action p_a)
        {
            State n = this.clone();

            Unit[] uu = (p_a.u.o == 0 ? n.i : n.o);
            Unit[] uo = (p_a.u.o == 0 ? n.o : n.i);

            // if (!uu[p_a.u.id].equals(p_a.u))
            // sep("WTF !!!i");
            // sep(this + "\n\n" + p_a);

            // If move & build
            if (p_a.t == ActionType.MOVEBUILD)
            {
                Unit moving = uu[p_a.u.id];
                // Move Unit :
                moving.p = moving.p.relativePos(p_a.m);
                // Build Map :
                Position r = moving.p.relativePos(p_a.b);
                n.m[r.y][r.x]++;
                if (n.m[r.y][r.x] > 3)
                    n.m[r.y][r.x] = 1000;
            }
            // if push & build
            else if (p_a.t == ActionType.PUSHBUILD)
            {
                Unit pushing = uu[p_a.u.id];
                Unit pushed = null;
                for (Unit op : uo)
                    if (op.p.equals(pushing.p.relativePos(p_a.m)))
                        pushed = op;

                // Move pushed Unit :
                pushed.p = pushed.p.relativePos(p_a.b);
            }

            return n;
        }

        public void upd(int p_t, String[] p_rows)
        {
            for (int i = 0; i < p_rows.length; i++)
            {
                char[] c = p_rows[i].toCharArray();
                for (int j = 0; j < c.length; j++)
                    switch (c[j])
                    {
                        case '.':
                        case '4':
                            this.m[i][j] = 1000;
                            break;
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                            this.m[i][j] = c[j] - '0';
                            break;
                    }
            }
        }

        public void upd(Unit[] p_my, Unit[] p_them)
        {
            this.i = p_my;
            this.o = p_them;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            int[][] mm = Arrays.stream(this.m).map(x -> x.clone()).toArray(int[][]::new);
            mm[this.i[0].p.y][this.i[0].p.x] = -10;
            mm[this.i[1].p.y][this.i[1].p.x] = -11;

            if (this.o[0].p.x != -1)
                mm[this.o[0].p.y][this.o[0].p.x] = -20;
            if (this.o[1].p.x != -1)
                mm[this.o[1].p.y][this.o[1].p.x] = -21;

            StringBuilder s = new StringBuilder();
            for (int[] l : mm)
            {
                for (int lc : l)
                    if (lc < -15)
                        s.append('o');
                    else if (lc < -5)
                        s.append('*');
                    else if (lc > 3)
                        s.append('X');
                    else
                        s.append(lc);
                s.append('\n');
            }

            return "S : " + this.tour + "\nmap:\n" + s;
        }
    }

    public enum Direction
    {
        /**
         * définitions de l'énumération. type de directions.
         */
        // N(-1, 0), S(1, 0), E(0, 1), W(0, -1), NE(-1, 1), SE(1, 1), NW(-1, -1), SW(1, -1);
        W(-1, 0), E(1, 0), S(0, 1), N(0, -1), SW(-1, 1), SE(1, 1), NW(-1, -1), NE(1, -1);

        public final int dx;
        public final int dy;

        private Direction(int p_dx, int p_dy)
        {
            this.dx = p_dx;
            this.dy = p_dy;
            // Merci Oli & Simon.
        }

        // /**
        // * Redéfinition pour débugage de la méthode toString.
        // *
        // * @see java.lang.Object#toString()
        // */
        // @Override
        // public String toString()
        // {
        // switch (this)
        // {
        // case N:
        // case S:
        // case W:
        // case E:
        // return super.toString().substring(0, 1);
        // case NE:
        // case SE:
        // case NW:
        // case SW:
        // return super.toString().substring(0, 1) + super.toString().substring(2, 3);
        // default:
        // return "";
        // }
        // }

        /**
         * Retourne le tableau des direction diagonales
         *
         * @return
         */
        static public Direction[] diagonalValues()
        {
            Direction[] d = { NE, SE, NW, SW };
            return d;
        }

        /**
         * Renvoie la direction relative à droite à p_dir d'un huitième de tour
         *
         * @param p_dir
         * @return
         */
        static public Direction droite(Direction p_dir)
        {
            switch (p_dir)
            {
                case N:
                    return NE;
                case S:
                    return SW;
                case W:
                    return NW;
                case E:
                    return SE;
                case NE:
                    return E;
                case SE:
                    return S;
                case NW:
                    return N;
                case SW:
                    return W;
                default:
                    return null;
            }
        }

        /**
         * Renvoie la direction relative à gauche à p_dir d'un huitième de tour
         *
         * @param p_dir
         * @return
         */
        static public Direction gauche(Direction p_dir)
        {
            switch (p_dir)
            {
                case N:
                    return NW;
                case S:
                    return SE;
                case W:
                    return SW;
                case E:
                    return NE;
                case NE:
                    return N;
                case SE:
                    return E;
                case NW:
                    return W;
                case SW:
                    return S;
                default:
                    return null;
            }
        }

        /**
         * Renvoie la direction inverse à p_dir
         *
         * @param p_dir
         * @return
         */
        static public Direction Inverse(Direction p_dir)
        {
            switch (p_dir)
            {
                case N:
                    return S;
                case S:
                    return N;
                case W:
                    return E;
                case E:
                    return W;
                case NE:
                    return SW;
                case SE:
                    return NW;
                case NW:
                    return SE;
                case SW:
                    return NE;
                default:
                    return null;
            }
        }

        /**
         * Retourne le tableau des direction verticales
         *
         * @return
         */
        static public Direction[] verticalValues()
        {
            Direction[] d = { N, S, E, W };
            return d;
        }

        /**
         * Retourne la Direction à partir de la String
         *
         * @return
         */
        static public Direction fromString(String p_s)
        {
            for (Direction d : values())
                if (p_s.equals(d.toString()))
                    return d;

            return null;
        }

    }

    enum Distance
    {
        VH, MANHATTAN, CHEBYSHEV, EUCLIDEAN, SQ_EUCLID;

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
        public boolean inside(Vector2D p_p1, Vector2D p_p2, int p_r)
        {
            switch (this)
            {
                case VH:
                    return ((p_p1.y == p_p2.y) && Math.abs(p_p1.x - p_p2.x) <= p_r)
                            || ((p_p1.x == p_p2.x) && Math.abs(p_p1.y - p_p2.y) <= p_r);
                case SQ_EUCLID:
                    return p_p1.minus(p_p2).getR2() <= p_r * p_r;
                case MANHATTAN:
                case CHEBYSHEV:
                default:
                    return this.val(p_p1, p_p2) <= p_r;
            }
        }

        public double val(Vector2D p_p1, Vector2D p_p2)
        {
            switch (this)
            {
                case VH:
                    if (p_p1.y == p_p2.y)
                        return Math.abs(p_p1.x - p_p2.x);
                    else if (p_p1.x == p_p2.x)
                        return Math.abs(p_p1.y - p_p2.y);
                    else
                        return Double.POSITIVE_INFINITY;
                case MANHATTAN:
                    return Math.abs(p_p1.x - p_p2.x) + Math.abs(p_p1.y - p_p2.y);
                case CHEBYSHEV:
                    return Math.max(Math.abs(p_p1.x - p_p2.x), Math.abs(p_p1.y - p_p2.y));
                case SQ_EUCLID:
                    return p_p1.minus(p_p2).getR2();
                default:
                    return Math.sqrt(Math.pow(p_p1.x - p_p2.x, 2) + Math.pow(p_p1.y - p_p2.y, 2));
            }
        }
    }

    public static class Position extends Vector2D
    {

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
            super(p_X, p_Y);
        }

        /**
         * Constructeur par copie. Initialise un objet Position avec la position en x et en y
         *
         * @param p_p
         *            position
         */
        public Position(Position p_p)
        {
            super(p_p.x, p_p.y);
        }

        public Position(Vector2D p_p)
        {
            super(p_p.x, p_p.y);
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
        public HashSet<Position> getInRange(int p_r, Distance p_d)
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
            if (p_Direction == null)
                return this;

            return new Position(this.x + p_Direction.dx, this.y + p_Direction.dy);
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return (this.x) + " " + (this.y);
        }

    }

    public static class Vector2D
    {
        final int x;
        final int y;

        /*
         * (non-Javadoc)
         *
         * @see java.awt.geom.Point2D.Double#Point2D.Double()
         */
        public Vector2D()
        {
            this.x = 0;
            this.y = 0;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.awt.geom.Point2D.Double#Point2D.Double()
         */
        public Vector2D(int p_x, int p_y)
        {
            this.x = p_x;
            this.y = p_y;
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
         * @return the radius squared (length, modulus) of the vector in polar coordinates
         */
        public double getR2()
        {
            return this.x * this.x + this.y * this.y;
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

        @Override
        public String toString()
        {
            return "[" + this.x + "," + this.y + "]";
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            final double prime = 31;
            double result = 1;
            result = -1 * prime * result + this.x;
            result = prime * result + this.y;
            return (int) result;
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

            Vector2D other = (Vector2D) obj;
            return this.floorEquals(other);
            // if (this.x != other.x)
            // return false;
            // if (this.y != other.y)
            // return false;
            // return true;
        }

        public boolean floorEquals(Vector2D obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;

            if ((this.x) != obj.x)
                return false;
            if ((this.y) != obj.y)
                return false;
            return true;
        }
    }

}
