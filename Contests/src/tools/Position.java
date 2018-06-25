package tools;

import java.util.HashSet;

public class Position extends Vector2D
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
    public HashSet<Position> getInRange(int p_r, Distance p_d)
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
        return ((int) this.x) + " " + ((int) this.y);
    }

}
