
import java.nio.channels.InterruptedByTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Shoot enemies before they collect all the incriminating data! The closer you are to an enemy, the more damage you do
 * but don't get too close or you'll get killed.
 **/
class Accountant
{
    final static int MX = 16000;
    final static int MY = 9000;

    final static boolean DEBUG = true;
    final static boolean RELEASE = false;
    // final static boolean RELEASE = true;

    public static final boolean DBG_NEEDED = false;
    public static boolean INTEST = false;

    // static Bot wolff;

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

    static final int[] deg_dist = { 2000, 2021, 2155, 2310, 2492, 2709, 2972, 3298, 3716, 4271, 5048, 6224, 8238,
            12610 };

    public static int degats(double p_distance)
    {
        if (p_distance > 12610)
            return 1;

        int degats = 14;
        for (int i = deg_dist.length - 2; i >= 0; i--)
            if (p_distance >= deg_dist[i] && p_distance < deg_dist[i + 1])
                return degats - i;

        // Wont happen
        if (p_distance < 2000)
            return (int) Math.round(125000 / Math.pow(p_distance, 1.2));
        ass("WTF in Calcul Degats ! : " + p_distance);
        if (p_distance > 18358)
            return (int) Math.round(125000 / Math.pow(p_distance, 1.2));

        return 0;
    }

    public static int distToOneShot(int p_degatsToDo)
    {
        if (p_degatsToDo > 14)
            return 2000;
        if (p_degatsToDo == 1)
            return 18358;
        return deg_dist[deg_dist.length - p_degatsToDo + 1];
    }

    public static void main(String args[])
    {

        Scanner in = new Scanner(System.in);
        // IStrategy strat = new StrHugeSansPlan();
        IStrategy strat = new StrDFS_BB();
        // IStrategy strat = new StrGreedy();

        int turn = 0;
        Bot wolff = new Bot(strat, turn);
        State s = new State(wolff);

        // INTEST = true;
        // TEST
        if (INTEST)
        {
            int oldv = (int) Math.round(125000 / Math.pow(1, 1.2));
            for (int i = 2; i < 2000; i++)
            {
                int v = (int) Math.round(125000 / Math.pow(i, 1.2));
                if (v != oldv)
                    System.out.println(i + " : " + v);
                oldv = v;
            }

            int t = 0;
            // s.updB(t, 8000, 4000);
            // s.updD(t, 0, 0, 4000);
            // s.updD(t, 1, 14400, 3800);
            // s.updD(t, 2, 8000, 6000);
            // s.updE(t, 0, 4500, 3600, 10);
            // s.updE(t, 1, 4333, 5400, 10);
            // s.updE(t, 2, 4500, 7200, 10);
            // s.updE(t, 3, 10666, 3600, 10);
            // s.updE(t, 4, 10666, 5400, 10);
            // s.updE(t, 5, 10666, 7200, 10);
            // s.updE(t, 6, 14000, 8000, 10);

            // s.updB(0, 1100, 1200);
            // s.updD(0, 0, 8250, 4500);
            // s.updE(0, 0, 8250, 8999, 10);

            s.updB(0, 0, 1200);
            for (int i = 0; i < 70; i++)
                s.updD(0, i, 8109 + i, 4500);

            s.updE(0, 0, 11200, 1800, 6);
            s.updE(0, 1, 12800, 1800, 6);
            s.updE(0, 2, 14400, 1800, 6);
            s.updE(0, 3, 3200, 2700, 6);
            s.updE(0, 4, 4800, 2700, 6);
            s.updE(0, 5, 6400, 2700, 6);
            s.updE(0, 6, 8000, 2700, 6);
            s.updE(0, 7, 9600, 2700, 6);
            s.updE(0, 8, 11200, 2700, 6);
            s.updE(0, 9, 12800, 2700, 6);
            s.updE(0, 10, 14400, 2700, 6);
            s.updE(0, 11, 1600, 3600, 6);
            s.updE(0, 12, 3200, 3600, 6);
            s.updE(0, 13, 4800, 3600, 6);
            s.updE(0, 14, 6400, 3600, 6);
            s.updE(0, 15, 8000, 3600, 6);
            s.updE(0, 16, 9600, 3600, 6);
            s.updE(0, 17, 11200, 3600, 6);
            s.updE(0, 18, 12800, 3600, 6);
            s.updE(0, 19, 14400, 3600, 6);
            s.updE(0, 20, 0, 4500, 6);
            s.updE(0, 21, 1600, 4500, 6);
            s.updE(0, 22, 3200, 4500, 6);
            s.updE(0, 23, 4800, 4500, 6);
            s.updE(0, 24, 6400, 4500, 6);
            s.updE(0, 25, 8000, 4500, 6);
            s.updE(0, 26, 9600, 4500, 6);
            s.updE(0, 27, 11200, 4500, 6);
            s.updE(0, 28, 12800, 4500, 6);
            s.updE(0, 29, 14400, 4500, 6);
            s.updE(0, 30, 0, 5400, 6);
            s.updE(0, 31, 1600, 5400, 6);
            s.updE(0, 32, 3200, 5400, 6);
            s.updE(0, 33, 4800, 5400, 6);
            s.updE(0, 34, 6400, 5400, 6);
            s.updE(0, 35, 8000, 5400, 6);
            s.updE(0, 36, 9600, 5400, 6);
            s.updE(0, 37, 11200, 5400, 6);
            s.updE(0, 38, 12800, 5400, 6);
            s.updE(0, 39, 14400, 5400, 6);
            s.updE(0, 40, 0, 6300, 6);
            s.updE(0, 41, 1600, 6300, 6);
            s.updE(0, 42, 3200, 6300, 6);
            s.updE(0, 43, 4800, 6300, 6);
            s.updE(0, 44, 6400, 6300, 6);
            s.updE(0, 45, 8000, 6300, 6);
            s.updE(0, 46, 9600, 6300, 6);
            s.updE(0, 47, 11200, 6300, 6);
            s.updE(0, 48, 12800, 6300, 6);
            s.updE(0, 49, 14400, 6300, 6);
            s.updE(0, 50, 0, 7200, 6);
            s.updE(0, 51, 1600, 7200, 6);
            s.updE(0, 52, 3200, 7200, 6);
            s.updE(0, 53, 4800, 7200, 6);
            s.updE(0, 54, 6400, 7200, 6);
            s.updE(0, 55, 8000, 7200, 6);
            s.updE(0, 56, 9600, 7200, 6);
            s.updE(0, 57, 11200, 7200, 6);
            s.updE(0, 58, 12800, 7200, 6);
            s.updE(0, 59, 14400, 7200, 6);
            s.updE(0, 60, 0, 8100, 6);
            s.updE(0, 61, 1600, 8100, 6);
            s.updE(0, 62, 3200, 8100, 6);
            s.updE(0, 63, 4800, 8100, 6);
            s.updE(0, 64, 6400, 8100, 6);
            s.updE(0, 65, 8000, 8100, 6);
            s.updE(0, 66, 9600, 8100, 6);
            s.updE(0, 67, 11200, 8100, 6);
            s.updE(0, 68, 12800, 8100, 6);
            s.updE(0, 69, 14400, 8100, 6);

            while (true)
            {
                long st = System.currentTimeMillis();

                s.moveToData = State.clustering(s.d, State.CLUSTER_DIST);
                // s.moveToEnnemies = WizState.clustering(s.e, WizState.CLUSTER_DIST);

                long en = System.currentTimeMillis() - st;
                System.err.println("Time prep : " + en + " ms for D:" + s.d.size() + "/E:" + s.e.size());

                st = System.currentTimeMillis();
                Action a = wolff.decide(turn, s);
                en = System.currentTimeMillis() - st;
                System.err.println("Time : " + en + " ms ");
                System.out.println(a);
                s = s.next(++t, a);
            }

        }
        else
            // game loop
            while (true)
            {
            // bot
            // int x = in.nextInt();
            // int y = in.nextInt();
            s.updB(turn, in.nextInt(), in.nextInt());

            // data
            int dataCount = in.nextInt();
            for (int i = 0; i < dataCount; i++)
            // int dataId = in.nextInt();
            // int dataX = in.nextInt();
            // int dataY = in.nextInt();
            s.updD(turn, in.nextInt(), in.nextInt(), in.nextInt());

            int enemyCount = in.nextInt();
            for (int i = 0; i < enemyCount; i++)
            // int enemyId = in.nextInt();
            // int enemyX = in.nextInt();
            // int enemyY = in.nextInt();
            // int enemyLife = in.nextInt();
            s.updE(turn, in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());

            long st = System.currentTimeMillis();

            s.clearOld(turn);
            // db("WizState postUpd : " + s);

            // if (turn == 0)
            // s.clustering();

            long en = System.currentTimeMillis() - st;
            System.err.println("Time prep : " + en + " ms for D:" + dataCount + "/E:" + enemyCount);

            st = System.currentTimeMillis();
            // Action a = wolff.decide(turn, s);
            // int lowerBound = StrDFS_BB.lowerBound(turn, s);
            Action a = wolff.decide(turn, s);
            en = System.currentTimeMillis() - st;

            System.err.println("Time : " + en + " ms ");

            System.out.println(a + wolff.getMsg()); // MOVE x y or SHOOT id
            turn++;
            }
    }

