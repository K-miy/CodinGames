package easy;

import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Player4
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        int road = in.nextInt(); // the length of the road before the gap.
        int gap = in.nextInt(); // the length of the gap.
        in.nextInt(); // the length of the landing platform.

        // game loop
        while (true)
        {
            int speed = in.nextInt(); // the motorbike's speed.
            int coordX = in.nextInt(); // the position on the road of the motorbike.

            if (speed < gap + 1 && coordX < road)
                System.out.println("SPEED");
            else if (coordX + speed > road && coordX < road)
                System.out.println("JUMP");
            else if (coordX > road || speed > gap + 1)
                System.out.println("SLOW");
            else
                System.out.println("WAIT");

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

        }
    }
}
