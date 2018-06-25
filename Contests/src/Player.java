import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Player
{

    static Scanner in = new Scanner(System.in);

    // static boolean LOCAL = true;
    static boolean LOCAL = false;
    final static boolean DEBUG = true;
    final static Random RAND = new Random();

    static int tour = 0;

    static Position mySide = new Position(0, 0);
    static Position oSide = new Position(1920, 1000);

    public static void ass(boolean b, Object s)
    {
        if (DEBUG && !b)
            System.err.println("(" + tour + ") /!\\ ASSERT ERROR /!\\ : " + s);
    }

    public static void sep(Object s)
    {
        if (DEBUG)
            System.err.println(tour + ": " + s);
    }

    public static void readSites(HashMap<Integer, Site> sites)
    {
        int numSites = in.nextInt();
        for (int i = 0; i < numSites; i++)
        {
            int siteId = in.nextInt();
            int x = in.nextInt();
            int y = in.nextInt();
            int radius = in.nextInt();

            sites.put(siteId, new Site(siteId, x, y, radius));
        }
    }

    public static void updSites(int t, HashMap<Integer, Site> sites)
    {
        for (int i = 0; i < sites.size(); i++)
        {
            int siteId = in.nextInt();
            int ignore1 = in.nextInt(); // used in future leagues
            int ignore2 = in.nextInt(); // used in future leagues
            int structureType = in.nextInt(); // -1 = No structure, 2 = Barracks
            int owner = in.nextInt(); // -1 = No structure, 0 = Friendly, 1 = Enemy
            int param1 = in.nextInt();
            int param2 = in.nextInt();

            sites.get(siteId).upd(t, ignore1, ignore2, structureType, owner, param1, param2);
        }
    }

    public static void main(String args[])
    {
        HashMap<Integer, Site> sites = new HashMap<>();
        readSites(sites);

        // IStrategy st = new Greedy();
        IStrategy st = new Miner();
        // game loop
        while (true)
        {
            int gold = in.nextInt();
            int touchedSite = in.nextInt(); // -1 if none

            updSites(tour, sites);

            State s = new State(tour, gold, touchedSite, sites);

            int numUnits = in.nextInt();
            for (int i = 0; i < numUnits; i++)
            {
                int x = in.nextInt();
                int y = in.nextInt();
                int owner = in.nextInt();
                int unitType = in.nextInt(); // -1 = QUEEN, 0 = KNIGHT, 1 = ARCHER
                int health = in.nextInt();

                s.addUnit(x, y, owner, unitType, health);

                if (tour == 0 && unitType == -1)
                    if (owner == 0)
                        mySide = new Position(x, y);
                    else
                        oSide = new Position(x, y);
            }

            Action queen = st.decide(s);
            Action train = st.decideTrain(s);

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            // First line: A valid queen action
            // Second line: A set of training instructions
            System.out.println(queen);
            System.out.println(train);

            tour++;
        }
    }

    public static enum ActionType
    {
        WAIT, MOVE, BUILD, TRAIN;
    }

    public static class Action
    {
        final ActionType t;
        final Position p;
        final int sid;
        final StructType st;
        final UnitType ct;

        ArrayList<Integer> sids = null;

        // Ctor wait
        public Action()
        {
            this.t = ActionType.WAIT;
            this.p = null;
            this.sid = -1;
            this.st = null;
            this.ct = null;
        }

        // ctor MOVE
        public Action(Position p_p)
        {
            this.t = ActionType.MOVE;
            this.p = p_p;
            this.sid = -1;
            this.st = null;
            this.ct = null;
        }

        // ctor BUILD
        public Action(int p_id, StructType p_st)
        {
            this(p_id, p_st, null);
            ass(p_st != StructType.BARRACKS, "Barracks build action without type : " + p_st);
        }

        public Action(int p_id, StructType p_st, UnitType p_ct)
        {
            ass((p_st != StructType.BARRACKS) == (p_ct == null),
                    "Barracks build action without type or other with : " + p_st + " | " + p_ct);
            this.t = ActionType.BUILD;
            this.p = null;
            this.sid = p_id;
            this.st = p_st;
            this.ct = p_ct;
        }

        // ctor TRAIN
        public Action(ArrayList<Integer> p_l)
        {
            this.t = ActionType.TRAIN;
            this.p = null;
            this.sid = -1;
            this.st = null;
            this.ct = null;
            this.sids = p_l;
        }

        // ctor copy with id for building order
        public Action(int p_id, Action p_a)
        {
            this.t = ActionType.BUILD;
            this.p = null;
            this.sid = p_id;
            this.st = p_a.st;
            this.ct = p_a.ct;
        }

        @Override
        public String toString()
        {
            switch (this.t)
            {
                case MOVE:
                    return this.t + " " + this.p;
                case BUILD:
                    return this.t + " " + this.sid + " " + this.st
                            + (this.st == StructType.BARRACKS ? "-" + this.ct : "");
                case TRAIN:
                    String s = "";
                    if (!this.sids.isEmpty())
                    {
                        s = this.sids.toString();
                        s = " " + s.substring(1, s.length() - 1).replaceAll(",", "");
                    }
                    return this.t + s;
                default:
                    return this.t.toString();
            }
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.ct == null) ? 0 : this.ct.hashCode());
            result = prime * result + ((this.p == null) ? 0 : this.p.hashCode());
            result = prime * result + this.sid;
            result = prime * result + ((this.st == null) ? 0 : this.st.hashCode());
            result = prime * result + ((this.t == null) ? 0 : this.t.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            Action other = (Action) obj;
            if (this.t != other.t)
                return false;
            if (this.sid != other.sid)
                return false;
            if (this.st != other.st)
                return false;
            return true;
        }

    }

    public static class State
    {
        int g; // gold
        int t; // time;
        int id; // touched site

        Unit myQueen;
        Unit oQueen;

        HashSet<Unit> units = new HashSet<>();
        HashMap<Integer, Site> sites;

        public State(int p_time, int p_g, int p_tsid, HashMap<Integer, Site> p_s)
        {
            this.t = p_time;
            this.g = p_g;
            this.id = p_tsid;
            this.sites = p_s;
        }

        // To add units
        public void addUnit(int p_x, int p_y, int p_o, int p_t, int p_h)
        {
            Unit u = new Unit();
            u.upd(this.t, p_x, p_y, p_o, p_t, p_h);

            if (u.isQueen())
                if (u.isMine())
                    this.myQueen = u;
                else
                    this.oQueen = u;

            this.units.add(u);
        }

    }

    public static class Miner extends Greedy
    {

        LinkedList<Action> toDo = new LinkedList<>();

        State old = null;

        public Miner()
        {
            this.toDo.add(new Action(-1, StructType.TOWER));
            this.toDo.add(new Action(-1, StructType.MINE));
            this.toDo.add(new Action(-1, StructType.BARRACKS, UnitType.KNIGHT));
            this.toDo.add(new Action(-1, StructType.TOWER));
            // this.toDo.add(new Action(-1, StructType.BARRACKS, UnitType.ARCHER));
            this.toDo.add(new Action(-1, StructType.BARRACKS, UnitType.GIANT));
            this.toDo.add(new Action(-1, StructType.TOWER));

        }

        public void updateToDo(State p_s)
        {
            if (this.old == null || this.toDo.isEmpty() || this.toDo.peekFirst().sid == -1)
                return;

            Action current = this.toDo.peekFirst();
            sep("upToDo " + current + "-->" + this.old.sites.get(current.sid));

            if (this.old.sites.get(current.sid).t == current.st)
                this.toDo.removeFirst();
        }

        public void evaluateLosses(State p_s)
        {
            Action mineAction = new Action(-1, StructType.MINE);
            if (this.mines.size() < 1 && this.toDo.contains(mineAction))
                this.toDo.add(mineAction);

            Action toAction = new Action(-1, StructType.TOWER);
            if (this.towers.size() < 2 && this.toDo.contains(toAction))
                this.toDo.add(toAction);
        }

        private Action upgradeTowers(State p_s)
        {
            for (Site t : this.towers)
                if (t.id == p_s.id && t.a < 700)
                    return this.buildSite(t, StructType.TOWER);

            return null;
        }

        @Override
        public Action decide(State p_s)
        {
            Action a = null;

            this.initTour(p_s);

            this.updateToDo(p_s);
            this.evaluateLosses(p_s);

            a = this.upgradeMines();
            if (a != null)
                return a;

            a = this.upgradeTowers(p_s);
            if (a != null)
                return a;

            boolean isSafe = this.computeSafeness(p_s);

            if (isSafe)
                if (!this.toDo.isEmpty())
                {
                    sep("Build TODO : " + this.toDo);
                    Action build = this.toDo.peekFirst();

                    // Filter goldy sites.
                    TreeSet<Site> goldEmptySafe = this.emptySafe;
                    if (build.st == StructType.MINE)
                    {
                        goldEmptySafe = new TreeSet<>(new RelativeComparator(mySide));
                        goldEmptySafe.addAll(this.emptySafe.stream().filter(s -> s.g > 100 || s.g == -1)
                                .collect(Collectors.toSet()));
                        sep("Mining : " + goldEmptySafe);
                    }
                    if (goldEmptySafe.isEmpty())
                        goldEmptySafe = this.emptySafe;
                    // if any we know are depleted.

                    this.toDo.set(0, new Action(goldEmptySafe.first().id, build));
                    a = this.toDo.peekFirst();
                }
                else // Build mine if in my territory of tower in her ?
                {
                    Site sn = this.emptySafe.first();
                    boolean siteSafe = this.siteSafe(sn);
                    sep("Build " + (sn.isOnMySide() ? "Mine" : "Tower") + (siteSafe ? " and safe" : " but unsafe"));
                    if (sn.isOnMySide() && sn.g > 10)
                        a = this.buildSite(sn, StructType.MINE);
                    else if (siteSafe)
                        a = this.buildSite(sn, StructType.TOWER);
                    else
                        a = this.flee(p_s);
                }

            // Flee ... intelligently ?
            if (a == null)
            {
                sep("Fleeeeeeeeeeeeeeeeeeee !");
                a = this.flee(p_s);
            }

            this.old = p_s;
            return a;
        }

        @Override
        public Action flee(State p_s)
        {
            // find pos in range of a max of towers of by default near archer.

            return new Action(mySide);
        }

        public boolean siteSafe(Site sn)
        {
            if (!this.oTowers.isEmpty())
                for (Site s : this.oTowers)
                    if ((s.p.distanceL2(this.me.p) - 30) <= s.rad)
                        return false;

            return true;
        }

        public boolean computeSafeness(State p_s)
        {
            // TODO Evaluate danger from ennemies

            // In ennemy tower range
            if (!this.oTowers.isEmpty())
                for (Site s : this.oTowers)
                    if ((s.p.distanceL2(this.me.p) - 30) <= s.rad)
                        return false;

            return true;
        }

        @Override
        public Action decideTrain(State p_s)
        {
            return super.decideTrain(p_s);
        }

    }

    public static class Greedy implements IStrategy
    {

        // speed + Queen size + Creep size + safeness
        public static final double ATTACK_DIST = 100 + 30 + 20 + 50;

        Unit me;
        Unit bad;

        TreeSet<Site> empty;
        TreeSet<Site> emptySafe;
        TreeSet<Site> barrKn;
        TreeSet<Site> barrAr;
        TreeSet<Site> barrGn;
        TreeSet<Site> towers;
        TreeSet<Site> mines;
        TreeSet<Site> oTowers;

        public void initTour(State p_s)
        {
            this.me = p_s.myQueen;
            this.bad = p_s.oQueen;

            this.empty = new TreeSet<>(new RelativeComparator(this.me.p));
            this.emptySafe = new TreeSet<>(new RelativeComparator(mySide));
            this.barrKn = new TreeSet<>(new RelativeComparator(this.me.p));
            this.barrAr = new TreeSet<>(new RelativeComparator(this.me.p));
            this.barrGn = new TreeSet<>(new RelativeComparator(this.me.p));
            this.towers = new TreeSet<>(new RelativeComparator(this.me.p));
            this.oTowers = new TreeSet<>(new RelativeComparator(this.me.p));
            this.mines = new TreeSet<>(new RelativeComparator(this.me.p));

            this.empty.addAll(p_s.sites.values().stream().filter(s -> s.isEmpty()).collect(Collectors.toSet()));
            this.emptySafe.addAll(this.empty);

            this.barrKn.addAll(p_s.sites.values().stream()
                    .filter(s -> s.isMine() && s.t == StructType.BARRACKS && s.c == UnitType.KNIGHT)
                    .collect(Collectors.toSet()));
            this.barrAr.addAll(p_s.sites.values().stream()
                    .filter(s -> s.isMine() && s.t == StructType.BARRACKS && s.c == UnitType.ARCHER)
                    .collect(Collectors.toSet()));
            this.barrGn.addAll(p_s.sites.values().stream()
                    .filter(s -> s.isMine() && s.t == StructType.BARRACKS && s.c == UnitType.GIANT)
                    .collect(Collectors.toSet()));

            this.mines.addAll(p_s.sites.values().stream().filter(s -> s.isMine() && s.t == StructType.MINE)
                    .collect(Collectors.toSet()));

            this.towers.addAll(p_s.sites.values().stream().filter(s -> s.isMine() && s.t == StructType.TOWER)
                    .collect(Collectors.toSet()));
            this.oTowers.addAll(p_s.sites.values().stream().filter(s -> !s.isMine() && s.t == StructType.TOWER)
                    .collect(Collectors.toSet()));
        }

        public Action buildSite(Site sn, StructType p_type)
        {
            return this.buildSite(sn, p_type, null);
        }

        public Action buildSite(Site sn, StructType p_type, UnitType p_unit)
        {
            double dist = this.me.p.distanceL2(sn.p);
            if (dist < (sn.rad + Unit.QR)) // in build range
                return new Action(sn.id, p_type, p_unit); // Build
            else
                return new Action(sn.p); // Move to there
        }

        public Action flee(State p_s)
        {
            Vector2D sumOfE = new Vector2D();
            for (Unit ee : p_s.units)
                if (!ee.isMine())
                {
                    Vector2D eToMe = this.me.p.minus(ee.p);
                    if (eToMe.getR() < ATTACK_DIST)
                        sumOfE = sumOfE.plus(eToMe);
                }

            Position moveTo = new Position(this.me.p.plus(sumOfE));
            sep("Flee to " + moveTo);
            return new Action(moveTo);
        }

        public Action upgradeMines()
        {
            // Find my sub exploited mines
            TreeSet<Site> m = new TreeSet<>(new RelativeComparator(this.me.p));
            m.addAll(this.mines.stream().filter(s -> s.cgr < s.mgr).collect(Collectors.toSet()));

            // Up mine if possible
            if (!m.isEmpty())
                return this.buildSite(m.first(), StructType.MINE);
            return null;
        }

        @Override
        public Action decide(State p_s)
        {
            this.initTour(p_s);

            // Find my towers
            double tc = this.towers.size();

            Action up = this.upgradeMines();
            if (up != null)
                return up;

            // Build anything
            if (!this.empty.isEmpty() && this.me.h >= this.bad.h) // Sites to build
            {

                double kc = this.barrKn.size();
                double ac = this.barrAr.size();
                double gc = this.barrGn.size();

                sep("Building : BK:" + kc + " | BA:" + ac + " | BG:" + gc + " | T:" + tc);

                StructType toBuild = StructType.BARRACKS;
                UnitType barrackBuild = UnitType.KNIGHT;
                if (kc < 1 || ac < 1)// || gc < 1)
                {
                    if (kc >= 1 & ac < 1)
                        barrackBuild = UnitType.ARCHER;
                    // else if (kc >= 1 & gc < 1)
                    // barrackBuild = UnitType.GIANT;
                }
                else if (tc < 1)
                    toBuild = StructType.TOWER;
                else
                    toBuild = StructType.MINE;

                // find nearest
                return this.buildSite(this.empty.first(), toBuild, barrackBuild);
            }
            else if (!this.empty.isEmpty())
                return this.buildSite(this.empty.first(), StructType.TOWER);

            // No more free sites of fleeing time : go back to nearest tower to renew
            if (!this.towers.isEmpty())
            {
                sep("Mv to tower " + this.towers.first().id + " because " + this.towers.first().rad);
                return this.buildSite(this.towers.first(), StructType.TOWER);
            }

            // if no tower to flee just flee

            // compute farthest position from other knights.
            return this.flee(p_s);
        }

        @Override
        public Action decideTrain(State p_s)
        {
            int gold = p_s.g;

            ArrayList<Integer> sToConstruct = new ArrayList<>();

            TreeSet<Site> t = new TreeSet<>(new RelativeComparator(this.bad.p));
            t.addAll(p_s.sites.values().stream().filter(s -> s.isMine()).collect(Collectors.toSet()));

            double kc = p_s.units.stream().filter(u -> u.isMine() && u.c == UnitType.KNIGHT).collect(Collectors.toSet())
                    .size();
            double ac = p_s.units.stream().filter(u -> u.isMine() && u.c == UnitType.ARCHER).collect(Collectors.toSet())
                    .size();

            double otc = p_s.sites.values().stream().filter(s -> !s.isMine() && s.t == StructType.TOWER)
                    .collect(Collectors.toSet()).size();

            sep("Training : K:" + kc + " | A:" + ac + " | r:" + (kc / ac));
            for (Site s : t)
                if (s.isAvailable() && gold >= s.c.c)
                    if ((s.c == UnitType.KNIGHT && kc < 1) || (s.c == UnitType.KNIGHT && kc / ac < 3)
                            || (s.c == UnitType.GIANT && otc >= 1) || (s.c == UnitType.ARCHER))
                    {
                        sToConstruct.add(s.id);
                        gold -= s.c.c;
                    }

            return new Action(sToConstruct);
        }

    }

    public static interface IStrategy
    {
        public Action decide(State p_s);

        public Action decideTrain(State p_s);
    }

    public static class Entity
    {
        Position p;
        int lastupd;

        @Override
        public Entity clone()
        {
            Entity e = new Entity();
            e.p = this.p;
            e.lastupd = this.lastupd;
            return e;
        }

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
            return this.p.equals(other.p);
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            return prime * (int) (this.p.x * this.p.y);
        }

        @Override
        public String toString()
        {
            return this.getClass().getSimpleName() + " [" + this.p + ", t=" + this.lastupd + "]";
        }

    }

    public static class Site extends Entity
    {
        final int id;
        int rad;
        StructType t; // type
        Owner o; // owner
        int a; // availability
        UnitType c; // creep type;

        // Gold
        int g; // gold left
        int mgr; // max gold rate
        int cgr; // current gold rate

        public Site(int p_id, int p_x, int p_y, int p_rad)
        {
            this.p = new Position(p_x, p_y);
            this.id = p_id;
            this.rad = p_rad;
        }

        public boolean isMine()
        {
            return this.o == Owner.ME;
        }

        public boolean isEmpty()
        {
            return this.t == StructType.NO;
        }

        public boolean isOnMySide()
        {
            double distMySide = this.p.minus(mySide).getR2();
            double distOSide = this.p.minus(oSide).getR2();

            return distMySide < distOSide;
        }

        public void upd(int p_time, int p_gold, int p_MaxMine, int p_t, int p_o, int p_p1, int p_p2)
        {
            this.lastupd = p_time;

            this.o = Owner.fromInt(p_o);
            this.t = StructType.fromInt(p_t);
            if (this.t == StructType.TOWER)
                this.rad = p_p2;
            else
                this.c = UnitType.fromInt(p_p2);

            if (this.t == StructType.MINE)
            {
                this.g = p_gold;
                this.mgr = p_MaxMine;
                this.cgr = p_p1;
            }
            else
                this.a = p_p1;

            // sep("upd : " + this);
        }

        public boolean isAvailable()
        {
            return this.a == 0;
        }

        @Override
        public String toString()
        {
            return this.t + " [" + this.p + ", t=" + this.lastupd + "] (" + this.o + ":" + this.c + "," + this.a + ") "
                    + " *" + this.g + ",+" + this.cgr + ",_" + this.mgr + "*";
        }
    }

    public static class Unit extends Entity
    {

        public static final int QR = 30 + 5;
        Owner o; // owner
        UnitType c; // unit type;
        int h; // health

        public void upd(int p_time, int p_x, int p_y, int p_o, int p_t, int p_h)
        {
            this.p = new Position(p_x, p_y);
            this.lastupd = p_time;

            this.o = Owner.fromInt(p_o);
            this.c = UnitType.fromInt(p_t);
            this.h = p_h;
            // sep("upd : " + this);
        }

        public boolean isMine()
        {
            return this.o == Owner.ME;
        }

        public boolean isQueen()
        {
            return this.c == UnitType.QUEEN;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((this.c == null) ? 0 : this.c.hashCode());
            result = prime * result + this.h;
            result = prime * result + ((this.o == null) ? 0 : this.o.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            Unit other = (Unit) obj;
            if (this.c != other.c)
                return false;
            if (this.h != other.h)
                return false;
            if (this.o != other.o)
                return false;
            return true;
        }

        @Override
        public String toString()
        {
            return this.getClass().getSimpleName() + " [" + this.p + ", t=" + this.lastupd + "] (" + this.o + ":"
                    + this.c + "," + this.h + ")";
        }

    }

    public static enum Owner
    {
        NO, ME, OTHER;

        static Owner fromInt(int p_o)
        {
            return Owner.values()[p_o + 1];
        }
    }

    public static enum StructType
    {
        NO, MINE, TOWER, BARRACKS;

        static StructType fromInt(int p_t)
        {
            return StructType.values()[p_t + 1];
        }
    }

    public static enum UnitType
    {
        QUEEN(Integer.MAX_VALUE), KNIGHT(80), ARCHER(100), GIANT(140);

        int c;

        UnitType(int p_cost)
        {
            this.c = p_cost;
        }

        static UnitType fromInt(int p_c)
        {
            return UnitType.values()[p_c + 1];
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
        private HashSet<Position> getInRange(int p_r, Distance p_d)
        {
            HashSet<Position> s = new HashSet<>();

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

    public static class OldestComparator implements Comparator<Site>
    {
        @Override
        public int compare(Site p_a, Site p_b)
        {
            ass(p_a.t == StructType.TOWER, p_a + " is no Tower !");
            ass(p_b.t == StructType.TOWER, p_b + " is no Tower !");
            return p_a.rad - p_b.rad;
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

}
