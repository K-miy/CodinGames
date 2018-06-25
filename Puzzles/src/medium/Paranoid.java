package medium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Player4
{
    HashMap<Integer, ArrayList<Elevator>> e = new HashMap<>();

    public boolean isElevator(int f, int p, String dir)
    {
        ArrayList<Elevator> l = this.e.get(f);

        boolean a = false;

        if (dir.equals("LEFT"))
            for (Elevator e : l)
                a |= e.p < p;
        else if (dir.equals("RIGHT"))
            for (Elevator e : l)
                a |= e.p > p;

        return a;
    }

    public void run()
    {
        Scanner in = new Scanner(System.in);

        int nbFloors = in.nextInt(); // number of floors

        for (int i = 0; i < nbFloors; i++)
            this.e.put(i, new ArrayList<Elevator>());

        int width = in.nextInt(); // width of the area
        in.nextInt(); // maximum number of rounds

        int exitFloor = in.nextInt(); // floor on which the exit is found
        int exitPos = in.nextInt(); // position of the exit on its floor

        Elevator exit = new Elevator(exitFloor, exitPos);
        this.e.get(exitFloor).add(exit);

        in.nextInt(); // number of generated clones
        in.nextInt(); // ignore (always zero)

        int nbElevators = in.nextInt(); // number of elevators
        for (int i = 0; i < nbElevators; i++)
        {
            int elevatorFloor = in.nextInt(); // floor on which this elevator is found
            int elevatorPos = in.nextInt(); // position of the elevator on its floor

            Elevator el = new Elevator(elevatorFloor, elevatorPos);
            this.e.get(elevatorFloor).add(el);
        }

        // game loop
        while (true)
        {
            int cloneFloor = in.nextInt(); // floor of the leading clone
            int clonePos = in.nextInt(); // position of the leading clone on its floor
            String direction = in.next(); // direction of the leading clone: LEFT or RIGHT

            String s = "WAIT";

            if (!this.isElevator(cloneFloor, (direction.equals("RIGHT") ? clonePos - 1 : clonePos + 1), direction))
                s = "BLOCK";
            else
            {
                if (clonePos == 0 && direction.equals("LEFT"))
                    s = "BLOCK";

                if (clonePos == width - 1 && direction.equals("RIGHT"))
                    s = "BLOCK";
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            System.out.println(s); // action: WAIT or BLOCK
        }

    }

    public static void main(String args[])
    {
        new Player4().run();
    }

    class Elevator
    {
        final public int f;
        final public int p;

        public Elevator(int ff, int pp)
        {
            this.f = ff;
            this.p = pp;
        }
    }
}
