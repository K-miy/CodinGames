package medium;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

class ActionML implements Comparable<Action>
{
    private static double alpha = 0.95;
    private static double nRoll = 1;
    private static Random r = new Random(0);

    public int ang;

    public int thr;

    public double val;

    int n = 0;

    public ActionML(int r, int t)
    {

        this.ang = r;
        this.thr = t;

    }

    @Override
    public int compareTo(Action p_a)
    {
        return (int) (this.val - p_a.val);
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
        if (this.getClass() != obj.getClass())
            return false;
        Action other = (Action) obj;
        if (this.ang != other.ang)
            return false;
        if (this.thr != other.thr)
            return false;
        return true;
    }

    public double evaluate(StateML s)
    {

        long st = System.currentTimeMillis();
        this.n = 0;
        double tmax = 99.0 / s.possibilities().size();

        while (System.currentTimeMillis() - st < tmax)
        {
            this.val += this.rollout(s.nextState(this));
            this.n++;
        }

        this.val /= this.n;
        return this.val;
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
        result = prime * result + this.ang;
        result = prime * result + this.thr;
        return result;
    }

    public double rollout(StateML s)
    {
        int i = 150;
        StateML sn = s;

        ArrayList<ActionML> poss = new ArrayList<>(s.possibilities());

        double v = 0;

        while (i > 0 && sn != null && !Player12.obj(sn))
        {
            i--;
            poss = new ArrayList<>(sn.possibilities());
            v += sn.getReward();

            sn = sn.nextState(poss.get(r.nextInt(poss.size())));

        }

        if (sn == null)
            return v - i * 100;
        else if (Player12.obj(sn))
            return v + 10000;
        else
            return v;
    }