    public static class StrGreedy implements IStrategy
    {
        @Override
        public Action decide(int p_turn, State p_s)
        {
            SortedSet<Ennemy> m = p_s.nearestEnnemies(p_s.b.p);

            Ennemy nearest = m.first();
            Entity nearestObj = p_s.getObj(nearest);
            Vector2D ennemyObj = nearestObj.p;
            Vector2D ennemyDirection = ennemyObj.minus(nearest.p).unitVector();
            Vector2D ennemyMove = ennemyDirection.scalarMult(Ennemy.MAX_MOVE_ENNEMY);

            Vector2D newEnnemyPos = nearest.p.plus(ennemyMove);
            double distToNewEnnemyPos = p_s.b.p.minus(newEnnemyPos).getR();

            // db("ennemyObj : " + ennemyObj + " --> Ennemy move : " + ennemyMove);
            // db("Safe ? " + distToNewEnnemyPos + " < " + Ennemy.MIN_SHOOT);

            // If not safe, move away to a data in the opposite direction
            if (distToNewEnnemyPos <= 1.2 * Ennemy.MIN_SHOOT)
            {
                Vector2D sumOfE = new Vector2D();
                for (Ennemy ee : p_s.e.values())
                {
                    Vector2D EtoME = p_s.b.p.minus(ee.p);
                    double d = EtoME.getR();
                    if (d < 2 * Ennemy.MIN_SHOOT)
                        sumOfE = sumOfE.plus(EtoME.scalarMult(degats(d)));
                }

                // Vector2D toE = newEnnemyPos.minus(p_s.b.p);
                // Vector2D flee = toE.scalarMult(-1);

                // for (Entity d : p_s.d.values())
                // if (d.p.minus(p_s.b.p).dotProduct(toE) < 0)
                // flee = d.p.minus(p_s.b.p);

                Vector2D my = new Vector2D(0, MY);
                Vector2D mx = new Vector2D(MX, 0);

                Vector2D res = p_s.b.p.plus(sumOfE.unitVector().scalarMult(Bot.MAX_MOVE));
                if (res.x < 0 || res.x >= MX)
                    sumOfE = sumOfE.plus(my.scalarMult(my.unitVector().dotProduct(sumOfE.unitVector())));
                if (res.y < 0 || res.y >= MY)
                    sumOfE = sumOfE.plus(mx.scalarMult(mx.unitVector().dotProduct(sumOfE.unitVector())));
                res = p_s.b.p.plus(sumOfE.unitVector().scalarMult(Bot.MAX_MOVE));
                return new Action(res);
            }

            // db("One Shoot : " + distToNewEnnemyPos + " <= " + distToOneShot(nearest.life));
            // If one shoot nearest;
            if (p_s.b.p.minus(newEnnemyPos.plus(ennemyMove)).getR() <= distToOneShot(nearest.life))
                return new Action(nearest.id);

            // If shoot last minute
            if (p_s.startToKillBe4Data(nearest, nearestObj))
                return new Action(nearest.id);

            // else move to one shoot ennemy and still stay safe
            // Vector2D myObj = ennemyObj.minus(newEnnemyPos).scalarMult(0.5).plus(newEnnemyPos);
            // Vector2D myMove = myObj.minus(p_s.b.p).unitVector().scalarMult(Bot.MAX_MOVE);

            // db("My next pos : " + wolff.p.plus(myMove) + "(" + myObj + "+" + myMove + ")");

            // return new Action(p_s.b.p.plus(myMove));

            // ELSE : SHOOT THE BASTARD !
            return new Action(nearest.id);
        }
    }

