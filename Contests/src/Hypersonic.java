import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Hypersonic
{

    static Random RAND = new Random();
    static final boolean DEBUG = true;
    static final boolean RELEASE = true;
    static final int NB_PLAYER = 4;

    static int MY_ID = 0;
    static int turn = 0;

    static int nmsg = 0;
    static final String[] MSG = { "Well Played !", "BOMB!BOMB!BOMB!", "TicToc!TicToc!", "I'll just leave this here.",
            "This bomb's for you!", "That's a good spot.", "Surprise!", "Don't move!",
            "Oh, you really stepped in it mate!", "Watch your step!", "Fire in the hole!", "Rest in pieces." };

    public static void ass(String p_s)
    {
        if (!RELEASE)
            System.err.println(" === ERROR === : " + p_s);
    }

    public static void db(String p_s)
    {
        if (DEBUG && !RELEASE)
            System.err.println("Dbg (" + turn + "): " + p_s);
    }

    public static String getRandomMsg(int p_t)
    {
        // if (p_t % 5 == 0)
        nmsg = RAND.nextInt(MSG.length);

        return MSG[nmsg];
    }

    public static void main(String args[])
    {
        new Hypersonic().run();
    }

    public void run()
    {
        Scanner in = new Scanner(System.in);
        int width = in.nextInt();
        int height = in.nextInt();
        MY_ID = in.nextInt();
        in.nextLine();

        Grid g = new Grid();
        Bot[] p = new Bot[NB_PLAYER];

        for (int i = 0; i < p.length; i++)
            /***********************************************************************************************************************/
            p[i] = new Bot(i, new StrPositionHeuristic(i));
        /***************************************************************************************************************************/

        turn = 0;
        State s = new State(g, p);
        // game loop
        while (true)
        {
            String[] rows = new String[height];
            for (int i = 0; i < height; i++)
                rows[i] = in.nextLine();

            int entities = in.nextInt();
            for (int i = 0; i < entities; i++)
            {
                int entityType = in.nextInt();
                int owner = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
                int param1 = in.nextInt();
                int param2 = in.nextInt();
                s.upd(turn, entityType, owner, y, x, param1, param2);
                // s.upd(turn, in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
            }
            s.upd(turn, rows);

            in.nextLine();

            long st = System.currentTimeMillis();
            Action a = p[MY_ID].decide(turn, s);
            long end = System.currentTimeMillis() - st;

            System.err.println(MY_ID + " : Time to decide : " + end);
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            System.out.println(a.t + " " + a.p.y + " " + a.p.x + p[MY_ID].getMsg());
            turn++;
        }
    }

    public static class StrGreedy extends IStrategy
    {

        public StrGreedy(int p_id)
        {
            super(p_id);
        }

        @Override
        public Action decide(int p_turn, State p_s)
        {
            Bot me = p_s.p[MY_ID];
            Position p = me.p;

            SortedSet<Position> pb = p_s.nearestBoxes(p, 13);

            Action a = new Action(ActionType.MOVE, pb.first());

            HashSet<Position> pbombed = p.getInRangeVH(me.range);

            // db("\n" + p_s);
            // db("boxes : " + pb);
            // db("Attain:" + pbombed);

            if (pbombed.contains(pb.first()))
                a.t = ActionType.BOMB;

            for (Position pn : pb)
                if (!pbombed.contains(pn))
                {
                    a.p = pn;
                    break;
                }

            return a;
        }

    }

    public static class StrGreedyEfficient extends IStrategy
    {

        public StrGreedyEfficient(int p_id)
        {
            super(p_id);
        }

        @Override
        public Action decide(int p_turn, State p_s)
        {
            Bot me = p_s.p[MY_ID];
            NearestComparator c = new NearestComparator(me.p, Distance.MANHATTAN);

            TreeMap<Integer, Position> places = new TreeMap<>();
            PriorityQueue<Position> safePlaces = new PriorityQueue<>(c);

            // Compute accessible Pos
            HashSet<Position> access = p_s.accessiblePos(me.p);
            // Find safe ones amongst them
            for (Position pT : access)
                if (p_s.isCompletelySafe(pT))
                    safePlaces.add(pT);

            // Find best place to bomb according to the position
            for (Position pT : access)
            {
                HashSet<Position> pbombed = pT.getInRangeVH(me.range);
                HashSet<Position> filtered = new HashSet<>();

                for (Position pB : pbombed)
                    if (p_s.g.isBox(pB) && p_s.isReachable(pT, pB))
                        filtered.add(pB);

                int n = filtered.size();

                // db("TestFilter : " + pbombed + "/" + safePlaces);
                // Valid that I can find a safe place after posing that bomb
                if (safePlaces.contains(pT) && !pbombed.containsAll(safePlaces))
                    if (!places.containsKey(n))
                        places.put(n, pT);
                    else if (!p_s.isBomb(places.get(n)) && c.compare(pT, places.get(n)) < 0)
                        places.put(n, pT);
            }

            // db("Safe : " + safePlaces);
            // db("toBomb : " + places);
            // db("me " + me);

            Action a = null;
            if (places.isEmpty())
                a = new Action(ActionType.MOVE, safePlaces.peek());
            else if (me.p.equals(places.lastEntry().getValue()) && me.isBombable())
            {
                for (Position np : me.p.getInRangeVH(me.range))
                    if (p_s.isReachable(me.p, np))
                        safePlaces.remove(np);

                a = new Action(ActionType.BOMB, safePlaces.peek());
            }
            else
                a = new Action(ActionType.MOVE, places.lastEntry().getValue());

            return a;
        }

    }

    public static class StrActionHeuristic extends IStrategy
    {

        public StrActionHeuristic(int p_id)
        {
            super(p_id);
        }

        @Override
        public Action decide(int p_turn, State p_s)
        {
            ActionHeuristic ah = new ActionHeuristic(p_turn, p_s);
            PriorityQueue<Action> whatToDo = new PriorityQueue<>(ah);

            for (Position pT : ah.access)
            {
                whatToDo.add(new Action(ActionType.BOMB, pT));
                whatToDo.add(new Action(ActionType.MOVE, pT));
            }

            // DEBUG
            // StringBuilder sb = new StringBuilder();
            // int i = 0;
            // for (Action a : whatToDo)
            // {
            // sb.append(a + "->" + ah.value(a) + "\n");
            // i++;
            // if (i > 5)
            // break;
            // }
            //
            // db("toDo : " + whatToDo.size() + "\n" + sb);
            // EOD!

            p_s.p[MY_ID].msg = "[" + p_s.p[MY_ID].p + "]>" + whatToDo.peek();

            return whatToDo.peek();
        }

    }

    public static class ActionHeuristic implements Comparator<Action>
    {
        public static final int SAFE_PLACE_VALUE = 1000;
        public static final int BOX_VALUE = 100;
        public static final int KILL_VALUE = 60;
        public static final int ITEM_VALUE = 100;
        public static final int BOMB_VALUE = 20;
        public static final int DIST_VALUE = 5;
        public static final int DEATH_VALUE = 1000000;
        int t;
        State s;

        HashSet<Position> access;
        PriorityQueue<Position> safePlaces;
        HashMap<Action, Double> values;
        Dijkstra dijkstra;

        public ActionHeuristic(int p_turn, State p_s)
        {
            this.t = p_turn;
            this.s = p_s;

            Bot me = p_s.p[MY_ID];

            NearestComparator c = new NearestComparator(me.p, Distance.MANHATTAN);
            this.safePlaces = new PriorityQueue<>(c);

            int[][] g = new int[Grid.MX][Grid.MY];
            // Compute accessible Pos
            this.access = p_s.accessiblePos(me.p);
            g[me.p.x][me.p.y] = 1;
            // Find safe ones amongst them
            for (Position pT : this.access)
            {
                g[pT.x][pT.y] = 1;
                if (p_s.isCompletelySafe(pT))
                    this.safePlaces.add(pT);
            }

            this.dijkstra = new Dijkstra(g, me.p);

            // Memorization of values
            this.values = new HashMap<>();

            // db("Safe : " + this.safePlaces);// + "\n" + this.dijkstra);

        }

        @Override
        public int compare(Action p_o1, Action p_o2)
        {
            // Ordre décroissant :
            return (int) (-1 * Math.round(this.value(p_o1) - this.value(p_o2)));
        }

        public double value(Action p_A)
        {
            if (this.safePlaces.size() == 1 && p_A.p.equals(this.safePlaces.peek()))
                return DEATH_VALUE;

            if (this.values.containsKey(p_A))
                return this.values.get(p_A);

            double v = 0.0;
            Bot me = this.s.p[MY_ID];

            // Item on route ?
            List<Position> l = this.dijkstra.solve(p_A.p);
            // db("Dj:" + me.p + "->" + p_A.p + ":" + l);
            for (Position pp : l)
            {
                if (this.s.o.containsKey(pp))
                    v += ITEM_VALUE;
                if (!this.s.isSafeXTurn(pp, p_A.duration(me)))
                    if (!this.safePlaces.contains(pp))
                        v -= DEATH_VALUE;
            }

            // If I can Bomb, I should do it right now
            if (me.isBombable())
                v += (p_A.t == ActionType.MOVE ? -1 : 1) * me.capa * 10;

            Position pT = p_A.p;
            // Will I Bomb or Am I bombing ?
            if (p_A.t == ActionType.BOMB)
                // bombing right now
                pT = me.p;

            // Accessible positions by bomb
            HashSet<Position> pbombed = pT.getInRangeVH(me.range);
            HashSet<Position> reachable = new HashSet<>();
            HashSet<Position> filteredbox = new HashSet<>();

            for (Position pB : pbombed)
                if (this.s.isReachable(pB, pT))
                {
                    reachable.add(pB);
                    if (this.s.g.isBox(pB))
                        filteredbox.add(pB);
                }

            if (this.safePlaces.contains(pT) && !reachable.containsAll(this.safePlaces))
                v += (this.safePlaces.size() - reachable.size()) * SAFE_PLACE_VALUE;
            else
                v -= DEATH_VALUE * 1000; // We dont want to die right ?

            v += filteredbox.size() * BOX_VALUE;

            v -= p_A.duration(me) * DIST_VALUE;

            this.values.put(p_A, v);
            return v;
        }
    }

    public static class StrPositionHeuristic extends IStrategy
    {

        private static final int DEPTH_MAX = 1;

        public StrPositionHeuristic(int p_id)
        {
            super(p_id);
        }

        @Override
        public Action decide(int p_turn, State p_s)
        {
            return this.decide(p_turn, p_s, 0);
        }

        public Action decide(int p_turn, State p_s, int p_depth)
        {

            Bot me = p_s.p[MY_ID];
            PositionHeuristic ph = new PositionHeuristic(p_turn, p_s);
            PriorityQueue<Action> whatToDo = new PriorityQueue<>(ph);

            ArrayList<Position> aaa = new ArrayList<>();
            aaa.addAll(ph.access);
            NearestComparator c = new NearestComparator(ph.dijkstra);
            Collections.sort(aaa, c);

            // Choose next position
            for (Position pT : aaa)
                // for (Position pT : ph.access)
                whatToDo.add(new Action(ActionType.MOVE, pT));

            // Im dead anyway :/
            if (whatToDo.size() == 1)
                return whatToDo.peek();

            // db("BOOM : ");
            // Exploding this turn :
            for (int i = 1; i <= 8; i++)
                if (p_s.explodingIn.get(i) != null)
                    db(i + " : " + p_s.explodingIn.get(i));

            // Find then best moment to bomb along the way,
            Action a = whatToDo.peek();
            Integer bombingBox = ph.bombValues.get(me.p);
            if (bombingBox == null)
                bombingBox = 0;

            // Bot adverse in range ?
            double distP = 0;
            boolean sureKill = false;
            boolean otherOnSpot = false;
            // for (Bot bb : p_s.p)
            // if (bb.p != null)
            // if (bb.id != me.id && me.p.getInRangeVH(me.range).contains(bb.p) && p_s.isReachable(bb.p, me.p))
            // {
            // // distP += me.p.distanceL1(bb.p);
            // // otherOnSpot |= bb.p.equals(me.p);
            // sureKill = p_s.isLockIfBomb(bb.p, me.p);
            // db("KILL THE OTHER : " + bb + " (" + distP + ")" + sureKill);
            // }

            // When do i get back a bomb ?
            int minLife = 8;
            for (Bomb bb : p_s.b.values())
                if (bb.id == MY_ID && bb.life < minLife)
                    minLife = bb.life;

            int minDistAct = ph.dijkstra.minDist(a.p);
            // Reasons to bomb :
            boolean placeBoxes = (me.capa > 1 && minDistAct > 1) || minDistAct >= minLife || me.p.equals(a.p);

            db("BombV? :" + ph.bombValues);
            db("Bombox? :" + bombingBox + "/" + placeBoxes + " --> " + ph.reachables.get(me.p) + " OR " + distP);
            // Got bomb left for the goal of move or I'm at goal of move
            // or I' in the end game and I'm at distance to kill another player
            if (!ph.reachables.get(me.p).containsAll(ph.safePlaces))
                if ((bombingBox > 0 && placeBoxes) || sureKill
                        || (p_s.g.countBoxes() == 0 && (distP > 0 || otherOnSpot)))
                    // But I'm not THAT stupid, if there is no place left :
                    a.t = ActionType.BOMB;

            // get next turn decision to find enxt decidable position
            Action[] acts = new Action[NB_PLAYER];
            acts[MY_ID] = a;
            State n = p_s.next(p_turn, acts);
            PositionHeuristic phn = new PositionHeuristic(p_turn + 1, n);

            // Verifying if I can accelerate things a bit
            if (p_depth < DEPTH_MAX && a.t == ActionType.MOVE && a.p.equals(me.p))
            {
                db("Moving " + me.p + " -> " + a.p + " " + me + " ++++++++++++++++++++++ HYPOTHETIC STATE");
                Action b = this.decide(p_turn, n, p_depth + 1);
                // Path to next goal
                ArrayList<Position> path = phn.dijkstra.solve(b.p);
                // If first step is safe
                if (path.size() > 1 && phn.s.isSafeXTurn(path.get(1), 2))
                    a.p = path.get(1);
                db("Moving " + me.p + " -> " + a.p + " " + me + " +++++++++++++++++END OF HYPOTHETIC STATE");
            }

            // Verify in next state if I'm not doing anything stupid.
            if (p_depth < DEPTH_MAX && a.t == ActionType.BOMB)

            {
                db("Bombing " + me.p + " -> " + a.p + " " + me + " ---------------------- HYPOTHETIC STATE");
                // db(phn.safePlaces + "\n" + n + "");

                boolean IAmStupid = phn.safePlaces.isEmpty();
                if (!phn.safePlaces.isEmpty())
                {
                    int minDist = phn.dijkstra.minDist(phn.safePlaces.peek());
                    for (int k = 1; k <= minDist + 1; k++)
                        // db("(" + n.p[MY_ID].p + ")(" + k + ") AM I STUPID ???? ---- > "
                        // + !n.isSafeXTurn(n.p[MY_ID].p, k) + "/" + phn.safePlaces);
                        IAmStupid |= !n.isSafeXTurn(n.p[MY_ID].p, k);
                }

                if (IAmStupid)// !n.isSafeXTurn(n.p[MY_ID].p, 3))
                    a.t = ActionType.MOVE;
                else
                {
                    HashSet<Position> unSafeNext = new HashSet<>();
                    for (Position pp : me.p.casesAutour())
                        if (n.isAccessible(pp) && n.isLockIfBomb(pp, me.p))
                            unSafeNext.add(pp);

                    Action b = this.decide(p_turn, n, p_depth + 1);
                    db("NOT Safe NEXT : " + me.p + "--> " + b.p + "?" + unSafeNext);

                    if (!unSafeNext.contains(b.p))
                        a.p = b.p;
                }

                db("Bombing " + me.p + " -> " + a.p + " -------------------------- END OF HYPOTHETIC STATE");
            }

            // Print dbg msg if not release ...
            if (!RELEASE)
                me.msg = "[" + me.p + "]>" + a + "(" + ph.dijkstra.minDist(a.p) + ")";
            else if (a.t == ActionType.BOMB)
                me.msg =

                        getRandomMsg(p_turn);

            // DEBUG
            PriorityQueue<Action> toPrint = new PriorityQueue<>(whatToDo);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; !toPrint.isEmpty() && i < 10; i++)
            {
                Action b = toPrint.poll();
                sb.append(b + "->" + ph.value(b.p) + "\n");
            }

            // db("bombV : " + ph.bombValues);
            db("toDo : " + whatToDo.size() + "\n" + sb);
            // EOD!

            return a;

        }

    }

    public static class PositionHeuristic implements Comparator<Action>
    {
        public static final int SAFE_PLACE_VALUE = 100;
        public static final int BOX_VALUE = 20;
        public static final int KILL_VALUE = 100;
        public static final int[] ITEM_VALUE = { 0, 19, 35 };
        public static final int DIST_VALUE = 6;
        public static final int DEATH_VALUE = -100;
        int t;
        State s;

        HashSet<Position> access;
        PriorityQueue<Position> safePlaces;
        HashMap<Position, HashSet<Position>> reachables;
        HashMap<Position, Double> values;
        HashMap<Position, Integer> bombValues;
        Dijkstra dijkstra;

        public PositionHeuristic(int p_turn, State p_s)
        {
            this.t = p_turn;
            this.s = p_s;

            Bot me = p_s.p[MY_ID];

            int[][] g = new int[Grid.MX][Grid.MY];
            // Compute accessible Pos
            this.access = p_s.accessiblePos(me.p);
            // Find safe ones amongst them
            g[me.p.x][me.p.y] = 1;
            for (Position pT : this.access)
                g[pT.x][pT.y] = 1;

            this.dijkstra = new Dijkstra(g, me.p);

            NearestComparator c = new NearestComparator(this.dijkstra);

            this.safePlaces = new PriorityQueue<>(c);

            for (Position pT : this.access)
                if (p_s.isCompletelySafe(pT))
                    this.safePlaces.add(pT);

            // Memorization of values
            this.values = new HashMap<>();
            this.bombValues = new HashMap<>();
            this.reachables = new HashMap<>();

            // db("Safe : " + this.safePlaces);// + "\n" + this.dijkstra);
        }

        @Override
        public int compare(Action p_o1, Action p_o2)
        {
            // Ordre décroissant :
            return (int) (-1 * Math.round(this.value(p_o1.p) - this.value(p_o2.p)));
        }

        public double value(Position p_p)
        {

            if (this.values.containsKey(p_p))
                return this.values.get(p_p);

            double v = 0.0;
            Bot me = this.s.p[MY_ID];

            // Accessible positions by bomb
            HashSet<Position> pbombed = p_p.getInRangeVH(me.range);
            HashSet<Position> reachable = new HashSet<>();
            HashSet<Position> filteredbox = new HashSet<>();

            for (Position pB : pbombed)
                if (this.s.isReachable(pB, p_p))
                {
                    reachable.add(pB);
                    if (this.s.g.isBox(pB))
                        filteredbox.add(pB);
                }

            // Remove box already bombed but unconcerned by the evaluated position
            for (Bomb bb : this.s.b.values())
            {
                HashSet<Position> bbp = bb.p.getInRangeVH(bb.range);
                if (bb.id == MY_ID || !bbp.contains(p_p))
                    for (Position pp : bbp)
                        if (this.s.isReachable(bb.p, pp))
                            filteredbox.remove(pp);
            }

            this.reachables.put(p_p, reachable);
            // db("Reach : " + p_p + " : " + reachable);

            int dist = this.dijkstra.minDist(p_p);

            if (this.safePlaces.contains(p_p))
                v = SAFE_PLACE_VALUE;

            // boolean safeFor4 = true;
            // for (int i = 0; i < 5; i++)
            // safeFor4 &= this.s.isSafeXTurn(p_p, i);
            // if (safeFor4)
            // v = SAFE_PLACE_VALUE;

            if (this.s.o.containsKey(p_p))
                v += ITEM_VALUE[this.s.o.get(p_p).t];

            // Path to the point
            ArrayList<Position> path = this.dijkstra.solve(p_p);
            for (int i = 0; i < path.size(); i++)
            {
                Position pp = path.get(i);
                // if not safe
                if (!this.s.isSafeXTurn(pp, i + 1))
                    // db(p_p + "(" + pp + (this.s.isSafeXTurn(pp, i + 1) ? ")" : ") NOT") + " SAFE ! for " + i);
                    v += DEATH_VALUE * 100;
                // if items
                if (this.s.o.containsKey(pp))
                    v += ITEM_VALUE[this.s.o.get(pp).t];
            }

            // Bombable in :
            int minBombable = Integer.MAX_VALUE;
            for (Bomb bb : this.s.b.values())
                if (bb.id == MY_ID && bb.life < minBombable)
                    minBombable = bb.life;
            // if (!me.isBombable())
            // db("No bombs but : " + p_p + " -> " + dist + " > " + minBombable);

            // Points for bombing are counted separately
            double v2 = filteredbox.size() * BOX_VALUE;
            for (Position pB : filteredbox)
                if (this.s.g.g[pB.x][pB.y] != Item.EMPTY_BOX)
                    v2 += ITEM_VALUE[this.s.g.whichBox(pB)] / 5;

            if (filteredbox.size() > 0)
                this.bombValues.put(p_p, filteredbox.size() * BOX_VALUE);

            if (reachable.containsAll(this.safePlaces))
                v2 += this.safePlaces.size() * DEATH_VALUE;

            // And added only if we (or will) have bombs
            if (me.isBombable() || dist >= minBombable)
            {
                v += v2;

                if (this.s.g.countBoxes() > 9)
                    v -= dist * DIST_VALUE;
            }

            // END GAME, NO MOAR BOXES
            // if (this.s.g.countBoxes() == 0)
            // for (Bot bb : this.s.p)
            // if (bb.p != null)
            // v += bb.p.distanceL1(p_p);
            // if (p_p.equals(bb.p))
            // v += KILL_VALUE;

            this.values.put(p_p, v);
            return v;
        }
    }

    public static abstract class IStrategy
    {
        int id = -1;

        public IStrategy(int p_id)
        {
            this.id = p_id;
        }

        public abstract Action decide(int p_turn, State p_s);
    }

    public static class Entity
    {
        int id;
        Position p;

        public Entity(int p_id)
        {
            this.id = p_id;
        }

        public void upd(int p_owner, int p_x, int p_y, int p_1, int p_2)
        {
            this.p = new Position(p_x, p_y);

            // Assert ownership.
            if (p_owner != this.id)
                ass("Bad Ownership in Entity Update! " + p_owner + "/" + this.id + ",(" + p_x + "," + p_y + "),[" + p_1
                        + "," + p_2 + "]");
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

    public static class Bot extends Entity
    {
        int capa = 1;
        int range = 3;
        IStrategy s;
        String msg = "";

        public Bot(int p_id, IStrategy p_s)
        {
            super(p_id);
            this.s = p_s;
        }

        @Override
        public Bot clone()
        {
            Bot m = new Bot(this.id, this.s);
            m.p = this.p;
            return m;
        }

        public Action decide(int p_turn, State p_s)
        {
            return this.s.decide(p_turn, p_s);
        }

        public boolean isBombable()
        {
            return this.capa > 0;
        }

        @Override
        public String toString()
        {
            return super.toString() + "(" + this.capa + "," + this.range + ")";
        }

        public String getMsg()
        {
            if (this.msg.isEmpty())
                return "";
            else
                return " " + this.msg;
        }

        @Override
        public void upd(int p_owner, int p_x, int p_y, int p_1, int p_2)
        {
            super.upd(p_owner, p_x, p_y, p_1, p_2);

            this.capa = p_1;
            this.range = p_2 - 1; // -1 ?
            this.msg = "";
        }

    }

    public static class Bomb extends Entity
    {
        static final int MAX_LIFE = 8;
        int turn;
        int life;
        int range;

        public Bomb(int p_turn, int p_id)
        {
            super(p_id);
            this.turn = p_turn;
            this.range = 3;
            this.life = MAX_LIFE;
        }

        @Override
        public void upd(int p_owner, int p_x, int p_y, int p_1, int p_2)
        {
            super.upd(p_owner, p_x, p_y, p_1, p_2);

            this.life = p_1;
            this.range = p_2 - 1; // -1 ?
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return super.toString() + "[t=" + this.turn + ",l=" + this.life + ",r=" + this.range + "]";
        }

    }

    public static class Item extends Entity
    {
        public static final int EMPTY_BOX = 0;
        public static final int RANGE_BOOST = 1;
        public static final int EXTRA_BOMB = 2;

        int t;
        int lastTurn;

        public Item(int p_type)
        {
            super(p_type);
        }

        @Override
        public void upd(int p_owner, int p_x, int p_y, int p_1, int p_2)
        {
            super.upd(p_1, p_x, p_y, p_1, 0);

            this.t = p_1;
            this.lastTurn = p_2;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return super.toString() + "[t=" + this.t + ", lT=" + this.lastTurn + "]";
        }

    }

    public static class Grid
    {
        public static final int VIDE = '.';
        public static final int WALL = 'X';
        public static final int EMPTY_BOX = '0';
        public static final int BOX_UP_RANGE = '1';
        public static final int BOX_UP_BOMB = '2';

        public final static int MY = 13;
        public final static int MX = 11;

        int[][] g = new int[MX][MY];

        public Grid()
        {
            for (int x = 0; x < MX; x++)
                for (int y = 0; y < MY; y++)
                    this.g[x][y] = VIDE;
        }

        public int countBoxes()
        {
            int b = 0;
            for (int x = 0; x < MX; x++)
                for (int y = 0; y < MY; y++)
                    if (this.isBox(new Position(x, y)))
                        b++;

            return b;
        }

        public HashSet<Position> accessiblePos()
        {

            HashSet<Position> s = new HashSet<>();

            for (int x = 0; x < MX; x++)
                for (int y = 0; y < MY; y++)
                {
                    Position p = new Position(x, y);
                    if (this.isAccessible(p))
                        s.add(p);
                }
            return s;
        }

        @Override
        public Grid clone()
        {
            Grid gg = new Grid();
            for (int x = 0; x < MX; x++)
                for (int y = 0; y < MY; y++)
                    gg.g[x][y] = this.g[x][y];

            return gg;
        }

        public boolean isAccessible(Position p_p)
        {
            return this.isInside(p_p) && this.g[p_p.x][p_p.y] == VIDE;
        }

        public boolean isBox(Position p_p)
        {
            return this.isInside(p_p) && (this.g[p_p.x][p_p.y] == EMPTY_BOX || this.g[p_p.x][p_p.y] == BOX_UP_BOMB
                    || this.g[p_p.x][p_p.y] == BOX_UP_RANGE);
        }

        public int whichBox(Position p_p)
        {
            if (this.isBox(p_p))
                switch (this.g[p_p.x][p_p.y])
                {
                    case EMPTY_BOX:
                        return 0;
                    case BOX_UP_BOMB:
                        return 2;
                    case BOX_UP_RANGE:
                        return 1;
                    default:
                        return -1;
                }
            return -1;
        }

        public boolean isInside(Position p_p)
        {
            return p_p.x >= 0 && p_p.x < MX && p_p.y >= 0 && p_p.y < MY;
        }

        public boolean isWall(Position p_p)
        {
            return this.isInside(p_p) && (this.g[p_p.x][p_p.y] == WALL);
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
                        s.append("" + (char) this.g[x][y]);
                    else
                        s.append(" ");
                s.append("|\n|");
            }

            for (int x = 0; x < MY; x++)
                s.append("_");

            s.append("|\n");

            return s.toString();
        }

        public void upd(String[] p_rows)
        {
            int x = 0;
            for (String s : p_rows)
            {
                int y = 0;
                for (char c : s.toCharArray())
                {
                    this.g[x][y] = c;
                    y++;
                }
                x++;
            }

        }

    }

    public static enum ActionType
    {
        MOVE, BOMB;
    }

    public static class Action
    {
        ActionType t;
        Position p;

        public Action(ActionType p_type, Position p_pb)
        {
            this.t = p_type;
            this.p = p_pb;
        }

        @Override
        public String toString()
        {
            return this.t + " " + this.p.x + " " + this.p.y;
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

        public int duration(Bot p_b)
        {
            return p_b.p.distanceL1(this.p);
        }

        // public boolean hasSafePath(WizState p_s)
        // {
        // Bot me = p_s.p[MY_ID];
        // List<Position> path = aStar(me.p, this.p, p_s.g);
        //
        // if (path.isEmpty()) // If no path ... then not safe.
        // return false;
        //
        // boolean safe = true;
        //
        // for (Position pp : path)
        // safe &= p_s.isSafeXurn(pp, me.p.distanceL1(pp));
        //
        // return safe;
        // }
    }

    public static class State
    {

        private static final int PLAYER = 0;
        private static final int BOMB = 1;
        private static final int ITEM = 2;

        Grid g;
        Bot[] p;
        HashMap<Position, Bomb> b;
        HashMap<Position, Item> o;

        HashMap<Integer, HashSet<Bomb>> explodingIn;

        public State(Grid p_g, Bot[] p_bots)
        {
            this.g = p_g;
            this.p = p_bots;
            this.b = new HashMap<>();
            this.o = new HashMap<>();
            this.explodingIn = new HashMap<>();
        }

        public boolean isLockIfBomb(Position p_Bot, Position p_Bomb)
        {
            // Clone all bots
            Bot[] mc = new Bot[this.p.length];
            for (int i = 0; i < this.p.length; i++)
                mc[i] = this.p[i].clone();

            State n = new State(this.g.clone(), mc);
            n.b.putAll(this.b);
            n.o.putAll(this.o);

            // Create hypothetical bomb
            Bomb hypothetical = new Bomb(0, MY_ID);
            hypothetical.upd(MY_ID, p_Bomb.x, p_Bomb.y, Bomb.MAX_LIFE, this.p[MY_ID].range);
            // Add it to state:

            n.b.put(p_Bomb, hypothetical);

            HashSet<Position> access = n.accessiblePos(p_Bot);
            if (access.size() <= 1)
                return true;

            boolean lock = false;
            for (int i = 2; i < 8; i++)
                n.isSafeXTurn(p_Bot, i);

            db("Lock :" + p_Bot + "," + p_Bomb + " --- " + n.explodingIn);

            for (HashSet<Bomb> hb : n.explodingIn.values())
            {
                boolean lockTurn = false;
                for (Bomb bb : hb)
                {
                    // Accessible positions by bomb
                    HashSet<Position> reachable = new HashSet<>();
                    for (Position pB : bb.p.getInRangeVH(bb.range))
                        if (this.isReachable(pB, p_Bomb))
                            reachable.add(pB);

                    lockTurn |= reachable.containsAll(access);
                }
                lock |= lockTurn;
            }
            db("LockIfBomb :" + p_Bot + "," + p_Bomb + " : " + lock);

            return lock;
        }

        public boolean isReachable(Position p_pTarget, Position p_pBomb)
        {
            if (p_pTarget.equals(p_pBomb))
                return true;

            if (p_pBomb.casesAutour().contains(p_pTarget))
                return true;

            HashSet<Position> s = p_pBomb.calculChemin(p_pTarget);
            boolean freePath = !s.isEmpty() && this.g.isInside(p_pBomb);
            for (Position p : s)
                freePath &= this.g.isAccessible(p) && !this.o.containsKey(p) && !this.b.containsKey(p);

            // db("Reachable : " + p_pTarget + "/" + p_pBomb + " : " + s + "->" + freePath);

            return freePath;
        }

        public boolean isAccessible(Position p_p)
        {
            return this.g.isAccessible(p_p) || (this.g.isInside(p_p) && this.o.containsKey(p_p));
        }

        public boolean isBomb(Position p_p)
        {
            return this.b.containsKey(p_p);
        }

        public boolean isCompletelySafe(Position p_p)
        {
            boolean safe = true;
            for (Bomb bb : this.b.values())
                safe &= !bb.p.getInRangeVH(bb.range).contains(p_p) || !this.isReachable(bb.p, p_p);

            // db("Safe : " + p_p + " is " + safe);
            return safe;
        }

        public boolean isSafeXTurn(Position p_p, int p_time)
        {

            boolean safe = true;
            HashSet<Bomb> e = this.explodingIn.get(p_time);

            if (e == null)
            {
                e = new HashSet<>();
                // Which bomb explose ?
                for (Bomb bb : this.b.values())
                    if (bb.life == p_time)
                        e.add(bb);

                // db(p_time + " : " + this.b.values() + "/" + e);

                // Check if Chain reaction
                int boom = e.size();
                int old_boom = 0;
                while (boom != old_boom)
                {
                    HashSet<Bomb> alsoBoom = new HashSet<>();
                    old_boom = boom;

                    for (Bomb bb : e)
                        for (Bomb bb2 : this.b.values())
                            if (!bb.equals(bb2) && bb.p.getInRangeVH(bb.range).contains(bb2.p)
                                    && this.isReachable(bb.p, bb2.p))
                                alsoBoom.add(bb2);

                    e.addAll(alsoBoom);
                    boom = e.size();
                }

                // db(p_time + " : " + this.b.values() + "/" + e);
                this.explodingIn.put(p_time, e);
            }
            for (int i = p_time + 1; i <= Bomb.MAX_LIFE; i++)
                if (this.explodingIn.get(i) != null)
                    this.explodingIn.get(i).removeAll(e);

            // if (!e.isEmpty() && e.iterator().next().turn >= 105)
            // db(this.p[MY_ID].p + "(" + p_p + (safe ? ")" : ") NOT") + " SAFE ! for " + (p_time - 1) + " |e = " + e);

            // All in e explose !
            for (Bomb bb : e)
            {
                safe &= !bb.p.getInRangeVH(bb.range).contains(p_p) || !this.isReachable(bb.p, p_p);
                safe &= bb.life != p_time + 1;
            }
            // || bb.life != p_time + 1);

            // if (safe && !e.isEmpty() && e.iterator().next().turn == 78)
            // db(this.p[MY_ID].p + "(" + p_p + (safe ? ")" : ") NOT") + " SAFE ! for " + (p_time - 1) + " |e = " + e);

            // if (!safe)
            // db(this.p[MY_ID].p + "(" + p_p + (safe ? ")" : ") NOT") + " SAFE ! for " + (p_life - 1) + " |e = " + e);
            // db(p_p + (safe ? "" : " NOT") + " SAFE ! for " + p_life);

            return safe;
        }

        public HashSet<Position> accessiblePos(Position p_p)
        {
            HashSet<Position> s = new HashSet<>();

            int[][] c = new int[Grid.MX][Grid.MY];
            for (int i = 0; i < c.length; i++)
                for (int j = 0; j < c[0].length; j++)
                {
                    Position p = new Position(i, j);
                    if (this.o.containsKey(p))
                        c[i][j] = Grid.VIDE;
                    else if (this.b.containsKey(p))// && !this.p[MY_ID].p.equals(p))
                        c[i][j] = 'B';
                    else
                        c[i][j] = this.g.g[i][j];
                }

            fillArea(p_p.x, p_p.y, Grid.VIDE, ' ', c);

            for (int i = 0; i < c.length; i++)
                for (int j = 0; j < c[0].length; j++)
                {
                    Position p = new Position(i, j);
                    if (c[i][j] == ' ')
                        s.add(p);
                }

            // DEBUG !
            // StringBuilder sb = new StringBuilder();
            // for (int[] element : c)
            // {
            // for (int y = 0; y < c[0].length; y++)
            // sb.append((char) element[y]);
            // sb.append("\n");
            // }
            //
            // db(this.p[MY_ID].p + "\n" + sb.toString());
            // EOD !

            return s;
        }

        public SortedSet<Position> nearestBoxes(Position p_p, int p_range)
        {

            NearestComparator c = new NearestComparator(p_p, Distance.MANHATTAN);
            SortedSet<Position> s = new TreeSet<Position>(c);

            for (Position p : p_p.getInRangeL1(p_range))
                if (this.g.isInside(p) && this.g.isBox(p))
                    s.add(p);

            return s;
        }

        public ArrayList<Direction> getAcc(int p_id)
        {
            ArrayList<Direction> possible = new ArrayList<>();
            Position p = this.getPos(p_id);

            for (Direction d : Direction.values())
                if (this.g.isAccessible(p.relativePos(d)))
                    possible.add(d);
            return possible;
        }

        public Position getPos(int p_id)
        {
            return this.p[p_id].p;
        }

        public State next(int p_t, Action[] p_act)
        {

            // Clone all bots
            Bot[] mc = new Bot[this.p.length];
            for (int i = 0; i < this.p.length; i++)
                mc[i] = this.p[i].clone();

            State n = new State(this.g.clone(), mc);
            n.b.putAll(this.b);
            n.o.putAll(this.o);

            HashSet<Bomb> exploded = new HashSet<>();
            // Bombing if BOMB in time
            for (Bomb bb : this.b.values())
                if (bb.life == 1)
                {
                    n.b.remove(bb.p);
                    exploded.add(bb);
                    db("Bomb : " + bb + "should explode now.");
                }

            // Move of me:
            Position op = mc[MY_ID].p;
            Position np = p_act[MY_ID].p;
            if (!p_act[MY_ID].p.equals(op) && !op.casesAutour().contains(p_act[MY_ID].p))
            {
                Grid gg = this.g.clone();
                for (Bomb bb : this.b.values())
                    gg.g[bb.p.x][bb.p.y] = Grid.WALL;

                // db("A* Grid : \n" + gg);
                List<Position> lp = aStar(op, p_act[MY_ID].p, gg);
                db("A* : " + op + "->" + p_act[MY_ID].p + " : " + lp);
                np = lp.get(0);
            }

            n.p[MY_ID].p = np;

            if (p_act[MY_ID].t == ActionType.BOMB)
                n.upd(p_t + 1, BOMB, MY_ID, op.x, op.y, Bomb.MAX_LIFE, mc[MY_ID].range);
            // TODO Move and actions of others ...maybe above in a single loop

            // update of grid as needed
            for (Bomb bb : exploded) // for each bomb
                for (Position pB : bb.p.getInRangeVH(bb.range))
                    if (n.isReachable(bb.p, pB))
                        if (n.g.isBox(pB)) // if a box is touched
                        {
                            // add of objects.
                            n.upd(p_t + 1, ITEM, 0, pB.x, pB.y, n.g.whichBox(pB), 0);
                            // removing of boxes.
                            n.g.g[pB.x][pB.y] = Grid.VIDE;
                        }

            return n;
        }

        @Override
        public String toString()
        {
            char[][] c = new char[Grid.MX][Grid.MY];
            for (int i = 0; i < c.length; i++)
                for (int j = 0; j < c[0].length; j++)
                    c[i][j] = (char) this.g.g[i][j];

            // for (int i = 0; i < this.p.length; i++)
            // c[this.p[i].p.x][this.p[i].p.x] = ((this.p[i].id) + "").charAt(0);

            for (Bomb bb : this.b.values())
                c[bb.p.x][bb.p.y] = 'B';

            StringBuilder s = new StringBuilder();
            for (char[] element : c)
            {
                for (int y = 0; y < c[0].length; y++)
                    s.append(element[y]);
                s.append("\n");
            }

            return s.toString();
        }

        public void upd(int p_turn, String[] p_rows)
        {
            this.g.upd(p_rows);

            HashSet<Position> toRemove = new HashSet<>();
            for (Bomb bb : this.b.values())
                if (bb.turn + Bomb.MAX_LIFE != p_turn + bb.life)
                    toRemove.add(bb.p);
            this.b.keySet().removeAll(toRemove);
            toRemove.clear();

            for (Item it : this.o.values())
                if (it.lastTurn != p_turn)
                    toRemove.add(it.p);
            this.o.keySet().removeAll(toRemove);

            // this.b.removeIf(b -> b.turn + Bomb.MAX_LIFE != p_turn + b.life);
            // this.o.removeIf(i -> i.lastTurn < p_turn + 1);
            // db("Bombs:" + this.b);
            // db("Items:" + this.o);
            // db("Bots:" + Arrays.toString(this.p));
        }

        public void upd(int p_turn, int p_type, int p_owner, int p_x, int p_y, int p_1, int p_2)
        {
            this.explodingIn = new HashMap<>();
            // db("UPD: " + p_type + "|" + p_owner + "[" + p_x + "," + p_y + "],[" + p_1 + "," + p_2 + "]");
            switch (p_type)
            {
                case PLAYER: // Hypersonic
                    this.p[p_owner].upd(p_owner, p_x, p_y, p_1, p_2);
                    break;
                case BOMB: // Bomb
                    Position pb = new Position(p_x, p_y);
                    Bomb nb = this.b.get(pb);
                    if (nb == null)
                    {
                        nb = new Bomb(p_turn, p_owner);
                        this.b.put(pb, nb);
                    }

                    // TODO MultiBomb on place.
                    nb.upd(p_owner, p_x, p_y, p_1, p_2);

                    break;
                case ITEM: // item
                    Position pi = new Position(p_x, p_y);
                    Item ni = this.o.get(pi);
                    if (ni == null)
                    {
                        ni = new Item(p_1);
                        this.o.put(pi, ni);
                    }

                    ni.upd(p_1, p_x, p_y, p_1, p_turn);
                    break;
                default:
                    ass("Wrong type of Entity !");
            }

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
        VH, MANHATTAN, CHEBYSHEV, EUCLIDEAN, INFTY;

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
                case VH:
                    return ((p_p1.y == p_p2.y) && Math.abs(p_p1.x - p_p2.x) <= p_r)
                            || ((p_p1.x == p_p2.x) && Math.abs(p_p1.y - p_p2.y) <= p_r);
                case MANHATTAN:
                    return p_p1.distanceL1(p_p2) <= p_r;
                case CHEBYSHEV:
                    return p_p1.distanceL15(p_p2) <= p_r;
                case INFTY:
                    return p_p1.distanceLI(p_p2) <= p_r;
                default:
                    return p_p1.distanceL2(p_p2) <= p_r;
            }
        }

        public double val(Position p_p1, Position p_p2)
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
                case INFTY:
                    return p_p1.distanceLI(p_p2);
                default:
                    return p_p1.distanceL2(p_p2);
            }
        }
    }

    public static class NearestComparator implements Comparator<Position>
    {
        Position ref = null;
        Distance d = Distance.EUCLIDEAN;
        Dijkstra di = null;

        public NearestComparator(Position p_ref, Distance p_d)
        {
            this.ref = p_ref;
            this.d = p_d;
        }

        public NearestComparator(Dijkstra p_dijkstra)
        {
            this.di = p_dijkstra;
        }

        @Override
        public int compare(Position p_o1, Position p_o2)
        {
            if (this.ref == null)
            {
                if (this.di == null)
                    ass("Both cant be null !!!");

                double d1 = this.d.val(this.di.start, p_o1);
                double d2 = this.d.val(this.di.start, p_o2);

                if (this.di.minDist(p_o1) - this.di.minDist(p_o2) == 0)
                    return (int) Math.round(d1 - d2);

                return this.di.minDist(p_o1) - this.di.minDist(p_o2);
            }
            double d1 = this.d.val(this.ref, p_o1);
            double d2 = this.d.val(this.ref, p_o2);
            return (int) Math.round(d1 - d2);
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

        public HashSet<Position> getInRangeVH(int p_r)
        {
            return this.getInRange(p_r, Distance.VH);
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
            result = -1 * prime * result + this.x;
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

        /**
         * Calcule le chemin direct entre deux positions (i.e. l'ensemble des positions) s'il existe.
         *
         * @param p_position
         * @param p_position2
         * @return l'ensemble des positions entre pos1 et pos2
         */
        public HashSet<Position> calculChemin(Position p_position2)
        {
            HashSet<Position> s = new HashSet<Position>();
            Direction d = this.relativeDir(p_position2);
            Position p = this.relativePos(d);

            double maxPas = this.distanceL2(p_position2);
            int pas = 0;

            while (!p.equals(p_position2) && pas < maxPas)
            {
                s.add(p);
                p = p.relativePos(d);
                pas++;
            }

            if (pas < maxPas)
                return s;
            else
                return new HashSet<Position>();
        }
    }

    public static class Vector2D extends Point2D.Double
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
        LinkedList<Position> path = new LinkedList<>();

        TreeMap<AStar2DNode, Double> open = new TreeMap<>();
        HashSet<AStar2DNode> close = new HashSet<>();
        open.put(new AStar2DNode(p_o, 0, 0), 0d);

        // Find path to goal
        while (!open.isEmpty() && !open.firstKey().p.equals(p_g))
        {
            AStar2DNode current = open.pollFirstEntry().getKey();
            close.add(current);

            // db("A*debug: c:" + current);
            // db("A*debug: C:" + close);
            // db("A*debug: O:" + open);

            for (Position p : current.p.casesAutour())
                // db("Considering ... " + p + " ? " + p_map.isAccessible(p));
                if (p_map.isAccessible(p))
                {
                    int cost = current.g + 1;
                    AStar2DNode neighbor = new AStar2DNode(p, cost, p.distanceL1(p_g));
                    neighbor.prev = current;

                    // db(p + ":" + neighbor + "?" + open.containsKey(neighbor) + " ?! " + close.contains(neighbor));
                    if (open.containsKey(neighbor) && (open.get(neighbor) - neighbor.h) > neighbor.g)
                        open.remove(neighbor);

                    if (!open.containsKey(neighbor) && !close.contains(neighbor))
                        open.put(neighbor, 1.01 * neighbor.h + neighbor.g);
                    // db("A*debug: O:" + open);
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

    public static class AStar2DNode implements Comparable<AStar2DNode>
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

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "[" + this.p + "|" + this.g + "+" + this.h + "]";
        }

        @Override
        public int compareTo(AStar2DNode p_o)
        {
            if (((this.g + this.h) - (p_o.g + p_o.h)) == 0)
                return 1;
            else
                return (this.g + this.h) - (p_o.g + p_o.h);

        }
    }

    public static class Dijkstra
    {
        private int[][] grid;
        private int[][] maze;
        public Position start;

        private HashMap<Position, ArrayList<Position>> m = new HashMap<>();

        /**
         * Constructeur
         *
         * @param maze
         *            Where all accessible positions are non-zero.
         * @param start
         *            Starting Position
         */
        public Dijkstra(int[][] maze, Position start)
        {
            this.maze = maze;
            this.start = start;
            this.grid = new int[maze.length][maze[0].length];

            for (int i = 0; i < this.grid.length; i++)
                for (int j = 0; j < this.grid[0].length; j++)
                    if (maze[i][j] == 0)
                        this.grid[i][j] = -1;
                    else
                        this.grid[i][j] = Integer.MAX_VALUE;

            this.grid[start.x][start.y] = 0;
            boolean modif = true;
            while (modif)
            {
                modif = false;
                for (int i = 0; i < this.grid.length; i++)
                    for (int j = 0; j < this.grid[0].length; j++)
                        if (this.grid[i][j] < Integer.MAX_VALUE && this.grid[i][j] > -1)
                        {
                            Position now = new Position(i, j);
                            for (Position a : now.casesAutour())
                                if (a.x >= 0 && a.y >= 0 && a.x < this.grid.length && a.y < this.grid[0].length)
                                    if (this.maze[a.x][a.y] != 0 && this.grid[a.x][a.y] > this.grid[now.x][now.y] + 1)
                                    {
                                        this.grid[a.x][a.y] = this.grid[now.x][now.y] + 1;
                                        modif = true;
                                    }
                        }
            }

            // db(this.toString());
            // Compute all distances to from the start
            // int maxX = this.grid.length;
            // int maxY = this.grid[0].length;
            // Stack<Position> stack = new Stack<>();
            // this.start = start;
            // stack.push(start);
            // this.grid[start.x][start.y] = 0;
            //
            // while (!stack.isEmpty())
            // {
            // Position c = stack.pop();
            // for (Position a : c.casesAutour())
            // if (a.x >= 0 && a.y >= 0 && a.x < maxX && a.y < maxY)
            // if (this.grid[a.x][a.y] == Integer.MAX_VALUE || this.grid[a.x][a.y] > this.grid[a.x][a.y] + 1)
            // {
            // int min = Integer.MAX_VALUE;
            // for (Position aa : a.casesAutour())
            // if (aa.x >= 0 && aa.y >= 0 && aa.x < maxX && aa.y < maxY && this.grid[aa.x][aa.y] >= 0
            // && this.grid[aa.x][aa.y] < min)
            // min = this.grid[aa.x][aa.y];
            //
            // this.grid[a.x][a.y] = min + 1;
            //
            // stack.push(a);
            // }
            // }
        }

        /**
         * Tiens Toé !
         *
         * @return
         */
        public ArrayList<Position> solve(Position p_goal)
        {
            if (this.m.containsKey(p_goal))
                return this.m.get(p_goal);

            ArrayList<Position> path = new ArrayList<Position>();
            Position curr = p_goal;

            int v = this.grid[p_goal.x][p_goal.y];

            if (v == Integer.MAX_VALUE)
                return path;

            // db("Solve : " + p_goal + "->" + v + " -- " + path);
            while (v > 0)
                for (Position nex : curr.casesAutour())
                {
                    Position next = nex;

                    // db("Solve : " + p_goal + "->(" + next + "?)" + v + " -- " + path);
                    if (next.x >= 0 && next.y >= 0 && next.x < this.grid.length && next.y < this.grid[0].length
                            && this.grid[next.x][next.y] == v - 1)
                    {
                        path.add(next);

                        v -= 1;
                        curr = next;
                        // db("Solve : " + p_goal + "->" + v + "," + path);
                        break;
                    }
                }
            Collections.reverse(path);
            this.m.put(p_goal, path);

            return path;
        }

        /**
         * Return the minimum distance to go to goal;
         *
         * @param p_goal
         * @return
         */
        public int minDist(Position p_goal)
        {
            return this.grid[p_goal.x][p_goal.y];
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
            // for (int[] element : this.maze)
            // {
            // for (int j = 0; j < this.maze[0].length; j++)
            // s.append((element[j] == 0 ? " " : "#"));
            // s.append("\n");
            // }
            // s.append("\n");
            for (int i = 0; i < this.maze.length; i++)
            {
                for (int j = 0; j < this.maze[0].length; j++)
                    s.append((this.grid[i][j] < 0 ? "#" : (this.grid[i][j] >= 10 ? "Z" : (this.grid[i][j]) + "")));
                s.append("\n");
            }

            return s.toString();
        }
    }

}
