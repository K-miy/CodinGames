package easy;

import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement. --- Hint:
 * You can use the debug stream to print initialTX and initialTY, if Thor seems not follow your orders.
 **/
class Player2
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        int lightX = in.nextInt(); // the X position of the light of power
        int lightY = in.nextInt(); // the Y position of the light of power
        int initialTX = in.nextInt(); // Thor's starting X position
        int initialTY = in.nextInt(); // Thor's starting Y position

        int thorX = initialTX;
        int thorY = initialTY;

        // game loop
        while (true)
        {
            // int remainingTurns =
            in.nextInt(); // The remaining amount of turns Thor can move. Do not remove this line.

            String directionX = "", directionY = "";
            directionX = thorX > lightX ? "W" : "";
            directionX = thorX < lightX ? "E" : directionX;

            directionY = thorY > lightY ? "N" : "";
            directionY = thorY < lightY ? "S" : directionY;

            System.err.println(lightX + ":" + lightY + "|" + thorX + ":" + thorY + "|" + directionY + directionX);
            switch (directionX)
            {
                case "W":
                    thorX++;
                    break;
                case "E":
                    thorX--;
                    break;
            }
            switch (directionY)
            {
                case "N":
                    thorY--;
                    break;
                case "S":
                    thorY++;
                    break;
            }
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            System.err.println(thorX + ":" + thorY + "|" + directionY + directionX);
            System.out.println(directionY + directionX); // A single line providing the move to be made: N NE E SE S SW
                                                         // W or NW
        }
    }
}