    public static class StrHugeSansPlan implements IStrategy
    {
        final double SAFERANGE2 = 1.6 * Ennemy.MIN_SHOOT * Ennemy.MIN_SHOOT;

        @Override
        public Action decide(int p_turn, State p_s)
        {

            HashMap<Entity, Ennemy> objs = new HashMap<>();

            NearestComparator c = new NearestComparator(p_s.b.p, Distance.SQ_EUCLID);
            SortedSet<Ennemy> oneShoot = new TreeSet<>(c);

            double minDist = Double.POSITIVE_INFINITY;
            double newMinDist = minDist;
            Ennemy nearest = null;

            Vector2D sumOfE = new Vector2D();
            int numberOfKillers = 0;

            double distToNewEnnemyPosAfterMove = Double.POSITIVE_INFINITY;
            double distToOneShootEnnemy = Double.POSITIVE_INFINITY;
            HashMap<Ennemy, Vector2D> posToOneShoot = new HashMap<>();

            for (Ennemy ee : p_s.e.values())
            {
                Entity obj = p_s.getObj(ee);
                objs.put(obj, ee);

                Vector2D ennemyObj = obj.p;
                Vector2D ennemyDirection = ennemyObj.minus(ee.p);

                Vector2D ennemyMove = ennemyDirection;
                if (ennemyDirection.getR2() >= Ennemy.MAX_MOVE_ENNEMY * Ennemy.MAX_MOVE_ENNEMY)
                    ennemyMove = ennemyDirection.unitVector().scalarMult(Ennemy.MAX_MOVE_ENNEMY);

                Vector2D newEnnemyPos = ee.p.plus(ennemyMove);

                Vector2D EtoME = p_s.b.p.minus(ee.p);

                double distToEnnemyPos = EtoME.getR2();
                double distToNewEnnemyPos = p_s.b.p.minus(newEnnemyPos).getR2();

                // Sum of near ennemies vector
                if (distToNewEnnemyPos < this.SAFERANGE2)
                {
                    sumOfE = sumOfE.plus(EtoME.scalarMult(degats(distToEnnemyPos)));
                    numberOfKillers++;
                }

                // One shoot computation
                int dShot = distToOneShot(ee.life);
                if (distToNewEnnemyPos <= dShot * dShot)
                    oneShoot.add(ee);

                // Pos to kill in one shot
                posToOneShoot.put(ee, EtoME.unitVector().scalarMult(dShot));

                // Nearest computation
                if (distToNewEnnemyPos < newMinDist)
                {
                    minDist = distToEnnemyPos;
                    newMinDist = distToNewEnnemyPos;
                    nearest = ee;

                    Vector2D myMove = obj.p.minus(p_s.b.p).unitVector().scalarMult(Bot.MAX_MOVE);
                    Vector2D myNewPos = p_s.b.p.plus(myMove);
                    distToNewEnnemyPosAfterMove = myNewPos.minus(newEnnemyPos).getR2();
                    distToOneShootEnnemy = dShot;
                }

            }

            // db("Safe ? " + numberOfKillers + " : " + minDist + "<" + newMinDist + " < "
            // + (1.21 * Ennemy.MIN_SHOOT * Ennemy.MIN_SHOOT));
            // If not safe, move away to a data in the opposite direction
            if (numberOfKillers > 1 && minDist > newMinDist && newMinDist <= this.SAFERANGE2)
            {

                // Check if outside the limits before
                Vector2D my = new Vector2D(0, MY);
                Vector2D mx = new Vector2D(MX, 0);

                Vector2D res = p_s.b.p.plus(sumOfE.unitVector().scalarMult(Bot.MAX_MOVE));
                if (res.x < 0 || res.x >= MX)
                    sumOfE = sumOfE.plus(my.scalarMult(my.unitVector().dotProduct(sumOfE.unitVector())));
                if (res.y < 0 || res.y >= MY)
                    sumOfE = sumOfE.plus(mx.scalarMult(mx.unitVector().dotProduct(sumOfE.unitVector())));
                res = p_s.b.p.plus(sumOfE.unitVector().scalarMult(Bot.MAX_MOVE));
                return new Action(res);
            }

            // db("One Shoot : " + newMinDist + " <= " + oneShoot);
            // If one shoot some one;
            if (!oneShoot.isEmpty())
                return new Action(oneShoot.first().id);

            // if safe to move and nobody to one shoot ... move towards nearest ennemy obj
            // db("Move ? " + distToNewEnnemyPosAfterMove + " > " + this.SAFERANGE2);
            if (distToNewEnnemyPosAfterMove > this.SAFERANGE2)
                return new Action(p_s.getObj(nearest).p);

            // ELSE : SHOOT THE NEAREST BASTARD !
            return new Action(nearest.id);
        }
    }

