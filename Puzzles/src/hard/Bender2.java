
package hard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Solution8
{

    public void run()
    {
        Scanner in = new Scanner(System.in);
        int L = in.nextInt();
        int C = in.nextInt();
        in.nextLine();

        String[] mapS = new String[L];
        for (int i = 0; i < L; i++)
            mapS[i] = in.nextLine();

        Map m = new Map(mapS);

        BenderPos b = new BenderPos(m);
        // System.err.println(m);
        b.simulate();

        System.out.println(b.printHist());
    }

    public static void main(String args[])
    {
        new Solution8().run();

        // Solution ss = new Solution();
        // String[] s = new String[] { "#########", "#@#I T$#", "#O IB #", "# W #", "# ##", "#B XBN# #",
        // "## #", "# #", "# W #", "# ##", "#B XBN# #", "## #", "# #", "# W #",
        // "# ##", "#B XBN# #", "## #", "# #", "# #", "# ##", "# XBIT #",
        // "#########" };
        //
        // Map m = ss.new Map(s);
        //
        // BenderPos b = ss.new BenderPos(m);
        // // System.err.println(m);
        // b.simulate();
        //
        // System.out.println(b.printHist());

    }

    public class BenderPos
    {
        final int LOOP;

        Position p;
        Direction d;
        Map m;
        boolean breakerMode = false;
        boolean inversePrio = false;

        ArrayList<Direction> hist = new ArrayList<>();

        final Direction[] prio = { Direction.S, Direction.E, Direction.N, Direction.W };
        final Direction[] prioI = { Direction.W, Direction.N, Direction.E, Direction.S };

        public BenderPos(Map p_m)
        {
            this.m = p_m;
            this.p = this.m.start;
            this.d = Direction.S;
            this.LOOP = this.m.i.length * this.m.i[0].length;
            this.m.b = this;
        }

        public void move(Direction p_d)
        {
            this.hist.add(this.d);
            this.p = this.p.relativePos(this.d);
            this.d = p_d;
        }

        public String printHist()
        {
            if (this.hist.isEmpty())
                return "LOOP";

            StringBuilder sb = new StringBuilder();
            for (Direction di : this.hist)
                sb.append(di + "\n");
            return sb.toString();
        }

        public void simulate()
        {
            int tour = -1;
            while (!this.p.equals(this.m.end) && tour < this.LOOP)
            {
                tour++;

                Position nextP = this.p.relativePos(this.d);
                Direction[] currentPrio = (this.inversePrio ? this.prioI : this.prio);

                // Si la prochaine case est un mur ... on change de direction AVANT !
                if (this.m.get(nextP).equals(Item.WALL) || (this.m.get(nextP).equals(Item.OBST) && !this.breakerMode))
                {
                    for (int i = 0; i < 4; i++)
                    {
                        Position np = this.p.relativePos(currentPrio[i]);
                        if (this.m.in(np) && this.m.get(np).free(this.breakerMode))
                        {
                            this.d = currentPrio[i];
                            break;
                        }
                    }

                    nextP = this.p.relativePos(this.d);
                }

                switch (this.m.get(nextP))
                {
                    case WALL:
                        System.err.println("WALL IMPOSSIBLE !!!!! ");
                        break;
                    case END:
                        this.move(this.d);
                        return;
                    case OBST:
                        if (this.breakerMode)
                        {
                            this.m.i[nextP.x][nextP.y] = Item.EMPTY;
                            this.move(this.d);
                            // System.err.println(this.m);
                            break;
                        }
                    case S:
                        this.move(Direction.S);
                        break;
                    case E:
                        this.move(Direction.E);
                        break;
                    case N:
                        this.move(Direction.N);
                        break;
                    case W:
                        this.move(Direction.W);
                        break;
                    case TP:
                        this.hist.add(this.d);
                        this.p = this.m.autreTP(nextP);
                        break;
                    case BEER:
                        this.breakerMode = !this.breakerMode;
                        this.move(this.d);
                        break;
                    case INV:
                        this.inversePrio = !this.inversePrio;
                    case START: // En cas de retour au départ ... c'est vide !
                    default: // Empty
                        this.move(this.d);
                        break;
                }

                // System.err.println("----------------- \n tour : " + tour + "\n" + this);
            }

            if (tour == this.LOOP)
                this.hist.clear();
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return this.m + "\n" + "p:" + this.p + "|d:" + this.d + "|B:" + this.breakerMode + "|I:" + this.inversePrio;
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
                    return "NORTH";
                case S:
                    return "SOUTH";
                case W:
                    return "WEST";
                case E:
                    return "EAST";
                default:
                    return "";
            }
        }
    }

    public enum Distance
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

    public enum Item
    {
        WALL, OBST, START, END, S, E, N, W, BEER, INV, TP, EMPTY;

        public boolean free(boolean ignoreObst)
        {
            switch (this)
            {
                case WALL:
                    return false;
                case OBST:
                    return ignoreObst;
                case START:
                case END:
                case S:
                case E:
                case N:
                case W:
                case BEER:
                case INV:
                case TP:
                default: // Empty
                    return true;
            }
        }

        @Override
        public String toString()
        {
            switch (this)
            {
                case WALL:
                    return "#";
                case OBST:
                    return "X";
                case START:
                    return "@";
                case END:
                    return "$";
                case S:
                case E:
                case N:
                case W:
                    return super.toString();
                case BEER:
                case INV:
                case TP:
                    return super.toString().substring(0, 1);
                default: // Empty
                    return " ";
            }
        }

        public static Item fromChar(char c)
        {
            switch (c)
            {
                case '#':
                    return WALL;
                case 'X':
                    return OBST;
                case '@':
                    return START;
                case '$':
                    return END;
                case 'S':
                    return S;
                case 'E':
                    return E;
                case 'N':
                    return N;
                case 'W':
                    return W;
                case 'B':
                    return BEER;
                case 'I':
                    return INV;
                case 'T':
                    return TP;
                default:
                    return EMPTY;
            }
        }
    }

    public class Map
    {
        final Item[][] i;
        Position start;
        Position end;
        Position tp1;
        Position tp2;

        BenderPos b;

        public Map(String[] p_mapS)
        {
            this.i = new Item[p_mapS.length][p_mapS[0].length()];

            int lin = 0;
            for (String s : p_mapS)
            {
                int col = 0;
                for (char c : s.toCharArray())
                {
                    Item it = Item.fromChar(c);
                    this.i[lin][col] = it;
                    if (it.equals(Item.START))
                        this.start = new Position(lin, col);
                    if (it.equals(Item.END))
                        this.end = new Position(lin, col);
                    if (it.equals(Item.TP) && this.tp1 == null)
                        this.tp1 = new Position(lin, col);
                    if (it.equals(Item.TP) && this.tp1 != null)
                        this.tp2 = new Position(lin, col);

                    col++;
                }
                lin++;
            }
            // System.err.println(this);
        }

        public Position autreTP(Position p_tp)
        {
            if (p_tp.equals(this.tp1))
                return this.tp2;
            else
                return this.tp1;
        }

        public Item get(Position p_p)
        {
            return this.i[p_p.x][p_p.y];
        }

        public boolean in(Position p_p)
        {
            return p_p.x >= 0 && p_p.y >= 0 && p_p.x < this.i.length && p_p.y < this.i[0].length;
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
            int lin = 0;
            for (Item[] l : this.i)
            {
                int col = 0;
                for (Item c : l)
                {
                    if (new Position(lin, col).equals(this.b.p))
                        sb.append('O');
                    else
                        sb.append(c);
                    col++;
                }
                sb.append("\n");
                lin++;
            }
            sb.append("s:" + this.start + "| e:" + this.end);
            sb.append("TP1:" + this.tp1 + "| TP2:" + this.tp2);
            return sb + "";
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

    }

}
