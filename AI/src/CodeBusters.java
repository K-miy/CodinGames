import java.awt.geom.Point2D;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Send your busters out into the fog to trap ghosts and bring them home!
 **/
class CodeBusters
{

    public static final Scanner in = new Scanner(System.in);

    final static int MAX_MOVE = 800;
    final static int MAX_STUN = 1760;
    final static int MIN_BUST = 900;
    final static int MAX_BUST = 1760;
    final static int MEAN_BUST = (MAX_BUST + MIN_BUST) / 2;
    final static int MIN_RELEASE = 1600;

    final static int FORGET_IT = 1;

    final static boolean DEBUG = false;

    final static Position[] HOMES = new Position[2];
    final static Position[] CHIANTS = new Position[2];

    final static String[] messages = { "We got one.", "I’m always serious.", "We believe you!", "I collect spores.",
            "We both have the same problem: YOU.", "We got one!", "I’ll take the next one.", "He slimed me.",
            "I feel so funky.", "He got slimed!", "Nice shooting, Tex!", "Ghostbusters, whaddya want?",
            "You will perish in flame!", "I don’t think he’s human.", "YOUR MOTHER!!!",
            "The dead rising from the grave!", "We’re the Ghostbusters.", "I love this town!" };

    static final int MAX_GHOST = 400;

    final static Random r = new Random();

    List<List<Buster>> teams;
    HashMap<Integer, Ghost> ghosts = new HashMap<>();
    HashMap<Integer, Buster> bus = new HashMap<>();
    HashMap<Buster, Action> act = new HashMap<>();
    int myTeamId;
    int ghostCount;

    Position home;

    GridExploration ge;
    ATeamCoordinator tc;

    public void arun()
    {
        HOMES[0] = new Position(0, 0);
        HOMES[1] = new Position(16000, 9000);
        CHIANTS[1] = new Position(1800, 1800);
        CHIANTS[0] = new Position(16000 - 1800, 9000 - 1800);

        int bustersPerPlayer = in.nextInt(); // the amount of busters you control
        this.ghostCount = in.nextInt(); // the amount of ghosts on the map
        this.myTeamId = in.nextInt(); // if this is 0, your base is on the top left of the map, if it is one, on the
                                      // bottom right

        this.home = HOMES[this.myTeamId];

        this.ge = new GridExploration(this.myTeamId);

        this.teams = new ArrayList<>();
        this.teams.add(new ArrayList<Buster>());
        this.teams.add(new ArrayList<Buster>());

        this.tc = new ATeamCoordinator(this.teams.get(this.myTeamId));

        int tour = 0;

        String[] message = new String[bustersPerPlayer];
        // Tour préparatoire ... plus tard.
        // this.read(tour);
        // tour++;

        // game loop
        while (true)
        {
            this.read(tour);
            this.ge.upd();
            this.tc.decide(tour);

            // sep(this.job);

            for (int i = 0; i < bustersPerPlayer; i++)
            {
                Buster b = this.teams.get(this.myTeamId).get(i);
                Action a = b.decide();

                this.act.put(b, a);

                if (!DEBUG && tour % 5 == 0)
                    message[i] = " " + messages[r.nextInt(messages.length)];
                if (DEBUG)
                    message[i] = " [" + b.id + "]";

                System.out.println(a + message[i]);
            }

            this.act.clear();
            tour++;
        }
    }

    public void read(int p_t)
    {
        int entities = in.nextInt(); // the number of busters and ghosts visible to you
        for (int i = 0; i < entities; i++)
        {
            int entityId = in.nextInt(); // buster id or ghost id

            int x = in.nextInt();
            int y = in.nextInt(); // position of this buster / ghost
            int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
            int state = in.nextInt(); // For busters: 0=idle, 1=carrying a ghost.
            int value = in.nextInt(); // For busters: Ghost id being carried. For ghosts: number of busters
                                      // attempting to trap this ghost.

            Entity e = null;

            if (entityType == -1)
                e = this.ghosts.get(entityId);
            else
                e = this.bus.get(entityId);

            if (e == null)
                if (entityType != -1)
                {
                    e = new Buster(entityId, entityType, new AGreedy());
                    this.bus.put(entityId, (Buster) e);
                    this.teams.get(entityType).add((Buster) e);
                }
                else
                {
                    e = new Ghost(entityId);
                    this.ghosts.put(entityId, (Ghost) e);
                }

            e.upd(x, y, state, value, p_t);

            if (e instanceof Buster && ((Buster) e).isCarrying())
                this.ghosts.remove(((Buster) e).ghid);

            sep("[" + p_t + "]" + e);
        }

        // Job map does not update strange ...
        for (Entry<Buster, Ghost> e : this.tc.job.entrySet())
            if (e.getValue() != null && this.ghosts.get(e.getValue().id) != null
                    && this.ghosts.get(e.getValue().id).lastupd != e.getValue().lastupd)
                this.tc.job.put(e.getKey(), this.ghosts.get(e.getValue().id));
        sep(this.ghosts);
    }