    public static class StrDFS_BB extends StrMonteCarlo
    {
        final double SAFERANGE2 = 1.6 * Ennemy.MIN_SHOOT * Ennemy.MIN_SHOOT;
        final static long TIME = 95;

        private static final int MAX_ENTITY = 20;

        Action lastChanceAction = null;
        long start;

        @Override
        public Action decide(int p_turn, State p_s)
        {
            this.start = System.currentTimeMillis();
            this.lastChanceAction = null;

            // IStrategy lowerStrat = new StrGreedy();
            // IStrategy lowerStrat = new StrHugeSansPlan();

            TreeMap<SearchNode, Integer> open = new TreeMap<>();
            try
            {
                int lowerBound = Integer.MIN_VALUE;
                if (p_s.e.size() + p_s.d.size() > MAX_ENTITY)
                {
                    lowerBound = p_s.score();
                    return new StrHugeSansPlan().decide(p_turn, p_s);
                }
                else
                {
                    lowerBound = this.lowerBound(p_turn, p_s, new StrHugeSansPlan());
                    db("BFS0 : " + this.lastChanceAction + " : " + lowerBound);
                }

                SearchNode st = new SearchNode(null, p_s, p_s.b.shots, p_s.e.size());

                open.put(st, lowerBound);

                this.checkTime();

                // db("BFS1 : " + open);
                while (!open.isEmpty() && !this.isEndState(open.firstKey().s))
                {
                    SearchNode current = open.pollFirstEntry().getKey();
                    // db("BFS2 : " + Arrays.toString(this.generateActions(current.s)));
                    for (Action a : this.generateActions(current.s))
                    {
                        State next = current.s.next(p_turn, a);
                        int c_lower = this.lowerBound(p_turn, next, new StrGreedy());

                        if (c_lower >= lowerBound)
                        {
                            SearchNode b = new SearchNode(a, next, next.b.shots, next.e.size());
                            b.prev = current;

                            open.put(b, c_lower);
                            // lowerBound = c_lower;
                            // db("BFS3 : " + a + "-->" + b + " -- " + lowerBound + "||" + open.firstEntry());
                        }
                        this.checkTime();

                    }

                    this.checkTime();

                }

            }
            catch (InterruptedByTimeoutException e)
            {
                // do nothing
                db("Time Interrupt ! ");
            }
            finally
            {
                // return this.lastChanceAction;
                // }
                if (!open.isEmpty())// && this.isEndState(open.firstKey().s))
                {
                    SearchNode b = open.firstKey();
                    if (b.prev == null)
                        db("--- > " + b);

                    else
                        db(b + "---+>" + b.prev);

                    while (b.prev.prev != null)
                        b = b.prev;

                    return b.a;
                }
                else
                    ass("I'm probably SCREWED ... ");
            }
            return this.lastChanceAction;
        }

        @Override
        public Action[] generateActions(State p_s)
        {
            HashSet<Action> as = new HashSet<>(Arrays.asList(super.generateActions(p_s)));

            boolean nearDeath = false;
            Vector2D sumOfE = new Vector2D();

            for (Ennemy ee : p_s.e.values())
            {
                Entity obj = p_s.getObj(ee);

                Vector2D ennemyObj = obj.p;
                Vector2D ennemyDirection = ennemyObj.minus(ee.p);

                Vector2D ennemyMove = ennemyDirection;
                if (ennemyDirection.getR2() >= Ennemy.MAX_MOVE_ENNEMY * Ennemy.MAX_MOVE_ENNEMY)
                    ennemyMove = ennemyDirection.unitVector().scalarMult(Ennemy.MAX_MOVE_ENNEMY);

                Vector2D newEnnemyPos = ee.p.plus(ennemyMove);
                Vector2D EtoME = p_s.b.p.minus(ee.p);
                double distToNewEnnemyPos = p_s.b.p.minus(newEnnemyPos).getR2();

                // Sum of near ennemies vector
                if (distToNewEnnemyPos < this.SAFERANGE2)
                    sumOfE = sumOfE.plus(EtoME.scalarMult(degats(distToNewEnnemyPos)));

                // One shoot computation
                int dShot = distToOneShot(ee.life);
                if (distToNewEnnemyPos <= dShot * dShot)
                    as.add(new Action(ee.id));

                // Pos to kill in one shot
                // as.add(new Action(newEnnemyPos.plus(EtoME.unitVector().scalarMult(dShot))));
            }

            // Check if outside the limits before
            Vector2D my = new Vector2D(0, MY);
            Vector2D mx = new Vector2D(MX, 0);

            Vector2D res = p_s.b.p.plus(sumOfE.unitVector().scalarMult(Bot.MAX_MOVE));
            if (res.x < 0 || res.x >= MX)
                sumOfE = sumOfE.plus(my.scalarMult(my.unitVector().dotProduct(sumOfE.unitVector())));
            if (res.y < 0 || res.y >= MY)
                sumOfE = sumOfE.plus(mx.scalarMult(mx.unitVector().dotProduct(sumOfE.unitVector())));
            res = p_s.b.p.plus(sumOfE.unitVector().scalarMult(Bot.MAX_MOVE));

            as.add(new Action(res));

            if (!nearDeath)
                as.addAll(p_s.moveToData);

            return as.toArray(new Action[] {});
        }

        public boolean isEndState(State p_s)
        {
            return p_s.e.isEmpty() || p_s.d.isEmpty();
        }

        public void checkTime() throws InterruptedByTimeoutException
        {
            // db("Time : " + (System.currentTimeMillis() - this.start));
            if (System.currentTimeMillis() - this.start > TIME)
                throw new InterruptedByTimeoutException();
        }

