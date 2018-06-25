package easy;

import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Player5
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        int surfaceN = in.nextInt(); // the number of points used to draw the surface of Mars.
        for (int i = 0; i < surfaceN; i++)
        {
            in.nextInt(); // X coordinate of a surface point. (0 to 6999)
            in.nextInt(); // Y coordinate of a surface point. By linking all the points together in a
                          // sequential fashion, you form the surface of Mars.
        }

        // game loop
        while (true)
        {
            in.nextInt();
            int Y = in.nextInt();
            in.nextInt(); // the horizontal speed (in m/s), can be negative.
            int vSpeed = in.nextInt(); // the vertical speed (in m/s), can be negative.
            in.nextInt(); // the quantity of remaining fuel in liters.
            in.nextInt(); // the rotation angle in degrees (-90 to 90).
            in.nextInt(); // the thrust power (0 to 4).

            int thrust = 0;

            if (vSpeed < -39 && Y < 2234)
                thrust = 4;

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            System.out.println("0 " + thrust); // 2 integers: rotate power. rotate is the desired rotation angle (should
                                               // be 0 for level 1), power is the desired thrust power (0 to 4).
        }
    }
}