    public static void main(String args[])
    {
        new CodeBusters().arun();
    }

    public static void sep(Object s)
    {
        if (DEBUG)
            System.err.println(s);
    }

    public class Action
    {
        ActionType t;
        Position moveTo;
        int bustId = -1;

        public Action()
        {
            this.t = ActionType.RELEASE;
        }

        public Action(int p_id, boolean p_bust)
        {
            if (p_bust)
            {
                this.t = ActionType.BUST;
                this.bustId = p_id;
            }
            else
            {
                this.t = ActionType.STUN;
                this.bustId = p_id;
            }
        }

        public Action(Position p)
        {
            this.t = ActionType.MOVE;
            this.moveTo = p;
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
            if (this.bustId != other.bustId)
                return false;
            if (this.moveTo == null)
            {
                if (other.moveTo != null)
                    return false;
            }
            else if (!this.moveTo.equals(other.moveTo))
                return false;
            if (this.t != other.t)
                return false;
            return true;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.bustId;
            result = prime * result + ((this.moveTo == null) ? 0 : this.moveTo.hashCode());
            result = prime * result + ((this.t == null) ? 0 : this.t.hashCode());
            return result;
        }

        @Override
        public String toString()
        {
            switch (this.t)
            {
                case MOVE:
                    return this.t + " " + this.moveTo;
                case BUST:
                    return this.t + " " + this.bustId;
                case RELEASE:
                    return this.t + "";
                case STUN:
                    return this.t + " " + this.bustId;
            }
            return "";
        }

    }

    public enum ActionType
    {
        MOVE, BUST, RELEASE, STUN;
    }

    public class AGreedy extends IStrategy
    {
        AGreedyState s = AGreedyState.LIBRE;

        Position pToR = null; // Position to Reach in exploration

        Action lastA = null;

        Action advCarrierStunnable()
        {
            Action a = null;
            for (Buster o : CodeBusters.this.teams.get(1 - CodeBusters.this.myTeamId))
            {
                Action toDo = new Action(o.id, false);

                if (this.b.canStun() && o.isCarrying()) // Je peux stun un carrier adverse
                    if (this.b.lastupd - o.lastupd < 1) // son information est à jour
                        if (o.p.distanceL2(this.b.p) < MAX_STUN && !CodeBusters.this.act.containsValue(toDo))
                        // il est à distance et personne le stun deja
                        {
                            sep(this.b.id + ": Libre et toi tu carry " + o.id + " ? dantayeule !");
                            this.pToR = o.p;
                            return toDo;
                        }
                        else
                        {
                            Vector2D oHome = new Vector2D(HOMES[1 - CodeBusters.this.myTeamId]); // other home
                            Vector2D op = new Vector2D(o.p); // old position
                            Vector2D wayBack = oHome.minus(op); // Objective
                            Vector2D ov = wayBack.unitVector().scalarMult(800); // speed

                            // estimate position at current turn
                            Vector2D ep = op.plus(ov.scalarMult(this.b.lastupd - o.lastupd));

                            if (new Position(ep).distanceL2(HOMES[1 - CodeBusters.this.myTeamId]) > this.b.p
                                    .distanceL2(HOMES[1 - CodeBusters.this.myTeamId]))// intercept way back
                            {
                                sep(this.b.id + ": INTERCEPTING " + o.id + " !");
                                return new Action(CHIANTS[CodeBusters.this.myTeamId]);
                            }
                        }
            }

            return a;
        }

