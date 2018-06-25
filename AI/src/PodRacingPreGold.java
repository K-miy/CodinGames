import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class PodRacingPreGold
{

    public void run()
    {
        Scanner in = new Scanner(System.in);

        Pod myp = new SimpleDrifter();
        Pod op = new SimpleDrifter();

        // game loop
        while (true)
        {
            int x = in.nextInt();
            int y = in.nextInt();
            int nextCheckpointX = in.nextInt(); // x position of the next check point
            int nextCheckpointY = in.nextInt(); // y position of the next check point
            // distance to the next checkpoint
            // int nextCheckpointDist =
            in.nextInt();
            // angle between your pod orientation and the direction of the next checkpoint
            int nextCheckpointAngle = in.nextInt();
            int opponentX = in.nextInt();
            int opponentY = in.nextInt();

            System.err.println(x + "," + y + "," + nextCheckpointX + "," + nextCheckpointY + "," + nextCheckpointAngle);
            myp.upd(x, y, nextCheckpointX, nextCheckpointY, nextCheckpointAngle);
            op.upd(opponentX, opponentY, x, y, 0);

            myp.decide(op);

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            // You have to output the target position
            // followed by the power (0 <= thrust <= 100)
            // i.e.: "x y thrust"
            System.out.println(myp);
        }
    }

    public static void main(String args[])
    {
        new PodRacingPreGold().run();
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
        public Vector2D nextCP = null;
        public int na;

        boolean boost = false;
        boolean CPcomplete = false;
        public ArrayList<Vector2D> CP = new ArrayList<>();

        // outputs : nextpos and thrust
        Vector2D np = new Vector2D(0, 0);
        public int t = 0;

        public abstract void decide(Pod other);

        @Override
        public String toString()
        {
            return (int) this.np.x + " " + (int) this.np.y + " " + (this.boost ? "BOOST" : this.t);
        }

        public void upd(int x, int y, int nx, int ny, int na)
        {
            this.v.x = x - this.p.x;
            this.v.y = y - this.p.y;
            this.p.x = x;
            this.p.y = y;

            this.na = na;

            Vector2D nCP = new Vector2D(nx, ny);

            // Change of next CheckPoint
            if (!(nCP.equals(this.nextCP)))
                this.nextCP = nCP;
            // Memorization of CPs if not existant
            if (!this.CP.contains(nCP))
                this.CP.add(nCP); // else verification if not around.
            else if (this.CP.size() > 1 && nCP.equals(this.CP.get(0)))
                this.CPcomplete = true;

            this.boost = false;
        }
    } // Fin Pod

    public class SimpleDrifter extends Pod
    {
        private final static int NB = 4;

        public SimpleDrifter()
        {
            super();
        }

        @Override
        public void decide(Pod other)
        {
            Vector2D cp = this.nextCP;

            Vector2D ncp = null;
            if (this.CP.indexOf(cp) != this.CP.size() - 1 || this.CPcomplete)
                ncp = this.CP.get((this.CP.indexOf(cp) + 1) % this.CP.size());

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

            if (ncp != null)
            {
                // calcul de l'apex
                // Vecteur entre le prochain CP et celui d'après
                Vector2D icp = ncp.minus(cp);
                icp = icp.unitVector();
                // Calcul du point sur le CP le plus proche du prochain CP
                Vector2D apex = cp.plus(icp.scalarMult(RISK * CPR));

                // Si dans le cercle de validation du prochain CP dans un des NB prochains coups
                if (insideNext)
                    this.np = ncp.minus(this.v.scalarMult(NB));
                else
                    // Amélioration de l'angle de sortie de virage à revoir
                    this.np = apex.minus(this.v.scalarMult(NB)).plus(icp.scalarMult(NB));
            }
            else // Si dans le cercle de validation du prochain CP dans un des NB prochains coups
                this.np = this.nextCP;

            if (insideNext)
                this.t = 0;
            else if (Math.abs(this.na) < 30)
            {
                this.t = 100;
                this.boost = true;
            }
            else
                // Amélioration de l'angle de sortie de virage à revoir
                this.t = 20;

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
