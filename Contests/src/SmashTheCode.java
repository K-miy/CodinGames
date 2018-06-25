import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class SmashTheCode
{
    private static Random r = new Random();

    static final long OOT = 95;
    static final int BRANCHING_FACTOR = 5;

    static boolean testing = false;

    long orig;
    long timer;
    int tour;

    String msg = "";

    Scanner in = new Scanner(System.in);
    String[] myGame = new String[12];

    String[] hisGame = new String[12];
    Pair[] inc = new Pair[8];

    Action bA = null;

    public boolean checkTime()
    {
        this.timer = System.currentTimeMillis() - this.orig;
        if (this.timer > SmashTheCode.OOT)
            // throw new OutOfTimeException("OutOfTime !!!");
            return false;
        else
            return true;
    }

    public void read()
    {
        this.orig = System.currentTimeMillis();
        // 8 next pairs of colored balls
        for (int i = 0; i < 8; i++)
            this.inc[i] = new Pair(this.in.nextInt(), this.in.nextInt());
        // lines of the actual screen ('.' = empty, '0' = skull block, '1' to '5' = colored block)
        for (int i = 0; i < 12; i++)
            this.myGame[i] = this.in.next();
        for (int i = 0; i < 12; i++)
            this.hisGame[i] = this.in.next();
        this.orig = System.currentTimeMillis();

        // this.sep(Arrays.toString(this.myGame));
    }

    void run()
    {
        this.sep("Welcome !");
        this.msg = "Welcome & GL !";

        this.tour = 0;

        // ScreenNode myS = null;
        ZeGeneticAlgo al = new ZeGeneticAlgo();
        ZeGeneticAlgo.Individual bestSeq = null;

        // game loop
        while (true)
        {
            this.tour++;

            // Get Inputs
            this.read();
            this.timer = System.currentTimeMillis() - this.orig;

            // VERSION BRONZE:

            // int colCol = this.inc[0].a.ordinal();
            // Rotation[] rots = Rotation.availableRots(colCol);

            // VERSION SILVER:

            // ScreenNode myOldS = myS;
            //
            // myS = new ScreenNode(this.myGame);
            //
            // if (this.tour != 1)
            // for (ScreenNode s : myOldS.next.keySet())
            // if (s.equals(myS))
            // {
            // myS = s;
            // break;
            // }

            // Screen hiS = new Screen(this.hisGame);

            // System.err.println("String[] testG = " + myS.s.toDebugString());
            // StringBuilder sb = new StringBuilder("Pair[] pp = new Pair[] {");
            // for (Pair a : this.inc)
            // sb.append(a.toDebugString());
            // sb.append("};");
            // System.err.println(sb.toString());
            //
            // try
            // {
            // myS.develop(this.inc);
            // }
            // catch (OutOfTimeException e)
            // {}

            // Action a = myS.bestEntry().getValue();// new Action(this.inc[0], colCol, rots[tour % rots.length]);

            // Screen oldS = new Screen(this.myGame);
            // Screen nextS = oldS.next(a);

            // this.sep(nextS);

            // VERSION GOLD:

            Screen hiS = new Screen(this.hisGame);

            System.err.println("String[] testG = " + hiS.toDebugString());
            StringBuilder sb = new StringBuilder("Pair[] pp = new Pair[] {");
            for (Pair p : this.inc)
                sb.append(p.toDebugString());
            sb.append("};");
            System.err.println(sb.toString());

            Screen myS = new Screen(this.myGame);

            if (this.tour == 1)
                bestSeq = al.new Individual(myS, this.inc, true);

            bestSeq = al.run(myS, this.inc, bestSeq);
            Action a = bestSeq.fromPair()[0];

            System.out.println(a.print());// + " " + this.msg); // "x": the column in which to drop your blocks
            this.msg = "";
        }
    }

    void sep(Object s)
    {
        this.timer = System.currentTimeMillis() - this.orig;
        System.err.println("[" + +this.tour + "/" + this.timer + "] " + s);
    }

    public static void main(String args[])
    {
        new SmashTheCode().run();
        test();
    }

    public static void test()
    {
        testing = true;
        SmashTheCode p = new SmashTheCode();

        String[] testG = { "......", "......", "......", "......", "......", "......", "......", "......", "......",
                "......", "...1..", "..344." };
        Pair[] pp = new Pair[] { p.new Pair(2, 3), p.new Pair(5, 2), p.new Pair(4, 2), p.new Pair(4, 4),
                p.new Pair(2, 1), p.new Pair(3, 5), p.new Pair(2, 3), p.new Pair(3, 2) };
        // String[] testG = { "......", "......", "......", "......", "......", "......", "......", "......", "......",
        // "......", ".2.1..", ".3344." };
        // Pair[] pp = new Pair[] { a.new Pair(5, 2), a.new Pair(4, 2), a.new Pair(4, 4), a.new Pair(2, 1),
        // a.new Pair(3, 5), a.new Pair(2, 3), a.new Pair(3, 2), a.new Pair(1, 3) };
        // String[] testG = { "......", "......", "......", "......", "......", "......", "......", "......", "..3...",
        // "..2...", "..2...", "431..." };
        // Pair[] pp = new Pair[] { a.new Pair(4, 5), a.new Pair(1, 1), a.new Pair(5, 3), a.new Pair(2, 1),
        // a.new Pair(3, 4), a.new Pair(2, 5), a.new Pair(4, 3), a.new Pair(2, 2), };

        // String[] testG = { "......", "......", "......", "......", "......", "......", "0.....", "33..0.", "31..10",
        // "500.14", "321.31", "551055" };
        // Pair[] pp = new Pair[] { a.new Pair(1, 1), a.new Pair(3, 1), a.new Pair(3, 3), a.new Pair(1, 4),
        // a.new Pair(1, 1), a.new Pair(3, 1), a.new Pair(3, 3), a.new Pair(1, 4) };

        // a.orig = System.currentTimeMillis();
        // Screen s = a.new Screen(testG);
        //
        // Action a = a.new Action(a.new Pair(1, 1), 3, Rotation.ABv);
        //
        // s.next(a);
        // ScreenNode ss = a.new ScreenNode(testG);

        // try
        // {
        // ss.develop(pp);
        // }
        // catch (OutOfTimeException e)
        // {}
        //
        // a.sep(ss.bestEntry());

        ZeGeneticAlgo al = p.new ZeGeneticAlgo();
        ZeGeneticAlgo.Individual bestSeq = null;

        Screen myS = p.new Screen(testG);
        p.orig = System.currentTimeMillis();

        bestSeq = al.new Individual(myS, pp, true);

        bestSeq = al.run(myS, pp, bestSeq);
        Action a = bestSeq.fromPair()[0];

    }

    class Action
    {
        /** The pair to play */
        final public Pair p;
        /** the column where player */
        final public int c;
        /** the rotation where player */
        final public Rotation r;

        public Action(Pair pp, int col, Rotation rot)
        {
            this.p = pp;
            this.c = col;
            this.r = rot;
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
            Action other = (Action) obj;
            if (this.c != other.c)
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
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.c;
            result = prime * result + ((this.p == null) ? 0 : this.p.hashCode());
            return result;
        }

        public String print()
        {
            return this.c + " " + this.r.print();
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "{" + this.p + "," + this.c + "," + this.r + "}";
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
    enum Direction
    {
        /**
         * définitions de l'énumération. type de directions.
         */
        NORD, SUD, EST, OUEST;

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
                case NORD:
                    return "N";
                case SUD:
                    return "S";
                case OUEST:
                    return "O";
                case EST:
                    return "E";
                default:
                    return "";
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
                case NORD:
                    return SUD;
                case SUD:
                    return NORD;
                case OUEST:
                    return EST;
                case EST:
                    return OUEST;
                default:
                    return null;
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

    class OutOfTimeException extends RuntimeException
    {

        public OutOfTimeException(String p_string)
        {
            super(p_string);
        }

    }

    class Pair
    {
        final Point a;
        final Point b;

        public Pair(int ca, int cb)
        {
            this.a = Point.toPoint(ca);
            this.b = Point.toPoint(cb);
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
            Pair other = (Pair) obj;
            if (this.a != other.a)
                return false;
            if (this.b != other.b)
                return false;
            return true;
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
            result = prime * result + ((this.a == null) ? 0 : this.a.hashCode());
            result = prime * result + ((this.b == null) ? 0 : this.b.hashCode());
            return result;
        }

        public Object toDebugString()
        {
            return "a.new Pair(" + this.a.toDebugString() + "," + this.b.toDebugString() + "),";
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "[" + this.a + "," + this.b + "]";
        }

    }

    enum Point
    {
        SKULL, BLEU, VERT, VIOLET, ROUGE, JAUNE, VIDE;

        public String toDebugString()
        {
            switch (this)
            {
                case SKULL:
                case BLEU:
                case VERT:
                case VIOLET:
                case ROUGE:
                case JAUNE:
                    return this.ordinal() + "";
                default:
                    return ".";
            }
        }

        @Override
        public String toString()
        {
            switch (this)
            {
                case SKULL:
                    return "X";// "\u2620";
                case BLEU:
                    return "B";// "\u2776";
                case VERT:
                    return "G";// "\u2777";
                case VIOLET:
                    return "P";// "\u2778";
                case ROUGE:
                    return "R";// "\u2779";
                case JAUNE:
                    return "Y";// "\u277A";
                default:
                    return " ";
            }
        }

        public static Point toPoint(char c)
        {
            switch (c)
            {
                case '.':
                    return VIDE;
                default:
                    return Point.values()[Integer.parseInt(c + "")];

            }
        }

        public static Point toPoint(int a)
        {
            return Point.values()[a];
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
                case NORD:
                    newPos = new Position(this.x - 1, this.y);
                    break;
                case SUD:
                    newPos = new Position(this.x + 1, this.y);
                    break;
                case EST:
                    newPos = new Position(this.x, this.y + 1);
                    break;
                case OUEST:
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
            return "[" + this.x + "," + this.y + "]";
        }

    }

    enum Rotation
    {
        AB, BAv, BA, ABv;

        public int[] availableCols()
        {
            switch (this)
            {
                case AB:
                    return new int[] { 0, 1, 2, 3, 4 };
                case BAv:
                    return new int[] { 0, 1, 2, 3, 4, 5 };
                case BA:
                    return new int[] { 1, 2, 3, 4, 5 };
                case ABv:
                    return new int[] { 0, 1, 2, 3, 4, 5 };
                default:
                    return new int[] { 0, 1, 2, 3, 4, 5 };
            }
        }

        public String print()
        {
            return "" + this.ordinal();
        }

        public int[] toCols(int c)
        {
            switch (this)
            {
                case AB:
                    return new int[] { c, c + 1 };
                case BAv:
                    return new int[] { c, c };
                case BA:
                    return new int[] { c, c - 1 };
                case ABv:
                    return new int[] { c, c };
                default:
                    return new int[] { c, c };
            }
        }

        public static Rotation[] availableRots(int col)
        {
            switch (col)
            {
                case 0:
                    return new Rotation[] { AB, BAv, ABv };
                case Screen.SCREEN_WIDTH - 1:
                    return new Rotation[] { BAv, BA, ABv };
                default:
                    return Rotation.values();
            }
        }
    }

    class Score
    {

        public final int[] GB = { 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8 };

        public final int[] CB = { 0, 0, 2, 4, 8, 16 };

        public int b = 0;

        public int chainCount = 0;
        public int[] colorCount = new int[Point.values().length];
        public int groupBonus = 0;

        public int toVal()
        {
            int cp = 0, cb = 0;
            if (this.chainCount != 0)
                cp = (int) (8 * Math.pow(2, this.chainCount));

            for (int v : this.colorCount)
                if (v != 0)
                    cb++;

            int d = cp + this.CB[cb] + this.groupBonus;
            return (10 * this.b) * (d > 999 ? 999 : d);
        }

    }

    class Screen implements Comparable<Screen>
    {
        public final static int SCREEN_HEIGHT = 12;
        public final static int SCREEN_WIDTH = 6;

        protected Point[][] s;
        protected int[] maxH;

        protected int v;

        public Screen(Screen s)
        {
            this.s = new Point[SCREEN_HEIGHT][SCREEN_WIDTH];
            for (int l = 0; l < SCREEN_HEIGHT; l++)
                for (int c = 0; c < SCREEN_WIDTH; c++)
                    this.s[l][c] = s.s[l][c];

            this.maxH = new int[SCREEN_WIDTH];
            for (int c = 0; c < SCREEN_WIDTH; c++)
                this.maxH[c] = s.maxH[c];

            this.v = s.v;
        }

        public Screen(String[] game)
        {
            this.s = new Point[SCREEN_HEIGHT][SCREEN_WIDTH];
            this.maxH = new int[SCREEN_WIDTH];

            for (int c = 0; c < SCREEN_WIDTH; c++)
                this.maxH[c] = SCREEN_HEIGHT;

            int i = 0;
            for (String line : game)
            {
                int j = 0;
                for (char c : line.toCharArray())
                {
                    this.s[i][j] = Point.toPoint(c);
                    if (!this.s[i][j].equals(Point.VIDE) && this.maxH[j] == SCREEN_HEIGHT)
                        this.maxH[j] = i;
                    j++;
                }
                i++;
            }

            this.v = this.heuristic();
        }

        @Override
        public int compareTo(Screen p_s)
        {
            return this.v - p_s.v;
        }

        private void dropAll()
        {
            LinkedList<Point> al = new LinkedList<>();
            for (int c = 0; c < SCREEN_WIDTH; c++)
            {

                al.clear();

                for (int l = 0; l < SCREEN_HEIGHT; l++)
                    if (!this.s[l][c].equals(Point.VIDE))
                        al.add(this.s[l][c]);

                int i = 0;
                for (int l = SCREEN_HEIGHT - 1; l >= 0; l--)
                    if (!al.isEmpty())
                    {
                        this.s[l][c] = al.pollLast();
                        i++;
                    }
                    else
                        this.s[l][c] = Point.VIDE;

                this.maxH[c] = SCREEN_HEIGHT - i;
            }

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
            Screen other = (Screen) obj;
            if (!Arrays.deepEquals(this.s, other.s))
                return false;
            return true;
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
            result = prime * result + Arrays.deepHashCode(this.s);
            return result;
        }

        /** Heuristic */
        private int heuristic()
        {
            int v = 0;

            for (int l = 0; l < SCREEN_HEIGHT; l++)
            {
                boolean el = true;
                for (int c = 0; c < SCREEN_WIDTH; c++)
                {
                    boolean ec = this.s[l][c].equals(Point.VIDE);
                    if (ec)
                        v += l;
                    else
                    {
                        Position p = new Position(l, c);
                        for (Position pp : p.casesAutour())
                            if (this.inScreen(pp) && this.s[pp.x][pp.y].equals(this.s[l][c]))
                                v += 1000;
                    }

                    el &= ec;
                }
                if (el)
                    v += l * 20;
            }

            return v;
        }

        private boolean inScreen(Position p)
        {
            return (p.x >= 0 && p.y >= 0 && p.x < SCREEN_HEIGHT && p.y < SCREEN_WIDTH);
        }

        /**
         * Calcule l'état suivant de l'écran après l'action a
         *
         * @param a
         *            Action jouée avec colonne et Paire de bulles
         * @return le nouvel écran après jeu de l'action et reduction s'il y a lieu. L'écran contient sa nouvelle
         *         valeur.
         */
        public Screen next(Action a)
        {
            Screen n = new Screen(this);

            int[] cols = a.r.toCols(a.c);

            // switch (a.r)
            // {
            // case AB:
            // case BA:
            // n.s[0][cols[0]] = a.p.a;
            // n.s[0][cols[1]] = a.p.b;
            // break;
            // case BAv:
            // n.s[0][a.c] = a.p.b;
            // n.s[1][a.c] = a.p.a;
            // break;
            // default:
            // n.s[0][a.c] = a.p.a;
            // n.s[1][a.c] = a.p.b;
            // }
            //
            // n.dropAll();

            switch (a.r)
            {
                case AB:
                case BA:
                    n.s[this.maxH[cols[0]] - 1][cols[0]] = a.p.a;
                    n.s[this.maxH[cols[1]] - 1][cols[1]] = a.p.b;
                    n.maxH[cols[0]]--;
                    n.maxH[cols[1]]--;
                    break;
                case BAv:
                    n.s[this.maxH[a.c] - 1][a.c] = a.p.a;
                    n.s[this.maxH[a.c] - 2][a.c] = a.p.b;
                    n.maxH[a.c] -= 2;
                    break;
                default:
                    n.s[this.maxH[a.c] - 2][a.c] = a.p.a;
                    n.s[this.maxH[a.c] - 1][a.c] = a.p.b;
                    n.maxH[a.c] -= 2;
            }

            // Réduction de l'état avec les couleurs par groupe de 4
            Score sc = new Score();
            // SmashTheCode.this.sep("Next.anteReduce \n" + n);
            Screen nr = n.reduce(sc);
            // SmashTheCode.this.sep("Next.postReduce \n" + nr);
            nr.dropAll();
            // SmashTheCode.this.sep("Next.postDrop \n" + nr);

            // sep("Next.ReduceUntilNoMove " + sc.chainCount);
            while (!nr.equals(n))
            {
                sc.chainCount++;
                n = nr;
                // SmashTheCode.this.sep("Next.anteReduce " + sc.chainCount + "\n" + n);
                nr = n.reduce(sc);
                // sep("Next.postReduce " + sc.chainCount + "\n" + nr);
                nr.dropAll();
                // SmashTheCode.this.sep("Next.postDrop " + sc.chainCount + "\n" + nr);
                // sep("Next.ReduceUntilNoMove " + sc.chainCount);
            }
            // sep("Next.EndOfReduceUntilNoMove");

            // sep("Next.valComputation");
            n.v = sc.toVal();
            // SmashTheCode.this.sep("Next.End +" + n.v);
            return n;
        }

        public Screen nextAndHeuristic(Action a)
        {
            Screen n = new Screen(this);

            int[] cols = a.r.toCols(a.c);

            // switch (a.r)
            // {
            // case AB:
            // case BA:
            // n.s[0][cols[0]] = a.p.a;
            // n.s[0][cols[1]] = a.p.b;
            // break;
            // case BAv:
            // n.s[0][a.c] = a.p.b;
            // n.s[1][a.c] = a.p.a;
            // break;
            // default:
            // n.s[0][a.c] = a.p.a;
            // n.s[1][a.c] = a.p.b;
            // }
            //
            // n.dropAll();

            switch (a.r)
            {
                case AB:
                case BA:
                    n.s[this.maxH[cols[0]] - 1][cols[0]] = a.p.a;
                    n.s[this.maxH[cols[1]] - 1][cols[1]] = a.p.b;
                    n.maxH[cols[0]]--;
                    n.maxH[cols[1]]--;
                    break;
                case BAv:
                    n.s[this.maxH[a.c] - 1][a.c] = a.p.a;
                    n.s[this.maxH[a.c] - 2][a.c] = a.p.b;
                    n.maxH[a.c] -= 2;
                    break;
                default:
                    n.s[this.maxH[a.c] - 2][a.c] = a.p.a;
                    n.s[this.maxH[a.c] - 1][a.c] = a.p.b;
                    n.maxH[a.c] -= 2;
            }

            // Réduction de l'état avec les couleurs par groupe de 4
            Score sc = new Score();
            // SmashTheCode.this.sep("Next.anteReduce \n" + n);
            Screen nr = n.reduce(sc);
            // SmashTheCode.this.sep("Next.postReduce \n" + nr);
            nr.dropAll();
            // SmashTheCode.this.sep("Next.postDrop \n" + nr);

            // sep("Next.ReduceUntilNoMove " + sc.chainCount);
            while (!nr.equals(n))
            {
                sc.chainCount++;
                n = nr;
                // SmashTheCode.this.sep("Next.anteReduce " + sc.chainCount + "\n" + n);
                nr = n.reduce(sc);
                // sep("Next.postReduce " + sc.chainCount + "\n" + nr);
                nr.dropAll();
                // SmashTheCode.this.sep("Next.postDrop " + sc.chainCount + "\n" + nr);
                // sep("Next.ReduceUntilNoMove " + sc.chainCount);
            }
            // sep("Next.EndOfReduceUntilNoMove");

            // sep("Next.valComputation");
            if (sc.toVal() != 0)
                n.v = sc.toVal();
            else
                n.v = n.heuristic();
            // SmashTheCode.this.sep("Next.End +" + n.v);
            return n;
        }

        public Action[] probableActionSet(Pair p)
        {
            TreeMap<Integer, ArrayList<Action>> m = new TreeMap<Integer, ArrayList<Action>>();
            for (int i = 0; i < 8; i++)
                m.put(i, new ArrayList<Action>());

            // SmashTheCode.this.sep(this);

            for (Rotation r : Rotation.values())
                for (int c : r.availableCols())
                {
                    int cA = 0, cB = 0;
                    Action a = new Action(p, c, r);
                    // Calcul de la postion finale pour une action donnée

                    int[] cols = a.r.toCols(a.c);

                    Position pA = null;
                    Position pB = null;

                    switch (a.r)
                    {
                        case AB:
                        case BA:
                            pA = new Position(this.maxH[cols[0]] - 1, cols[0]);
                            pB = new Position(this.maxH[cols[1]] - 1, cols[1]);
                            break;
                        case BAv:
                            pA = new Position(this.maxH[c] - 1, c);
                            pB = new Position(this.maxH[c] - 2, c);
                            break;
                        default:
                            pA = new Position(this.maxH[c] - 2, c);
                            pB = new Position(this.maxH[c] - 1, c);
                    }

                    if (this.inScreen(pA) && this.inScreen(pB))
                    {
                        this.s[pA.x][pA.y] = a.p.a;
                        this.s[pB.x][pB.y] = a.p.b;

                        for (Position aa : pA.casesAutour())
                            if (this.inScreen(aa) && this.s[aa.x][aa.y].equals(a.p.a))
                                cA++;
                        for (Position ab : pB.casesAutour())
                            if (this.inScreen(ab) && this.s[ab.x][ab.y].equals(a.p.b))
                                cB++;

                        this.s[pA.x][pA.y] = Point.VIDE;
                        this.s[pB.x][pB.y] = Point.VIDE;

                        m.get(cA + cB).add(a);
                    }
                }

            ArrayList<Action> bL = new ArrayList<>();
            int v = 0;
            for (int i = 7; i >= 0 && bL.size() < BRANCHING_FACTOR && v < 2; i--)
            {
                bL.addAll(m.get(i));
                if (bL.size() > 0)
                    v++;
            }

            // SmashTheCode.this.sep("heurCol.Out:" + v + "/" + bL.size());
            Action[] bAL = new Action[bL.size()];
            return bL.toArray(bAL);

        }

        /**
         * Reduit un écran après avoir posé une action pour élminer les bulles de meme couleurs adjacentes.**@return le
         * nouvel écran après réduction.
         */

        private Screen reduce(Score sc)
        {
            Screen r = new Screen(this);

            for (int l = 0; l < SCREEN_HEIGHT; l++)
                for (int c = 0; c < SCREEN_WIDTH; c++)
                {
                    Screen temp = new Screen(r);

                    if (!r.s[l][c].equals(Point.VIDE) && !r.s[l][c].equals(Point.SKULL))
                    {
                        int v = temp.removeByFloodFill(l, c, r.s[l][c]);
                        // sep("Reduce.remove: " + v + " [" + l + "," + c + "] : " + r.s[l][c]);
                        if (v >= 4)
                        {
                            sc.colorCount[r.s[l][c].ordinal()]++;
                            sc.b += v;
                            if (v < sc.GB.length)
                                sc.groupBonus += sc.GB[v];
                            else
                                sc.groupBonus += sc.GB[sc.GB.length - 1];
                            r = temp;
                        }
                    }
                }

            return r;
        }

        /**
         * FloodFill for removal
         */
        private int removeByFloodFill(int row, int col, Point srcColor)
        {
            // make sure row and col are inside the screen
            if (row < 0)
                return 0;
            if (col < 0)
                return 0;
            if (row >= SCREEN_HEIGHT)
                return 0;
            if (col >= SCREEN_WIDTH)
                return 0;

            // make sure this point is the right color to fill OR a skull
            if (!this.s[row][col].equals(srcColor) && !this.s[row][col].equals(Point.SKULL))
                return 0;

            boolean isSkull = this.s[row][col].equals(Point.SKULL);

            // empty point since its right color OR SKULL
            this.s[row][col] = Point.VIDE;

            // But if SKULL return stop and no point
            if (isSkull)
                return 0;

            // else continue and see around
            int v = 1;
            // recursively fill surrounding pixels
            // (this is equivelant to depth-first search)
            v += this.removeByFloodFill(row - 1, col, srcColor);
            v += this.removeByFloodFill(row + 1, col, srcColor);
            v += this.removeByFloodFill(row, col - 1, srcColor);
            v += this.removeByFloodFill(row, col + 1, srcColor);

            // return the number of actual removed of that color
            return v;
        }

        public String to2String(Screen o_s)
        {
            StringBuilder sb = new StringBuilder();
            for (int l = 0; l < SCREEN_HEIGHT; l++)
            {
                for (int c = 0; c < SCREEN_WIDTH; c++)
                    sb.append(this.s[l][c]);

                sb.append(" ");

                for (int c = 0; c < SCREEN_WIDTH; c++)
                    sb.append(o_s.s[l][c]);

                sb.append("\n");
            }
            return sb.toString();
        }

        public String toDebugString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            for (Point[] points : this.s)
            {
                sb.append("\"");
                for (Point p : points)
                    sb.append(p.toDebugString());
                sb.append("\",");
            }
            sb.append("};");
            return sb.toString();
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            for (Point[] points : this.s)
            {
                for (Point p : points)
                    sb.append(p);
                sb.append("\n");
            }
            return sb.toString();
        }

    }

    class ScreenNode implements Comparable<ScreenNode>
    {

        public static final double ALPHA = 0.9;

        private final Screen s;

        private HashMap<ScreenNode, Action> next = new HashMap<>();
        private HashMap<ScreenNode, Boolean> nextDone = new HashMap<>();

        private ScreenNode(Screen p_s)
        {
            this.s = p_s;
        }

        public ScreenNode(String[] p_myGame)
        {
            this.s = new Screen(p_myGame);
        }

        public Entry<ScreenNode, Action> bestEntry()
        {

            Entry<ScreenNode, Action> b = null;
            int max = -Integer.MAX_VALUE;

            SmashTheCode.this.sep("bestEntry.In:" + this.next.size());
            for (Entry<ScreenNode, Action> e : this.next.entrySet())
                if (e.getKey().s.v > max)
                {
                    max = e.getKey().s.v;
                    b = e;
                }

            SmashTheCode.this.sep("bestEntry.Out:" + b);
            return b;
        }

        @Override
        public int compareTo(ScreenNode p_s)
        {
            return -1 * (this.s.v - p_s.s.v);
        }

        private void develop(Pair[] p)
        {
            LinkedList<Pair> pp = new LinkedList<>();

            for (Pair ip : p)
                pp.add(ip);

            this.developRecursive(pp);

            // this.aStar(pp);
        }

        private void developRecursive(LinkedList<Pair> p)
        {
            // SmashTheCode.this.sep("devRec.In" + a.size());

            if (p.isEmpty())
                return;

            if (!testing)
                SmashTheCode.this.checkTime();

            Pair ip = p.pollFirst();

            for (Action a : this.s.probableActionSet(ip))
            {
                ScreenNode n = new ScreenNode(this.s.nextAndHeuristic(a));
                if (!this.next.containsKey(n))
                {
                    this.next.put(n, a);
                    this.nextDone.put(n, false);
                }
            }

            // SmashTheCode.this.sep("devRec.Mid" + this.next.entrySet().size());

            int max = -Integer.MAX_VALUE;

            ArrayList<ScreenNode> ordL = new ArrayList<>(this.next.keySet());
            Collections.sort(ordL);

            for (ScreenNode n : ordL)
                if (!this.nextDone.get(n))
                {
                    n.developRecursive(new LinkedList<>(p));

                    if (n.s.v > max)
                        max = n.s.v;

                    this.nextDone.put(n, true);
                }

            this.s.v = this.s.v + (int) (ALPHA * max);
            // SmashTheCode.this.sep("DevRec.Out:" + bA + "/" + max + " -> " + this.s.v);
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
            ScreenNode other = (ScreenNode) obj;
            if (this.s == null)
            {
                if (other.s != null)
                    return false;
            }
            else if (!this.s.equals(other.s))
                return false;
            return true;
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
            result = prime * result + ((this.s == null) ? 0 : this.s.hashCode());
            return result;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "(" + this.s.v + ")";
        }

    }

    class ZeGeneticAlgo
    {

        private static final int numberOfPair = 4;
        /* GA parameters */
        private static final double uniformRate = 0.5;
        private static final double alpha = 0.7;

        private static final double mutationRate = 0.05;
        private static final int tournamentSize = 5;
        private static final int popSize = 15;
        private static final boolean elitism = true;

        Screen o = null;
        Pair[] p = null;

        /* Public methods */

        // Evolve a population
        public Population evolvePopulation(Population pop)
        {
            Population newPopulation = new Population(this.o, this.p, pop.size(), false);

            // Keep our best individual
            if (elitism)
                newPopulation.add(pop.getFittest());

            Iterator<Individual> it1 = pop.iterator();
            if (elitism)
                it1.next();

            for (; it1.hasNext();)
                newPopulation.add(it1.next());

            // Crossover population
            // Loop over the population size and create new individuals
            // for (int i = 0; i < pop.size() - 1; i++)
            // {
            // Individual indiv1 = this.tournamentSelection(pop);
            // Individual indiv2 = this.tournamentSelection(pop);
            // Individual newIndiv = indiv1.crossover(indiv2);
            // newPopulation.add(newIndiv);
            // }

            // Mutate population
            Iterator<Individual> it = newPopulation.iterator();
            if (elitism)
                it.next();

            for (; it.hasNext();)
                (it.next()).mutate();

            return newPopulation;
        }

        public Individual run(Screen ps, Pair[] pp, Individual seed)
        {

            this.o = ps;
            this.p = Arrays.copyOf(pp, numberOfPair);

            // Create an initial population
            Population myPop = new Population(this.o, this.p, seed, popSize);

            // Evolve our population until we reach an optimum solution
            int generationCount = 0;
            while (SmashTheCode.this.checkTime() || testing)
            {
                generationCount++;
                // if (generationCount % 100 == 0)
                SmashTheCode.this.sep("Generation: " + generationCount + " Fittest: " + myPop.getFittest().getFitness());
                myPop = this.evolvePopulation(myPop);
            }
            SmashTheCode.this.sep("Generation: " + generationCount);
            SmashTheCode.this.sep("Genes:" + myPop.getFittest());

            return myPop.getFittest();
        }

        // Select individuals for crossover
        private Individual tournamentSelection(Population pop)
        {
            // Create a tournament population
            Population tournament = new Population(this.o, this.p, tournamentSize, false);
            // For each place in the tournament get a random individual
            for (int i = 0; i < tournamentSize; i++)
            {
                int randomId = r.nextInt(pop.size());
                tournament.add(pop.get(randomId));
            }
            // Get the fittest
            Individual fittest = tournament.getFittest();
            return fittest;
        }

        class Individual implements Comparable<Individual>
        {
            // un individu est une séquence de 8 actions, 1 action est un triplet Pair, col, rot
            // donc un gène est 8 * (2+1+1) Mais les Pairs sont fixes et l'ordre aussi ...
            // la seule chose qui bouge est donc les 8 col et rot.
            // Donc les indices PAIRS : col
            // Et les indices IMPAIRS : rot

            int defaultGeneLength = numberOfPair * 2;
            private byte[] genes = new byte[this.defaultGeneLength];
            // Cache
            int defaultFitnessVal = -1;
            private int fitness = this.defaultFitnessVal;

            Screen o = null;
            Pair[] p = null;

            private Individual(Individual copy)
            {
                this.o = new Screen(copy.o);
                this.p = copy.p;
            }

            // Create a random individual
            public Individual(Screen orig, Pair[] pp, boolean random)
            {
                this.o = new Screen(orig);
                this.p = Arrays.copyOf(pp, numberOfPair);

                if (random)
                {
                    Action[] a = new Action[this.p.length];
                    Screen[] ss = new Screen[a.length + 1];
                    ss[0] = this.o;
                    for (int i = 0; i < a.length; i++)
                    {
                        Action[] c = ss[i].probableActionSet(this.p[i]);
                        a[i] = c[r.nextInt(c.length)];
                        ss[i + 1] = ss[i].next(a[i]);

                        this.genes[i * 2] = (byte) a[i].c;
                        this.genes[i * 2 + 1] = (byte) a[i].r.ordinal();
                    }

                }
                // for (int i = 0; i < this.defaultGeneLength; i++)
                // {
                // byte gene = 0;
                //
                // if (i % 2 == 0)
                // gene = (byte) r.nextInt(Screen.SCREEN_WIDTH);
                // else
                // {
                // Rotation[] ra = Rotation.availableRots(this.genes[i - 1]);
                // gene = (byte) ra[r.nextInt(ra.length)].ordinal();
                // }
                //
                // this.genes[i] = gene;
                // }
            }

            public Individual(Screen orig, Pair[] pp, Individual prec)
            {
                this.o = new Screen(orig);
                this.p = pp;

                for (int i = 0; i < this.defaultGeneLength - 2; i++)
                    this.genes[i] = prec.genes[i + 2];

                this.genes[this.defaultGeneLength - 2] = (byte) r.nextInt(Screen.SCREEN_WIDTH);
                Rotation[] ra = Rotation.availableRots(this.genes[this.defaultGeneLength - 2]);
                this.genes[this.defaultGeneLength - 1] = (byte) ra[r.nextInt(ra.length)].ordinal();

            }

            @Override
            public int compareTo(Individual p_o)
            {
                return p_o.getFitness() - this.getFitness();
            }

            // Crossover individuals
            private Individual crossover(Individual indiv)
            {
                Individual newSol = new Individual(this);
                // Loop through genes
                for (int i = 0; i < this.defaultGeneLength; i += 2)
                    // Crossover
                    if (r.nextDouble() < uniformRate)
                    {
                        newSol.genes[i] = this.genes[i];
                        newSol.genes[i + 1] = this.genes[i + 1];
                    }
                    else
                    {
                        newSol.genes[i] = indiv.genes[i];
                        newSol.genes[i + 1] = indiv.genes[i + 1];
                    }

                return newSol;
            }

            public Action[] fromPair()
            {
                Action[] r = new Action[this.p.length];

                // SmashTheCode.this.sep(Arrays.toString(this.genes));

                for (int i = 0; i < this.defaultGeneLength; i += 2)
                {
                    int k = i / 2;
                    r[k] = new Action(this.p[k], this.genes[i], Rotation.values()[this.genes[i + 1]]);
                }

                return r;
            }

            public int getFitness()
            {
                if (this.fitness == this.defaultFitnessVal)
                {
                    double d = 0;

                    Action[] a = this.fromPair();
                    Screen[] ss = new Screen[a.length + 1];
                    ss[0] = this.o;
                    for (int i = 0; i < a.length; i++)
                        ss[i + 1] = ss[i].next(a[i]);

                    for (int i = a.length; i >= 0; i--)
                        d = ss[i].v + alpha * d;

                    this.fitness = (int) d;
                }
                return this.fitness;
            }

            // Mutate an individual
            private void mutate()
            {
                // Loop through genes

                Action[] a = this.fromPair();
                Screen[] ss = new Screen[a.length + 1];
                ss[0] = this.o;

                boolean fromHere = false;

                for (int i = 0; i < a.length; i++)
                {
                    if (r.nextDouble() < mutationRate)
                        fromHere = true;

                    if (fromHere)
                    {
                        Action[] c = ss[i].probableActionSet(this.p[i]);
                        a[i] = c[r.nextInt(c.length)];

                        this.genes[i * 2] = (byte) a[i].c;
                        this.genes[i * 2 + 1] = (byte) a[i].r.ordinal();
                    }
                    ss[i + 1] = ss[i].next(a[i]);

                }

                // for (int i = 0; i < this.defaultGeneLength; i++)
                // if (r.nextDouble() < mutationRate)
                // {
                // // Create random gene
                // byte gene = 0;
                //
                // if (i % 2 == 0)
                // gene = (byte) r.nextInt(Screen.SCREEN_WIDTH);
                // else
                // {
                // Rotation[] ra = Rotation.availableRots(this.genes[i - 1]);
                // gene = (byte) ra[r.nextInt(ra.length)].ordinal();
                // }
                // this.genes[i] = gene;
                // }
                if (fromHere)
                    this.fitness = this.defaultFitnessVal;
            }

            /* Getters and setters */
            // Use this if you want to create individuals with different gene lengths
            public void setDefaultGeneLength(int length)
            {
                this.defaultGeneLength = length;
            }

            @Override
            public String toString()
            {
                return this.fitness + ":" + Arrays.toString(this.fromPair());
            }
        }

        class Population implements Iterable<Individual>
        {
            Screen o;
            Pair[] p;

            PriorityQueue<Individual> individuals;

            /*
             * Constructors
             */
            // Create a population
            public Population(Screen orig, Pair[] pp, Individual pre_fittest, int populationSize)
            {
                this.o = new Screen(orig);
                this.p = pp;
                this.individuals = new PriorityQueue<Individual>(populationSize);
                // Initialise population
                // Loop and create individuals
                Individual seed = new Individual(orig, pp, pre_fittest);
                this.individuals.add(seed);

                for (int i = 0; i < populationSize - 1; i++)
                {
                    Individual newIndividual = new Individual(this.o, this.p, true);
                    this.individuals.add(newIndividual);
                }
            }

            /*
             * Constructors
             */
            // Create a population
            public Population(Screen orig, Pair[] pp, int populationSize, boolean initialise)
            {
                this.o = new Screen(orig);
                this.p = pp;
                this.individuals = new PriorityQueue<Individual>(populationSize);
                // Initialise population
                if (initialise)
                    // Loop and create individuals
                    for (int i = 0; i < populationSize; i++)
                    {
                    Individual newIndividual = new Individual(this.o, this.p, true);
                    this.individuals.add(newIndividual);
                    }
            }

            public void add(Individual p_newIndiv)
            {
                this.individuals.add(p_newIndiv);
            }

            public Individual get(int p_i)
            {
                return (Individual) this.individuals.toArray()[p_i];
            }

            public Individual getFittest()
            {
                return this.individuals.peek();
            }

            @Override
            public Iterator<Individual> iterator()
            {
                return this.individuals.iterator();
            }

            /* Public methods */
            // Get population size
            public int size()
            {
                return this.individuals.size();
            }

            /**
             * Redéfinition.
             *
             * @see java.lang.Object#toString()
             */
            @Override
            public String toString()
            {
                StringBuilder sb = new StringBuilder();
                for (Individual i : this)
                    sb.append(i + "\n");

                return sb + "";
            }
        }
    }

}
