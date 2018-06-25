package tools;

public class Vector2D
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
     * Since Vector2D works only in the x-y plane, (u x v) points directly along the z axis. This function returns the
     * value on the z axis that (u x v) reaches.
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
     * Returns a new vector with the same direction as the vector but with length 1, except in the case of zero vectors,
     * which return a copy of themselves.
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
