import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Scanner;

class PodRace
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
        Driver[] d = new Driver[2];
        for (int i = 0; i < 2; i++)
        {
            p[i] = new Pod(c, null);
            d[i] = new SimpleDrifter(p[i]);
            // d[i] = new SimpleDrifter(p[i]);
        }

        // The Ennemy
        Pod[] o = new Pod[2];
        for (int i = 0; i < 2; i++)
            o[i] = new Pod(c, null);

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
                d[i].decide(o);

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            System.out.println(p[0]);
            System.out.println(p[1]);
        }
    }

    public static void main(String args[])
    {
        new PodRace().run();
    }

    public class Beziers extends Drifter
    {

        public Beziers(Pod p_myPod)
        {
            super(p_myPod);
        }

        private Vector2D beziers2(double t, Vector2D[] ctr)
        {
            double t2 = t * t;
            double mt = 1 - t;
            double mt2 = mt * mt;
            return new Vector2D(ctr[0].x * mt2 + 2 * ctr[1].x * mt * t + ctr[2].x * t2,
                    ctr[0].y * mt2 + 2 * ctr[1].y * mt * t + ctr[2].y * t2);
        }

        private Vector2D beziers3(double t, Vector2D[] ctr)
        {
            double t2 = t * t;
            double t3 = t2 * t;
            double mt = 1 - t;
            double mt2 = mt * mt;
            double mt3 = mt2 * mt;
            return new Vector2D(ctr[0].x * mt3 + 3 * ctr[1].x * mt2 * t + 3 * ctr[2].x * mt * t2 + ctr[3].x * t3,
                    ctr[0].y * mt3 + 3 * ctr[1].y * mt2 * t + 3 * ctr[2].y * mt * t2 + ctr[3].y * t3);
        }

        @Override
        public Vector2D decide(Pod[] p_others)
        {
            int lastId = (this.p.nextId - 1 < 0 ? this.p.cps.length - 1 : this.p.nextId - 1);
            Vector2D ncp = this.p.cps[(this.p.nextId + 1) % this.p.cps.length].to2D();
            Vector2D cp = this.p.cps[this.p.nextId].to2D();
            Vector2D lcp = this.p.cps[lastId].to2D();

            // Vecteur entre le prochain CP et celui d'après
            Vector2D d2ncp = ncp.minus(cp).unitVector().scalarMult(Pod.CPR);

            // Determination des points de Controle:
            Vector2D a = cp.plus(lcp.minus(cp).unitVector().scalarMult(Pod.CPR));
            Vector2D b = cp.minus(d2ncp);
            Vector2D c = cp.plus(d2ncp);
            Vector2D d = ncp;

            System.err.println(a + "|" + b + "|" + c + "|" + d);

            Vector2D[] ctr = new Vector2D[3];
            ctr[0] = b.minus(a).scalarMult(3);
            ctr[1] = c.minus(b).scalarMult(3);
            ctr[2] = d.minus(c).scalarMult(3);

            System.err.println(Arrays.toString(ctr));

            // int next = (a.minus(this.p).length() < FFR?1:0);
            // next = (b.minus(this.p).length() < FFR?2:1);
            // next = (c.minus(this.p).length() < FFR?3:2);
            // next = (d.minus(this.p).length() < FFR?4:3);

            int next = (ctr[0].minus(this.p.p).length() < Pod.FFR ? 1 : 0);
            next = (ctr[1].minus(this.p.p).length() < Pod.FFR ? 2 : 1);
            next = (ctr[2].minus(this.p.p).length() < Pod.FFR ? 3 : 2);
            switch (next)
            {
                case 1:
                    this.setThrustAndOri(b.minus(this.p.p));
                    break;
                case 2:
                    this.setThrustAndOri(c.minus(this.p.p));
                    break;
                case 3:
                    this.setThrustAndOri(d.minus(this.p.p));
                    break;
                default:
                    this.setThrustAndOri(a.minus(this.p.p));
                    break;
            }

            return (new Vector2D(this.p.np)).unitVector().scalarMult(this.p.t);

        }

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

    public class Drifter extends SimpleDrifter
    {
        private final static int NB = 3;

        private final static double DOTSHIELD = 0.1;
        private Vector2D toDo = new Vector2D(0, 0);

        public Drifter(Pod p_myPod)
        {
            super(p_myPod);
        }

        @Override
        public Vector2D decide(Pod[] others)
        {
            Vector2D ncp = this.p.cps[(this.p.nextId + 1) % this.p.cps.length].to2D();
            Vector2D cp = this.p.cps[this.p.nextId].to2D();

            // Vecteur entre le prochain CP et celui d'après
            Vector2D d2ncp = ncp.minus(cp);

            // Estimations des positions dans les NB tours selon la vitesse actuelle
            Pod[] nt = new Pod[NB];

            boolean insideNext = false;
            int nb = -1;

            // Estimations des positions suivantes des ennemis
            Vector2D[][] ont = new Vector2D[2][NB];
            ont[0] = new Vector2D[NB];
            ont[1] = new Vector2D[NB];
            boolean[][] collision = new boolean[2][NB];
            collision[0] = new boolean[NB];
            collision[1] = new boolean[NB];
            boolean faceSpeedCollision = false;

            // On est au CP dans les NB prochains ?
            for (int i = 0; i < NB; i++)
            {
                if (i == 0)
                    nt[i] = this.p.oneStepAhead(new SimpleDrifter(this.p).decide(others));
                else
                    nt[i] = nt[i - 1].oneStepAhead(new SimpleDrifter(nt[i - 1]).decide(others));

                ont[0][i] = others[0].p.plus(others[0].v.scalarMult(i + 1));
                ont[1][i] = others[1].p.plus(others[1].v.scalarMult(i + 1));

                collision[0][i] = (ont[0][i].minus(nt[i].p).length() < Pod.FFR);
                collision[1][i] = (ont[1][i].minus(nt[i].p).length() < Pod.FFR);

                insideNext |= nt[i].p.minus(cp).length() < Pod.CPR;

                if (insideNext && nb == -1)
                    nb = i + 1;
            }

            faceSpeedCollision = (collision[0][0]
                    && this.p.v.unitVector().dotProduct(others[0].v.unitVector()) < DOTSHIELD)
                    || (collision[1][0] && this.p.v.unitVector().dotProduct(others[1].v.unitVector()) < DOTSHIELD);

            if (faceSpeedCollision)
                this.p.shieldOn();

            // Vecteur entre le prochain CP
            Vector2D toMake = cp.minus(this.p.p);
            // Vector2D toMakeAfter = ncp.minus(this.p);

            System.err.println(this.p.toDebug() + "\n1:" + nt[0].toDebug() + "\n2: " + nt[1].toDebug() + "\n3: "
                    + nt[2].toDebug() + " v:" + this.p.v + "\nC?:" + insideNext + "|in:" + nb);

            this.toDo = new Vector2D(0, 1);

            // Si dans le cercle de validation du prochain CP dans un des NB prochains coups
            if (insideNext)
                this.toDo = ncp.minus(this.p.v.scalarMult(NB)).unitVector();
            else
                this.toDo = cp.plus(d2ncp.unitVector().scalarMult(this.p.v.length()));
            // this.np = cp.minus(this.v.scalarMult(NB)).minus(icp.unitVector().scalarMult(this.v.length()));

            // Calcul du meilleur Thrust
            if (insideNext && nb > 1)
                this.toDo.setR(0);
            else
            {
                Vector2D ori = new Vector2D(0, 100);
                ori.setPolar(toMake.length(), (this.p.a / 180d) * Math.PI);
                // System.err.println(
                // "O:" + ori + "|T:" + toMake + "|prd:" + ori.unitVector().dotProduct(toMake.unitVector()));

                this.toDo.setR(Math.round(200d * ori.unitVector().dotProduct(toMake.unitVector())));

                // if (ori.dotProduct(toMake) < 0)
                // this.t = 50;
                // else
                // this.t = 200;
            }

            this.setThrustAndOri(this.toDo);

            return this.toDo;
        }

        protected void setThrustAndOri(Vector2D toDo)
        {
            this.p.t = (int) toDo.getR();
            this.p.np = this.p.p.plus(toDo);
        }
    }

    public interface Driver
    {
        public Vector2D decide(Pod[] others);
    }

    public class Pod
    {
        // Constantes
        public static final int CPR = 600; // check point Radius
        public static final int FFR = (400 * 2); // force field Radius
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

        protected String msg = "";

        public boolean shield = false;

        public Pod(CP[] cps, Driver d)
        {
            this.cps = cps;
        }

        public Pod(Pod p_p)
        {
            this.cps = p_p.cps;
            this.upd((int) p_p.p.x, (int) p_p.p.y, (int) p_p.v.x, (int) p_p.v.y, p_p.a, p_p.nextId);
        }

        protected Pod oneStepAhead(Vector2D d)
        {
            Pod pt = new Pod(this);
            pt.upd((int) this.p.x, (int) this.p.y, (int) this.v.x, (int) this.v.y, this.a, this.nextId);

            // System.err.print(" : " + pt.toDebug());

            // New Angle of the Pod
            Vector2D dir = new Vector2D(0, 1);
            dir.setTheta((pt.a / 180d) * Math.PI);
            Vector2D wangle = d.unitVector();
            double deltaangle = Math.acos(dir.dotProduct(wangle));

            double tx = d.length() * Math.cos(pt.a);
            double ty = d.length() * Math.sin(pt.a);

            pt.a = pt.a + (Math.abs(deltaangle) < 18 ? (int) deltaangle : (int) Math.signum(deltaangle) * 18);
            pt.p.x = Math.round(pt.p.x + pt.v.x + tx);
            pt.p.y = Math.round(pt.p.y + pt.v.y + ty);
            pt.v.x = (int) ((pt.v.x + tx) * .85);
            pt.v.y = (int) ((pt.v.y + ty) * .85);

            // System.err.println(" ---> " + pt.toDebug());

            return pt;
        }

        public void shieldOn()
        {
            this.shield = true;
        }

        public String toDebug()
        {
            return this.p + " " + this.a + " " + this.nextId;
        }

        @Override
        public String toString()
        {
            if (this.shield)
            {
                this.shield = false;
                return (int) this.np.x + " " + (int) this.np.y + " " + "SHIELD" + this.msg;
            }
            return (int) this.np.x + " " + (int) this.np.y + " " + this.t + this.msg;
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

    public class SimpleDrifter implements Driver
    {
        private final static int NB = 3;

        protected Pod p = null;

        public SimpleDrifter(Pod myPod)
        {
            this.p = myPod;
        }

        @Override
        public Vector2D decide(Pod[] others)
        {
            Vector2D ncp = this.p.cps[(this.p.nextId + 1) % this.p.cps.length].to2D();
            Vector2D cp = this.p.cps[this.p.nextId].to2D();

            // Vecteur entre le prochain CP et celui d'après
            Vector2D d2ncp = ncp.minus(cp);
            d2ncp = d2ncp.unitVector();

            // Calcul des positions dans les NB tours selon la vitesse actuelle
            Vector2D[] nt = new Vector2D[NB];

            nt[0] = this.p.p.plus(this.p.v);
            // On est au CP au prochain ?
            boolean insideNext = nt[0].minus(cp).length() < Pod.CPR;
            // On est au CP dans les NB-1 prochains ?
            for (int i = 1; i < NB; i++)
            {
                nt[i] = nt[i - 1].plus(this.p.v);
                insideNext |= nt[i].minus(cp).length() < Pod.CPR;
            }

            // Si dans le cercle de validation du prochain CP dans un des NB prochains coups
            if (insideNext)
            {// On coupe les gazs et on commence à s'orienter vers le prochain
                this.p.t = 0;
                this.p.np = ncp.minus(this.p.v.scalarMult(NB));
            }
            else
            { // On fonce vers le prochain
                // Amélioration de l'angle de sortie de virage à revoir
                this.p.t = 200;
                this.p.np = cp.minus(this.p.v.scalarMult(NB));
            }

            return (new Vector2D(this.p.np)).unitVector().scalarMult(this.p.t);
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
