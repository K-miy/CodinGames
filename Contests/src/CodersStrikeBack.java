import java.awt.geom.Point2D;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class CodersStrikeBack
{

    public void run()
    {
        Scanner in = new Scanner(System.in);
        int laps = in.nextInt();
        int checkpointCount = in.nextInt();

        CP[] c = new CP[checkpointCount];

        for (int i = 0; i < c.length; i++)
            c[i] = new CP(in.nextInt(), in.nextInt());

        // Me
        Pod[] p = new Pod[2];
        for (int i = 0; i < 2; i++)
            // a[i] = new Pod(c);
            p[i] = new SimpleDrifter(c);

        // The Ennemy
        Pod[] o = new Pod[2];
        for (int i = 0; i < 2; i++)
            o[i] = new Pod(c)
            {
                @Override
                public void decide(Pod[] others)
                {}
            };

        // game loop
        while (true)
        {
            // Me
            for (int i = 0; i < 2; i++)
                p[i].upd(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
            // The Ennemy
            for (int i = 0; i < 2; i++)
                o[i].upd(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());

            for (int i = 0; i < 2; i++)
                p[i].decide(o);

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            System.out.println(p[0]);
            System.out.println(p[1]);
        }
    }

    public static void main(String args[])
    {
        new CodersStrikeBack().run();
    }

    public class CP
    {
        public int x;
        public int y;

        public CP(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public Vector2D to2D()
        {
            return new Vector2D(this.x, this.y);
        }
    }

    public class Drifter extends Pod
    {
        private final static int NB = 3;

        public Drifter(CP[] cps)
        {
            super(cps);
        }

        @Override
        public void decide(Pod[] others)
        {
            Vector2D ncp = this.cps[(this.nextId + 1) % this.cps.length].to2D();
            Vector2D cp = this.cps[this.nextId].to2D();

            // Vecteur entre le prochain CP et celui d'après
            Vector2D d2ncp = ncp.minus(cp);
            d2ncp = d2ncp.unitVector();

            // Calcul des positions dans les NB tours selon la vitesse actuelle
            Vector2D[] nt = new Vector2D[NB];

            boolean insideNext = false;
            int nb = -1;

            // On est au CP dans les NB prochains ?
            for (int i = 0; i < NB; i++)
            {
                nt[i] = this.p.plus(this.v.scalarMult(i));
                insideNext |= nt[i].minus(cp).length() < CPR;
                if (insideNext && nb == -1)
                    nb = i + 1;
            }

            boolean eNearby = false;
            for (int j = 0; j < 2; j++)
                eNearby |= others[j].p.minus(this.p).length() < FFR;

            // Vecteur entre le prochain CP
            Vector2D toMake = cp.minus(this.p);
            Vector2D toMakeAfter = ncp.minus(this.p);

            // calcul de l'apex
            // Vecteur entre le prochain CP et celui d'après
            Vector2D icp = ncp.minus(cp);
            icp = icp.unitVector();
            // Calcul du point sur le CP le plus proche du prochain CP
            Vector2D apex = cp.plus(icp.scalarMult(RISK * CPR));

            // Si dans le cercle de validation du prochain CP dans un des NB prochains coups
            if (insideNext)
            {
                this.t = 0;
                this.np = ncp.minus(this.v.scalarMult(NB));
            }
            else
            {
                this.t = 200;
                this.np = apex.minus(this.v);
            }
        }
    }

    public abstract class Pod
    {
        // Constantes
        public static final int CPR = 600; // check point Radius
        public static final int FFR = (400 * 2) + 200; // force field Radius
        public static final double RISK = 0.5; // check point Radius

        // inputs
        public Vector2D p = new Vector2D(0, 0);
        public Vector2D v = new Vector2D(0, 0);
        public int a = 0;
        public int nextId = 0;
        public CP[] cps = null;

        // outputs : nextpos and thrust
        Vector2D np = new Vector2D(0, 0);
        public int t = 0;

        public boolean shield = false;

        public Pod(CP[] cps)
        {
            this.cps = cps;
        }

        public abstract void decide(Pod[] others);

        public void shieldOn()
        {
            this.shield = true;
        }

        @Override
        public String toString()
        {
            if (this.shield)
            {
                this.shield = false;
                return (int) this.np.x + " " + (int) this.np.y + " " + "SHIELD";
            }
            return (int) this.np.x + " " + (int) this.np.y + " " + this.t;
        }

        public void upd(int x, int y, int vx, int vy, int a, int n)
        {
            this.p.x = x;
            this.p.y = y;
            this.v.x = vx;
            this.v.y = vy;
            this.a = a;
            this.nextId = n;
        }
    } // Fin Pod

    public class SimpleDrifter extends Pod
    {
        private final static int NB = 3;

        public SimpleDrifter(CP[] cps)
        {
            super(cps);
        }

        @Override
        public void decide(Pod[] others)
        {
            Vector2D ncp = this.cps[(this.nextId + 1) % this.cps.length].to2D();
            Vector2D cp = this.cps[this.nextId].to2D();

            // Vecteur entre le prochain CP et celui d'après
            Vector2D d2ncp = ncp.minus(cp);
            d2ncp = d2ncp.unitVector();

            // Calcul des positions dans les NB tours selon la vitesse actuelle
            Vector2D[] nt = new Vector2D[NB];

            nt[0] = this.p.plus(this.v);
            // On est au CP au prochain ?
            boolean insideNext = nt[0].minus(cp).length() < CPR;
            // On est au CP dans les NB-1 prochains ?
            for (int i = 1; i < NB; i++)
            {
                nt[i] = nt[i - 1].plus(this.v);
                insideNext |= nt[i].minus(cp).length() < CPR;
            }

            // calcul de l'apex
            // Vecteur entre le prochain CP et celui d'après
            Vector2D icp = ncp.minus(cp);
            icp = icp.unitVector();
            // Calcul du point sur le CP le plus proche du prochain CP
            Vector2D apex = cp.plus(icp.scalarMult(RISK * CPR));

            // Si dans le cercle de validation du prochain CP dans un des NB prochains coups
            if (insideNext)
            {// On coupe les gazs et on commence à s'orienter vers le prochain
                this.t = 0;
                this.np = ncp.minus(this.v.scalarMult(NB));
            }
            else
            { // On fonce vers le prochain
              // Amélioration de l'angle de sortie de virage à revoir
                this.t = 200;
                this.np = apex.minus(this.v.scalarMult(NB)).plus(icp.scalarMult(NB));
            }
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

        public boolean equals(Vector2D rhs)
        {
            return this.x == rhs.x && this.y == rhs.y;
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
