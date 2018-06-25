import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet;

/**
 * Send your busters out into the fog to trap ghosts and bring them home!
 **/
class FantasticBits
{

    public static final Scanner in = new Scanner(System.in);

    final static int WIZ_RAD = 400;
    final static int SNA_RAD = 150;
    final static int BLU_RAD = 200;
    final static int MAXX = 16001;
    final static int MAXY = 75001;

    final static int MAXPOW = 500;
    final static int MAXTHRUST = 150;

    final static boolean DEBUG = true;

    final static Position[] GOALS = new Position[2];

    final static Random r = new Random();

    static Wizard[][] teams = new Wizard[2][2];

    static int tour = 0;

    static TeamCoordinator TC;

    public static void main(String args[])
    {

        State s = new State();
        GOALS[0] = new Position(0, 3750);
        GOALS[1] = new Position(16000, 3750);

        // if 0 you need to score on the right of the map, if 1 you need to score on the left
        int myTeamId = in.nextInt();

        for (int i = 0; i < 4; i++)
        {
            int tid = (i < 2 ? 0 : 1);
            Wizard w = new Wizard(i, tid, new Greedy());
            s.wiz.put(i, w);
            teams[tid][i - 2 * tid] = w;
        }

        TC = new TeamCoordinator(myTeamId, teams[myTeamId], GOALS[1 - myTeamId]);

        // Tour préparatoire ?

        // tour++;

        // game loop
        while (true)
        {
            read(tour, s);

            sep(s);

            // team coordination
            TC.decide(tour, s);
            // each wizard then
            for (int i = 0; i < 2; i++)
            {
                Wizard w = teams[myTeamId][i];
                Action a = w.decide(s);

                TC.upd(a);

                System.out.println(a + " " + a);
            }

            tour++;
            TC.m++;
        }

    }

    public static void read(int p_t, State s)
    {

        int entities = in.nextInt(); // number of entities still in game

        for (int i = 0; i < entities; i++)
        {
            int entityId = in.nextInt(); // entity identifier
            String entityType = in.next(); // "WIZARD", "OPPONENT_WIZARD" or "SNAFFLE" (or "BLUDGER" after first
                                           // league)
            // int teamid = TC.tid;
            Entity e = null;
            switch (entityType)
            {
                case "OPPONENT_WIZARD":
                    // teamid = 1 - TC.tid;
                case "WIZARD":
                    e = s.wiz.get(entityId);
                    // if (e == null)
                    // {
                    // e = new Wizard(entityId, teamid, new Greedy());
                    // wiz.put(entityId, (Wizard) e);
                    // }
                    break;

                case "BLUDGER":
                    e = s.bludgers.get(entityId);
                    if (e == null)
                    {
                        e = new Bludger(entityId);
                        s.bludgers.put(entityId, (Ball) e);
                    }

                    break;
                case "SNAFFLE":
                    e = s.balls.get(entityId);
                    if (e == null)
                    {
                        e = new Snaffle(entityId);
                        s.balls.put(entityId, (Ball) e);
                    }
                    break;
            }

            int x = in.nextInt(); // position
            int y = in.nextInt(); // position
            int vx = in.nextInt(); // velocity
            int vy = in.nextInt(); // velocity
            int state = in.nextInt(); // 1 if the wizard is holding a Snaffle, 0 otherwise

            e.upd(x, y, vx, vy, state, p_t);

            if (e instanceof Bludger)
                ((Bludger) e).updTarget(s);
        }

        // Old balls
        HashSet<Integer> toR = new HashSet<>();
        for (Ball b : s.balls.values())
            if (b.lastupd < p_t)
            {
                toR.add(b.id);

                // Count score when removing ball
                if (b.p.x < 1000)
                    TC.sc[1]++;
                else
                    TC.sc[0]++;

            }
        s.balls.keySet().removeAll(toR);

    }

    public static void sep(Object s)
    {
        if (DEBUG)
            System.err.println(tour + ": " + s);
    }

    public static class TeamCoordinator
    {
        int tid;
        int m = 0;
        Wizard[] team;

        HashMap<Wizard, Snaffle> job = new HashMap<>();

        int[] sc = new int[2];
        Position goal = null;

