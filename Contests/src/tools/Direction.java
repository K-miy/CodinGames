package tools;

/**
 * Classe représentant une énumération de directions.
 * <ul>
 * <li>Nord</li>
 * <li>Sud</li>
 * <li>Est</li>
 * <li>Ouest</li>
 * <li>Nord E</li>
 * <li>Sud E</li>
 * <li>Nord Ouest</li>
 * <li>Sud Ouest</li>
 * </ul>
 *
 * @author Camille Besse
 */
public enum Direction
{
    /**
     * définitions de l'énumération. type de directions.
     */
    N(-1, 0), S(1, 0), E(0, 1), W(0, -1), NE(-1, 1), SE(1, 1), NW(-1, -1), SW(1, -1);

    public final int dx;
    public final int dy;

    private Direction(int p_dx, int p_dy)
    {
        this.dx = p_dx;
        this.dy = p_dy;
        // Merci Oli & Simon.
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
            case S:
            case W:
            case E:
                return super.toString().substring(0, 1);
            case NE:
            case SE:
            case NW:
            case SW:
                return super.toString().substring(0, 1) + super.toString().substring(2, 3);
            default:
                return "";
        }
    }

    /**
     * Retourne le tableau des direction diagonales
     *
     * @return
     */
    static public Direction[] diagonalValues()
    {
        Direction[] d = { NE, SE, NW, SW };
        return d;
    }

    /**
     * Renvoie la direction relative à droite à p_dir d'un huitième de tour
     *
     * @param p_dir
     * @return
     */
    static public Direction droite(Direction p_dir)
    {
        switch (p_dir)
        {
            case N:
                return NE;
            case S:
                return SW;
            case W:
                return NW;
            case E:
                return SE;
            case NE:
                return E;
            case SE:
                return S;
            case NW:
                return N;
            case SW:
                return W;
            default:
                return null;
        }
    }

    /**
     * Renvoie la direction relative à gauche à p_dir d'un huitième de tour
     *
     * @param p_dir
     * @return
     */
    static public Direction gauche(Direction p_dir)
    {
        switch (p_dir)
        {
            case N:
                return NW;
            case S:
                return SE;
            case W:
                return SW;
            case E:
                return NE;
            case NE:
                return N;
            case SE:
                return E;
            case NW:
                return W;
            case SW:
                return S;
            default:
                return null;
        }
    }

    /**
     * Renvoie la direction inverse à p_dir
     *
     * @param p_dir
     * @return
     */
    static public Direction Inverse(Direction p_dir)
    {
        switch (p_dir)
        {
            case N:
                return S;
            case S:
                return N;
            case W:
                return E;
            case E:
                return W;
            case NE:
                return SW;
            case SE:
                return NW;
            case NW:
                return SE;
            case SW:
                return NE;
            default:
                return null;
        }
    }

    /**
     * Retourne le tableau des direction verticales
     *
     * @return
     */
    static public Direction[] verticalValues()
    {
        Direction[] d = { N, S, E, W };
        return d;
    }

}