        Action advStunnable(boolean p_occ)
        {
            Action a = this.advCarrierStunnable();

            // sep(CodeBusters.this.teams.get(1 - CodeBusters.this.myTeamId));
            if (a != null)
                return a;

            for (Buster o : CodeBusters.this.teams.get(1 - CodeBusters.this.myTeamId))
            {
                Action toDo = new Action(o.id, false);

                if (!CodeBusters.this.act.containsValue(toDo))
                    if ((this.b.lastupd - o.lastupd) < 1 && o.p.distanceL2(this.b.p) < MAX_STUN && this.b.canStun()
                            && !o.isStun())
                    {
                        if (!p_occ)
                            if ((this.lastA != null && !CHIANTS[CodeBusters.this.myTeamId].equals(this.lastA.moveTo))
                                    || this.noReasonToKeepStun())
                            // Non occupé mais en train de me diriger vers la zone d'interception
                            {
                                sep(this.b.id + ": Libre donc dantayeule !");
                                return toDo;
                            }
                            else
                                sep(this.b.id
                                        + ": Chanceux! je ne t'ai pas stun parce que j'allais attendre un de tes amis à son retour :-P");

                        if (this.b.isCarrying())
                        {
                            sep(this.b.id + ": J'en ramenais un et tu m'attendais ? dantayeule !");
                            return toDo;
                        }

                        if (p_occ && o.isCarrying()) // occupé mais l'adversaire a un fantome dans les bras
                        {
                            sep(this.b.id + ": occupé mais t'en a un donc dantayeule !");
                            this.pToR = o.p;
                            return toDo;
                        }

                        if (this.b.lastupd > 75 && CodeBusters.this.tc.job.get(this.b) != null)
                            sep("##### # #" + this.b.id + " : gid:" + CodeBusters.this.tc.job.get(this.b).id + " sta:"
                                    + CodeBusters.this.tc.job.get(this.b).stamina + "/"
                                    + (CodeBusters.this.tc.job.get(this.b).nbOnIt * 5) + " o : " + o.id + " gid:" + o.ghid
                                    + " t?" + o.isTrapping());

                        if (o.isTrapping() // Les deux catch le meme
                                && CodeBusters.this.tc.job.get(this.b) != null && CodeBusters.this.tc.job.get(this.b).id == o.ghid
                                && CodeBusters.this.tc.job.get(this.b).stamina < (CodeBusters.this.tc.job.get(this.b).nbOnIt * 5))
                        // et il est bientot attrapé.
                        {
                            sep(this.b.id + ": Lache MON fantome ! dantayeule !");
                            return toDo;
                        }
                    }
            }

            return a;
        }

        Action backHome()
        {
            Vector2D bpos = new Vector2D(this.b.p);
            Vector2D hpos = new Vector2D(CodeBusters.this.home);
            Vector2D v = hpos.minus(bpos);
            double h = v.getR();
            v = v.unitVector().scalarMult(h - MIN_RELEASE + 10);
            Vector2D res = bpos.plus(v);
            return new Action(new Position(res));
        }