        public TeamCoordinator(int p_tid, Wizard[] p_list, Position p_goal)
        {
            this.tid = p_tid;
            this.team = p_list;
            this.goal = p_goal;
        }

        public void upd(Action p_a)
        {
            if (p_a.t.equals(ActionType.CAST))
                this.m -= p_a.s.c;
        }

        public boolean inMyZone(Snaffle p_s)
        {
            return (p_s.p.distanceL2(this.goal) < 2000);
        }

        public void decide(int p_t, State p_s)
        {
            this.job.clear();

            HashSet<Ball> outZone = new HashSet<>();
            for (Ball s : p_s.balls.values())
                if (!this.inMyZone((Snaffle) s))
                    outZone.add(s);

            sep("OutZone : " + outZone);

            if (outZone.size() < 2)
                outZone.addAll(p_s.balls.values());

            double min = Double.POSITIVE_INFINITY;
            if (outZone.size() > 1)
            {
                for (Ball s1 : outZone)
                    for (Ball s2 : outZone)
                        if (!s1.equals(s2))
                        {
                            double d1 = this.team[0].p.distanceL2(s1.p);
                            double d2 = this.team[1].p.distanceL2(s2.p);
                            if (d1 + d2 < min)
                            {
                                min = d1 + d2;
                                this.job.put(this.team[0], (Snaffle) s1);
                                this.job.put(this.team[1], (Snaffle) s2);
                            }
                        }
            }
            else
            {
                Snaffle myBall = (Snaffle) p_s.balls.values().iterator().next();
                this.job.put(this.team[0], myBall);
                this.job.put(this.team[1], myBall);
            }
            // Assign balls to wizards
            // for (Wizard w : this.team)
            // {
            // FactoryDistComparator c = new FactoryDistComparator(w.p);
            // TreeSet<Entity> t = new TreeSet<>(c);
            // t.addAll(balls.values());
            // Snaffle myBall = (Snaffle) t.pollFirst();
            //
            // if (this.job.containsValue(myBall) && t.size() > 0)
            // myBall = (Snaffle) t.pollFirst();
            //
            // this.job.put(w, myBall);
            // }

            sep("Score : " + Arrays.toString(this.sc));
            sep("Magic : " + this.m);
            sep("Current jobs ---> ");
            for (Entry<Wizard, Snaffle> e : this.job.entrySet())
                sep(e);
            sep("<--- ");
        }

        public Entity firstEntityOnTrajectory(State p_s, Entity p_e, Position p_obj)
        {
            Position p_pos = p_e.p;
            RelativeComparator c = new RelativeComparator(p_pos);
            TreeSet<Entity> t = new TreeSet<>(c);

            double d = p_pos.distanceL2(p_obj);
            Vector2D v = p_obj.minus(p_pos);

            Rectangle rect = new Rectangle((int) (p_pos.x - SNA_RAD), (int) p_pos.y, (int) d, 2 * SNA_RAD);

            sep("Pre Rotation (" + v + "): " + rect.getBounds2D());

            AffineTransform a = new AffineTransform();
            a.rotate(v.getTheta(), p_pos.x, p_pos.y);
            Shape s = a.createTransformedShape(rect);

            sep("Post Rotation (" + v.getTheta() + "): " + s.getBounds2D());

            for (Wizard w : p_s.wiz.values())
                // if (s.contains(w.p.x, w.p.y))
                if (!p_e.equals(w) && s.intersects(w.p.x - WIZ_RAD, w.p.y - WIZ_RAD, WIZ_RAD * 2, WIZ_RAD * 2))
                    t.add(w);

            for (Ball b : p_s.balls.values())
                // if (s.contains(b.p.x, b.p.y))
                if (!p_e.equals(b) && s.intersects(b.p.x - SNA_RAD, b.p.y - SNA_RAD, SNA_RAD * 2, SNA_RAD * 2))
                    t.add(b);

            for (Ball b : p_s.bludgers.values())
                // if (s.contains(b.p.x, b.p.y))
                if (!p_e.equals(b) && s.intersects(b.p.x - BLU_RAD, b.p.y - BLU_RAD, BLU_RAD * 2, BLU_RAD * 2))
                    t.add(b);

            if (t.size() > 0)
                return t.first();
            else
                return null;
        }
    }

