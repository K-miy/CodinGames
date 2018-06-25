import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Pirates
{

    static Scanner in = new Scanner(System.in);

    // static boolean LOCAL = true;
    static boolean LOCAL = false;
    final static boolean DEBUG = true;
    final static Random RAND = new Random();

    static int tour = 0;

    static TeamCoordinator TC;

    public static void sep(Object s)
    {
        if (DEBUG)
            System.err.println(tour + ": " + s);
    }

    public static void read(HashMap<Integer, Entity> ent, State s)
    {
        int entityCount = in.nextInt(); // the number of entities (e.g. ships, mines or cannonballs)
        for (int i = 0; i < entityCount; i++)
        {
            int entityId = in.nextInt();

            Entity ee = ent.get(entityId);

            String entityType = in.next();
            if (ee == null)
            {
                switch (entityType)
                {
                    case "SHIP":
                        ee = new Ship(entityId, new Greedy());
                        break;
                    case "BARREL":
                        ee = new Barrel(entityId);
                        break;
                    case "MINE":
                        ee = new Mine(entityId);
                        break;
                    case "CANNONBALL":
                        ee = new CannonBall(entityId);
                        break;
                }
                ent.put(ee.id, ee);
            }

            // int x = in.nextInt();
            // int y = in.nextInt();
            // int arg1 = in.nextInt();
            // int arg2 = in.nextInt();
            // int arg3 = in.nextInt();
            // int arg4 = in.nextInt();

            ee.upd(tour, in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
            s.upd(tour, ee);
        }
    }

    public static void main(String args[])
    {
        HashMap<Integer, Entity> ent = new HashMap<>();

        TC = new TeamCoordinator();

        // game loop
        while (true)
        {
            tour++;
            State s = new State();
            // int myShipCount = in.nextInt(); // the number of remaining ships
            in.nextInt(); // the number of remaining ships

            read(ent, s);

            TC.upd(ent);
            TC.decide(tour, s);
            sep(TC);

            // for (int i = 0; i < myShipCount; i++)
            for (Action a : TC.job.values())
                System.out.println(a); // Any valid action, such as "WAIT" or "MOVE x y"
        }
    }

    public static class TeamCoordinator
    {
        int tid;
        HashMap<Ship, Action> job = new HashMap<>();

        public TeamCoordinator()
        {
            this.tid = 1;
        }

        public void upd(HashMap<Integer, Entity> p_ent)
        {
            for (Entity e : p_ent.values())
                if (e instanceof Ship)
                {
                    Ship s = (Ship) e;
                    if (s.isMine())
                        this.job.put(s, new Action());
                }
        }

        public void decide(int p_t, State p_s)
        {
            this.job.clear();

            for (Ship s : p_s.s.values())
                if (s.isMine())
                    this.job.put(s, s.strat.decide(p_s));

            sep("Current jobs ---> ");
            for (Entry<Ship, Action> e : this.job.entrySet())
                sep(e);
            sep("<--- ");
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "TC [ID=" + this.tid + "]\n" + this.job + "";
        }

    }

    public static class Greedy extends IStrategy
    {
        Action lastA = null;

        @Override
        public Action decide(State p_s)
        {
            Action a = new Action(this.s.canMine());
            RelativeComparator c = new RelativeComparator(this.s.p);

            ArrayList<Barrel> l = new ArrayList<>(p_s.b.values());
            l.sort(c);
            if (!l.isEmpty())
            {
                int i = 0;
                if (l.size() > 1 && l.get(i).p.distanceTo(this.s.p) <= 1)
                    i++;
                a = new Action(l.get(i).p);
            }

            // Shooting possible while continuing the course of action ?
            if (this.lastA != null && this.s.canShoot() && this.lastA.equals(a))
            {
                ArrayList<Ship> ll = new ArrayList<>(p_s.ennemies());
                ll.sort(c);
                int i = 0;
                int estdur = ll.get(i).p.distanceTo(this.s.p);
                while (i < ll.size() && ll.get(i).whereToShoot(estdur).distanceTo(this.s.p) > 10)
                    i++;
                if (i < ll.size())
                    a = new Action(ll.get(i).whereToShoot(estdur), true);
            }

            // Avoid mine and balls

            {

            }

            sep("Act:" + this.s + " -> " + a);

            this.lastA = a;
            return a;
        }

    }

    public static abstract class IStrategy
    {
        Ship s;

        abstract Action decide(State p_s);

        public void setShip(Ship p_s)
        {
            this.s = p_s;
        }
    }

    public static class Entity
    {
        final int id;
        int lastUpd = -1;
        Position p;

        private Entity(int p_entityId)
        {
            this.id = p_entityId;
            this.p = null;
        }

        @Override
        public Entity clone()
        {
            Entity e = new Entity(this.id);
            e.p = this.p;
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
            return this.getClass().getSimpleName() + "(" + this.id + "/" + this.p + ")";
        }

        void upd(int p_t, int p_x, int p_y, int p_1, int p_2, int p_3, int p_4)
        {
            this.lastUpd = p_t;
            this.p = new Position(p_x, p_y);
            // sep("upd : " + this);
        }

    }

    public static class Ship extends Entity
    {
        IStrategy strat;

        int owner = 0;
        int o; // orientation
        int s; // speed
        int r; // rum left
        int mcd; // mine CD
        int ccd; // cannon CD

        private Ship(int p_entityId, IStrategy p_s)
        {
            super(p_entityId);
            this.strat = p_s;
            this.strat.setShip(this);
        }

        public Position whereToShoot(int p_dur)
        {
            int turns = 1 + (int) Math.round(p_dur / 3d);

            turns *= this.s;
            Position pos = this.p;
            Position oldPos = pos;
            while (turns > 0 && pos.isInsideMap())
            {
                oldPos = pos;
                pos = oldPos.neighbor(this.o);
                turns--;
            }
            if (!pos.isInsideMap())
                return oldPos;
            else
                return pos;
        }

        public boolean isMine()
        {
            return this.owner == 1;
        }

        public Position stern()
        {
            return this.p.neighbor((this.o + 3) % 6);
        }

        public Position bow()
        {
            return this.p.neighbor(this.o);
        }

        public boolean canMine()
        {
            return this.mcd == 0;
        }

        public boolean canShoot()
        {
            return this.ccd == 0;
        }

        /**
         * Redéfinition.
         *
         * @see Pirates.Entity#clone()
         */
        @Override
        public Entity clone()
        {
            Ship s = new Ship(this.id, this.strat);
            s.upd(this.lastUpd, this.p.x, this.p.y, this.o, this.s, this.r, this.owner);
            return s;
        }

        /**
         * Redéfinition.
         *
         * @see Pirates.Entity#upd(int, int, int, int, int, int, int)
         */
        @Override
        void upd(int p_t, int p_x, int p_y, int p_1, int p_2, int p_3, int p_4)
        {
            super.upd(p_t, p_x, p_y, p_1, p_2, p_3, p_4);
            this.o = p_1;
            this.s = p_2;
            this.r = p_3;
            this.owner = p_4;
        }

    }

    public static class Barrel extends Entity
    {
        int rum;

        private Barrel(int p_entityId)
        {
            super(p_entityId);
        }

        /**
         * Redéfinition.
         *
         * @see Pirates.Entity#clone()
         */
        @Override
        public Entity clone()
        {
            Barrel b = new Barrel(this.id);
            b.rum = this.rum;
            return b;
        }

        /**
         * Redéfinition.
         *
         * @see Pirates.Entity#upd(int, int, int, int, int, int, int)
         */
        @Override
        void upd(int p_t, int p_x, int p_y, int p_1, int p_2, int p_3, int p_4)
        {
            super.upd(p_t, p_x, p_y, p_1, p_2, p_3, p_4);
            this.rum = p_1;
        }

    }

    public static class Mine extends Entity
    {
        private Mine(int p_entityId)
        {
            super(p_entityId);
        }
    }

    public static class CannonBall extends Entity
    {
        int owner;
        int remaining;

        private CannonBall(int p_entityId)
        {
            super(p_entityId);
        }

        /**
         * Redéfinition.
         *
         * @see Pirates.Entity#clone()
         */
        @Override
        public Entity clone()
        {
            CannonBall c = new CannonBall(this.id);
            c.owner = this.owner;
            c.remaining = this.remaining;
            return c;
        }

        /**
         * Redéfinition.
         *
         * @see Pirates.Entity#upd(int, int, int, int, int, int, int)
         */
        @Override
        void upd(int p_t, int p_x, int p_y, int p_1, int p_2, int p_3, int p_4)
        {
            super.upd(p_t, p_x, p_y, p_1, p_2, p_3, p_4);
            this.owner = p_1;
            this.remaining = p_2;
        }

    }

    public enum ActionType
    {
        WAIT, MOVE, SLOWER, MINE, FIRE;
    }

    public static class Action
    {
        ActionType t;
        Position to;

        public Action()
        {
            this.t = ActionType.WAIT;
        }

        public Action(Position p_p)
        {
            this.to = p_p;
            this.t = ActionType.MOVE;
        }

        public Action(boolean p_b)
        {
            if (p_b)
                this.t = ActionType.MINE;
            else
                this.t = ActionType.SLOWER;
        }

        public Action(Position p_p, boolean p_b)
        {
            this.to = p_p;
            if (p_b)
                this.t = ActionType.FIRE;
            else
                this.t = ActionType.MOVE;

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
            result = prime * result + ((this.to == null) ? 0 : this.to.hashCode());
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
            if (this.t != other.t)
                return false;
            if (this.to == null)
            {
                if (other.to != null)
                    return false;
            }
            else if (!this.to.equals(other.to))
                return false;
            return true;
        }

        @Override
        public String toString()
        {
            switch (this.t)
            {
                case MINE:
                case SLOWER:
                case WAIT:
                    return this.t + "";
                case MOVE:
                case FIRE:
                    return this.t + " " + this.to;
            }
            return "";
        }

    }

    public static class State
    {
        int tour = 0;
        HashMap<Integer, Ship> s;
        HashMap<Integer, Barrel> b;
        HashMap<Integer, Mine> m;
        HashMap<Integer, CannonBall> c;

        public State()
        {
            this.s = new HashMap<>();
            this.b = new HashMap<>();
            this.m = new HashMap<>();
            this.c = new HashMap<>();
        }

        @Override
        public State clone()
        {
            State c = new State();

            for (Ship e : this.s.values())
                c.s.put(e.id, (Ship) e.clone());
            for (Barrel e : this.b.values())
                c.b.put(e.id, (Barrel) e.clone());
            for (Mine e : this.m.values())
                c.m.put(e.id, (Mine) e.clone());
            for (CannonBall e : this.c.values())
                c.c.put(e.id, (CannonBall) e.clone());

            return c;
        }

        public Collection<Ship> ennemies()
        {
            // return this.s.values().stream().filter(ship -> !ship.isMine()).collect(Collectors.toList());
            ArrayList<Ship> e = new ArrayList<>();
            for (Ship ship : this.s.values())
                if (!ship.isMine())
                    e.add(ship);
            return e;
        }

        /**
         * Given a State and an action, returns the next state
         *
         * @param p_a
         * @return the enxt State after the action and no following action
         */
        public State nextState(Collection<Action> p_A)
        {
            Referee r = new Referee();
            r.initReferee(this);
            r.updateGame(this.tour);
            State n = r.getState(this.tour);

            return n;
        }

        public void upd(int p_t, Entity p_e)
        {
            // sep("Update0 :" + p_e);
            this.tour = p_t;
            if (p_e instanceof Ship)
                this.s.put(p_e.id, (Ship) p_e);
            else if (p_e instanceof Barrel)
                this.b.put(p_e.id, (Barrel) p_e);
            else if (p_e instanceof Mine)
                this.m.put(p_e.id, (Mine) p_e);
            else if (p_e instanceof CannonBall)
                this.c.put(p_e.id, (CannonBall) p_e);
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "S : " + this.tour + "\nf:" + this.s + "\nt:" + this.b;
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
            double a = p_a.p.distanceTo(this.toCompare);
            double b = p_b.p.distanceTo(this.toCompare);
            return (int) (a - b);
        }

    }

    public static class Position
    {
        private final static int[][] DIRECTIONS_EVEN = new int[][] { { 1, 0 }, { 0, -1 }, { -1, -1 }, { -1, 0 },
                { -1, 1 }, { 0, 1 } };
        private final static int[][] DIRECTIONS_ODD = new int[][] { { 1, 0 }, { 1, -1 }, { 0, -1 }, { -1, 0 }, { 0, 1 },
                { 1, 1 } };
        private final int x;
        private final int y;

        public Position(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public Position(Position other)
        {
            this.x = other.x;
            this.y = other.y;
        }

        public double angle(Position targetPosition)
        {
            double dy = (targetPosition.y - this.y) * Math.sqrt(3) / 2;
            double dx = targetPosition.x - this.x + ((this.y - targetPosition.y) & 1) * 0.5;
            double angle = -Math.atan2(dy, dx) * 3 / Math.PI;
            if (angle < 0)
                angle += 6;
            else if (angle >= 6)
                angle -= 6;
            return angle;
        }

        CubeCoordinate toCubeCoordinate()
        {
            int xp = this.x - (this.y - (this.y & 1)) / 2;
            int zp = this.y;
            int yp = -(xp + zp);
            return new CubeCoordinate(xp, yp, zp);
        }

        Position neighbor(int orientation)
        {
            int newY, newX;
            if (this.y % 2 == 1)
            {
                newY = this.y + DIRECTIONS_ODD[orientation][1];
                newX = this.x + DIRECTIONS_ODD[orientation][0];
            }
            else
            {
                newY = this.y + DIRECTIONS_EVEN[orientation][1];
                newX = this.x + DIRECTIONS_EVEN[orientation][0];
            }

            return new Position(newX, newY);
        }

        boolean isInsideMap()
        {
            return this.x >= 0 && this.x < Referee.MAP_WIDTH && this.y >= 0 && this.y < Referee.MAP_HEIGHT;
        }

        int distanceTo(Position dst)
        {
            return this.toCubeCoordinate().distanceTo(dst.toCubeCoordinate());
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null || this.getClass() != obj.getClass())
                return false;
            Position other = (Position) obj;
            return this.y == other.y && this.x == other.x;
        }

        @Override
        public String toString()
        {
            return this.x + " " + this.y;
        }
    }

    public static class CubeCoordinate
    {
        static int[][] directions = new int[][] { { 1, -1, 0 }, { +1, 0, -1 }, { 0, +1, -1 }, { -1, +1, 0 },
                { -1, 0, +1 }, { 0, -1, +1 } };
        int x, y, z;

        public CubeCoordinate(int x, int y, int z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        Position toOffsetCoordinate()
        {
            int newX = this.x + (this.z - (this.z & 1)) / 2;
            int newY = this.z;
            return new Position(newX, newY);
        }

        CubeCoordinate neighbor(int orientation)
        {
            int nx = this.x + directions[orientation][0];
            int ny = this.y + directions[orientation][1];
            int nz = this.z + directions[orientation][2];

            return new CubeCoordinate(nx, ny, nz);
        }

        int distanceTo(CubeCoordinate dst)
        {
            return (Math.abs(this.x - dst.x) + Math.abs(this.y - dst.y) + Math.abs(this.z - dst.z)) / 2;
        }

    }

    public static class Referee
    {
        private static final int LEAGUE_LEVEL = 1;

        private static final int MAP_WIDTH = 23;
        private static final int MAP_HEIGHT = 21;
        private static final int COOLDOWN_CANNON = 2;
        private static final int COOLDOWN_MINE = 5;
        private static final int INITIAL_SHIP_HEALTH = 100;
        private static final int MAX_SHIP_HEALTH = 100;
        private static final int MAX_SHIP_SPEED;
        private static final int REWARD_RUM_BARREL_VALUE = 30;
        private static final int FIRE_DISTANCE_MAX = 10;
        private static final int LOW_DAMAGE = 25;
        private static final int HIGH_DAMAGE = 50;
        private static final int MINE_DAMAGE = 25;
        private static final int NEAR_MINE_DAMAGE = 10;
        private static final boolean CANNONS_ENABLED;
        private static final boolean MINES_ENABLED;

        static
        {
            switch (LEAGUE_LEVEL)
            {
                case 0: // 1 ship / no mines / s 1
                    CANNONS_ENABLED = false;
                    MINES_ENABLED = false;
                    MAX_SHIP_SPEED = 1;
                    break;
                case 1: // add mines
                    CANNONS_ENABLED = true;
                    MINES_ENABLED = true;
                    MAX_SHIP_SPEED = 1;
                    break;
                case 2: // 3 ships max
                    CANNONS_ENABLED = true;
                    MINES_ENABLED = true;
                    MAX_SHIP_SPEED = 1;
                    break;
                default: // increase max s
                    CANNONS_ENABLED = true;
                    MINES_ENABLED = true;
                    MAX_SHIP_SPEED = 2;
                    break;
            }
        }

        private static final Pattern PLAYER_INPUT_MOVE_PATTERN = Pattern.compile(
                "MOVE (?<x>-?[0-9]{1,8})\\s+(?<y>-?[0-9]{1,8})(?:\\s+(?<message>.+))?", Pattern.CASE_INSENSITIVE);
        private static final Pattern PLAYER_INPUT_SLOWER_PATTERN = Pattern.compile("SLOWER(?:\\s+(?<message>.+))?",
                Pattern.CASE_INSENSITIVE);
        private static final Pattern PLAYER_INPUT_FASTER_PATTERN = Pattern.compile("FASTER(?:\\s+(?<message>.+))?",
                Pattern.CASE_INSENSITIVE);
        private static final Pattern PLAYER_INPUT_WAIT_PATTERN = Pattern.compile("WAIT(?:\\s+(?<message>.+))?",
                Pattern.CASE_INSENSITIVE);
        private static final Pattern PLAYER_INPUT_PORT_PATTERN = Pattern.compile("PORT(?:\\s+(?<message>.+))?",
                Pattern.CASE_INSENSITIVE);
        private static final Pattern PLAYER_INPUT_STARBOARD_PATTERN = Pattern
                .compile("STARBOARD(?:\\s+(?<message>.+))?", Pattern.CASE_INSENSITIVE);
        private static final Pattern PLAYER_INPUT_FIRE_PATTERN = Pattern
                .compile("FIRE (?<x>[0-9]{1,8})\\s+(?<y>[0-9]{1,8})(?:\\s+(?<message>.+))?", Pattern.CASE_INSENSITIVE);
        private static final Pattern PLAYER_INPUT_MINE_PATTERN = Pattern.compile("MINE(?:\\s+(?<message>.+))?",
                Pattern.CASE_INSENSITIVE);

        public static int clamp(int val, int min, int max)
        {
            return Math.max(min, Math.min(max, val));
        }

        @SafeVarargs
        static final <T> String join(T... v)
        {
            return Stream.of(v).map(String::valueOf).collect(Collectors.joining(" "));
        }

        private static enum EntityType
        {
            SHIP, BARREL, MINE, CANNONBALL
        }

        public static abstract class EntityRef
        {
            private static int UNIQUE_ENTITY_ID = 0;

            protected final int id;
            protected final EntityType type;
            protected Position position;

            public EntityRef(EntityType type, int x, int y)
            {
                this.id = UNIQUE_ENTITY_ID++;
                this.type = type;
                this.position = new Position(x, y);
            }

            public String toViewString()
            {
                return join(this.id, this.position.y, this.position.x);
            }

            protected String toPlayerString(int arg1, int arg2, int arg3, int arg4)
            {
                return join(this.id, this.type.name(), this.position.x, this.position.y, arg1, arg2, arg3, arg4);
            }
        }

        public static class Mine extends EntityRef
        {
            public Mine(int x, int y)
            {
                super(EntityType.MINE, x, y);
            }

            public String toPlayerString(int playerIdx)
            {
                return this.toPlayerString(0, 0, 0, 0);
            }

            public List<Damage> explode(List<Ship> ships, boolean force)
            {
                List<Damage> damage = new ArrayList<>();
                Ship victim = null;

                for (Ship ship : ships)
                    if (this.position.equals(ship.bow()) || this.position.equals(ship.stern())
                            || this.position.equals(ship.position))
                    {
                        damage.add(new Damage(this.position, MINE_DAMAGE, true));
                        ship.damage(MINE_DAMAGE);
                        victim = ship;
                    }

                if (force || victim != null)
                {
                    if (victim == null)
                        damage.add(new Damage(this.position, MINE_DAMAGE, true));

                    for (Ship ship : ships)
                        if (ship != victim)
                        {
                            Position impactPosition = null;
                            if (ship.stern().distanceTo(this.position) <= 1)
                                impactPosition = ship.stern();
                            if (ship.bow().distanceTo(this.position) <= 1)
                                impactPosition = ship.bow();
                            if (ship.position.distanceTo(this.position) <= 1)
                                impactPosition = ship.position;

                            if (impactPosition != null)
                            {
                                ship.damage(NEAR_MINE_DAMAGE);
                                damage.add(new Damage(impactPosition, NEAR_MINE_DAMAGE, true));
                            }
                        }
                }

                return damage;
            }
        }

        public static class Cannonball extends EntityRef
        {
            final int ownerEntityId;
            final int srcX;
            final int srcY;
            final int initialRemainingTurns;
            int remainingTurns;

            public Cannonball(int row, int col, int ownerEntityId, int srcX, int srcY, int remainingTurns)
            {
                super(EntityType.CANNONBALL, row, col);
                this.ownerEntityId = ownerEntityId;
                this.srcX = srcX;
                this.srcY = srcY;
                this.initialRemainingTurns = this.remainingTurns = remainingTurns;
            }

            @Override
            public String toViewString()
            {
                return join(this.id, this.position.y, this.position.x, this.srcY, this.srcX, this.initialRemainingTurns,
                        this.remainingTurns, this.ownerEntityId);
            }

            public String toPlayerString(int playerIdx)
            {
                return this.toPlayerString(this.ownerEntityId, this.remainingTurns, 0, 0);
            }
        }

        public static class RumBarrel extends EntityRef
        {
            private int health;

            public RumBarrel(int x, int y, int health)
            {
                super(EntityType.BARREL, x, y);
                this.health = health;
            }

            @Override
            public String toViewString()
            {
                return join(this.id, this.position.y, this.position.x, this.health);
            }

            public String toPlayerString(int playerIdx)
            {
                return this.toPlayerString(this.health, 0, 0, 0);
            }
        }

        public static class Damage
        {
            private final Position position;
            private final int health;
            private final boolean hit;

            public Damage(Position position, int health, boolean hit)
            {
                this.position = position;
                this.health = health;
                this.hit = hit;
            }

            public String toViewString()
            {
                return join(this.position.y, this.position.x, this.health, (this.hit ? 1 : 0));
            }
        }

        public static enum Action
        {
            FASTER, SLOWER, PORT, STARBOARD, FIRE, MINE
        }

        public static class Ship extends EntityRef
        {
            int orientation;
            int speed;
            int health;
            int initialHealth;
            int owner;
            String message;
            Action action;
            int mineCooldown;
            int cannonCooldown;
            Position target;
            public int newOrientation;
            public Position newPosition;
            public Position newBowCoordinate;
            public Position newSternCoordinate;

            public Ship(int x, int y, int orientation, int owner)
            {
                super(EntityType.SHIP, x, y);
                this.orientation = orientation;
                this.speed = 0;
                this.health = INITIAL_SHIP_HEALTH;
                this.owner = owner;
            }

            @Override
            public String toViewString()
            {
                return join(this.id, this.position.y, this.position.x, this.orientation, this.health, this.speed,
                        (this.action != null ? this.action : "WAIT"), this.bow().y, this.bow().x, this.stern().y,
                        this.stern().x, " ;" + (this.message != null ? this.message : ""));
            }

            public String toPlayerString(int playerIdx)
            {
                return this.toPlayerString(this.orientation, this.speed, this.health, this.owner == playerIdx ? 1 : 0);
            }

            public void setMessage(String message)
            {
                if (message != null && message.length() > 50)
                    message = message.substring(0, 50) + "...";
                this.message = message;
            }

            public void moveTo(int x, int y)
            {
                Position currentPosition = this.position;
                Position targetPosition = new Position(x, y);

                if (currentPosition.equals(targetPosition))
                {
                    this.action = Action.SLOWER;
                    return;
                }

                double targetAngle, angleStraight, anglePort, angleStarboard, centerAngle, anglePortCenter,
                        angleStarboardCenter;

                switch (this.speed)
                {
                    case 2:
                        this.action = Action.SLOWER;
                        break;
                    case 1:
                        // Suppose we've moved first
                        currentPosition = currentPosition.neighbor(this.orientation);
                        if (!currentPosition.isInsideMap())
                        {
                            this.action = Action.SLOWER;
                            break;
                        }

                        // Target reached at next turn
                        if (currentPosition.equals(targetPosition))
                        {
                            this.action = null;
                            break;
                        }

                        // For each neighbor cell, find the closest to target
                        targetAngle = currentPosition.angle(targetPosition);
                        angleStraight = Math.min(Math.abs(this.orientation - targetAngle),
                                6 - Math.abs(this.orientation - targetAngle));
                        anglePort = Math.min(Math.abs((this.orientation + 1) - targetAngle),
                                Math.abs((this.orientation - 5) - targetAngle));
                        angleStarboard = Math.min(Math.abs((this.orientation + 5) - targetAngle),
                                Math.abs((this.orientation - 1) - targetAngle));

                        centerAngle = currentPosition.angle(new Position(MAP_WIDTH / 2, MAP_HEIGHT / 2));
                        anglePortCenter = Math.min(Math.abs((this.orientation + 1) - centerAngle),
                                Math.abs((this.orientation - 5) - centerAngle));
                        angleStarboardCenter = Math.min(Math.abs((this.orientation + 5) - centerAngle),
                                Math.abs((this.orientation - 1) - centerAngle));

                        // Next to target with bad angle, slow down then rotate (avoid to turn around the target!)
                        if (currentPosition.distanceTo(targetPosition) == 1 && angleStraight > 1.5)
                        {
                            this.action = Action.SLOWER;
                            break;
                        }

                        Integer distanceMin = null;

                        // Test forward
                        Position nextPosition = currentPosition.neighbor(this.orientation);
                        if (nextPosition.isInsideMap())
                        {
                            distanceMin = nextPosition.distanceTo(targetPosition);
                            this.action = null;
                        }

                        // Test port
                        nextPosition = currentPosition.neighbor((this.orientation + 1) % 6);
                        if (nextPosition.isInsideMap())
                        {
                            int distance = nextPosition.distanceTo(targetPosition);
                            if (distanceMin == null || distance < distanceMin
                                    || distance == distanceMin && anglePort < angleStraight - 0.5)
                            {
                                distanceMin = distance;
                                this.action = Action.PORT;
                            }
                        }

                        // Test starboard
                        nextPosition = currentPosition.neighbor((this.orientation + 5) % 6);
                        if (nextPosition.isInsideMap())
                        {
                            int distance = nextPosition.distanceTo(targetPosition);
                            if (distanceMin == null || distance < distanceMin
                                    || (distance == distanceMin && angleStarboard < anglePort - 0.5
                                            && this.action == Action.PORT)
                                    || (distance == distanceMin && angleStarboard < angleStraight - 0.5
                                            && this.action == null)
                                    || (distance == distanceMin && this.action == Action.PORT
                                            && angleStarboard == anglePort && angleStarboardCenter < anglePortCenter)
                                    || (distance == distanceMin && this.action == Action.PORT
                                            && angleStarboard == anglePort && angleStarboardCenter == anglePortCenter
                                            && (this.orientation == 1 || this.orientation == 4)))
                            {
                                distanceMin = distance;
                                this.action = Action.STARBOARD;
                            }
                        }
                        break;
                    case 0:
                        // Rotate ship towards target
                        targetAngle = currentPosition.angle(targetPosition);
                        angleStraight = Math.min(Math.abs(this.orientation - targetAngle),
                                6 - Math.abs(this.orientation - targetAngle));
                        anglePort = Math.min(Math.abs((this.orientation + 1) - targetAngle),
                                Math.abs((this.orientation - 5) - targetAngle));
                        angleStarboard = Math.min(Math.abs((this.orientation + 5) - targetAngle),
                                Math.abs((this.orientation - 1) - targetAngle));

                        centerAngle = currentPosition.angle(new Position(MAP_WIDTH / 2, MAP_HEIGHT / 2));
                        anglePortCenter = Math.min(Math.abs((this.orientation + 1) - centerAngle),
                                Math.abs((this.orientation - 5) - centerAngle));
                        angleStarboardCenter = Math.min(Math.abs((this.orientation + 5) - centerAngle),
                                Math.abs((this.orientation - 1) - centerAngle));

                        Position forwardPosition = currentPosition.neighbor(this.orientation);

                        this.action = null;

                        if (anglePort <= angleStarboard)
                            this.action = Action.PORT;

                        if (angleStarboard < anglePort
                                || angleStarboard == anglePort && angleStarboardCenter < anglePortCenter
                                || angleStarboard == anglePort && angleStarboardCenter == anglePortCenter
                                        && (this.orientation == 1 || this.orientation == 4))
                            this.action = Action.STARBOARD;

                        if (forwardPosition.isInsideMap() && angleStraight <= anglePort
                                && angleStraight <= angleStarboard)
                            this.action = Action.FASTER;
                        break;
                }

            }

            public void faster()
            {
                this.action = Action.FASTER;
            }

            public void slower()
            {
                this.action = Action.SLOWER;
            }

            public void port()
            {
                this.action = Action.PORT;
            }

            public void starboard()
            {
                this.action = Action.STARBOARD;
            }

            public void placeMine()
            {
                if (MINES_ENABLED)
                    this.action = Action.MINE;
            }

            public Position stern()
            {
                return this.position.neighbor((this.orientation + 3) % 6);
            }

            public Position bow()
            {
                return this.position.neighbor(this.orientation);
            }

            public Position newStern()
            {
                return this.position.neighbor((this.newOrientation + 3) % 6);
            }

            public Position newBow()
            {
                return this.position.neighbor(this.newOrientation);
            }

            public boolean at(Position position)
            {
                Position stern = this.stern();
                Position bow = this.bow();
                return stern != null && stern.equals(position) || bow != null && bow.equals(position)
                        || this.position.equals(position);
            }

            public boolean newBowIntersect(Ship other)
            {
                return this.newBowCoordinate != null && (this.newBowCoordinate.equals(other.newBowCoordinate)
                        || this.newBowCoordinate.equals(other.newPosition)
                        || this.newBowCoordinate.equals(other.newSternCoordinate));
            }

            public boolean newBowIntersect(List<Ship> ships)
            {
                for (Ship other : ships)
                    if (this != other && this.newBowIntersect(other))
                        return true;
                return false;
            }

            public boolean newPositionsIntersect(Ship other)
            {
                boolean sternCollision = this.newSternCoordinate != null
                        && (this.newSternCoordinate.equals(other.newBowCoordinate)
                                || this.newSternCoordinate.equals(other.newPosition)
                                || this.newSternCoordinate.equals(other.newSternCoordinate));
                boolean centerCollision = this.newPosition != null && (this.newPosition.equals(other.newBowCoordinate)
                        || this.newPosition.equals(other.newPosition)
                        || this.newPosition.equals(other.newSternCoordinate));
                return this.newBowIntersect(other) || sternCollision || centerCollision;
            }

            public boolean newPositionsIntersect(List<Ship> ships)
            {
                for (Ship other : ships)
                    if (this != other && this.newPositionsIntersect(other))
                        return true;
                return false;
            }

            public void damage(int health)
            {
                this.health -= health;
                if (this.health <= 0)
                    this.health = 0;
            }

            public void heal(int health)
            {
                this.health += health;
                if (this.health > MAX_SHIP_HEALTH)
                    this.health = MAX_SHIP_HEALTH;
            }

            public void fire(int x, int y)
            {
                if (CANNONS_ENABLED)
                {
                    Position target = new Position(x, y);
                    this.target = target;
                    this.action = Action.FIRE;
                }
            }
        }

        private static class PlayerRef
        {
            public int id;
            private List<Ship> ships;
            private List<Ship> shipsAlive;

            public PlayerRef(int id)
            {
                this.id = id;
                this.ships = new ArrayList<>();
                this.shipsAlive = new ArrayList<>();
            }

            public void setDead()
            {
                for (Ship ship : this.ships)
                    ship.health = 0;
            }
        }

        private List<Cannonball> cannonballs;
        private List<Mine> mines;
        private List<RumBarrel> barrels;
        private List<PlayerRef> playerRefs;
        private List<Ship> ships;
        private List<Damage> damage;
        private List<Position> cannonBallExplosions;

        protected void initReferee(State p_s)
        {
            int playerCount = 2;

            this.cannonballs = new ArrayList<>();
            this.cannonBallExplosions = new ArrayList<>();
            this.damage = new ArrayList<>();

            // Generate Players
            this.playerRefs = new ArrayList<PlayerRef>(playerCount);
            for (int i = 0; i < playerCount; i++)
                this.playerRefs.add(new PlayerRef(i));
            // Set Ships
            for (Pirates.Ship e : p_s.s.values())
            {
                Ship s = new Ship(e.p.x, e.p.y, e.o, e.owner);
                if (e.isMine())
                    this.playerRefs.get(0).ships.add(s);
                else
                    this.playerRefs.get(1).ships.add(s);
            }

            this.ships = this.playerRefs.stream().map(p -> p.ships).flatMap(List::stream).collect(Collectors.toList());

            // Set mines
            this.mines = new ArrayList<>();
            for (Pirates.Mine e : p_s.m.values())
                this.mines.add(new Mine(e.p.x, e.p.y));
            // Set balls
            // this.cannonballs = new ArrayList<>();
            // for (Pirates.CannonBall e : p_s.c.values())
            // this.cannonballs.add(new Cannonball(e.p.x, e.p.y));
            // Set supplies
            this.barrels = new ArrayList<>();
            for (Pirates.Barrel e : p_s.b.values())
                this.barrels.add(new RumBarrel(e.p.x, e.p.y, e.rum));

        }

        protected void prepare(int round)
        {
            for (PlayerRef playerRef : this.playerRefs)
                for (Ship ship : playerRef.ships)
                {
                    ship.action = null;
                    ship.message = null;
                }
            this.cannonBallExplosions.clear();
            this.damage.clear();
        }

        protected void handlePlayerOutput(int frame, int round, int playerIdx, String[] outputs)
        {
            PlayerRef playerRef = this.playerRefs.get(playerIdx);

            try
            {
                int i = 0;
                for (String line : outputs)
                {
                    Matcher matchWait = PLAYER_INPUT_WAIT_PATTERN.matcher(line);
                    Matcher matchMove = PLAYER_INPUT_MOVE_PATTERN.matcher(line);
                    Matcher matchFaster = PLAYER_INPUT_FASTER_PATTERN.matcher(line);
                    Matcher matchSlower = PLAYER_INPUT_SLOWER_PATTERN.matcher(line);
                    Matcher matchPort = PLAYER_INPUT_PORT_PATTERN.matcher(line);
                    Matcher matchStarboard = PLAYER_INPUT_STARBOARD_PATTERN.matcher(line);
                    Matcher matchFire = PLAYER_INPUT_FIRE_PATTERN.matcher(line);
                    Matcher matchMine = PLAYER_INPUT_MINE_PATTERN.matcher(line);
                    Ship ship = playerRef.shipsAlive.get(i++);

                    if (matchMove.matches())
                    {
                        int x = Integer.parseInt(matchMove.group("x"));
                        int y = Integer.parseInt(matchMove.group("y"));
                        ship.setMessage(matchMove.group("message"));
                        ship.moveTo(x, y);
                    }
                    else if (matchFaster.matches())
                    {
                        ship.setMessage(matchFaster.group("message"));
                        ship.faster();
                    }
                    else if (matchSlower.matches())
                    {
                        ship.setMessage(matchSlower.group("message"));
                        ship.slower();
                    }
                    else if (matchPort.matches())
                    {
                        ship.setMessage(matchPort.group("message"));
                        ship.port();
                    }
                    else if (matchStarboard.matches())
                    {
                        ship.setMessage(matchStarboard.group("message"));
                        ship.starboard();
                    }
                    else if (matchWait.matches())
                        ship.setMessage(matchWait.group("message"));
                    else if (matchMine.matches())
                    {
                        ship.setMessage(matchMine.group("message"));
                        ship.placeMine();
                    }
                    else if (matchFire.matches())
                    {
                        int x = Integer.parseInt(matchFire.group("x"));
                        int y = Integer.parseInt(matchFire.group("y"));
                        ship.setMessage(matchFire.group("message"));
                        ship.fire(x, y);
                    }
                    else
                        throw new IllegalStateException("A valid action");
                }
            }
            catch (IllegalStateException e)
            {
                playerRef.setDead();
                throw e;
            }
        }

        private void decrementRum()
        {
            for (Ship ship : this.ships)
                ship.damage(1);
        }

        private void updateInitialRum()
        {
            for (Ship ship : this.ships)
                ship.initialHealth = ship.health;
        }

        private void moveCannonballs()
        {
            for (Iterator<Cannonball> it = this.cannonballs.iterator(); it.hasNext();)
            {
                Cannonball ball = it.next();
                if (ball.remainingTurns == 0)
                {
                    it.remove();
                    continue;
                }
                else if (ball.remainingTurns > 0)
                    ball.remainingTurns--;

                if (ball.remainingTurns == 0)
                    this.cannonBallExplosions.add(ball.position);
            }
        }

        private void applyActions()
        {
            for (PlayerRef playerRef : this.playerRefs)
                for (Ship ship : playerRef.shipsAlive)
                {
                    if (ship.mineCooldown > 0)
                        ship.mineCooldown--;
                    if (ship.cannonCooldown > 0)
                        ship.cannonCooldown--;

                    ship.newOrientation = ship.orientation;

                    if (ship.action != null)
                        switch (ship.action)
                        {
                            case FASTER:
                                if (ship.speed < MAX_SHIP_SPEED)
                                    ship.speed++;
                                break;
                            case SLOWER:
                                if (ship.speed > 0)
                                    ship.speed--;
                                break;
                            case PORT:
                                ship.newOrientation = (ship.orientation + 1) % 6;
                                break;
                            case STARBOARD:
                                ship.newOrientation = (ship.orientation + 5) % 6;
                                break;
                            case MINE:
                                if (ship.mineCooldown == 0)
                                {
                                    Position target = ship.stern().neighbor((ship.orientation + 3) % 6);

                                    if (target.isInsideMap())
                                    {
                                        boolean cellIsFreeOfBarrels = this.barrels.stream()
                                                .noneMatch(barrel -> barrel.position.equals(target));
                                        boolean cellIsFreeOfMines = this.mines.stream()
                                                .noneMatch(mine -> mine.position.equals(target));
                                        boolean cellIsFreeOfShips = this.ships.stream().filter(b -> b != ship)
                                                .noneMatch(b -> b.at(target));

                                        if (cellIsFreeOfBarrels && cellIsFreeOfShips && cellIsFreeOfMines)
                                        {
                                            ship.mineCooldown = COOLDOWN_MINE;
                                            Mine mine = new Mine(target.x, target.y);
                                            this.mines.add(mine);
                                        }
                                    }

                                }
                                break;
                            case FIRE:
                                int distance = ship.bow().distanceTo(ship.target);
                                if (ship.target.isInsideMap() && distance <= FIRE_DISTANCE_MAX
                                        && ship.cannonCooldown == 0)
                                {
                                    int travelTime = (int) (1 + Math.round(ship.bow().distanceTo(ship.target) / 3.0));
                                    this.cannonballs.add(new Cannonball(ship.target.x, ship.target.y, ship.id,
                                            ship.bow().x, ship.bow().y, travelTime));
                                    ship.cannonCooldown = COOLDOWN_CANNON;
                                }
                                break;
                            default:
                                break;
                        }
                }
        }

        private void checkCollisions(Ship ship)
        {
            Position bow = ship.bow();
            Position stern = ship.stern();
            Position center = ship.position;

            // Collision with the barrels
            for (Iterator<RumBarrel> it = this.barrels.iterator(); it.hasNext();)
            {
                RumBarrel barrel = it.next();
                if (barrel.position.equals(bow) || barrel.position.equals(stern) || barrel.position.equals(center))
                {
                    ship.heal(barrel.health);
                    it.remove();
                }
            }

            // Collision with the mines
            for (Iterator<Mine> it = this.mines.iterator(); it.hasNext();)
            {
                Mine mine = it.next();
                List<Damage> mineDamage = mine.explode(this.ships, false);

                if (!mineDamage.isEmpty())
                {
                    this.damage.addAll(mineDamage);
                    it.remove();
                }
            }
        }

        private void moveShips()
        {
            // ---
            // Go forward
            // ---
            for (int i = 1; i <= MAX_SHIP_SPEED; i++)
            {
                for (PlayerRef playerRef : this.playerRefs)
                    for (Ship ship : playerRef.shipsAlive)
                    {
                        ship.newPosition = ship.position;
                        ship.newBowCoordinate = ship.bow();
                        ship.newSternCoordinate = ship.stern();

                        if (i > ship.speed)
                            continue;

                        Position newCoordinate = ship.position.neighbor(ship.orientation);

                        if (newCoordinate.isInsideMap())
                        {
                            // Set new coordinate.
                            ship.newPosition = newCoordinate;
                            ship.newBowCoordinate = newCoordinate.neighbor(ship.orientation);
                            ship.newSternCoordinate = newCoordinate.neighbor((ship.orientation + 3) % 6);
                        }
                        else
                            ship.speed = 0;
                    }

                // Check ship and obstacles collisions
                List<Ship> collisions = new ArrayList<>();
                boolean collisionDetected = true;
                while (collisionDetected)
                {
                    collisionDetected = false;

                    for (Ship ship : this.ships)
                        if (ship.newBowIntersect(this.ships))
                            collisions.add(ship);

                    for (Ship ship : collisions)
                    {
                        // Revert last move
                        ship.newPosition = ship.position;
                        ship.newBowCoordinate = ship.bow();
                        ship.newSternCoordinate = ship.stern();

                        // Stop ships
                        ship.speed = 0;

                        collisionDetected = true;
                    }
                    collisions.clear();
                }

                for (PlayerRef playerRef : this.playerRefs)
                    for (Ship ship : playerRef.shipsAlive)
                    {
                        ship.position = ship.newPosition;
                        this.checkCollisions(ship);
                    }
            }
        }

        private void rotateShips()
        {
            // Rotate
            for (PlayerRef playerRef : this.playerRefs)
                for (Ship ship : playerRef.shipsAlive)
                {
                    ship.newPosition = ship.position;
                    ship.newBowCoordinate = ship.newBow();
                    ship.newSternCoordinate = ship.newStern();
                }

            // Check collisions
            boolean collisionDetected = true;
            List<Ship> collisions = new ArrayList<>();
            while (collisionDetected)
            {
                collisionDetected = false;

                for (Ship ship : this.ships)
                    if (ship.newPositionsIntersect(this.ships))
                        collisions.add(ship);

                for (Ship ship : collisions)
                {
                    ship.newOrientation = ship.orientation;
                    ship.newBowCoordinate = ship.newBow();
                    ship.newSternCoordinate = ship.newStern();
                    ship.speed = 0;
                    collisionDetected = true;
                }

                collisions.clear();
            }

            // Apply rotation
            for (PlayerRef playerRef : this.playerRefs)
                for (Ship ship : playerRef.shipsAlive)
                {
                    ship.orientation = ship.newOrientation;
                    this.checkCollisions(ship);
                }
        }

        private boolean gameIsOver()
        {
            for (PlayerRef playerRef : this.playerRefs)
                if (playerRef.shipsAlive.isEmpty())
                    return true;
            return this.barrels.size() == 0 && LEAGUE_LEVEL == 0;
        }

        void explodeShips()
        {
            for (Iterator<Position> it = this.cannonBallExplosions.iterator(); it.hasNext();)
            {
                Position position = it.next();
                for (Ship ship : this.ships)
                    if (position.equals(ship.bow()) || position.equals(ship.stern()))
                    {
                        this.damage.add(new Damage(position, LOW_DAMAGE, true));
                        ship.damage(LOW_DAMAGE);
                        it.remove();
                        break;
                    }
                    else if (position.equals(ship.position))
                    {
                        this.damage.add(new Damage(position, HIGH_DAMAGE, true));
                        ship.damage(HIGH_DAMAGE);
                        it.remove();
                        break;
                    }
            }
        }

        void explodeMines()
        {
            for (Iterator<Position> itBall = this.cannonBallExplosions.iterator(); itBall.hasNext();)
            {
                Position position = itBall.next();
                for (Iterator<Mine> it = this.mines.iterator(); it.hasNext();)
                {
                    Mine mine = it.next();
                    if (mine.position.equals(position))
                    {
                        this.damage.addAll(mine.explode(this.ships, true));
                        it.remove();
                        itBall.remove();
                        break;
                    }
                }
            }
        }

        void explodeBarrels()
        {
            for (Iterator<Position> itBall = this.cannonBallExplosions.iterator(); itBall.hasNext();)
            {
                Position position = itBall.next();
                for (Iterator<RumBarrel> it = this.barrels.iterator(); it.hasNext();)
                {
                    RumBarrel barrel = it.next();
                    if (barrel.position.equals(position))
                    {
                        this.damage.add(new Damage(position, 0, true));
                        it.remove();
                        itBall.remove();
                        break;
                    }
                }
            }
        }

        protected void updateGame(int round)
        {
            this.moveCannonballs();
            this.decrementRum();
            this.updateInitialRum();

            this.applyActions();
            this.moveShips();
            this.rotateShips();

            this.explodeShips();
            this.explodeMines();
            this.explodeBarrels();

            // For each sunk ship, create a new rum barrel with the amount of rum the ship had at the begin of the turn
            // (up
            // to 30).
            for (Ship ship : this.ships)
                if (ship.health <= 0)
                {
                    int reward = Math.min(REWARD_RUM_BARREL_VALUE, ship.initialHealth);
                    if (reward > 0)
                        this.barrels.add(new RumBarrel(ship.position.x, ship.position.y, reward));
                }

            for (Position position : this.cannonBallExplosions)
                this.damage.add(new Damage(position, 0, false));

            for (Iterator<Ship> it = this.ships.iterator(); it.hasNext();)
            {
                Ship ship = it.next();
                if (ship.health <= 0)
                {
                    this.playerRefs.get(ship.owner).shipsAlive.remove(ship);
                    it.remove();
                }
            }

            if (this.gameIsOver())
                throw new IllegalStateException("endReached");
        }

        public State getState(int p_t)
        {
            State s = new State();

            // PlayerRef's ships first
            for (Ship ship : this.playerRefs.get(1).shipsAlive)
            {
                Pirates.Ship ss = new Pirates.Ship(ship.id, null);
                ss.upd(p_t + 1, ship.position.x, ship.position.y, ship.orientation, ship.speed, ship.health, 1);
                s.upd(p_t + 1, ss);
            }

            // Opponent's ships
            for (Ship ship : this.playerRefs.get(0).shipsAlive)
            {
                Pirates.Ship ss = new Pirates.Ship(ship.id, null);
                ss.upd(p_t + 1, ship.position.x, ship.position.y, ship.orientation, ship.speed, ship.health, 0);
                s.upd(p_t + 1, ss);
            }

            // Visible mines
            for (Mine mine : this.mines)
            {
                Pirates.Mine m = new Pirates.Mine(mine.id);
                m.upd(p_t + 1, mine.position.x, mine.position.y, 0, 0, 0, 0);
                s.upd(p_t + 1, m);
            }

            for (Cannonball ball : this.cannonballs)
            {
                Pirates.CannonBall b = new Pirates.CannonBall(ball.id);
                b.upd(p_t + 1, ball.position.x, ball.position.y, ball.ownerEntityId, ball.remainingTurns, 0, 0);
                s.upd(p_t + 1, b);
            }

            for (RumBarrel barrel : this.barrels)
            {
                Pirates.Barrel b = new Pirates.Barrel(barrel.id);
                b.upd(p_t + 1, barrel.position.x, barrel.position.y, barrel.health, 0, 0, 0);
                s.upd(p_t + 1, b);
            }

            s.tour++;
            return s;
        }
    }

}