        @Override
        public Action decide()
        {
            if (this.b.stunLeft > 0)
                this.s = AGreedyState.STUNNED;
            else if (this.b.isCarrying())
                this.s = AGreedyState.CHARGE;
            else if (CodeBusters.this.tc.job.get(this.b) != null)
                this.s = AGreedyState.OCCUPE;
            else
                this.s = AGreedyState.LIBRE;

            Ghost g = CodeBusters.this.tc.job.get(this.b);

            // NMumber of my own on g
            int count = 0;
            for (Ghost gg : CodeBusters.this.tc.job.values())
                if (gg != null && gg.equals(g))
                    count++;

            sep(this.b + " --> " + this.s + " ==> " + g + "(" + (g == null ? "0" : g.p.distanceL2(this.b.p)) + ")");

            Action a = null;
            switch (this.s)
            {
                case OCCUPE: // Assigné à un fantome non capturé.
                    // Si buster affecté va le capturer.
                    double d = g.p.distanceL2(this.b.p);
                    // Au cas ou on croise un adversaire chargé
                    a = this.advStunnable(true);
                    if (a != null)
                        break;

                    if (d > MAX_BUST || d < MIN_BUST) // Pas à distance
                    {
                        Vector2D gpos = new Vector2D(g.p);
                        if (g.p.equals(this.b.p))
                            gpos = new Vector2D(CodeBusters.this.home);

                        Vector2D bpos = new Vector2D(this.b.p);
                        Vector2D v = gpos.minus(bpos).unitVector().scalarMult(d - MEAN_BUST);
                        Vector2D res = bpos.plus(v);
                        a = new Action(new Position(res));
                    }
                    else if (this.b.lastupd - g.lastupd > FORGET_IT) // A distance mais le fantome n'est plus la ...
                    {
                        CodeBusters.this.tc.job.remove(this.b); // On se libère de la job
                        CodeBusters.this.ghosts.remove(g.id); // On dit que le ghost n'existe plus jusqu'à qu'on le revoie
                        return this.decide();
                    }
                    else if (g.nbOnIt - count > 0 && g.stamina <= 2 * g.nbOnIt)
                    // plus de 5 tours à 0 ... blocage avec autre team ou recup après stun
                    {
                        for (Buster o : CodeBusters.this.teams.get(1 - CodeBusters.this.myTeamId))
                            if (o.lastupd - this.b.lastupd < 1 && o.isTrapping() && o.ghid == this.b.ghid)
                            {
                                a = new Action(o.p);
                                break;
                            }
                        if (a == null)
                            a = new Action(CHIANTS[CodeBusters.this.myTeamId]);
                    }

                    else
                        // On capture
                        a = new Action(g.id, true);
                    break;
                case CHARGE:
                    // Si buster aevc ghost capturé, va le libérer dans la zone autorisée.

                    // J'ai attrapé ma cible, je l'enlève des fantomes libres connus
                    if (this.b.ghid != g.id)
                        sep("ERREUR MONUMENTALE ! JE N'AI PAS ATTRAPÉ LE BON FANTOME ! :-/");
                    CodeBusters.this.ghosts.remove(this.b.ghid);

                    a = this.advStunnable(true);

                    if (a != null)
                        break;
                    else if (!this.b.isHome())
                        a = this.backHome();
                    else // Sinon RELEASE ! and remove job !
                    {
                        a = new Action();
                        CodeBusters.this.tc.job.remove(this.b);
                    }

                    break;
                case STUNNED:
                    CodeBusters.this.tc.job.remove(this.b);

                default:// case LIBRE ou STUN: on explore et on stun dès qu'on peut !
                    // Si Buster Libre (Aucun ghost affecté)
                    a = this.advStunnable(false); // kkun a portée à assomer ?

                    if (a == null)     // sinon on se balade.
                    {
                        this.pToR = CodeBusters.this.ge.nearestNext(this.b.p);

                        double r = this.pToR.distanceL2(this.b.p);
                        if (r < 200)
                            this.pToR = CodeBusters.this.ge.nearestNext(this.b.p);
                        a = new Action(this.pToR);
                    }
                    else // Si je compte stun un joueur qui carry, je me rajoute sa job.
                    if (a.moveTo == null && CodeBusters.this.bus.get(a.bustId).isCarrying())
                        CodeBusters.this.tc.job.put(this.b, CodeBusters.this.ghosts.get(CodeBusters.this.bus.get(a.bustId).ghid));

                    // sep(this.b + "|" + this.s + "=" + g + "-->" + a);
            }

            this.lastA = a;
            return a;
        }

        boolean noReasonToKeepStun()
        {
            boolean noReason = true;

            // un ami en attente chez l'ennemi
            for (Buster n : CodeBusters.this.teams.get(CodeBusters.this.myTeamId))
                noReason |= (n.p.distanceL2(CHIANTS[CodeBusters.this.myTeamId]) < 50 && n.canStun());

            // C'est la fin de la game dans 40 tours.
            noReason |= this.b.lastupd > 160;

            return noReason;
        }

    }

    public enum AGreedyState
    {
        LIBRE, OCCUPE, CHARGE, STUNNED;
    }

    public class ASoldier extends AGreedy
    {

    }

    public class ATeamCoordinator
    {
        List<Buster> team;
        HashMap<Buster, Ghost> job = new HashMap<>();

        Buster chieur = null;

        public ATeamCoordinator(List<Buster> p_list)
        {
            this.team = p_list;
        }

