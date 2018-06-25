import java.awt.geom.Point2D;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class PodRacing
{

    public void run()
    {
        Scanner in = new Scanner(System.in);
        // int laps =
        in.nextInt();
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
            {
                p[i].decide(o);
                System.err.println(p[i].toDebugString());
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            System.out.println(p[0]);
            System.out.println(p[1]);
        }
    }

    public static void main(String args[])
    {
        new PodRacing().run();
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

        // current lap
        public int clap = 0;

        // outputs : nextpos and thrust
        Vector2D np = new Vector2D(0, 0);
        public int t = 0;

        public boolean boost = false;
        public boolean shield = false;

        public Pod(CP[] cps)
        {
            this.cps = cps;
        }

        public void boostOn()
        {
            this.boost = true;
        }

        public abstract void decide(Pod[] others);

        private int getRank()
        {
            // TODO Auto-generated method stub
            return -1;
        }

        public int nextCheckpointAngle()
        {
            Vector2D nextCPtoPos = this.cps[this.nextId].to2D().minus(this.p);
            int angle3 = (int) ((Math.atan2(nextCPtoPos.y, nextCPtoPos.x) * 180 / Math.PI) + 360) % 360;
            return this.a - angle3;
        }

        public void shieldOn()
        {
            this.shield = true;
        }

        public String toDebugString()
        {
            return this.p + "," + this.v + "," + this.a + "," + this.nextCheckpointAngle() + "," + this.getRank();
        }

        @Override
        public String toString()
        {
            if (this.shield)
            {
                this.shield = false;
                return (int) this.np.x + " " + (int) this.np.y + " " + "SHIELD";
            }
            if (this.boost)
            {
                this.boost = false;
                return (int) this.np.x + " " + (int) this.np.y + " " + "BOOST";
            }
            return (int) this.np.x + " " + (int) this.np.y + " " + this.t;
        }

        public void upd(int x, int y, int vx, int vy, int a, int n)
        {
            if (this.nextId == 0 && n == 1)
                this.clap++;

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
                this.np = ncp.minus(this.v.scalarMult(NB));
            else
                // Amélioration de l'angle de sortie de virage à revoir
                this.np = apex.minus(this.v.scalarMult(NB)).plus(icp.scalarMult(NB));

            // Thrusting
            if (insideNext)
                this.t = 0;
            else if (Math.abs(this.nextCheckpointAngle()) < 30)
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

// Genome description
// In the case of Coders Strike Back, I considered each gene to be a set of instructions for a drone. So a typical
// genome could be read as:
//
// [(no shield, angle = 12°, thrust = 200), (no shield, angle = 18°, thrust = 150), (shield, angle = 18°, thrust
// ignored)]
//
// In practice, I simulated the game state up to 6 turns in the future, so there would be 6 triplets of instructions in
// one drone’s genome. The genome itself is encoded as an array of doubles between 0.0 and 1.0. So a raw genome is
// actually closer to this:
//
// {(0.08, 0.43, 0.07), (0.27, 0.55, 0.15), (0.18, 0.07, 0.02), (0.51, 0.97, 0.43), (0.15, 0.69, 0.99), (0.33, 0.05,
// 0.01)};
//
// The algorithm reads it like so, in pseudo code:
//
// for each triplet (gene1, gene2, gene3)
// if(gene1 > 0.95)
// requestShield();
// if(gene2 < 0.25)
// requestAngle(-18);
// else if(gene2 > 0.75)
// requestAngle(18);
// else
// requestAngle(-18 + 36 * ((gene2 - 0.25) * 2.0));
// if(gene3 < 0.25)
// requestThrust(0);
// else if(gene3 > 0.75)
// requestThrust(200);
// else
// requestThrust(200 * ((gene3 - 0.25) * 2.0));
//
// Specifying the 0.25 and 0.75 limits to the genes makes it select way more often the extreme angles and thrust values,
// which intuitively seemed like values with extra importance.
// Shared genome
// Actually, I concatenated both drones’ genomes into a unique shared genome, a series of 36 genes. The first 18 are for
// drone1, the last 18 for drone2. By considering both drones as a single entity, I could evolve cooperative behavior
// that would otherwise be impossible if each drone was evolved separately. That’s where all the “wow!” moments from the
// replays come from, it’s all emergent behavior. I was amazed when I first saw one of my drones collide with the other
// drone on purpose to give it a boost to it’s next checkpoint! I sometimes felt like I was watching a pool game instead
// of a drone race...
// Direct bot
// Remember how I said I could switch AIs easily? Well I coded a controlling bot (no genetic algorithms here, just basic
// if/then/else) that would target the next checkpoint for my first drone, and would target the opponent’s best drone
// for my second drone. By the way, when I write “first” and “second” drone, I mean the first and second with regards to
// the distance to the finish line, not the order in which they are given in input.
//
// Specifying this bot as my main bot allowed me to validate its behavior in the IDE. It would effectively turn to face
// its target (checkpoint or opponent drone) and go full thrust towards it. Not very effective, as it would overshoot
// the checkpoint and waste a lot of time turning around, but as a first approximation of the opponent’s behavior it was
// good enough.
//
// So my first version of the genetic algorithm would evolve a series of controlling genes for both of my drones, trying
// to beat a virtual opponent controlled by this “direct bot.” This worked pretty well for a good part of the week, and
// I spent my time improving the performance and number of simulations I could run in the allotted timespan.
//
// Evaluation function
//
// I tried a lot of different evaluation functions, but eventually settled for something very simple. I believe the most
// important in this game is having the biggest difference in checkpoints between the two teams, and then having the
// biggest distance to finish line difference between the two teams. The rest is just minor adjustments to achieve this
// goal. So to compare two genomes, and select the best of the two, I compare the checkpoint differences. Only if they
// are equal do I compare the distance to finish line difference. And if those are equal, I chose the one where my
// second drone is closest to the opponent’s next checkpoint (or the one after the next, if it can’t reach the next
// checkpoint before the opponent.)
//
// There was also a few lines of code in case of danger of timeout, to force both drones to target their respective next
// checkpoints, and in case the opponent was in risk of timeout, to prevent both opponent drones from reaching their
// respective next checkpoints.
//
// I was quite happy with my discrete evaluation. There was no need for coefficients to apply to each parameter, keeping
// it simple and efficient.
// First improvement: use direct bot in genome
// I was dissatisfied by how long my genetic algorithm took to evolve a basic control scheme. For the first tens of
// milliseconds, the genomes were completely random, wasting precious time resources. I modified the “shield gene” to
// make it more into a “meta gene”. The rules for the angle and thrust stayed the same, but now the “shield gene” had
// much more meaning than just shield on or shield off, as it could switch the drone control over to the direct bot:
//
// if(gene1 > 0.95)
// requestShield()
// else if(gene1 < 0.3 && drone is first drone)
// use direct bot for control to next checkpoint
// else if(gene1 < 0.3 && drone is second drone)
// use direct bot to target best opponent
// else if(gene1 < 0.2 && drone is second drone)
// use direct bot to target best opponent's next checkpoint
// else if(gene1 < 0.1 && drone is second drone)
// use direct bot to target best opponent's next next checkpoint
// else
// use gene2 and gene3 to control angle and thrust
//
//
// After the input phase, I also fill one of the genomes with the value 0.3 so that it effectively uses only the direct
// bot at each turn.
//
// There was an immediate gain in performance. Now, even at generation 0, there was at least one genome which would
// control a coherent behavior. Printing out the genome in the debug logs at the end of turn showed that about a third
// of the genes used the direct bot.
// Second improvement: evolve opponent
// The second major improvement I did was use the genetic algorithm the evolve the opponent for some time.
//
// Effectively, for the first 30ms of a turn, I would pretend I was the opponent, and evolve a good strategy to counter
// the direct bot. I would then switch back to my own drone, and evolve them against the opponent drones controlled by
// their best genome.
//
// That provided another huge performance gain. My leading drone would now often accurately predict that the opponent
// would try to block it, and go around the opponent instead of blindly slamming into it over and over again.
//
//
// Optimization for speed and win ratio
//
// Throughout the week, I was continuously trying to get more simulations out my AI. I used Callgrind and Kcachegrind
// extensively to identify bottlenecks in the simulation code. One of the improvements I implemented was to use a cache
// for the direct bot calculations, so as not to waste time recalculating 10,000 times the same output angle and thrust
// for the given input position, speed, and angle. At the end, I could evolve around 1,500 total generations per round
// (shared between the opponent’s and my drones). So with a population of 10, that means 15,000 game simulations with a
// prediction of 6 turns in the future.
//
// I also did many tests in the IDE against the top 5 players to select my best parameters. The number of simulation
// rounds, the 0.25 and 0.75 bounds for the genes, the genetic population size, the time allotted to evolve the
// opponent… were all set so as to maximize the win ratio.
