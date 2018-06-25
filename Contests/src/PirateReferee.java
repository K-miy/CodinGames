
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sun.media.sound.InvalidFormatException;

public class PirateReferee
{
    private static final int LEAGUE_LEVEL = 3;

    private static final int MAP_WIDTH = 23;
    private static final int MAP_HEIGHT = 21;
    private static final int COOLDOWN_CANNON = 2;
    private static final int COOLDOWN_MINE = 5;
    private static final int INITIAL_SHIP_HEALTH = 100;
    private static final int MAX_SHIP_HEALTH = 100;
    private static final int MAX_SHIP_SPEED;
    private static final int MIN_SHIPS = 1;
    private static final int MAX_SHIPS;
    private static final int MIN_MINES;
    private static final int MAX_MINES;
    private static final int MIN_RUM_BARRELS = 10;
    private static final int MAX_RUM_BARRELS = 26;
    private static final int MIN_RUM_BARREL_VALUE = 10;
    private static final int MAX_RUM_BARREL_VALUE = 20;
    private static final int REWARD_RUM_BARREL_VALUE = 30;
    private static final int MINE_VISIBILITY_RANGE = 5;
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
                MAX_SHIPS = 1;
                CANNONS_ENABLED = false;
                MINES_ENABLED = false;
                MIN_MINES = 0;
                MAX_MINES = 0;
                MAX_SHIP_SPEED = 1;
                break;
            case 1: // add mines
                MAX_SHIPS = 1;
                CANNONS_ENABLED = true;
                MINES_ENABLED = true;
                MIN_MINES = 5;
                MAX_MINES = 10;
                MAX_SHIP_SPEED = 1;
                break;
            case 2: // 3 ships max
                MAX_SHIPS = 3;
                CANNONS_ENABLED = true;
                MINES_ENABLED = true;
                MIN_MINES = 5;
                MAX_MINES = 10;
                MAX_SHIP_SPEED = 1;
                break;
            default: // increase max s
                MAX_SHIPS = 3;
                CANNONS_ENABLED = true;
                MINES_ENABLED = true;
                MIN_MINES = 5;
                MAX_MINES = 10;
                MAX_SHIP_SPEED = 2;
                break;
        }
    }

    private static final Pattern PLAYER_INPUT_MOVE_PATTERN = Pattern
            .compile("MOVE (?<x>-?[0-9]{1,8})\\s+(?<y>-?[0-9]{1,8})(?:\\s+(?<message>.+))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern PLAYER_INPUT_SLOWER_PATTERN = Pattern.compile("SLOWER(?:\\s+(?<message>.+))?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PLAYER_INPUT_FASTER_PATTERN = Pattern.compile("FASTER(?:\\s+(?<message>.+))?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PLAYER_INPUT_WAIT_PATTERN = Pattern.compile("WAIT(?:\\s+(?<message>.+))?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PLAYER_INPUT_PORT_PATTERN = Pattern.compile("PORT(?:\\s+(?<message>.+))?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PLAYER_INPUT_STARBOARD_PATTERN = Pattern.compile("STARBOARD(?:\\s+(?<message>.+))?",
            Pattern.CASE_INSENSITIVE);
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

    public static class Coord
    {
        private final static int[][] DIRECTIONS_EVEN = new int[][] { { 1, 0 }, { 0, -1 }, { -1, -1 }, { -1, 0 },
                { -1, 1 }, { 0, 1 } };
        private final static int[][] DIRECTIONS_ODD = new int[][] { { 1, 0 }, { 1, -1 }, { 0, -1 }, { -1, 0 }, { 0, 1 },
                { 1, 1 } };
        private final int x;
        private final int y;

        public Coord(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public Coord(Coord other)
        {
            this.x = other.x;
            this.y = other.y;
        }

        public double angle(Coord targetPosition)
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

        Coord neighbor(int orientation)
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

            return new Coord(newX, newY);
        }

        boolean isInsideMap()
        {
            return this.x >= 0 && this.x < MAP_WIDTH && this.y >= 0 && this.y < MAP_HEIGHT;
        }

        int distanceTo(Coord dst)
        {
            return this.toCubeCoordinate().distanceTo(dst.toCubeCoordinate());
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null || this.getClass() != obj.getClass())
                return false;
            Coord other = (Coord) obj;
            return this.y == other.y && this.x == other.x;
        }

        @Override
        public String toString()
        {
            return join(this.x, this.y);
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

        Coord toOffsetCoordinate()
        {
            int newX = this.x + (this.z - (this.z & 1)) / 2;
            int newY = this.z;
            return new Coord(newX, newY);
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

        @Override
        public String toString()
        {
            return join(this.x, this.y, this.z);
        }
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
        protected Coord position;

        public EntityRef(EntityType type, int x, int y)
        {
            this.id = UNIQUE_ENTITY_ID++;
            this.type = type;
            this.position = new Coord(x, y);
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
                        Coord impactPosition = null;
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
        private final Coord position;
        private final int health;
        private final boolean hit;

        public Damage(Coord position, int health, boolean hit)
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
        Coord target;
        public int newOrientation;
        public Coord newPosition;
        public Coord newBowCoordinate;
        public Coord newSternCoordinate;

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
            Coord currentPosition = this.position;
            Coord targetPosition = new Coord(x, y);

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

                    centerAngle = currentPosition.angle(new Coord(MAP_WIDTH / 2, MAP_HEIGHT / 2));
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
                    Coord nextPosition = currentPosition.neighbor(this.orientation);
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
                                || (distance == distanceMin && this.action == Action.PORT && angleStarboard == anglePort
                                        && angleStarboardCenter < anglePortCenter)
                                || (distance == distanceMin && this.action == Action.PORT && angleStarboard == anglePort
                                        && angleStarboardCenter == anglePortCenter
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

                    centerAngle = currentPosition.angle(new Coord(MAP_WIDTH / 2, MAP_HEIGHT / 2));
                    anglePortCenter = Math.min(Math.abs((this.orientation + 1) - centerAngle),
                            Math.abs((this.orientation - 5) - centerAngle));
                    angleStarboardCenter = Math.min(Math.abs((this.orientation + 5) - centerAngle),
                            Math.abs((this.orientation - 1) - centerAngle));

                    Coord forwardPosition = currentPosition.neighbor(this.orientation);

                    this.action = null;

                    if (anglePort <= angleStarboard)
                        this.action = Action.PORT;

                    if (angleStarboard < anglePort
                            || angleStarboard == anglePort && angleStarboardCenter < anglePortCenter
                            || angleStarboard == anglePort && angleStarboardCenter == anglePortCenter
                                    && (this.orientation == 1 || this.orientation == 4))
                        this.action = Action.STARBOARD;

                    if (forwardPosition.isInsideMap() && angleStraight <= anglePort && angleStraight <= angleStarboard)
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

        public Coord stern()
        {
            return this.position.neighbor((this.orientation + 3) % 6);
        }

        public Coord bow()
        {
            return this.position.neighbor(this.orientation);
        }

        public Coord newStern()
        {
            return this.position.neighbor((this.newOrientation + 3) % 6);
        }

        public Coord newBow()
        {
            return this.position.neighbor(this.newOrientation);
        }

        public boolean at(Coord coord)
        {
            Coord stern = this.stern();
            Coord bow = this.bow();
            return stern != null && stern.equals(coord) || bow != null && bow.equals(coord)
                    || this.position.equals(coord);
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
                    || this.newPosition.equals(other.newPosition) || this.newPosition.equals(other.newSternCoordinate));
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
                Coord target = new Coord(x, y);
                this.target = target;
                this.action = Action.FIRE;
            }
        }
    }

    private static class PlayerRef
    {
        private int id;
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

        public int getScore()
        {
            int score = 0;
            for (Ship ship : this.ships)
                score += ship.health;
            return score;
        }

        public List<String> toViewString()
        {
            List<String> data = new ArrayList<>();

            data.add(String.valueOf(this.id));
            for (Ship ship : this.ships)
                data.add(ship.toViewString());

            return data;
        }
    }

    private long seed;
    private List<Cannonball> cannonballs;
    private List<Mine> mines;
    private List<RumBarrel> barrels;
    private List<PlayerRef> playerRefs;
    private List<Ship> ships;
    private List<Damage> damage;
    private List<Coord> cannonBallExplosions;
    private int shipsPerPlayer;
    private int mineCount;
    private int barrelCount;
    private Random random;

    protected void initReferee(int playerCount, Properties prop) throws InvalidFormatException
    {
        this.seed = Long
                .valueOf(prop.getProperty("seed", String.valueOf(new Random(System.currentTimeMillis()).nextLong())));
        this.random = new Random(this.seed);

        this.shipsPerPlayer = clamp(
                Integer.valueOf(prop.getProperty("shipsPerPlayer",
                        String.valueOf(this.random.nextInt(1 + MAX_SHIPS - MIN_SHIPS) + MIN_SHIPS))),
                MIN_SHIPS, MAX_SHIPS);

        if (MAX_MINES > MIN_MINES)
            this.mineCount = clamp(
                    Integer.valueOf(prop.getProperty("mineCount",
                            String.valueOf(this.random.nextInt(MAX_MINES - MIN_MINES) + MIN_MINES))),
                    MIN_MINES, MAX_MINES);
        else
            this.mineCount = MIN_MINES;

        this.barrelCount = clamp(
                Integer.valueOf(prop.getProperty("barrelCount",
                        String.valueOf(this.random.nextInt(MAX_RUM_BARRELS - MIN_RUM_BARRELS) + MIN_RUM_BARRELS))),
                MIN_RUM_BARRELS, MAX_RUM_BARRELS);

        this.cannonballs = new ArrayList<>();
        this.cannonBallExplosions = new ArrayList<>();
        this.damage = new ArrayList<>();

        // Generate Players
        this.playerRefs = new ArrayList<PlayerRef>(playerCount);
        for (int i = 0; i < playerCount; i++)
            this.playerRefs.add(new PlayerRef(i));
        // Generate Ships
        for (int j = 0; j < this.shipsPerPlayer; j++)
        {
            int xMin = 1 + j * MAP_WIDTH / this.shipsPerPlayer;
            int xMax = (j + 1) * MAP_WIDTH / this.shipsPerPlayer - 2;

            int y = 1 + this.random.nextInt(MAP_HEIGHT / 2 - 2);
            int x = xMin + this.random.nextInt(1 + xMax - xMin);
            int orientation = this.random.nextInt(6);

            Ship ship0 = new Ship(x, y, orientation, 0);
            Ship ship1 = new Ship(x, MAP_HEIGHT - 1 - y, (6 - orientation) % 6, 1);

            this.playerRefs.get(0).ships.add(ship0);
            this.playerRefs.get(1).ships.add(ship1);
            this.playerRefs.get(0).shipsAlive.add(ship0);
            this.playerRefs.get(1).shipsAlive.add(ship1);
        }

        this.ships = this.playerRefs.stream().map(p -> p.ships).flatMap(List::stream).collect(Collectors.toList());

        // Generate mines
        this.mines = new ArrayList<>();
        while (this.mines.size() < this.mineCount)
        {
            int x = 1 + this.random.nextInt(MAP_WIDTH - 2);
            int y = 1 + this.random.nextInt(MAP_HEIGHT / 2);

            Mine m = new Mine(x, y);

            boolean cellIsFreeOfMines = this.mines.stream().noneMatch(mine -> mine.position.equals(m.position));
            boolean cellIsFreeOfShips = this.ships.stream().noneMatch(ship -> ship.at(m.position));

            if (cellIsFreeOfShips && cellIsFreeOfMines)
            {
                if (y != MAP_HEIGHT - 1 - y)
                    this.mines.add(new Mine(x, MAP_HEIGHT - 1 - y));
                this.mines.add(m);
            }
        }
        this.mineCount = this.mines.size();

        // Generate supplies
        this.barrels = new ArrayList<>();
        while (this.barrels.size() < this.barrelCount)
        {
            int x = 1 + this.random.nextInt(MAP_WIDTH - 2);
            int y = 1 + this.random.nextInt(MAP_HEIGHT / 2);
            int h = MIN_RUM_BARREL_VALUE + this.random.nextInt(1 + MAX_RUM_BARREL_VALUE - MIN_RUM_BARREL_VALUE);

            RumBarrel m = new RumBarrel(x, y, h);

            boolean cellIsFreeOfBarrels = this.barrels.stream().noneMatch(barrel -> barrel.position.equals(m.position));
            boolean cellIsFreeOfMines = this.mines.stream().noneMatch(mine -> mine.position.equals(m.position));
            boolean cellIsFreeOfShips = this.ships.stream().noneMatch(ship -> ship.at(m.position));

            if (cellIsFreeOfShips && cellIsFreeOfMines && cellIsFreeOfBarrels)
            {
                if (y != MAP_HEIGHT - 1 - y)
                    this.barrels.add(new RumBarrel(x, MAP_HEIGHT - 1 - y, h));
                this.barrels.add(m);
            }
        }
        this.barrelCount = this.barrels.size();

    }

    protected Properties getConfiguration()
    {
        Properties prop = new Properties();
        prop.setProperty("seed", String.valueOf(this.seed));
        prop.setProperty("shipsPerPlayer", String.valueOf(this.shipsPerPlayer));
        prop.setProperty("barrelCount", String.valueOf(this.barrelCount));
        prop.setProperty("mineCount", String.valueOf(this.mineCount));
        return prop;
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

    protected int getExpectedOutputLineCountForPlayer(int playerIdx)
    {
        return this.playerRefs.get(playerIdx).shipsAlive.size();
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
                                Coord target = ship.stern().neighbor((ship.orientation + 3) % 6);

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
                            if (ship.target.isInsideMap() && distance <= FIRE_DISTANCE_MAX && ship.cannonCooldown == 0)
                            {
                                int travelTime = (int) (1 + Math.round(ship.bow().distanceTo(ship.target) / 3.0));
                                this.cannonballs.add(new Cannonball(ship.target.x, ship.target.y, ship.id, ship.bow().x,
                                        ship.bow().y, travelTime));
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
        Coord bow = ship.bow();
        Coord stern = ship.stern();
        Coord center = ship.position;

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

                    Coord newCoordinate = ship.position.neighbor(ship.orientation);

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
        for (Iterator<Coord> it = this.cannonBallExplosions.iterator(); it.hasNext();)
        {
            Coord position = it.next();
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
        for (Iterator<Coord> itBall = this.cannonBallExplosions.iterator(); itBall.hasNext();)
        {
            Coord position = itBall.next();
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
        for (Iterator<Coord> itBall = this.cannonBallExplosions.iterator(); itBall.hasNext();)
        {
            Coord position = itBall.next();
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

        // For each sunk ship, create a new rum barrel with the amount of rum the ship had at the begin of the turn (up
        // to 30).
        for (Ship ship : this.ships)
            if (ship.health <= 0)
            {
                int reward = Math.min(REWARD_RUM_BARREL_VALUE, ship.initialHealth);
                if (reward > 0)
                    this.barrels.add(new RumBarrel(ship.position.x, ship.position.y, reward));
            }

        for (Coord position : this.cannonBallExplosions)
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

    protected void populateMessages(Properties p)
    {
        p.put("endReached", "End reached");
    }

    protected String[] getInitInputForPlayer(int playerIdx)
    {
        return new String[0];
    }

    protected String[] getInputForPlayer(int round, int playerIdx)
    {
        List<String> data = new ArrayList<>();

        // PlayerRef's ships first
        for (Ship ship : this.playerRefs.get(playerIdx).shipsAlive)
            data.add(ship.toPlayerString(playerIdx));

        // Number of ships
        data.add(0, String.valueOf(data.size()));

        // Opponent's ships
        for (Ship ship : this.playerRefs.get((playerIdx + 1) % 2).shipsAlive)
            data.add(ship.toPlayerString(playerIdx));

        // Visible mines
        for (Mine mine : this.mines)
        {
            boolean visible = false;
            for (Ship ship : this.playerRefs.get(playerIdx).shipsAlive)
                if (ship.position.distanceTo(mine.position) <= MINE_VISIBILITY_RANGE)
                {
                    visible = true;
                    break;
                }
            if (visible)
                data.add(mine.toPlayerString(playerIdx));
        }

        for (Cannonball ball : this.cannonballs)
            data.add(ball.toPlayerString(playerIdx));

        for (RumBarrel barrel : this.barrels)
            data.add(barrel.toPlayerString(playerIdx));

        data.add(1, String.valueOf(data.size() - 1));

        return data.toArray(new String[data.size()]);
    }

    protected String[] getInitDataForView()
    {
        List<String> data = new ArrayList<>();

        data.add(join(MAP_WIDTH, MAP_HEIGHT, this.playerRefs.get(0).ships.size(), MINE_VISIBILITY_RANGE));

        data.add(0, String.valueOf(data.size() + 1));

        return data.toArray(new String[data.size()]);
    }

    protected String[] getFrameDataForView(int round, int frame, boolean keyFrame)
    {
        List<String> data = new ArrayList<>();

        for (PlayerRef playerRef : this.playerRefs)
            data.addAll(playerRef.toViewString());
        data.add(String.valueOf(this.cannonballs.size()));
        for (Cannonball ball : this.cannonballs)
            data.add(ball.toViewString());
        data.add(String.valueOf(this.mines.size()));
        for (Mine mine : this.mines)
            data.add(mine.toViewString());
        data.add(String.valueOf(this.barrels.size()));
        for (RumBarrel barrel : this.barrels)
            data.add(barrel.toViewString());
        data.add(String.valueOf(this.damage.size()));
        for (Damage d : this.damage)
            data.add(d.toViewString());

        return data.toArray(new String[data.size()]);
    }

    protected String getGameName()
    {
        return "CodersOfTheCaribbean";
    }

    protected String getHeadlineAtGameStartForConsole()
    {
        return null;
    }

    protected int getMinimumPlayerCount()
    {
        return 2;
    }

    protected boolean showTooltips()
    {
        return true;
    }

    protected String[] getPlayerActions(int playerIdx, int round)
    {
        return new String[0];
    }

    protected boolean isPlayerDead(int playerIdx)
    {
        return false;
    }

    protected String getDeathReason(int playerIdx)
    {
        return "$" + playerIdx + ": Eliminated!";
    }

    protected int getScore(int playerIdx)
    {
        return this.playerRefs.get(playerIdx).getScore();
    }

    protected String[] getGameSummary(int round)
    {
        return new String[0];
    }

    protected void setPlayerTimeout(int frame, int round, int playerIdx)
    {
        this.playerRefs.get(playerIdx).setDead();
    }

    protected int getMaxRoundCount(int playerCount)
    {
        return 200;
    }

    protected int getMillisTimeForRound()
    {
        return 50;
    }
}
