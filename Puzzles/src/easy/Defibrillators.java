package easy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Solution9
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        int N = in.nextInt(); // Number of elements which make up the association table.
        in.nextLine();
        int Q = in.nextInt(); // Number Q of file names to be analyzed.
        in.nextLine();

        HashMap<String, String> extmap = new HashMap<String, String>();
        String[] files = new String[Q];

        for (int i = 0; i < N; i++)
        {
            String EXT = in.next(); // file extension
            String MT = in.next(); // MIME type.
            extmap.put(EXT.toLowerCase(), MT);
            in.nextLine();
        }
        System.err.println(N + " - " + extmap);

        for (int i = 0; i < Q; i++)
        {
            String FNAME = in.nextLine(); // One file name per line.
            files[i] = FNAME;
        }

        System.err.println(Q + " - " + Arrays.toString(files));
        for (int i = 0; i < Q; i++)
        {

            String fext = "";
            int p = files[i].lastIndexOf('.');
            if (p >= 0)
                fext = files[i].substring(p + 1);
            fext = fext.toLowerCase();
            System.err.println(files[i] + " -> " + fext + "(" + p + ")");
            // String[] fextTab = files[i].split("\\.(?=[^\\.]+$)");
            // System.err.println(Arrays.toString(fextTab));

            // String fext = "";
            // if(fextTab.length > 0)
            // fext = fextTab[fextTab.length-1];

            // System.err.println(fext);
            if (extmap.containsKey(fext))
                System.out.println(extmap.get(fext));
            else
                System.out.println("UNKNOWN");

        }
        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        // System.out.println("UNKNOWN"); // For each of the Q filenames, display on a line the corresponding MIME type.
        // If there is no corresponding type, then display UNKNOWN.
    }
}