        public void decide(int p_t)
        {
            if (p_t == 0) // first tour explore.
                return;

            for (Buster b : this.team)
                if (this.job.get(b) == null)
                    this.job.remove(b);

            TreeMap<Double, Entry<Buster, Ghost>> l = new TreeMap<>();

            HashMap<Ghost, Integer> count = new HashMap<>();
            for (Buster b : this.team)
                for (Ghost g : CodeBusters.this.ghosts.values())
                {
                    AbstractMap.SimpleEntry<Buster, Ghost> e = new AbstractMap.SimpleEntry<Buster, Ghost>(b, g);

                    boolean bHasjob = this.job.containsKey(b);
                    double d = b.p.distanceL2(g.p);

                    if (bHasjob)
                    {
                        Ghost currentG = this.job.get(b);
                        // Nouveau fantome plus proche que sa job actuelle
                        if (b.p.distanceL2(currentG.p) > d && b.st == BusterState.MOVN)
                            this.job.remove(b);
                    }

                    l.put(d + 10 * e.getValue().stamina, e);
                    count.put(g, 0);
                }

            while (l.size() > 0)
            {

                Entry<Double, Entry<Buster, Ghost>> ee = l.pollFirstEntry();
                Entry<Buster, Ghost> e = ee.getValue();
                // if (!this.job.containsKey(e.getKey()) && !this.job.containsValue(e.getValue()) &&
                // !e.getKey().isStun()
                // && ee.getKey() < 8000)
                if (this.job.containsValue(e.getValue()))
                    count.put(e.getValue(), count.get(e.getValue()) + 1);

                if (!this.job.containsKey(e.getKey()) && count.get(e.getValue()) < this.maxOnOne(e.getValue())
                        && !e.getKey().isStun())
                {
                    sep("Adding job : " + e + "(" + ee.getKey() + ")");
                    this.job.put(e.getKey(), e.getValue());
                }
            }

            sep("Current jobs ---> ");
            for (Entry<Buster, Ghost> e : this.job.entrySet())
                sep(e);
            sep("<--- ");

        }

        int maxOnOne(Ghost g)
        {

            return (int) Math.round((g.stamina + 5) / 10.0);
        }

    }

    public class Buster extends Entity
    {
        final int tid; // team id
        int ghid; // ghost id si chargé
        BusterState st; // state
        BusterState ost; // oldstate
        int stunLeft; // durée restante stun si stun
        int nextStun; // durée avant le prochain stun disponible
        final IStrategy s;

        public Buster(int p_entityId, int p_teamId, IStrategy p_s)
        {
            super(p_entityId);
            this.tid = p_teamId;
            this.s = p_s;
            this.s.setBuster(this);
        }

        boolean canStun()
        {
            return !this.isStun() && this.nextStun == 0;
        }

        public Action decide()
        {
            Action a = this.s.decide();
            if (a.t.equals(ActionType.STUN))
                this.nextStun = 20;
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

        boolean isCarrying()
        {
            return this.ghid != -1 && this.st == BusterState.CARRYN;
        }

        public boolean isHome()
        {
            double h = CodeBusters.this.home.distanceL2(this.p);
            return h < MIN_RELEASE;
        }

        boolean isStun()
        {
            return this.stunLeft > 0;
        }

        boolean isTrapping()
        {
            return this.ghid != -1 && this.st == BusterState.TRAPPN;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return super.toString() + "[" + this.st + "|g:" + this.ghid + "|st:" + this.stunLeft + "|ns:"
                    + this.nextStun + "]";
        }

        /**
         * Redéfinition.
         *
         * @see CodeBusters.Entity#upd(int, int, int, int, int, int)
         */
        @Override
        void upd(int p_x, int p_y, int p_s, int p_v, int p_t)
        {
            this.ghid = -1;
            this.stunLeft = -1;

            super.upd(p_x, p_y, p_s, p_v, p_t);

            if (this.st != BusterState.values()[p_s])
                this.ost = this.st;

            this.st = BusterState.values()[p_s];

            if (p_s == 2)
                this.stunLeft = p_v;
            else
                this.ghid = p_v;

            if (this.nextStun > 0)
                this.nextStun--;

            // sep("[" + p_t + "]" + this.id + ":" + this.p + "-" + p_s + "|" + p_v);

        }

    }

    enum BusterState
    {
        MOVN, CARRYN, STUND, TRAPPN;
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

    public abstract class Entity
    {
        Position p;
        final int id;
        int lastupd;

        public Entity(int p_entityId)
        {
            this.id = p_entityId;
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
            return this.getClass().getSimpleName() + " [" + this.p + ",i=" + this.id + ",t=" + this.lastupd + "]";
        }

        void upd(int p_x, int p_y, int p_s, int p_v, int p_t)
        {
            this.p = new Position(p_x, p_y);
            this.lastupd = p_t;
        }

    }

    public class Ghost extends Entity
    {
        int nbOnIt;
        int stamina;
        int first0stam = -1;

        public Ghost(int p_entityId)
        {
            super(p_entityId);
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return super.toString() + "[sta=" + this.stamina + "]";
        }

