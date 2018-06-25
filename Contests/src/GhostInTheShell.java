import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class GhostInTheShell
{
    // static boolean LOCAL = true;
    static boolean LOCAL = false;
    final static boolean DEBUG = true;
    final static Random RAND = new Random();

    static int tour = 0;

    static int[][] DIST;
    static double[] STRAT;

    public static void sep(Object s)
    {
        if (DEBUG)
            System.err.println(tour + ": " + s);
    }

    public static void debug()
    {
        DIST = new int[4][4];

        State s = new State();

        for (int i = 0; i < 4; i++)
        {
            Factory f = new Factory(i);
            f.cyb = 5;
            f.prod = 1;

            if (i == 0)
            {
                f.owner = 1;
                f.cyb += 10;
                f.prod += 1;
            }
            if (i == 2)
            {
                f.owner = -1;
                f.cyb += 10;
                f.prod += 1;
            }

            s.f.put(i, f);
        }

        for (int i = 0; i < DIST.length; i++)
            for (int j = 0; j < DIST[0].length; j++)
            {
                DIST[i][j] = 2;
                DIST[j][i] = 2;
                DIST[i][i] = 0;
            }

        Coordinator coor = new Coordinator();
        for (int t = 0; t < 5; t++)
        {
            Collection<Action> a = coor.decide(t, s);
            s = s.nextState(a);
            tour++;
        }

    }

    public static void main(String args[])
    {
        if (LOCAL)
        {
            debug();
            return;
        }

        HashMap<Integer, Entity> ent = new HashMap<>();

        Scanner in = new Scanner(System.in);
        int factoryCount = in.nextInt(); // the number of factories
        DIST = new int[factoryCount][factoryCount];
        STRAT = new double[factoryCount];

        int linkCount = in.nextInt(); // the number of links between factories
        for (int i = 0; i < linkCount; i++)
        {
            int factory1 = in.nextInt();
            int factory2 = in.nextInt();
            int distance = in.nextInt();

            DIST[factory1][factory2] = distance;
            DIST[factory2][factory1] = distance;

            Factory f1 = (Factory) ent.get(factory1);
            if (f1 == null)
            {
                f1 = new Factory(factory1);
                ent.put(factory1, f1);
            }

            Factory f2 = (Factory) ent.get(factory2);
            if (f2 == null)
            {
                f2 = new Factory(factory2);
                ent.put(factory2, f2);
            }
        }

        for (int i = 0; i < DIST.length; i++)
        {
            for (int j = 0; j < DIST.length; j++)
                STRAT[i] += DIST[i][j];

            STRAT[i] = 5000 / (STRAT[i] * STRAT[i]);
        }

        if (ent.size() != factoryCount)
            sep("ERROR : " + ent.size() + " != " + factoryCount);

        // for (int[] element : DIST)
        // sep(Arrays.toString(element));
        sep(Arrays.toString(STRAT));

        // Coordinator coor = new Coordinator();
        Coordinator coor = new Decentralized();

        tour = 0;
        // game loop
        while (true)
        {
            State s = new State();
            int entityCount = in.nextInt(); // the number of entities (e.g. factories and troops)
            for (int i = 0; i < entityCount; i++)
            {
                int entityId = in.nextInt();
                String entityType = in.next();

                Entity ee = ent.get(entityId);
                if (ee == null)
                {
                    switch (entityType)
                    {
                        case "BOMB":
                            ee = new Bomb(entityId);

                            break;
                        case "TROOP":
                            ee = new Troop(entityId);
                    }
                    ent.put(ee.id, ee);
                }
                // int p_o = in.nextInt();
                // int p_2 = in.nextInt();
                // int p_3 = in.nextInt();
                // int p_4 = in.nextInt();
                // int p_5 = in.nextInt();
                //
                // ee.upd(tour, p_o, p_2, p_3, p_4, p_5);

                ee.upd(tour, in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
                s.upd(tour, ee);
            }

            HashSet<Integer> toRem = new HashSet<>();
            for (Entity e : ent.values())
                if (e.lastUpd < tour)
                    toRem.add(e.id);
            ent.keySet().removeAll(toRem);

            PriorityQueue<Action> a = coor.decide(tour, s);

            // HashMap<Integer, Action> fa = new HashMap<>();
            // for (Action aa : a)
            // if (!fa.containsKey(aa.from))
            // fa.put(aa.from, aa);

            String as = "";
            while (!a.isEmpty() && !a.peek().equals(new Action()))// && a.peek().val > 0)
                as += a.poll() + ";";

            as += "MSG GLHF !";
            System.out.println(as);
            tour++;
        }
    }

    public static class Decentralized extends Coordinator
    {

        // Future actions to do
        // HashMap<Integer, PriorityQueue<Action>> fa = new HashMap<>();
        // Future States according to current actions
        // HashMap<Integer, State> fs = new HashMap<>();

        @Override
        public PriorityQueue<Action> decide(int p_t, State p_s)
        {
            PriorityQueue<Action> q = new PriorityQueue<>(this);

            q.add(new Action());
            int myScore = 0;
            int hisScore = 0;

            for (Factory e : p_s.f.values())
                if (e.isMine())
                {
                    q.addAll(this.decide(p_t, e, p_s));
                    myScore += e.cyb;
                }
                else if (e.owner == -1)
                    hisScore += e.cyb;

            for (Troop tt : p_s.t.values())
                if (tt.isMine() && !(tt instanceof Bomb))
                    myScore += tt.cyb;
                else if (tt.owner == -1 && !(tt instanceof Bomb))
                    hisScore += tt.cyb;

            sep("SCORE : " + myScore + "/" + hisScore);

            // BOMB Action
            for (Factory ee : p_s.f.values())
                if (ee.owner == -1 && !p_s.containsBomb(ee.id))
                {
                    // All choice for bombing this one
                    ArrayList<Action> bb = new ArrayList<>();
                    for (Factory e : p_s.f.values())
                        if (e.isMine())
                        {
                            Action a = new Action(e.id, ee.id);
                            State fs = p_s.nextEndState(a);
                            // sep("Bomb ? \n " + p_s.f.get(ee.id) + "=" + heuristic(p_s) + "--" + a + "++"
                            // + fs.f.get(ee.id) + "=" + heuristic(fs));
                            if (!fs.f.get(ee.id).isMine() && ee.prod >= PROD_DEST)
                                bb.add(a);
                        }
                    // Get nearest
                    if (!bb.isEmpty())
                    {
                        bb.sort(new ActionDistComparator());
                        q.add(bb.get(0));
                    }
                }

            // INC Action
            for (Factory e : p_s.f.values())
                if (e.isMine() && e.prod < 3 && myScore > hisScore + 5)
                {
                    Action a = new Action(e.id);
                    // State s = p_s.nextEndState(a);
                    if (e.cyb > 10)
                        q.add(a);
                }

            ArrayList<Action> la = new ArrayList<>(q);
            la.sort(this);

            for (Action aa : la)
                sep(aa + " -> " + aa.val);

            return q;
        }

        public PriorityQueue<Action> decide(int p_t, Factory p_f, State p_s)
        {
            this.s = p_s;
            PriorityQueue<Action> a = new PriorityQueue<>(this);

            // Attack neutral factories
            for (Factory f : p_s.f.values())
                if (f.owner == 0)
                    a.add(new Action(p_f.id, f.id, f.cyb + 1));

            // Defend my factories
            for (Factory f : p_s.f.values())
                if (f.isMine() && f.prod > 1 && f.id != p_f.id)
                {
                    int attVal = 0;
                    for (Troop tt : p_s.t.values())
                        if (!(tt instanceof Bomb))
                            if (tt.to == f.id)
                                attVal += tt.cyb;

                    a.add(new Action(p_f.id, f.id, attVal));
                }
            // Save Action against Bomb against me
            for (Troop tt : p_s.t.values())
                if (tt instanceof Bomb && tt.owner == -1 && tt.to == p_f.id && tt.time < 2)
                {
                    ArrayList<Factory> af = new ArrayList<>(p_s.f.values());
                    af.remove(p_f);
                    af.sort(new FactoryDistComparator(p_f.id));

                    a.add(new Action(tt.to, af.get(0).id, p_f.cyb));
                }

            // Save Action against Bomb against another of my factories
            for (Troop tt : p_s.t.values())
                if (tt instanceof Bomb && tt.owner == -1 && tt.to != -1 && tt.time < DIST[p_f.id][tt.to])
                {
                    ArrayList<Factory> af = new ArrayList<>(p_s.f.values());
                    af.remove(p_f);
                    af.sort(new FactoryDistComparator(p_f.id));

                    for (int i = 1; i <= p_f.cyb; i++)
                        a.add(new Action(p_f.id, tt.to, i));
                }

            // Attack ennemy factories if enough cyb left and not attacked
            int attVal = 0;
            for (Troop tt : p_s.t.values())
                if (!(tt instanceof Bomb))
                    if (tt.to == p_f.id)
                        attVal += tt.cyb;

            for (Factory f : p_s.f.values())
                if (!f.isMine())
                    for (int i = 1; i <= p_f.cyb - attVal; i += p_f.prod + 1)
                        a.add(new Action(p_f.id, f.id, i));

            // Transfer to unproductive if score is good.
            for (Factory f : p_s.f.values())
                if (f.isMine() && f.prod < 3 && f.id != p_f.id && attVal < 10 && p_f.cyb > 10 && p_f.prod == 3)
                    a.add(new Action(p_f.id, f.id, 10));

            // Check doable
            ArrayList<Action> la = new ArrayList<>(a);
            la.sort(this);
            int[] remainingCyb = new int[DIST.length];

            for (Factory e : p_s.f.values())
                remainingCyb[e.id] = e.cyb;
            for (Action aa : la)
            {
                boolean isDoable = aa.cyb != 0 && remainingCyb[aa.from] >= aa.cyb;
                // sep(p_f.id + " --- " + aa + " -> " + aa.val + " - " + isDoable);
                if (isDoable)
                    // sep(p_f.id + " --- " + aa + " -> " + aa.val);
                    remainingCyb[aa.from] -= aa.cyb;
                else
                    a.remove(aa);
            }

            return a;
        }

        @Override
        public int compare(Action p_a0, Action p_a1)
        {
            State s0 = this.s.nextEndState(p_a0);
            State s1 = this.s.nextEndState(p_a1);

            int i0 = heuristic(s0) - DIST[p_a0.from][p_a0.to] * DIST[p_a0.from][p_a0.to] * DIS;
            int i1 = heuristic(s1) - DIST[p_a1.from][p_a1.to] * DIST[p_a1.from][p_a1.to] * DIS;

            p_a0.setVal(i0);
            p_a1.setVal(i1);

            return i1 - i0;
        }
    }

    public static class Coordinator implements Comparator<Action>
    {
        final static int PROD = 3000;
        final static int DIS = 100;
        final static int CYB = 100;

        // Destruction Threshold
        final static int DEST = 10;
        final static int PROD_DEST = 2;

        public State s;

        public HashSet<Action> moveAction(State p_s)
        {
            HashSet<Action> a = new HashSet<>();
            State doNothing = p_s.nextEndState(new Action());

            // MOVE Actions
            for (Factory e : p_s.f.values())
                if (e.isMine() && e.cyb > 0)
                    for (Factory ee : p_s.f.values())
                        if (!ee.isMine()) // Attack move
                        {
                            int num = Math.abs(ee.cyb - ee.owner * ee.prod + 1);
                            if (num < e.cyb)
                                a.add(new Action(e.id, ee.id, num));
                            else
                                a.add(new Action(e.id, ee.id, e.cyb));
                        }
                        else if (!doNothing.f.get(ee.id).isMine() && ee.prod > 0)
                        {
                            int num = 0;
                            for (Troop tt : p_s.t.values())
                                num += (tt.to == ee.id ? tt.cyb : 0);

                            if (num < e.cyb)
                                a.add(new Action(e.id, ee.id, num + 1));
                        }

            return a;
        }

        public PriorityQueue<Action> decide(int p_t, State p_s)
        {
            this.s = p_s;

            PriorityQueue<Action> q = new PriorityQueue<>(this);

            q.add(new Action());
            // State w = p_s.nextEndState(new Action());

            // MOVE Actions
            q.addAll(this.moveAction(p_s));
            // q.addAll(this.moveAction(w));

            // BOMB Action
            for (Factory ee : p_s.f.values())
                if (ee.owner == -1 && !p_s.containsBomb(ee.id))
                    for (Factory e : p_s.f.values())
                        if (e.isMine())
                        {
                            Action a = new Action(e.id, ee.id);
                            State s = p_s.nextEndState(a);
                            // sep("Bomb ? \n " + p_s.f.get(ee.id) + "=" + heuristic(p_s) + "--" + a + "++"
                            // + s.f.get(ee.id) + "=" + heuristic(s));
                            // if (s.f.get(ee.id).cyb > DEST &&
                            if (s.f.get(ee.id).prod >= PROD_DEST)
                            {
                                q.add(a);
                                break;
                            }

                        }

            // INC Action
            for (Factory e : p_s.f.values())
                if (e.isMine() && e.prod < 3)
                {
                    Action a = new Action(e.id);
                    // State s = p_s.nextEndState(a);
                    if (e.cyb > 10)
                        q.add(a);
                }

            // SAVE Action against Troop/Bomb
            for (Troop tt : p_s.t.values())
                if (tt instanceof Bomb && tt.owner == -1 && tt.to != -1)
                {
                    Factory to = p_s.f.get(tt.to);
                    int min = 111;
                    int minId = tt.from;
                    for (Factory f : p_s.f.values())
                        if (f.isMine() && f.id != tt.to && DIST[f.id][tt.to] < min)
                        {
                            min = DIST[f.id][tt.to];
                            minId = f.id;
                        }

                    sep("SAVE From Bomb : " + new Action(tt.to, minId, to.cyb));
                    q.add(new Action(tt.to, minId, to.cyb));
                }

            ArrayList<Action> la = new ArrayList<>(q);
            la.sort(this);

            int[] remainingCyb = new int[DIST.length];
            for (Factory e : p_s.f.values())
                remainingCyb[e.id] = e.cyb;
            // sep("-->" + q);
            for (Action aa : la)
            {
                boolean isDoable = remainingCyb[aa.from] >= aa.cyb;
                sep(aa + " -> " + aa.val + " - " + isDoable);
                if (isDoable)
                    remainingCyb[aa.from] -= aa.cyb;
                else
                    q.remove(aa);
            }

            return q;
        }

        @Override
        public int compare(Action p_a0, Action p_a1)
        {
            State s0 = this.s.nextEndState(p_a0);
            State s1 = this.s.nextEndState(p_a1);

            int i0 = heuristic(s0) - DIST[p_a0.from][p_a0.to] * DIST[p_a0.from][p_a0.to] * DIS;
            int i1 = heuristic(s1) - DIST[p_a1.from][p_a1.to] * DIST[p_a1.from][p_a1.to] * DIS;

            // if (p_a0.t == ActionType.INC || p_a1.t == ActionType.INC)
            // sep(p_a0 + "(" + i0 + ") " + " ---- " + " (" + i1 + ") " + p_a1);

            p_a0.setVal(i0);
            p_a1.setVal(i1);

            return i1 - i0;
        }

        public static int heuristic(State p_s)
        {
            int v = 0;
            for (Factory ff : p_s.f.values())
            {
                v += ff.owner * PROD * ff.prod * STRAT[ff.id]; // For mine and ennemies
                v += ff.owner * CYB * ff.cyb * ff.prod * STRAT[ff.id];

                // if (ff.owner == 0)
                // {
                // v -= ff.prod * PROD;
                // v -= ff.cyb * CYB * ff.prod;
                // }
            }

            for (Troop tt : p_s.t.values())
                v += tt.owner * CYB * tt.cyb;

            return v;
        }
    }

    public enum ActionType
    {
        WAIT, MOVE, BOMB, INC;
    }

    public static class Action
    {
        public int val = -1;

        ActionType t;
        int from = 0;
        int to = 0;
        int cyb = 0;

        public Action()
        {
            this.t = ActionType.WAIT;
        }

        public Action(int p_f)
        {
            this.t = ActionType.INC;
            this.from = p_f;
            this.to = p_f;
            this.cyb = -10;
        }

        public Action(int p_f, int p_to)
        {
            this.t = ActionType.BOMB;
            this.from = p_f;
            this.to = p_to;
        }

        public Action(int p_from, int p_to, int p_num)
        {
            this.t = ActionType.MOVE;
            this.from = p_from;
            this.to = p_to;
            this.cyb = p_num;
        }

        public void setVal(int p_v)
        {
            if (this.val != -1 && this.val != p_v)
                sep("Deux chabgement de valeurs : " + this.val + " -> " + p_v);
            this.val = p_v;
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
            result = prime * result + ((this.t == null) ? 0 : this.t.hashCode());
            result = prime * result + this.from;
            result = prime * result + this.to;
            result = prime * result + this.cyb;
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
            if (this.from != other.from)
                return false;
            if (this.t != other.t)
                return false;
            if (this.to != other.to)
                return false;
            if (this.cyb != other.cyb)
                return false;
            return true;
        }

        @Override
        public String toString()
        {
            switch (this.t)
            {
                case WAIT:
                    return this.t + "";
                case INC:
                    return this.t + " " + this.from;
                case BOMB:
                    return this.t + " " + this.from + " " + this.to;
                case MOVE:
                    return this.t + " " + this.from + " " + this.to + " " + this.cyb;
            }
            return "";
        }

    }

    public static class State
    {
        int tour = 0;
        HashMap<Integer, Factory> f;
        HashMap<Integer, Troop> t;

        public State()
        {
            this.f = new HashMap<>();
            this.t = new HashMap<>();
        }

        @Override
        public State clone()
        {
            State c = new State();

            for (Factory e : this.f.values())
                c.f.put(e.id, (Factory) e.clone());
            for (Troop e : this.t.values())
                c.t.put(e.id, (Troop) e.clone());

            return c;
        }

        /**
         * Given a State and an action, returns the next state
         *
         * @param p_a
         * @return the enxt State after the action and no following action
         */
        public State nextState(Collection<Action> p_A)
        {
            State n = this.clone();
            n.tour++;

            HashMap<Factory, ArrayList<Troop>> battle = new HashMap<>();
            for (Factory e : n.f.values())
            {
                // Prepare battle
                battle.put(e, new ArrayList<>());
                // Reduce stopped prod time;
                e.time--;
                if (e.time == 0)
                    e.prod = e.o_prod;
            }

            // Move existing troops
            for (Troop tt : n.t.values())
                if (tt.time > 1)
                    tt.time -= 1;
                else
                    battle.get(tt.to).add(tt);

            for (Action p_a : p_A)
            {
                // Execute orders
                if (p_a.t == ActionType.MOVE)
                {
                    // Remove from FROM
                    Factory from = n.f.get(p_a.from);
                    int num = Math.max(0, from.cyb - p_a.cyb);
                    from.cyb = num;
                    // Add to Troop
                    Troop p = new Troop(RAND.nextInt());
                    p.upd(n.tour, 1, p_a.from, p_a.to, p_a.cyb, DIST[p_a.from][p_a.to]);
                    n.t.put(p.id, p);
                }
                if (p_a.t == ActionType.BOMB)
                {
                    Bomb b = new Bomb(RAND.nextInt());
                    b.upd(n.tour, 1, p_a.from, p_a.to, DIST[p_a.from][p_a.to], 0);
                    n.t.put(b.id, b);
                }
                if (p_a.t == ActionType.INC)
                {
                    Factory from = n.f.get(p_a.from);
                    if (from.cyb >= 10)
                    {
                        from.cyb -= 10;
                        from.prod++;
                    }
                }
            }

            // produce cyb
            for (Factory e : n.f.values())
                if (e.owner != 0)
                    e.cyb += e.prod;

            // Solve Battles
            for (Factory e : n.f.values())
                if (!battle.get(e).isEmpty())
                {
                    ArrayList<Bomb> b = new ArrayList<>();
                    ArrayList<Troop> a = battle.get(e);
                    int res = 0;
                    for (Troop tt : a)
                        if (tt instanceof Bomb)
                            b.add((Bomb) tt);
                        else
                            res += tt.owner * tt.cyb;

                    if (e.owner == 0)
                        e.owner = (int) Math.signum(res);

                    e.cyb += e.owner * res;

                    if (e.cyb < 0)
                    {
                        e.owner *= -1;
                        e.cyb *= -1;
                    }

                    // BOMBING
                    for (Bomb bb : b)
                        if (bb.to != -1)
                        {
                            Factory to = n.f.get(bb.to);
                            int num = Math.min(to.cyb, Math.max(10, to.cyb / 2));
                            to.cyb -= num;

                            to.upd(n.tour, to.owner, to.cyb, 0, 5, 0);
                        }
                }

            return n;
        }

        public HashMap<Action, State> endStates = new HashMap<>();

        /**
         * Given a State and an action, returns the long end state without any other action
         *
         * @param p_a
         * @return the next State after the action and no following action
         */
        public State nextEndState(Action p_a)
        {
            // caching
            State es = this.endStates.get(p_a);
            if (es != null)
                return es;

            State n = this.clone();
            n.tour += DIST[p_a.from][p_a.to];

            // produce cyb while time of action
            for (Factory e : n.f.values())
                if (e.owner != 0)
                    e.cyb += e.prod * DIST[p_a.from][p_a.to];

            for (Troop tt : n.t.values())
                if (tt instanceof Bomb)
                {
                    if (tt.to != -1)
                    {
                        Factory to = n.f.get(tt.to);
                        int num = Math.min(to.cyb, Math.max(10, to.cyb / 2));
                        to.cyb -= num;

                        if (DIST[p_a.from][p_a.to] < 5)
                            to.prod = 0;
                    }
                }
                else
                {
                    Factory to = n.f.get(tt.to);
                    // sep(tt + " - " + to);
                    int camp = (to.owner == 0 ? -1 * tt.owner : to.owner);
                    to.cyb += camp * tt.owner * tt.cyb;
                    if (to.cyb < 0)
                    {
                        to.cyb *= -1;
                        to.owner = tt.owner;
                    }
                }

            if (p_a.t == ActionType.MOVE)
            {
                Factory fr = n.f.get(p_a.from);
                Factory to = n.f.get(p_a.to);
                int num = Math.min(p_a.cyb, fr.cyb);

                // Remove from FROM
                fr.cyb -= num;

                int camp = (to.owner == 0 ? -1 * fr.owner : to.owner);
                // Add to TO
                to.cyb += camp * num;
                if (to.cyb < 0)
                {
                    to.cyb *= -1;
                    to.owner = fr.owner;
                }
            }

            if (p_a.t == ActionType.BOMB)
            {
                Factory to = n.f.get(p_a.to);

                int num = Math.min(to.cyb, Math.max(10, to.cyb / 2));
                to.cyb -= num;

                if (DIST[p_a.from][p_a.to] < 5)
                    to.prod = 0;
            }

            if (p_a.t == ActionType.INC)
            {
                Factory from = n.f.get(p_a.from);
                if (from.cyb >= 10)
                {
                    from.cyb -= 10;
                    from.prod++;
                }
            }

            // caching
            this.endStates.put(p_a, n);

            return n;
        }

        /**
         * Given a State and a set of actions, returns the long end state without any other action
         *
         * @param p_la
         * @return the next State after the action and no following action
         */
        public State nextEndState(Collection<Action> p_la)
        {
            State n = this.clone();

            for (Troop tt : n.t.values())
                if (tt instanceof Bomb)
                {
                    if (tt.to != -1)
                    {
                        Factory to = n.f.get(tt.to);
                        int num = Math.min(to.cyb, Math.max(10, to.cyb / 2));
                        to.cyb -= num;

                        if (DIST[tt.from][tt.to] < 5)
                            to.prod = 0;
                    }
                }
                else
                {
                    Factory to = n.f.get(tt.to);
                    // sep(tt + " - " + to);
                    to.cyb += to.owner * tt.owner * tt.cyb;
                }

            int tmax = 1;
            for (Action p_a : p_la)
            {
                if (p_a.t == ActionType.MOVE)
                {
                    Factory fr = n.f.get(p_a.from);
                    Factory to = n.f.get(p_a.to);
                    int num = Math.min(p_a.cyb, fr.cyb);

                    // Remove from FROM
                    fr.cyb -= num;

                    int camp = to.owner;
                    if (to.owner == 0)
                        camp = fr.owner * -1;

                    // Add to TO
                    to.cyb += camp * num;
                    if (to.cyb < 0)
                    {
                        to.cyb *= -1;
                        to.owner = fr.owner;
                    }

                    if (DIST[p_a.from][p_a.to] > tmax)
                        tmax = DIST[p_a.from][p_a.to];
                }

                if (p_a.t == ActionType.BOMB)
                {
                    Factory to = n.f.get(p_a.to);

                    int num = Math.min(to.cyb, Math.max(10, to.cyb / 2));
                    to.cyb -= num;

                    if (DIST[p_a.from][p_a.to] < 5)
                        to.prod = 0;

                    if (DIST[p_a.from][p_a.to] > tmax)
                        tmax = DIST[p_a.from][p_a.to];
                }

                if (p_a.t == ActionType.INC)
                {
                    Factory from = n.f.get(p_a.from);
                    if (from.cyb >= 10)
                    {
                        from.cyb -= 10;
                        from.prod++;
                    }
                }

            }

            n.tour += tmax;
            return n;
        }

        public void upd(int p_t, Entity p_e)
        {
            // sep("Update0 :" + p_e);
            this.tour = p_t;
            if (p_e instanceof Factory)
                this.f.put(p_e.id, (Factory) p_e);
            else if (p_e instanceof Bomb)
            {
                // sep("Update1 :" + p_e);
                Bomb bb = (Bomb) p_e;
                this.t.put(p_e.id, bb);

                if (bb.to == -1)
                    bb.guessTimeAndTarget(this);
            }
            else if (p_e instanceof Troop)
                this.t.put(p_e.id, (Troop) p_e);
        }

        public boolean containsBomb(int p_id)
        {
            for (Troop tt : this.t.values())
                if (tt instanceof Bomb && tt.to == p_id)
                    return true;

            return false;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "S : " + this.tour + "\nf:" + this.f + "\nt:" + this.t;
        }
    }

    public static class Factory extends Entity
    {
        int prod = -1;
        int cyb = -1;
        int time = -1;

        int o_prod = -1;

        public Factory(int p_id)
        {
            super(p_id);
        }

        @Override
        public Entity clone()
        {
            Factory e = new Factory(this.id);
            e.owner = this.owner;
            e.prod = this.prod;
            e.cyb = this.cyb;

            return e;
        }

        @Override
        void upd(int p_t, int p_o, int p_2, int p_3, int p_4, int p_5)
        {
            this.cyb = p_2;

            if (p_4 != 0 && p_3 == 0)
                this.o_prod = this.prod;

            this.prod = p_3;
            this.time = p_4;
            super.upd(p_t, p_o, p_2, p_3, p_4, p_5);
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return super.toString() + "[" + this.prod + "," + this.cyb + "," + this.time + "]";
        }

    }

    public static class Troop extends Entity
    {
        int from = -1;
        int to = -1;
        int cyb = -1;
        int time = -1;

        public Troop(int p_id)
        {
            super(p_id);
        }

        @Override
        public Entity clone()
        {
            Troop e = new Troop(this.id);
            e.owner = this.owner;
            e.from = this.from;
            e.to = this.to;
            e.cyb = this.cyb;
            e.time = this.time;

            return e;
        }

        @Override
        void upd(int p_t, int p_o, int p_2, int p_3, int p_4, int p_5)
        {
            this.from = p_2;
            this.to = p_3;
            this.cyb = p_4;
            this.time = p_5;
            super.upd(p_t, p_o, p_2, p_3, p_4, p_5);
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return super.toString() + "[" + this.from + "->" + this.to + "," + this.cyb + "," + this.time + "]";
        }
    }

    public static class Bomb extends Troop
    {

        public int launch_time = -1;

        public Bomb(int p_id)
        {
            super(p_id);
        }

        @Override
        public Entity clone()
        {
            Bomb e = new Bomb(this.id);
            e.owner = this.owner;
            e.from = this.from;
            e.to = this.to;
            e.cyb = this.cyb;
            e.time = this.time;
            e.launch_time = this.launch_time;

            return e;
        }

        @Override
        void upd(int p_t, int p_o, int p_2, int p_3, int p_4, int p_5)
        {
            if (this.launch_time == -1)
                this.launch_time = p_t;

            if (p_3 != -1 && p_4 != -1) // My BOMB
                super.upd(p_t, p_o, p_2, p_3, 10, p_4);
            else // HIS BOMB
                super.upd(p_t, p_o, p_2, this.to, 10, this.time - 1);
        }

        public void guessTimeAndTarget(State s)
        {
            ArrayList<Integer> targets = new ArrayList<>();
            for (int i = 0; i < DIST[this.from].length && s.f.get(i) != null; i++)
                // sep("f:" + i + " -- " + DIST[this.from][i] + " > " + s.tour + " - " + this.launch_time);
                if (s.f.get(i).isMine() && DIST[this.from][i] > s.tour - this.launch_time)
                    targets.add(i);

            if (targets.size() == 1)
            {
                this.to = targets.get(0);
                this.time = DIST[this.from][this.to] - (s.tour - 1 - this.launch_time);
            }
            sep("Bomb guessing " + targets + "\n -->" + this);
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return super.toString() + "[" + this.from + "->" + this.to + "," + this.launch_time + "," + this.time + "]";
        }
    }

    public static class Entity
    {
        final int id;
        int owner = 0;
        int lastUpd = -1;

        private Entity(int p_entityId)
        {
            this.id = p_entityId;
        }

        @Override
        public Entity clone()
        {
            Entity e = new Entity(this.id);
            e.owner = this.owner;
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
            return this.getClass().getSimpleName() + "(" + this.id + "/" + this.owner + ")";
        }

        public boolean isMine()
        {
            return this.owner == 1;
        }

        void upd(int p_t, int p_o, int p_2, int p_3, int p_4, int p_5)
        {
            this.owner = p_o;
            this.lastUpd = p_t;

            // sep("upd : " + this);
        }

    }

    public static class FactoryDistComparator implements Comparator<Factory>
    {

        int toCompare;

        public FactoryDistComparator(int p_p)
        {
            this.toCompare = p_p;
        }

        @Override
        public int compare(Factory p_a, Factory p_b)
        {
            int a = DIST[p_a.id][this.toCompare];
            int b = DIST[p_b.id][this.toCompare];
            return a - b;
        }

    }

    public static class ActionDistComparator implements Comparator<Action>
    {
        @Override
        public int compare(Action p_a, Action p_b)
        {
            int a = DIST[p_a.from][p_a.to];
            int b = DIST[p_b.from][p_b.to];
            return a - b;
        }

    }
}