    /**
     * Redéfinition.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return this.ang + " " + this.thr;
    }

}

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Player12
{
    public static Vector2D[] flat = new Vector2D[2];

    public static Polygon terrain;

    public void run()
    {
        Scanner in = new Scanner(System.in);
        int surfaceN = in.nextInt(); // the number of points used to draw the surface of Mars.

        Vector2D[] land = new Vector2D[surfaceN];

        for (int i = 0; i < surfaceN; i++)
        {
            int landX = in.nextInt(); // X coordinate of a surface point. (0 to 6999)
            int landY = in.nextInt(); // Y coordinate of a surface point. By linking all the points together in a
            // sequential fashion, you form the surface of Mars.
            land[i] = new Vector2D(landX, landY);
            if (flat[0] != null && flat[0].y == land[i].y && flat[0].x != land[i].x)
                flat[1] = land[i];
            if (flat[1] == null)
                flat[0] = land[i];
        }

        calculTerrain(land);

        System.err.println(Arrays.toString(land));
        System.err.println(Arrays.toString(flat));

        // int H = (int) flat[0].y;
        // int Xmin = (int) flat[0].x;
        // int Xmax = (int) flat[1].x;
        // int nPow = 0;
        // int nRot = 0;

        // Vector2D obj = flat[0].plus(flat[1].minus(flat[0]).scalarMult(0.5));

        // State[] seq = new State[2];
        // State[] real = new State[2];
        // Action toDo = null;

        // game loop
        while (true)
        {
            int X = in.nextInt();
            int Y = in.nextInt();
            int hSpeed = in.nextInt(); // the horizontal speed (in m/s), can be negative.
            int vSpeed = in.nextInt(); // the vertical speed (in m/s), can be negative.
            int fuel = in.nextInt(); // the quantity of remaining fuel in liters.
            int rotate = in.nextInt(); // the rotation angle in degrees (-90 to 90).
            int power = in.nextInt(); // the thrust power (0 to 4).

            Vector2D pos = new Vector2D(X, Y);
            Vector2D sp = new Vector2D(hSpeed, vSpeed);

            State s = new State(pos, sp, rotate, power, fuel);
            TreeMap<Action, Double> eval = new TreeMap<>();
            for (Action a : s.possibilities())
                eval.put(a, a.evaluate(s));

            System.err.println("S : " + s);
            for (Action a : eval.keySet())
                System.err.println("A : " + a + " v : " + String.format("%.2f", a.val) + "(" + a.n + ")");

            // real[1] = new State(pos, sp, rotate, power);
            //
            // if (X < Xmin && hSpeed < 30)
            // nRot = -22;
            // else if (X > Xmax && hSpeed > -30)
            // nRot = 22;
            // else if (X < Xmax && X > Xmin && hSpeed < -19)
            // nRot = -22;
            // else if (X < Xmax && X > Xmin && hSpeed > 19)
            // nRot = 22;
            // else
            // nRot = 0;
            //
            // if ((vSpeed < -39) || (nRot != 0))
            // nPow = 4;
            // else
            // nPow = 0;
            //
            // if (seq[0] != null && toDo != null)
            // seq[1] = seq[0].nextState(toDo);
            //
            // System.err.println("Valid prediction : Action : " + toDo);
            // System.err.println(seq[0] + " -> " + seq[1]);
            // System.err.println(real[0] + " -> " + real[1]);
            //
            // seq[0] = real[1];
            // real[0] = real[1];

            // else if()
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            // toDo = new Action(nRot, nPow);

            // System.out.println(nRot + " " + nPow); // rotate power. rotate is the desired rotation angle. power is
            // the desired thrust power.
            System.out.println(eval.lastKey());

        }
    }

    private static void calculTerrain(Vector2D[] land)
    {
        int[] xp = new int[land.length + 2];
        int[] yp = new int[land.length + 2];

        xp[0] = 3000;
        yp[0] = 0;
        for (int i = 1; i < land.length + 1; i++)
        {
            xp[i] = 3000 - (int) land[i - 1].y;
            yp[i] = (int) land[i - 1].x;
        }
        xp[land.length + 1] = 3000;
        yp[land.length + 1] = 7000;

        terrain = new Polygon(xp, yp, land.length + 2);

    }

    public static boolean insideTerrain(StateML s)
    {
        return terrain.contains(3000 - s.pos.y, s.pos.x) || s.pos.y > 3000 || s.pos.y < 0 || s.pos.x < 0
                || s.pos.x > 7000;
    }

    public static void main(String args[])
    {
        new Player12().run();
    }

    public static boolean obj(StateML s)
    {
        if (s == null)
            return false;

        boolean hs = s.sp.x < 19 && s.sp.x > -19;
        boolean vs = s.sp.y < 39 && s.sp.y > -39;
        boolean pos = s.pos.x > flat[0].x && s.pos.x < flat[1].x;
        boolean r = s.rot == 0;

        return (hs && vs && pos && r);
    }

}

class StateML
{
    private static final Vector2D GRAV = new Vector2D(0, -3.711);
    private static final HashMap<StateML, HashSet<ActionML>> MEM = new HashMap<>();

    public Vector2D pos;
    public Vector2D sp;
    public int rot;
    public int th;
    public int fuel;

    public StateML(StateML p_state)
    {
        this.pos = new Vector2D(p_state.pos);
        this.sp = new Vector2D(p_state.sp);
        this.rot = p_state.rot;
        this.th = p_state.th;
        this.fuel = p_state.fuel;
    }

    public StateML(Vector2D pp, Vector2D ss, int r, int t, int f)
    {
        this.pos = pp;
        this.sp = ss;
        this.rot = r;
        this.th = t;
        this.fuel = f;
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
        if (this.getClass() != obj.getClass())
            return false;
        State other = (State) obj;
        if (this.rot != other.rot)
            return false;
        if (this.th != other.th)
            return false;
        return true;
    }

    public double getReward()
    {
        double v = 0;

        Vector2D obj = Player12.flat[0].plus(Player12.flat[1].minus(Player12.flat[0]).scalarMult(0.5));

        boolean hs = this.sp.x < 19 && this.sp.x > -19;
        boolean vs = this.sp.y < 39 && this.sp.y > -39;
        boolean px = this.pos.x > Player12.flat[0].x && this.pos.x < Player12.flat[1].x;
        boolean py = this.pos.y > Player12.flat[0].y && this.pos.y < Player12.flat[1].y + 500;
        boolean r = this.rot == 0;

        v -= this.pos.minus(obj).getR() / 100;
        // v -= (Player.insideTerrain(this) ? 15 : 0);
        // v -= (px && py ? Math.abs(this.rot) / 5 : 0);

        // v += (hs ? 1 : 0);
        // v += (vs ? 4 : 0);
        // v += (px ? 5 : 0);
        // v += (px && py && vs && r ? 10 : 0);
        // v += (px && py && r ? 10 : 0);
        // v += (px && r ? 20 : 0);

        v += (hs && vs && py && px && r ? 30 : 0);

        v += this.fuel;

        return v;

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
        result = prime * result + this.rot;
        result = prime * result + this.th;
        return result;
    }

    public StateML nextState(ActionML a)
    {

        if (Player12.insideTerrain(this))
            return null;

        StateML s = new StateML(this);

        int dr = (a.ang - s.rot);
        int dt = (a.thr - s.th);

        if (dr > 15)
            dr = 15;
        else if (dr < -15)
            dr = -15;

        if (dt > 1)
            dt = 1;
        else if (dt < -1)
            dt = -1;

        s.rot = this.rot + dr;
        s.th = this.th + dt;

        Vector2D thrust = new Vector2D(0, 1);
        thrust.setPolar(s.th, Math.toRadians(s.rot + 90));
        Vector2D acc = GRAV.plus(thrust);

        // System.err.println("State speed: " + s.sp + "| thrust: " + thrust + "| acc: " + acc);

        s.sp = s.sp.plus(acc).toRound();
        s.pos = s.pos.plus(s.sp);

        // System.err.println("nextState speed: " + s.sp);

        if (s.th > 4)
            s.th = 4;
        if (s.th < 0)
            s.th = 0;

        s.fuel = s.fuel - s.th;
        // s.rot = s.rot % 360 - 180;

        return s;
    }

    public HashSet<ActionML> possibilities()
    {
        if (this.pos.x > Player12.flat[0].x && this.pos.x < Player12.flat[1].x && this.sp.x < 19 && this.sp.x > -19)
        {
            int[] actthr = { this.th + 1, this.th, this.th - 1 };
            HashSet<ActionML> poss = new HashSet<>();
            for (int thr : actthr)
            {

                ActionML a = new ActionML(0, thr);
                if (a.ang <= 90 && a.ang >= -90 && a.thr <= 4 && a.thr >= 0)
                    poss.add(a);
            }
            return poss;

        }

        if (MEM.get(this) != null)
            return MEM.get(this);

        int[] actang = { this.rot - 15, this.rot, this.rot + 15 };
        int[] actthr = { this.th + 1, this.th, this.th - 1 };

        HashSet<ActionML> poss = new HashSet<>();
        for (int ang : actang)
            for (int thr : actthr)
            {
                ActionML a = new ActionML(ang, thr);

                if (a.ang <= 90 && a.ang >= -90 && a.thr <= 4 && a.thr >= 0)
                    poss.add(a);

            }

        MEM.put(this, poss);

        return poss;
    }

    @Override
    public String toString()
    {
        return "[" + this.pos + "," + this.sp + "," + this.rot + "," + this.th + "," + this.fuel + "]";
    }
}