        /**
         * Redéfinition.
         *
         * @see CodeBusters.Entity#upd(int, int, int, int, int, int)
         */
        @Override
        void upd(int p_x, int p_y, int p_s, int p_v, int p_t)
        {
            super.upd(p_x, p_y, p_s, p_v, p_t);
            this.stamina = p_s;
            this.nbOnIt = p_v;

            if (this.stamina == 0 && this.first0stam == -1)
                this.first0stam = p_t;
        }

    }

    public class GridExploration
    {
        final static int MAXX = 16000;
        final static int MAXY = 9000;
        final static int VIEW = 2001;

        int c = 0;

        Position home;
        Position chiant;

        final List<Position> explored = new ArrayList<>();
        final List<Position> unexplored = new ArrayList<>();

        public GridExploration(int p_team)
        {
            this.home = HOMES[p_team];
            this.chiant = CHIANTS[p_team];

            // LinkedList<Position> lx = new LinkedList<>();
            // LinkedList<Position> ly = new LinkedList<>();
            //
            // if (CodeBusters.this.myTeamId == 0)
            // {
            //
            // for (int x = VIEW; x < MAXX; x += VIEW)
            // lx.add(new Position(x, MAXY - 1800));
            // for (int y = VIEW; y < MAXY; y += VIEW)
            // ly.add(new Position(MAXX - 1800, y));
            //
            // }
            // else
            // {
            // for (int x = VIEW; x < MAXX; x += VIEW)
            // lx.add(new Position(x, 1800));
            // for (int y = VIEW; y < MAXY; y += VIEW)
            // ly.add(new Position(1800, y));
            // }
            //
            // // Alternate x and y max values
            // for (int i = 0; i < lx.size() && i < ly.size(); i++)
            // {
            // this.unexplored.add(lx.poll());
            // this.unexplored.add(ly.poll());
            // }
            // this.unexplored.add(this.chiant);
            // this.unexplored.addAll(lx);
            // this.unexplored.addAll(ly);

            for (int x = 2000; x < MAXX; x += VIEW)
                for (int y = 2000; y < MAXY; y += VIEW)
                    this.unexplored.add(new Position(x, y));

            Comparator<Position> c = new RelativeComparator(this.home);

            this.unexplored.sort(c);
        }

        Position nearestNext(Position p_p)
        {
            sep("== Explore : " + this.unexplored);

            if (this.unexplored.isEmpty())
            {
                int[][] p = { { -200, 200 }, { 0, 0 }, { 200, -200 } };

                this.c = (this.c + 1) % 3;
                return new Position(this.chiant.x + p[this.c][0], this.chiant.y + p[this.c][1]);

            }

            this.unexplored.sort(new RelativeComparator(p_p));
            return this.unexplored.get(0);
        }

        void reset()
        {
            this.unexplored.addAll(this.explored);
            this.explored.clear();
        }

        void upd()
        {
            List<Position> toMove = new ArrayList<>();
            for (Position p : this.unexplored)
                for (Buster b : CodeBusters.this.teams.get(CodeBusters.this.myTeamId))
                    if (p.distanceL2(b.p) < 2200)
                        toMove.add(p);

            this.unexplored.removeAll(toMove);
            this.explored.addAll(toMove);
        }

    }

    public abstract class IStrategy
    {
        Buster b;

        abstract Action decide();

        public void setBuster(Buster p_b)
        {
            this.b = p_b;

        }
    }

    public class Position
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

        public Position(Vector2D p_v)
        {
            this.x = (int) p_v.x;
            this.y = (int) p_v.y;
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

        public Position nextPos(Position p_Direction)
        {
            if (this.distanceL2(p_Direction) <= MAX_MOVE)
                return p_Direction;
            else
            {
                Vector2D v_Dir = new Vector2D(p_Direction).minus(new Vector2D(this)).unitVector();
                Vector2D res = v_Dir.scalarMult(MAX_MOVE).plus(new Vector2D(this));
                return new Position(res);
            }
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return this.x + " " + this.y;
        }

    }

    public class RelativeComparator implements Comparator<Position>
    {

        Position toCompare;

        public RelativeComparator(Position p_p)
        {
            this.toCompare = p_p;
        }

        @Override
        public int compare(Position p_a, Position p_b)
        {
            double a = p_a.distanceL2(this.toCompare);
            double b = p_b.distanceL2(this.toCompare);
            return (int) (a - b);
        }

    }

    public class Vector2D extends Point2D.Double
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
        public Vector2D(Position p)
        {
            this.x = p.x;
            this.y = p.y;
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

}