        public int lowerBound(int p_turn, State p_s, IStrategy lowerStrat) throws InterruptedByTimeoutException
        {
            if (p_s.isEndState())
                return p_s.score;

            this.checkTime();
            // db("LBin");

            int t = p_turn;
            Action a = lowerStrat.decide(t, p_s);

            if (this.lastChanceAction == null)
                this.lastChanceAction = a;

            this.checkTime();
            // db("LBpreN");

            State n = p_s.next(t, a);

            while (!n.isEndState())
            {

                t++;
                a = lowerStrat.decide(t, n);

                this.checkTime();
                n = n.next(t, a);
                this.checkTime();
                // db("LBnext " + t);
                // db("LB : " + a + "\n" + n);
            }
            return n.score;
        }

        public static class SearchNode implements Comparable<SearchNode>
        {
            Action a;
            State s;
            int g;
            int h;
            SearchNode prev = null;

            public SearchNode(Action p_a, State p_s, int p_g, int p_h)
            {
                this.a = p_a;
                this.s = p_s;
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
                return "[s=" + (this.prev != null ? this.prev.s.b : "*") + ", a=" + this.a + ", s'=" + this.s.b + ", V="
                        + this.g + "+" + this.h + "]";
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
                SearchNode other = (SearchNode) obj;
                if (this.s == null)
                {
                    if (other.s != null)
                        return false;
                }
                else if (!this.s.equals(other.s))
                    return false;
                return true;
            }

            @Override
            public int compareTo(SearchNode p_o)
            {
                if (((this.g + this.h) - (p_o.g + p_o.h)) == 0)
                    return 1;
                else
                    return ((this.g + this.h) - (p_o.g + p_o.h));

            }
        }

    }

    public static class StrMonteCarlo implements IStrategy
    {
        final static Random RAND = new Random();
        final static long TIME = 95;

        @Override
        public Action decide(int p_turn, State p_s)
        {
            long start = System.currentTimeMillis();

            HashMap<Action, Integer> values = new HashMap<>();
            HashMap<Action, Integer> count = new HashMap<>();

            Action[] toDo = this.generateActions(p_s);

            for (Action a : toDo)
            {
                values.put(a, 0);
                count.put(a, 0);
            }

            int maxDepth = 0;

            // db("MC start: " + p_s);
            db("MC acts : " + Arrays.toString(toDo));
            while (System.currentTimeMillis() - start < TIME || (INTEST && maxDepth < 20))
                for (Action a : toDo)
                {
                    count.put(a, count.get(a) + 1);

                    int t = p_turn + 1;
                    State nextA = p_s.next(t, a);

                    int sc = nextA.score();
                    int depth = 0;
                    while (sc > 0 && !nextA.isEndState())
                    {
                        Action[] na = this.generateActions(nextA);
                        Action ar = na[RAND.nextInt(na.length)];

                        if (ar.t == ActionType.SHOOT)
                            nextA.b.shots++;

                        nextA = nextA.next(++t, ar);

                        sc = nextA.score();
                        depth++;
                        if (!INTEST && System.currentTimeMillis() - start > TIME)
                            break;
                    }

                    if (maxDepth < depth)
                        maxDepth = depth;

                    values.put(a, values.get(a) + sc);
                    // db("MC : " + a + " += " + sc);
                    if (!INTEST && System.currentTimeMillis() - start > TIME)
                        break;
                }
            SortedMap<Double, Action> res = new TreeMap<>();
            for (Action a : toDo)
                res.put((values.get(a) / (double) count.get(a)), a);

            db("MC res : " + res + " | " + maxDepth);

            return res.get(res.lastKey());
        }

        public Action[] generateActions(State p_s)
        {
            HashSet<Action> as = new HashSet<>();

            boolean nearDeath = false;
            Vector2D sumOfE = new Vector2D();

            // Shoot actions :
            for (Ennemy ee : p_s.e.values())
            {
                Vector2D EtoME = p_s.b.p.minus(ee.p);
                double d = EtoME.getR();
                nearDeath |= (d < 2600);

                if (p_s.startToKillBe4Data(ee, p_s.getObj(ee)) || d < 3 * Ennemy.MIN_SHOOT)
                    as.add(new Action(ee.id));

                if (d < 2 * Ennemy.MIN_SHOOT)
                    sumOfE = sumOfE.plus(EtoME.scalarMult(degats(d)));
            }
            // Move actions :
            // - Move towards ennemy NOT.
            // - flee ennemy IF NEAR ENOUGH
            // - move towards data CLUSTERED
            // - others ?
            // Vector2D opp = p_s.b.p.minus(ee.p).plus(p_s.b.p);
            // as.add(new Action(ee.p));

            // for (Ennemy ee : p_s.e.values())
            // {
            // Vector2D EtoME = p_s.b.p.minus(ee.p);
            // Vector2D pp = EtoME.plus(p_s.b.p);
            // // if (EtoME.getR() < 2 * Ennemy.MIN_SHOOT && p_s.isInside(pp.scalarMult(Bot.MAX_MOVE)))
            // as.add(new Action(pp));
            // }

            // for (Entity dd : p_s.d.values())
            // as.add(new Action(dd.p));
            if (!nearDeath)
                as.addAll(p_s.moveToData);

            // META-FLEE Action !
            as.add(new Action(sumOfE));

            return as.toArray(new Action[] {});
        }

    }

    public static class Entity
    {
        int id;
        Vector2D p;
        int turn;

        public Entity(int p_id, int p_turn)
        {
            this.id = p_id;
            this.turn = p_turn;
        }

        @Override
        public Entity clone()
        {
            Entity c = new Entity(this.id, this.turn);
            c.p = this.p;
            return c;
        }