    public static class Greedy extends IStrategy
    {
        Action lastA = null;

        @Override
        public Action decide(State p_s)
        {
            Wizard partner = p_s.wiz.get(this.w.pid);

            Action a = null;
            switch (this.w.s)
            {
                case CHARGE:

                    Entity ee = TC.firstEntityOnTrajectory(p_s, this.w, partner.p);
                    boolean advOnRoute = (ee instanceof Wizard && ((Wizard) ee).tid != this.w.tid);

                    double dw = TC.goal.distanceL2(this.w.p);
                    double dp = TC.goal.distanceL2(partner.p);
                    double ddw = partner.p.plus(partner.v).distanceL2(TC.goal);

                    if (Math.abs(dw - dp) < 1500 && Math.abs(partner.p.y - this.w.p.y) < 3000 && ddw < dw
                            && !advOnRoute)
                    {
                        double scalar = partner.p.plus(partner.v).minus(this.w.p).getR() / partner.v.getR();

                        Position where = new Position(partner.p.plus(partner.v.scalarMult(scalar)).minus(this.w.v));
                        a = new Action(where, MAXPOW, true);
                    }
                    else
                    {
                        Position g = TC.goal;
                        Vector2D v = new Vector2D(0, this.w.v.y);
                        Position gg = new Position(g.minus(v));
                        a = new Action(gg, MAXPOW, true);
                    }
                    break;

                default:// case LIBRE

                    RelativeComparator c = new RelativeComparator(this.w.p);
                    TreeSet<Entity> t = new TreeSet<>(c);
                    t.addAll(p_s.balls.values());
                    for (Entity e : t)
                    {
                        Snaffle b = (Snaffle) e;
                        // Si ball FLIPENDABLE
                        if (TC.m >= Spell.FLIPENDO.c && !b.inHands(p_s) && this.w.p.distanceL2(b.p) < 3500
                                && (!TC.inMyZone(b) || p_s.balls.size() == 1) && this.goalSightClear(p_s, b))
                        {
                            a = new Action(Spell.FLIPENDO, b.id);
                            break;
                        }

                        // Si ball ACCIABLE antiGoal
                        // TODO
                    }

                    sep("Act:" + this.w + " -> " + a);
                    if (a == null)
                    {
                        Snaffle myBall = TC.job.get(this.w);
                        if (TC.m >= Spell.ACCIO.c && !myBall.inHands(p_s)
                                && myBall.p.distanceL2(TC.goal) > this.w.p.distanceL2(TC.goal)
                                && myBall.p.distanceL2(this.w.p) > 2500)
                            a = new Action(Spell.ACCIO, myBall.id);
                        // else if() // COUPER LE CHEMIN À UN LANCER Adverse.

                        else
                            // Gothere
                            a = new Action(new Position(myBall.p.plus(myBall.v)), MAXTHRUST);
                    }

            }
            // sep("Act:" + this.w + " -> " + a);

            this.lastA = a;
            return a;
        }

        public boolean goalSightClear(State p_s, Snaffle p_sn)
        {
            // La ou la balle va être moins la ou je vais être à peu près (collisions et accélérations exclues)
            Vector2D v = p_sn.p.plus(p_sn.v).minus(this.w.p.plus(this.w.v));

            Position directObj = new Position(p_sn.p.plus(v.scalarMult(10)));
            Position upPole = new Position(TC.goal.x, 2050);
            Position dwPole = new Position(TC.goal.x, 5450);

            Entity e = TC.firstEntityOnTrajectory(p_s, p_sn, directObj);

            boolean directLine = Position.lineIntersect(this.w.p, directObj, upPole, dwPole) != null;
            boolean directClear = e == null;

            sep(this.w + " --> " + p_sn + " ==  goal sight ? " + directLine + "/" + e);

            // Position de rebond sur le TOP WALL si elle existe
            Position firstUpRebound = Position.lineIntersect(this.w.p, directObj, new Position(0, 0),
                    new Position(MAXX, 0));
            // Position de rebond sur le BOTTOM WALL si elle existe
            Position firstDwRebound = Position.lineIntersect(this.w.p, directObj, new Position(0, MAXY),
                    new Position(MAXX, MAXY));

            Position rebound = firstUpRebound != null ? firstUpRebound : firstDwRebound;

            boolean reboundLine = false;
            boolean reboundClear = false;
            // Calcul du rebond
            if (rebound != null)
            {
                Vector2D preRebound = p_sn.p.minus(this.w.p);
                Vector2D postRebound = new Vector2D(preRebound.x * -1, preRebound.y).scalarMult(10);
                Position postReboundObj = new Position(rebound.plus(postRebound));

                reboundLine = Position.lineIntersect(rebound, postReboundObj, upPole, dwPole) != null;
                reboundClear = TC.firstEntityOnTrajectory(p_s, p_sn, directObj) == null;

                sep(this.w + " --> " + p_sn + " ==  goal sight with rebound ? " + firstUpRebound + "/" + firstDwRebound
                        + " || " + reboundLine + "/" + reboundClear);

            }

            return (directLine && directClear) || (reboundLine && reboundClear);
        }

    }

