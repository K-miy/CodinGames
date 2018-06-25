package tools;

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
            case SQ_EUCLID:
                return p_p1.minus(p_p2).getR2() <= p_r * p_r;
            case MANHATTAN:
            case CHEBYSHEV:
            default:
                return this.val(p_p1, p_p2) <= p_r;
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