        public void upd(int p_turn, int p_id, int p_x, int p_y)
        {
            this.turn = p_turn;
            this.p = new Vector2D(p_x, p_y);

            // Assert ownership.
            if (p_id != this.id)
                ass("Bad Ownership in Entity Update! " + p_id + "/" + this.id + ",(" + p_x + "," + p_y + ")");
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.id;
            result = prime * result + ((this.p == null) ? 0 : this.p.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (this.getClass() != obj.getClass())
                return false;
            Entity other = (Entity) obj;
            if (this.id != other.id)
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

        @Override
        public String toString()
        {
            return "[" + this.id + ":" + this.p + "]";
        }

    }

    public static interface IStrategy
    {
        public Action decide(int p_turn, State p_s);
    }

    public static class Bot extends Entity
    {
        final static int MAX_MOVE = 1000;

        int shots = 0;

        IStrategy s;
        String msg = "";

        public Bot(IStrategy p_s, int p_turn)
        {
            super(-11, p_turn);
            this.s = p_s;
        }

        @Override
        public Bot clone()
        {
            Bot m = new Bot(this.s, this.turn);
            m.p = this.p;
            m.shots = this.shots;
            return m;
        }

        public Action decide(int p_turn, State p_s)
        {
            Action a = this.s.decide(p_turn, p_s);

            if (a.t == ActionType.SHOOT)
                this.shots++;

            return a;
        }

        public String getMsg()
        {
            if (this.msg.isEmpty())
                return "";
            else
                return " " + this.msg;
        }

        @Override
        public void upd(int p_turn, int p_id, int p_x, int p_y)
        {
            super.upd(p_turn, -11, p_x, p_y);

            this.msg = "";
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "[" + this.p + " : " + this.shots + "]";
        }

    }

    public static class Ennemy extends Entity
    {

        final static int MAX_MOVE_ENNEMY = 500;
        final static int MIN_SHOOT = 2000;
        final static int MIN_DATA = 500;
        final static int MAX_LIFE = 125000;

        int life;

        public Ennemy(int p_id, int p_turn)
        {
            super(p_id, p_turn);
            this.life = MAX_LIFE;
        }

        @Override
        public Ennemy clone()
        {
            Ennemy c = new Ennemy(this.id, this.turn);
            c.p = this.p;
            c.life = this.life;
            return c;
        }

        public void upd(int p_turn, int p_id, int p_x, int p_y, int p_life)
        {
            super.upd(p_turn, p_id, p_x, p_y);

            this.life = p_life;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return super.toString() + "[l=" + this.life + ",lt=" + this.turn + "]";
        }

    }

    public static enum ActionType
    {
        MOVE, SHOOT;
    }

    public static class Action
    {
        ActionType t;
        Vector2D p;
        int id;

        public Action(int p_id)
        {
            this.t = ActionType.SHOOT;
            this.id = p_id;
        }

        public Action(Vector2D p_pb)
        {
            this.t = ActionType.MOVE;
            this.p = p_pb;
        }

        @Override
        public String toString()
        {
            if (this.t == ActionType.SHOOT)
                return this.t + " " + this.id;

            return this.t + " " + (int) Math.round(this.p.x) + " " + (int) Math.round(this.p.y);
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
            result = prime * result + ((this.p == null) ? 0 : this.p.hashCode());
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
            if (this.id != other.id)
                return false;
            if (this.p == null)
            {
                if (other.p != null)
                    return false;
            }
            else if (!this.p.equals(other.p))
                return false;
            if (this.t != other.t)
                return false;
            return true;
        }

    }

    public static class State
    {

        // Total life at start.
        int L = 0;
        // Number of Ennemies at start
        int NE = 0;

        // Clusters
        final static int CLUSTER_DIST = 3000;
        HashSet<Action> moveToData;
        // HashSet<Action> moveToEnnemies;
        static HashMap<Ennemy, Entity> obj = new HashMap<>();

        Bot b;
        HashMap<Integer, Entity> d = new HashMap<>();
        HashMap<Integer, Ennemy> e = new HashMap<>();

        int score = Integer.MIN_VALUE;
        int heuristic = Integer.MIN_VALUE;
        boolean safe = true;

        public State(Bot p_bot)
        {
            this.b = p_bot;
        }

        @Override
        public State clone()
        {
            State c = new State(this.b.clone());
            c.L = this.L;
            c.NE = this.NE;
            for (Entity dd : this.d.values())
                c.d.put(dd.id, dd.clone());
            for (Ennemy ee : this.e.values())
                c.e.put(ee.id, ee.clone());

            return c;
        }

        public static <T extends Entity> ArrayList<HashSet<Vector2D>> cluster(HashMap<Integer, T> p_d, int p_dist_min)
        {
            ArrayList<HashSet<Vector2D>> pos = new ArrayList<>();
            pos.add(new HashSet<>());

            int first = p_d.keySet().iterator().next();

            pos.get(0).add(p_d.get(first).p);

            int n = p_d.size();
            int[][] dist = new int[n][n];

            // StringBuilder sb = new StringBuilder();
            for (int i = 1; i < dist.length; i++)
            {
                Entity e = p_d.get(i);
                if (e == null)
                    continue;

                boolean addedI = false;

                for (int j = 0; j < i && !addedI; j++)
                {
                    Entity ee = p_d.get(j);
                    if (ee == null)
                        continue;

                    if (!e.equals(ee))
                        dist[i][j] = (int) e.p.distanceL2(ee.p);

                    if (dist[i][j] < p_dist_min)
                    {
                        pos.get(dist[j][j]).add(e.p);
                        dist[i][i] = dist[j][j];
                        addedI = true;
                    }
                }
                if (!addedI)
                {
                    pos.add(new HashSet<>());
                    pos.get(pos.size() - 1).add(e.p);
                    dist[i][i] = pos.size() - 1;
                }

                // sb.append(Arrays.toString(dist[i]) + "\n");
            }

            return pos;
        }

        public static <T extends Entity> HashSet<Action> clustering(HashMap<Integer, T> p_d, int p_dist_min)
        {
            HashSet<Action> moveTo = new HashSet<>();

            if (p_d.size() <= 3)
            {
                for (int i = 0; i < p_d.size(); i++)
                    if (p_d.containsKey(i))
                        moveTo.add(new Action(p_d.get(i).p));
                // db("Data Clusters : " + this.moveToData);
                return moveTo;
            }

            ArrayList<HashSet<Vector2D>> pos = cluster(p_d, p_dist_min);

            for (HashSet<Vector2D> c : pos)
            {
                Vector2D s = new Vector2D();
                for (Vector2D v : c)
                    s = s.plus(v);
                double m = c.size();
                moveTo.add(new Action(s.scalarMult(1.0 / m)));
            }

            // db("\n" + sb.toString());
            // db("" + pos);
            // db("Data Clusters : " + this.moveToData);
            return moveTo;
        }

        public boolean startToKillBe4Data(Ennemy p_nearest, Entity p_nearestObj)
        {
            Vector2D ennemyObj = p_nearestObj.p;
            Vector2D ennemyDirection = ennemyObj.minus(p_nearest.p).unitVector();
            Vector2D ennemyMove = ennemyDirection.scalarMult(Ennemy.MAX_MOVE_ENNEMY);
            Vector2D newEnnemyPos = p_nearest.p.plus(ennemyMove);

            // ArrayList<Vector2D> nextEnnemyPos = new ArrayList<>();
            // nextEnnemyPos.add(newEnnemyPos);
            // while (newEnnemyPos.distanceL2(ennemyObj) > Ennemy.MIN_DATA)
            // {
            // newEnnemyPos = newEnnemyPos.plus(ennemyMove);
            // nextEnnemyPos.add(newEnnemyPos);
            // }
            int numberOfMoves = (int) (newEnnemyPos.distanceL2(ennemyObj) / ennemyMove.getR()) + 1;

            Vector2D toEnnemy = p_nearest.p.minus(this.b.p);
            int timeToCurrentlyKill = (int) Math.floor(p_nearest.life / (double) degats(toEnnemy.getR()));

            // db("start to kill now ? " + nextEnnemyPos + " (" + nextEnnemyPos.size() + ") - " + timeToCurrentlyKill +
            // "("
            // + p_nearest.life + "/" + degats(toEnnemy.getR()) + ")");

            return timeToCurrentlyKill >= numberOfMoves;// nextEnnemyPos.size();
        }

        public boolean isInside(Vector2D p_p)
        {
            return p_p.x >= 0 && p_p.x < MX && p_p.y >= 0 && p_p.y < MY;
        }

        public boolean isEndState()
        {
            // return !this.isCompletelySafe(this.b.p) || (this.e.size() == 0) || (this.d.size() == 0);
            return !this.safe || (this.e.size() == 0) || (this.d.size() == 0);
        }

        // public boolean isCompletelySafe(Vector2D p_p)
        // {
        // boolean safe = true;
        // for (Ennemy ee : this.e.values())
        // safe &= ee.p.distanceL2(p_p) > Ennemy.MIN_SHOOT;
        //
        // // db("Safe : " + p_p + " is " + safe);
        // return safe;
        // }

        public SortedSet<Ennemy> nearestEnnemies(Vector2D p_p)
        {
            NearestComparator c = new NearestComparator(p_p, Distance.EUCLIDEAN);
            SortedSet<Ennemy> s = new TreeSet<>(c);

            s.addAll(this.e.values());

            return s;
        }

        public Entity getObj(Ennemy p_e)
        {
            Entity dd = obj.get(p_e);
            if (dd == null || this.d.get(dd.id) == null)
            {
                NearestComparator c = new NearestComparator(p_e.p, Distance.EUCLIDEAN);
                SortedSet<Entity> s = new TreeSet<>(c);

                s.addAll(this.d.values());

                obj.put(p_e, dd);
                return s.first();
            }
            return dd;
        }

        public State next(int p_t, Action p_act)
        {

            State n = this.clone();
            // Move action for each ennemy
            for (Ennemy ee : n.e.values())
            {
                Entity d = n.getObj(ee);
                Vector2D ennemyObj = d.p;
                Vector2D ennemyMove = ennemyObj.minus(ee.p);
                if (ennemyMove.getR() >= Ennemy.MAX_MOVE_ENNEMY)
                    ennemyMove = ennemyMove.unitVector().scalarMult(Ennemy.MAX_MOVE_ENNEMY);
                Vector2D newEnnemyPos = ee.p.plus(ennemyMove);
                ee.p = newEnnemyPos;

                n.safe &= ee.p.distanceL2(n.b.p) > Ennemy.MIN_SHOOT;
            }

            // Move action for me if any
            if (p_act.t == ActionType.MOVE && p_act.p != null)
            {
                Vector2D myMove = p_act.p.minus(n.b.p);
                if (myMove.getR() >= Bot.MAX_MOVE)
                    myMove = myMove.unitVector().scalarMult(Bot.MAX_MOVE);

                n.b.p = n.b.p.plus(myMove);
                if (n.b.p.x < 0)
                    n.b.p = new Vector2D(0, n.b.p.y);
                if (n.b.p.y < 0)
                    n.b.p = new Vector2D(n.b.p.x, 0);
                if (n.b.p.x >= MX)
                    n.b.p = new Vector2D(MX - 1, n.b.p.y);
                if (n.b.p.y >= MY)
                    n.b.p = new Vector2D(n.b.p.x, MY - 1);
            }

            // If dead by ennemy
            // boolean isDead = !n.isCompletelySafe(n.b.p);
            // Do nothing, end of the game.

            // Shoot in case not dead an shhot action.
            if (n.safe) // (!isDead)
                if (p_act.t == ActionType.SHOOT)
                {
                    Ennemy target = n.e.get(p_act.id);
                    target.life -= degats(n.b.p.distanceL2(target.p));

                    if (target.life <= 0)
                        n.e.remove(p_act.id);

                    n.b.shots++;
                }

            // Clear Dead ennemies
            HashSet<Integer> toR = new HashSet<>();

            // for (Integer i : n.e.keySet())
            // if (n.e.get(i).life <= 0)
            // toR.add(i);
            // n.e.keySet().removeAll(toR);
            // toR.clear();

            // Clear captured data
            // for (Ennemy ee : n.e.values())
            // for (Integer i : n.d.keySet())
            // // if (n.d.get(i).p.distanceL2(ee.p) <= Ennemy.MIN_DATA)
            // if (n.d.get(i).p.floorEquals(ee.p))
            // {
            // toR.add(i);
            // break;
            // }

            for (Ennemy ee : n.e.values())
            {
                Entity dd = this.getObj(ee);
                if (dd.p.floorEquals(ee.p))
                    toR.add(dd.id);
            }

            // if (toR.size() > 0)
            {
                n.d.keySet().removeAll(toR);
                n.moveToData = State.clustering(n.d, CLUSTER_DIST);
                // n.moveToEnnemies = WizState.clustering(n.e, CLUSTER_DIST);
            }
            // compute state score :
            n.score();

            return n;
        }

        public int heuristic()
        {
            // do not recompute each time ...
            if (this.heuristic != Integer.MIN_VALUE)
                return this.heuristic;
            this.heuristic = 0;
            if (!this.safe)
                return 0;
            // End game bonus
            int bonus = this.d.size() * Math.max(0, (this.L - 3 * this.b.shots)) * 3;
            this.heuristic = (this.d.size() * 100) + (this.NE * 10) + bonus;
            // value:
            return this.heuristic;
        }

        public int score()
        {
            // do not recompute each time ...
            if (this.score != Integer.MIN_VALUE)
                return this.score;

            this.score = 0;
            if (!this.safe)
                return 0;

            // End game bonus
            int bonus = this.d.size() * Math.max(0, (this.L - 3 * this.b.shots)) * 3;
            this.score = (this.d.size() * 100) + ((this.NE - this.e.size()) * 10) + (this.e.size() == 0 ? bonus : 0);

            // value:
            return this.score;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            StringBuilder[] sbs = new StringBuilder[3];
            for (int i = 0; i < sbs.length; i++)
                sbs[i] = new StringBuilder();

            int t = -1;
            if (!this.e.isEmpty())
                t = this.e.values().iterator().next().turn;
            sb.append(t + " : " + this.score() + "\n");
            sb.append("Wolff : " + this.b + "\n");
            sb.append("Ennemies : ---------------------------------------- \n");
            for (Ennemy ee : this.e.values())
            {
                sbs[0].append(String.format("%15d", ee.id));
                sbs[1].append(String.format("%15d", ee.life));
                sbs[2].append(String.format("%15s", ee.p.toString()));
            }
            for (int i = 0; i < sbs.length; i++)
            {
                sb.append(sbs[i] + "\n");
                sbs[i] = new StringBuilder();
            }
            sb.append("Data : ---------------------------------------- \n");
            for (Entity dd : this.d.values())
            {
                sbs[0].append(String.format("%15d", dd.id));
                sbs[1].append(String.format("%15s", dd.p.toString()));
            }
            for (StringBuilder sb2 : sbs)
                sb.append(sb2 + "\n");

            return sb.toString();
        }

        // upd for bot
        public void updB(int p_turn, int p_x, int p_y)
        {
            if (DBG_NEEDED)
                System.err.println("s.updB(" + p_turn + "," + p_x + "," + p_y + ");");

            this.b.upd(p_turn, 0, p_x, p_y);
        }

        // upd for ennemies
        public void updE(int p_turn, int p_id, int p_x, int p_y, int p_life)
        {
            if (DBG_NEEDED)
                System.err.println("s.updE(" + p_turn + "," + p_id + "," + p_x + "," + p_y + "," + p_life + ");");

            if (!this.e.containsKey(p_id))
            {
                this.L += p_life;
                this.NE++;
                this.e.put(p_id, new Ennemy(p_id, p_turn));
            }

            this.e.get(p_id).upd(p_turn, p_id, p_x, p_y, p_life);

        }

        // Upd for data
        public void updD(int p_turn, int p_id, int p_x, int p_y)
        {
            if (DBG_NEEDED)
                System.err.println("s.updD(" + p_turn + "," + p_id + "," + p_x + "," + p_y + ");");

            if (!this.d.containsKey(p_id))
                this.d.put(p_id, new Entity(p_id, p_turn));

            this.d.get(p_id).upd(p_turn, p_id, p_x, p_y);
        }

        public void clearOld(int p_turn)
        {
            // Clear dead ennemies (unupdated)
            HashSet<Integer> toR = new HashSet<>();
            for (Integer i : this.e.keySet())
                if (this.e.get(i).turn < p_turn)
                    toR.add(i);
            this.e.keySet().removeAll(toR);

            toR.clear();
            // Clear captured data (unupdated)
            for (Integer i : this.d.keySet())
                if (this.d.get(i).turn < p_turn)
                    toR.add(i);
            this.d.keySet().removeAll(toR);

            this.moveToData = State.clustering(this.d, CLUSTER_DIST);
            // this.moveToEnnemies = WizState.clustering(this.e, CLUSTER_DIST);
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

    public static class NearestComparator implements Comparator<Entity>
    {
        Vector2D ref = null;
        Distance d = Distance.EUCLIDEAN;

        public NearestComparator(Vector2D p_ref, Distance p_d)
        {
            this.ref = p_ref;
            this.d = p_d;
        }

        @Override
        public int compare(Entity p_o1, Entity p_o2)
        {
            double d1 = this.d.val(this.ref, p_o1.p);
            double d2 = this.d.val(this.ref, p_o2.p);

            int i = (int) Math.round(d1 - d2);

            if (i == 0)
                return p_o1.id - p_o2.id;

            return i;
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