    public enum Spell
    {
        OBLIVIATE(5, 3), PETRIFICUS(10, 1), ACCIO(20, 6), FLIPENDO(20, 3);

        int c; // cost
        int d; // duration

        private Spell(int p_c, int p_d)
        {
            this.c = p_c;
            this.d = p_d;
        }

        public boolean canTarget(Entity p_e)
        {
            switch (this)
            {
                case OBLIVIATE:
                    return p_e instanceof Bludger;
                case PETRIFICUS:
                case FLIPENDO:
                    return (p_e instanceof Wizard ? ((Wizard) p_e).id != TC.tid : true);
                case ACCIO:
                    return p_e instanceof Ball;
            }
            return false;
        }

    }

    public enum ActionType
    {
        MOVE, THROW, CAST;
    }

    public static class Action
    {
        ActionType t;
        Position moveTo;
        int pow;

        Spell s;

        public Action(Position p_p, int p_th)
        {
            this(p_p, p_th, false);
        }

        public Action(Spell p_s, int p_id)
        {
            this.t = ActionType.CAST;
            this.s = p_s;
            this.pow = p_id;
        }

        public Action(Position p_p, int p_thpow, boolean p_throw)
        {
            this.t = ActionType.MOVE;
            if (p_throw)
                this.t = ActionType.THROW;

            this.pow = p_thpow;

            this.moveTo = p_p;
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
            result = prime * result + ((this.moveTo == null) ? 0 : this.moveTo.hashCode());
            result = prime * result + this.pow;
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
            if (this.getClass() != obj.getClass())
                return false;
            Action other = (Action) obj;
            if (this.moveTo == null)
            {
                if (other.moveTo != null)
                    return false;
            }
            else if (!this.moveTo.equals(other.moveTo))
                return false;
            if (this.pow != other.pow)
                return false;
            if (this.t != other.t)
                return false;
            return true;
        }

        @Override
        public String toString()
        {
            switch (this.t)
            {
                case MOVE:
                case THROW:
                    return this.t + " " + this.moveTo + " " + this.pow;
                case CAST:
                    return this.s + " " + this.pow;

            }
            return "";
        }

    }

    public static class State
    {
        HashMap<Integer, Wizard> wiz;
        HashMap<Integer, Ball> balls;
        HashMap<Integer, Ball> bludgers;

        public State()
        {
            this.wiz = new HashMap<>();
            this.balls = new HashMap<>();
            this.bludgers = new HashMap<>();
        }

        @Override
        public State clone()
        {
            State c = new State();

            for (Wizard w : this.wiz.values())
                c.wiz.put(w.id, (Wizard) w.clone());

            for (Ball b : this.balls.values())
                c.balls.put(b.id, (Ball) b.clone());

            for (Ball b : this.bludgers.values())
                c.bludgers.put(b.id, (Bludger) b.clone());

            return c;
        }

        public State nextState(Action[] p_a)
        {
            State n = this.clone();

            // TODO Impossible de penser à tout ...
            //
            // for (Wizard w : n.wiz.values())
            // {
            //
            // }
            //
            // for (Ball b : n.balls.values())
            // {
            //
            // }
            //
            // for (Ball b : n.bludgers.values())
            // {
            //
            // }

            return n;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "wiz=" + this.wiz + "\nballs=" + this.balls + "\nbludgers=" + this.bludgers;
        }
    }

