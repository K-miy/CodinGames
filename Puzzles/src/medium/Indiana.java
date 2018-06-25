
package medium;

import java.util.HashSet;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Player6
{

    public void run()
    {
        Scanner in = new Scanner(System.in);
        int W = in.nextInt(); // number of columns.
        int H = in.nextInt(); // number of rows.
        in.nextLine();

        IndiMap m = new IndiMap(W, H);
        for (int i = 0; i < H; i++)
        {
            String LINE = in.nextLine(); // represents a line in the grid and contains W integers. Each integer
                                         // represents one room of a given type.
            String[] l = LINE.split(" ");
            for (int j = 0; j < l.length; j++)
                m.t[i][j] = Tile.values()[Integer.parseInt(l[j])];
        }
        in.nextInt(); // the coordinate along the X axis of the exit
        // (not useful for this first mission, but must be read).

        System.err.println(m);

        // game loop
        while (true)
        {
            int c = in.nextInt();
            int l = in.nextInt();
            String POS = in.next();
            Direction d = Direction.N;
            switch (POS)
            {
                case "TOP":
                    d = Direction.N;
                    break;
                case "LEFT":
                    d = Direction.W;
                    break;
                case "RIGHT":
                    d = Direction.E;
                    break;
                default:
                    d = null;

            }
            Position p = new Position(l, c);
            System.err.println(p + " / " + d);
            Position pnext = p.relativePos(m.t[l][c].towards(d));
            System.err.println(m.t[l][c].towards(d));
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            // One line containing the X Y coordinates of the room in which you believe Indy will be on the next turn.
            System.out.println(pnext.y + " " + pnext.x);
        }
    }

    public static void main(String args[])
    {
        new Player6().run();
    }

    /**
     * Classe représentant une énumération de directions.
     * <ul>
     * <li>Nord</li>
     * <li>Sud</li>
     * <li>Est</li>
     * <li>Ouest</li>
     * </ul>
     *
     * @author Camille Besse
     */
    enum Direction
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

    class IndiMap
    {
        Tile[][] t;

        public IndiMap(int w, int h)
        {
            this.t = new Tile[h][w];
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            for (Tile[] line : this.t)
            {
                for (Tile e : line)
                    sb.append(e + " ");
                sb.append("\n");
            }

            return sb.toString();
        }
    }

    class Position
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

    enum Tile
    {
        P, C, // PLAIN, CROSS
        S_H, S_V,// STRAINGHT Hori and Vert
        R_NW_SE, R_NE_SW, // Double rounded
        T_N, T_E, T_S, T_W, // T-shape
        R_NW, R_NE, R_SE, R_SW; // Single Rounded;

        public Direction towards(Direction in)
        {
            switch (this)
            {
                case C:
                    if (in.equals(Direction.N) || in.equals(Direction.W) || in.equals(Direction.E))
                        return Direction.S;
                    break;
                case T_N:
                case S_H:
                    if (in.equals(Direction.W) || in.equals(Direction.E))
                        return in.inverse();
                    break;
                case S_V:
                    if (in.equals(Direction.N))
                        return Direction.S;
                    break;
                case R_NW_SE:
                    if (in.equals(Direction.N))
                        return Direction.W;
                    if (in.equals(Direction.E))
                        return Direction.S;
                    break;
                case R_NE_SW:
                    if (in.equals(Direction.N))
                        return Direction.E;
                    if (in.equals(Direction.W))
                        return Direction.S;
                    break;
                case T_E:
                    if (in.equals(Direction.N) || in.equals(Direction.E))
                        return Direction.S;
                    break;
                case T_S:
                    if (in.equals(Direction.W) || in.equals(Direction.E))
                        return Direction.S;
                    break;
                case T_W:
                    if (in.equals(Direction.N) || in.equals(Direction.W))
                        return Direction.S;
                    break;
                case R_NW:
                    if (in.equals(Direction.N))
                        return Direction.W;
                    break;
                case R_NE:
                    if (in.equals(Direction.N))
                        return Direction.E;
                    break;
                case R_SE:
                    if (in.equals(Direction.E))
                        return Direction.S;
                    break;
                case R_SW:
                    if (in.equals(Direction.W))
                        return Direction.S;
                    break;

                default: // Plain
            }
            return null;
        }

    }

}
