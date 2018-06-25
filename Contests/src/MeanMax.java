import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class MeanMax
{
    static Scanner in = new Scanner(System.in);

    // static boolean LOCAL = true;
    static boolean LOCAL = false;
    final static boolean DEBUG = true;
    // final static Random RAND = new Random();

    public final static ArrayList<Position> PS;

    static
    {
        PS = new ArrayList<>();
        for (int i : new int[] { 3200, 4500, 5800 })
            for (int r = 0; r < 2 * Math.PI; r += Math.PI / 3)
            {
                Vector2D v = new Vector2D(i, r);
                PS.add(new Position(v.toRect()));
            }
        sep(PS);
    }

    public static int SIZE = 0;

    static int tour = 0;

    public static void sep(Object s)
    {
        if (DEBUG)
            System.err.println("(" + tour + ") : " + s);
    }

    public static int ro(double d)
    {
        return (int) Math.round(d);
    }

    public static void ass(boolean b, Object s)
    {
        if (DEBUG && !b)
            System.err.println("(" + tour + ") /!\\ ASSERT ERROR /!\\ : " + s);
    }

    public static void read(State p_s)
    {
        // Score
        // int myScore = in.nextInt();
        // int enemyScore1 = in.nextInt();
        // int enemyScore2 = in.nextInt();
        p_s.updSc(in.nextInt(), in.nextInt(), in.nextInt());
        // Rage
        // int myRage = in.nextInt();
        // int enemyRage1 = in.nextInt();
        // int enemyRage2 = in.nextInt();
        p_s.updRage(in.nextInt(), in.nextInt(), in.nextInt());

        // Entities
        int unitCount = in.nextInt();
        for (int i = 0; i < unitCount; i++)
            p_s.upd(tour, in.nextInt(), in.nextInt(), in.nextInt(), in.nextFloat(), in.nextInt(), in.nextInt(),
                    in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
        // int unitId = in.nextInt();
        // int unitType = in.nextInt();
        // int player = in.nextInt();
        // float mass = in.nextFloat();
        // int radius = in.nextInt();
        // int x = in.nextInt();
        // int y = in.nextInt();
        // int vx = in.nextInt();
        // int vy = in.nextInt();
        // int extra = in.nextInt();
        // int extra2 = in.nextInt();

        p_s.filterOldEntities(tour);
    }

    public static void main(String args[])
    {

        State s = new State();

        IStrategy st = new Greedy();

        // game loop
        while (true)
        {
            tour++;

            read(s);
            sep(s);

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            System.out.println(st.decideR(s));
            System.out.println(st.decideD(s));
            System.out.println(st.decideDd(s));
        }
    }

    public static class Greedy implements IStrategy
    {
        Action lastA = null;

        public Greedy()
        {}

        @Override
        public Action decideR(State p_s)
        {
            // Action a = new Action(new Position(0, 0), 1000);
            Reaper me = (Reaper) p_s.get(0);
            Destroyer meD = (Destroyer) p_s.get(1);
            Doofus med = (Doofus) p_s.get(2);

            HashSet<Wreck> wrecks = new HashSet<>(p_s.getWrecks());

            TreeSet<Position> w = new TreeSet<>(new NearestComparator(me.p));
            for (Wreck w1 : wrecks)
                for (Wreck w2 : wrecks)
                    if (w1 != w2 && w1.p.distanceL2(w2.p) < (w1.rad + w2.rad))
                        w.add(new Position(w1.p.plus(w2.p.minus(w1.p).scalarMult(0.5))));

            if (!w.isEmpty())
                sep(" ---- OVERLAPS : " + w);

            // No overlaps
            if (w.isEmpty())
                w.addAll(wrecks.stream().map(q -> q.p).collect(Collectors.toList()));

            // No Wrecks
            if (w.isEmpty())
                w.addAll(p_s.getTankers().stream().map(q -> q.p).collect(Collectors.toList()));

            // Not inside.
            w.removeIf(p -> p.distanceL2(new Position(0, 0)) > 6000);

            Destroyer d = (Destroyer) p_s.get(1);
            Action a = new Action(new Position(me.p.plus(d.p.minus(me.p).scalarMult(0.5))),
                    (int) me.p.distanceL2(d.p) / 2);
            if (!w.isEmpty())
                a = new Action(w.first(), (int) me.p.distanceL2(w.first()) / 4);

            this.lastA = a;
            return a;
        }

        @Override
        public Action decideD(State p_s)
        {
            // Action a = new Action(new Position(0, 0), 1000);
            Reaper meR = (Reaper) p_s.get(0);
            Destroyer me = (Destroyer) p_s.get(1);
            Doofus meD = (Doofus) p_s.get(2);

            TreeSet<Position> w = new TreeSet<>(new NearestComparator(me.p));
            w.addAll(p_s.getTankers().stream().map(q -> q.p).collect(Collectors.toList()));
            w.removeAll(p_s.getWrecks().stream().map(q -> q.p).collect(Collectors.toList()));

            // Not inside.
            w.removeIf(p -> p.distanceL2(new Position(0, 0)) > 6000);

            Action a = new Action();
            if (!w.isEmpty())
                a = new Action(w.first(), (int) me.p.distanceL2(w.first()));

            this.lastA = a;
            return a;
        }

        @Override
        public Action decideDd(State p_s)
        {
            // Action a = new Action(new Position(0, 0), 1000);
            // Reaper meR = (Reaper) p_s.get(0);
            // Destroyer meD = (Destroyer) p_s.get(1);
            Doofus me = (Doofus) p_s.get(2);

            TreeSet<Position> nextEmpty = new TreeSet<>(new NearestComparator(me.p));
            TreeSet<Position> nextNotEmpty = new TreeSet<>(new NearestComparator(me.p));
            TreeSet<Position> next = new TreeSet<>(new NearestComparator(me.p));
            next.addAll(PS);
            for (Position pp : next)
            {
                Entity u = p_s.firstEntityOnTrajectory(me, pp);
                if (u == null)
                    nextEmpty.add(pp);
                else
                    nextNotEmpty.add(u.p);
            }

            Action a = new Action(new Position(0, 0), 300);
            if (!nextEmpty.isEmpty())
                a = new Action(nextEmpty.last(), 300);
            else
                a = new Action(nextNotEmpty.last(), 300);

            this.lastA = a;
            return a;
        }

    }

    static interface IStrategy
    {
        Action decideR(State p_s);

        Action decideDd(State p_s);

        Action decideD(State p_s);
    }

    enum ActionType
    {
        WAIT, ACC;
    }

    static class Action
    {
        ActionType t;
        Position p;
        int th; // throttle;

        Action()
        {
            this.t = ActionType.WAIT;
        }

        Action(Position p_p, int p_th)
        {
            this.t = ActionType.ACC;
            this.th = p_th;
            this.p = p_p;
        }

        @Override
        public String toString()
        {
            switch (this.t)
            {
                case WAIT:
                    return this.t + "";
                case ACC:
                    return this.p + " " + this.th;
            }
            return "";
        }

    }

    static class Wreck extends Tanker
    {

        public Wreck(int p_entityId)
        {
            super(p_entityId);
        }

        @Override
        public Entity clone()
        {
            Wreck c = new Wreck(this.id);
            c.upd(this.lastupd, this.mass, this.rad, ro(this.p.x), ro(this.p.y), ro(this.v.x), ro(this.v.y), this.w,
                    -1);
            return c;
        }
    }

    static class Tanker extends Entity
    {
        int w; // water left
        int c; // water capacity

        static final float MASS = 1.5f;
        static final float FRICTION = 0.3f;

        public Tanker(int p_entityId)
        {
            super(p_entityId, -1);
        }

        @Override
        public Entity clone()
        {
            Tanker c = new Tanker(this.id);
            c.upd(this.lastupd, this.mass, this.rad, ro(this.p.x), ro(this.p.y), ro(this.v.x), ro(this.v.y), this.w,
                    this.c);
            return c;
        }

        @Override
        void upd(int p_t, float p_m, int p_r, int p_x, int p_y, int p_vx, int p_vy, int p_e, int p_e2)
        {
            super.upd(p_t, p_m, p_r, p_x, p_y, p_vx, p_vy, p_e, p_e2);
            this.w = p_e;
            this.c = p_e2;
        }

        @Override
        public String toString()
        {
            return this.getClass().getSimpleName() + "\t(t=" + this.lastupd + ") id=" + this.id + "\tp=[" + this.p
                    + "]\tw=" + this.w + "";
        }
    }

    static class Doofus extends Reaper
    {

        static final float FRICTION = 0.3f;

        public Doofus(int p_entityId, int p_o)
        {
            super(p_entityId, p_o);
        }

        @Override
        public Entity clone()
        {
            Doofus c = new Doofus(this.id, this.owner);
            c.upd(this.lastupd, this.mass, this.rad, ro(this.p.x), ro(this.p.y), ro(this.v.x), ro(this.v.y), -1, -1);
            return c;
        }
    }

    static class Destroyer extends Reaper
    {

        static final float FRICTION = 0.3f;

        public Destroyer(int p_entityId, int p_o)
        {
            super(p_entityId, p_o);
        }

        @Override
        public Entity clone()
        {
            Destroyer c = new Destroyer(this.id, this.owner);
            c.upd(this.lastupd, this.mass, this.rad, ro(this.p.x), ro(this.p.y), ro(this.v.x), ro(this.v.y), -1, -1);
            return c;
        }
    }

    static class Reaper extends Entity
    {

        static final float FRICTION = 0.2f;

        public Reaper(int p_entityId, int p_o)
        {
            super(p_entityId, p_o);
        }

        @Override
        public Entity clone()
        {
            Reaper c = new Reaper(this.id, this.owner);
            c.upd(this.lastupd, this.mass, this.rad, ro(this.p.x), ro(this.p.y), ro(this.v.x), ro(this.v.y), -1, -1);
            return c;
        }

        @Override
        void upd(int p_t, float p_m, int p_r, int p_x, int p_y, int p_vx, int p_vy, int p_e, int p_e2)
        {
            super.upd(p_t, p_m, p_r, p_x, p_y, p_vx, p_vy, p_e, p_e2);
        }

        @Override
        public String toString()
        {
            return this.getClass().getSimpleName() + "\t(t=" + this.lastupd + ") id=" + this.id + "\tp=[" + this.p
                    + "]\tv=[" + this.v + "]";
        }

    }

    static class State implements Cloneable
    {
        int t = 0;

        int[] sc = new int[3];
        int[] r = new int[3];
        HashMap<Integer, Entity> e = new HashMap<>();

        @Override
        public State clone()
        {
            State n = new State();

            n.sc = Arrays.copyOf(this.sc, 3);
            n.r = Arrays.copyOf(this.r, 3);

            for (Entity ne : this.e.values())
                n.e.put(ne.id, ne.clone());

            return n;
        }

        public Collection<Wreck> getWrecks()
        {
            return this.e.values().stream().filter(v -> v instanceof Wreck).map(c -> (Wreck) c)
                    .collect(Collectors.toList());

        }

        public Collection<Tanker> getTankers()
        {
            return this.e.values().stream().filter(v -> v instanceof Tanker).map(c -> (Tanker) c)
                    .collect(Collectors.toList());

        }

        public Entity firstEntityOnTrajectory(Entity p_e, Position p_obj)
        {
            Position p_pos = p_e.p;
            RelativeComparator c = new RelativeComparator(p_pos);
            TreeSet<Entity> t = new TreeSet<>(c);

            double d = p_pos.distanceL2(p_obj);
            Vector2D v = p_obj.minus(p_pos);

            Rectangle rect = new Rectangle((int) (p_pos.x - p_e.rad), (int) p_pos.y, (int) d, 2 * p_e.rad);

            // sep("Pre Rotation (" + v + "): " + rect.getBounds2D());

            AffineTransform a = new AffineTransform();
            a.rotate(v.getTheta(), p_pos.x, p_pos.y);
            Shape s = a.createTransformedShape(rect);

            // sep("Post Rotation (" + v.getTheta() + "): " + s.getBounds2D());

            for (Entity ee : this.e.values())
                if (!p_e.equals(ee) && s.intersects(ee.p.x - ee.rad, ee.p.y - ee.rad, ee.rad * 2, ee.rad * 2))
                    t.add(ee);

            if (t.size() > 0)
                return t.first();
            else
                return null;
        }

        public Entity get(int p_id)
        {
            return this.e.get(p_id);
        }

        public void updSc(int p_0, int p_1, int p_2)
        {
            this.sc[0] = p_0;
            this.sc[1] = p_1;
            this.sc[2] = p_2;
        }

        public void updRage(int p_0, int p_1, int p_2)
        {
            this.r[0] = p_0;
            this.r[1] = p_1;
            this.r[2] = p_2;
        }

        public void upd(int p_t, int p_id, int p_type, int p_o, float p_m, int p_r, int p_x, int p_y, int p_vx,
                int p_vy, int p_e, int p_e2)
        {
            this.t = p_t;
            Entity u = this.get(p_id);
            if (u == null)
            {
                u = this.createEntity(p_id, p_type, p_o);
                this.e.put(u.id, u);
            }

            // ass(u != null, "Entity Unknown Type : " + p_type);
            // ass(u.id == p_id, "Entity ID");
            // ass((u instanceof Reaper && p_type == 0) || (u instanceof Doofus && p_type == 2)
            // || (u instanceof Destroyer && p_type == 1) || (u instanceof Tanker && p_type == 3)
            // || (u instanceof Wreck && p_type == 4), "Entity type");
            // ass(u.owner == p_o, "Entity Owner");
            u.upd(p_t, p_m, p_r, p_x, p_y, p_vx, p_vy, p_e, p_e2);
        }

        public void filterOldEntities(int p_t)
        {
            this.e.entrySet().removeIf(e -> e.getValue().lastupd < p_t);
        }

        private Entity createEntity(int p_id, int p_type, int p_o)
        {
            switch (p_type)
            {
                case 0:
                    return new Reaper(p_id, p_o);
                case 1:
                    return new Destroyer(p_id, p_o);
                case 2:
                    return new Doofus(p_id, p_o);
                case 3:
                    return new Tanker(p_id);
                case 4:
                    return new Wreck(p_id);
                default:
                    return null;
            }
        }

        @Override
        public String toString()
        {
            String s = "(" + this.t + ") \n";
            s += this.e.entrySet().stream().map((entry) -> "[" + entry.getKey() + "]" + entry.getValue())
                    .collect(Collectors.joining("\n"));
            return s;
        }

    }

    public static abstract class Entity implements Cloneable
    {
        Position p;
        Vector2D v;
        float mass;
        int rad;
        final int id;
        final int owner;
        int lastupd;

        public Entity(int p_entityId, int p_owner)
        {
            this.id = p_entityId;
            this.owner = p_owner;
        }

        @Override
        public abstract Entity clone();

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {

            Entity other = (Entity) obj;
            if (obj == null)
                return false;
            return (this.id == other.id);
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
            return prime * this.id;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return this.getClass().getSimpleName() + "(t=" + this.lastupd + ") id=" + this.id + ",p=[" + this.p
                    + "],v=[" + this.v + "]";
        }

        void upd(int p_t, float p_m, int p_r, int p_x, int p_y, int p_vx, int p_vy, int p_e, int p_e2)
        {
            this.p = new Position(p_x, p_y);
            this.v = new Vector2D(p_vx, p_vy);
            this.mass = p_m;
            this.rad = p_r;
            this.lastupd = p_t;
        }

    }

    public static enum Distance
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
                    return p_p1.distanceL1(p_p2);
                case CHEBYSHEV:
                    return p_p1.distanceL15(p_p2);
                case SQ_EUCLID:
                    return p_p1.minus(p_p2).getR2();
                default:
                    return p_p1.distanceL2(p_p2);
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
        public Position(double p_X, double p_Y)
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

        public static Position lineIntersect(Position p1, Position p2, Position p3, Position p4)
        {
            double EPS = 0.0000001;

            double denom = (p4.y - p3.y) * (p2.x - p1.x) - (p4.x - p3.x) * (p2.y - p1.y);
            double numera = (p4.x - p3.x) * (p1.y - p3.y) - (p4.y - p3.y) * (p1.x - p3.x);
            double numerb = (p2.x - p1.x) * (p1.y - p3.y) - (p2.y - p1.y) * (p1.x - p3.x);

            /* Are the line coincident? */
            if (Math.abs(numera) < EPS && Math.abs(numerb) < EPS && Math.abs(denom) < EPS)
                return new Position((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);

            /* Are the line parallel */
            if (Math.abs(denom) < EPS)
                return null;

            /* Is the intersection along the the segments */
            double mua = numera / denom;
            double mub = numerb / denom;
            if (mua < 0 || mua > 1 || mub < 0 || mub > 1)
                return null;

            return new Position(p1.x + mua * (p2.x - p1.x), p1.y + mua * (p2.y - p1.y));
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return ((int) this.x) + " " + ((int) this.y);
        }

    }

    public static class Vector2D
    {
        final double x;
        final double y;

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
        public Vector2D(double p_x, double p_y)
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

        /** Product of the vector and scalar */
        public Vector2D scalarMult(double scalar)
        {
            return new Vector2D(scalar * this.x, scalar * this.y);
        }

        /**
         * Renvoie la distance (de Manhattan) entre deux position
         *
         * @param p_rel
         *            La position relative
         * @return la distance de Manhattan entre la position courante et la position relative
         */
        public double distanceL1(Vector2D p_o)
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

        public double distanceL15(Vector2D p_rel)
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
        public double distanceL2(Vector2D p_rel)
        {
            return Math.sqrt(Math.pow(this.x - p_rel.x, 2) + Math.pow(this.y - p_rel.y, 2));
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
            return "[" + (int) this.x + "," + (int) this.y + "]";
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

            if (((int) this.x) != (int) obj.x)
                return false;
            if (((int) this.y) != (int) obj.y)
                return false;
            return true;
        }
    }

    public static class NearestComparator implements Comparator<Position>
    {
        Position ref = null;
        Distance d = Distance.EUCLIDEAN;

        public NearestComparator(Position p_ref)
        {
            this.ref = p_ref;
        }

        public NearestComparator(Position p_ref, Distance p_d)
        {
            this.ref = p_ref;
            this.d = p_d;
        }

        @Override
        public int compare(Position p_o1, Position p_o2)
        {
            double d1 = this.d.val(this.ref, p_o1);
            double d2 = this.d.val(this.ref, p_o2);

            return (int) Math.round(d1 - d2);
        }

    }

    public static class RelativeComparator implements Comparator<Entity>
    {

        Position toCompare;

        public RelativeComparator(Position p_p)
        {
            this.toCompare = p_p;
        }

        @Override
        public int compare(Entity p_a, Entity p_b)
        {
            double a = p_a.p.distanceL2(this.toCompare);
            double b = p_b.p.distanceL2(this.toCompare);
            return (int) (a - b);
        }

    }

}