    public static enum WizState
    {
        LIBRE, CHARGE;
    }

    public static class Wizard extends Entity
    {
        final int tid; // team id
        final int pid; // partner id

        final IStrategy st;

        public Wizard(int p_entityId, int p_teamId, IStrategy p_s)
        {
            super(p_entityId, 1, 0.75);
            this.tid = p_teamId;
            this.pid = (1 - p_entityId) + 2 * this.tid;
            this.st = p_s;
            this.st.setWizard(this);
        }

        @Override
        public Entity clone()
        {
            Wizard e = new Wizard(this.id, this.tid, this.st);
            e.p = this.p;
            e.v = this.v;
            e.s = this.s;
            e.lastupd = this.lastupd;
            return e;
        }

        public Action decide(State p_s)
        {
            Action a = this.st.decide(p_s);

            return a;
        }

        @Override
        public boolean equals(Object obj)
        {
            return super.equals(obj);
        }

        @Override
        public int hashCode()
        {
            return super.hashCode();
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return super.toString();// + "[" + "]";
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
                case MANHATTAN:
                    return p_p1.distanceL1(p_p2) <= p_r;
                case CHEBYSHEV:
                    return p_p1.distanceL15(p_p2) <= p_r;
                case SQ_EUCLID:
                    return p_p1.minus(p_p2).getR2() <= p_r * p_r;
                default:
                    return p_p1.distanceL2(p_p2) <= p_r;
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

    public static class Entity
    {
        Position p;
        Vector2D v;
        WizState s;
        final int id;
        int lastupd;

        double mass = 1;
        double frict = 1;

        private Entity(int p_entityId, double p_m, double p_f)
        {
            this.id = p_entityId;
            this.mass = p_m;
            this.frict = p_f;
        }

        @Override
        public Entity clone()
        {
            Entity e = new Entity(this.id, this.mass, this.frict);
            e.p = this.p;
            e.v = this.v;
            e.s = this.s;
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
            return this.getClass().getSimpleName() + "(" + this.id + ")" + " [" + this.p + ",v=" + this.v + ",t="
                    + this.lastupd + "]";
        }

        void upd(int p_x, int p_y, int p_vx, int p_vy, int p_s, int p_t)
        {
            this.p = new Position(p_x, p_y);
            this.v = new Vector2D(p_vx, p_vy);
            this.s = WizState.values()[p_s];
            this.lastupd = p_t;

            // sep("upd : " + this);
        }

    }

    public static class Ball extends Entity
    {

        public Ball(int p_entityId, double p_m, double p_f)
        {
            super(p_entityId, p_m, p_f);
        }

        public boolean inGoal()
        {
            return (this.p.x < 1 || this.p.x > MAXX) && (this.p.y > 1900 || this.p.y < 5600);
        }

    }

    public static class Snaffle extends Ball
    {

        public Snaffle(int p_entityId)
        {
            super(p_entityId, 0.5, 0.75);
        }

        public boolean inHands(State p_s)
        {
            boolean inHand = false;
            for (Wizard w : p_s.wiz.values())
                inHand |= w.p.equals(this.p);

            return inHand;
        }
    }

    public static class Bludger extends Ball
    {

        Wizard target = null;
        Wizard last_target = null;

        public Bludger(int p_entityId)
        {
            super(p_entityId, 8, 0.9);
        }

        public Action getAction(Wizard p_w)
        {
            return new Action(p_w.p, 1000);
        }

        void updTarget(State p_s)
        {
            RelativeComparator c = new RelativeComparator(this.p);
            TreeSet<Entity> t = new TreeSet<>(c);
            t.addAll(p_s.wiz.values());

            if (this.last_target != null)
                t.remove(this.last_target);

            this.target = (Wizard) t.first();

            sep("Bludge ! " + this + " --> " + this.target + " OLD : " + this.last_target);
        }

    }

    public static abstract class IStrategy
    {
        Wizard w;

        abstract Action decide(State p_s);

        public void setWizard(Wizard p_w)
        {
            this.w = p_w;
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
