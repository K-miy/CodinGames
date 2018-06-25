package medium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse the standard input according to the problem statement.
 **/
class Solution10
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        int N = in.nextInt();
        int C = in.nextInt();

        ArrayList<Integer> budgets = new ArrayList<>();
        int sum = 0;

        for (int i = 0; i < N; i++)
        {
            int b = in.nextInt();
            budgets.add(b);
            sum += b;
        }

        Collections.sort(budgets);

        ArrayList<Integer> val = new ArrayList<>();

        System.err.println(N + " pour " + C);

        if (sum < C) // somme des budgets inférieur au cout
            System.out.println("IMPOSSIBLE");
        else
        {
            int coutRestant = C;

            // while (coutRestant > 0 && val.size() != N)
            int coutMoyen = Math.round(coutRestant / (N - val.size()));// coutRestant / (N - val.size());

            int lastCout = coutRestant % (N - val.size());

            while (coutMoyen >= budgets.get(0) && coutRestant > 0 && val.size() != N)
            {
                System.err.println("Il reste des fauchés qui ne peuvent pas payer : " + coutMoyen);// + "/" + budgets);
                int i = 0;
                while (i < budgets.size() && budgets.get(i) <= coutMoyen)
                {
                    int budgetCourant = budgets.get(i);
                    val.add(budgetCourant);
                    coutRestant -= budgetCourant;

                    // System.err.println("cr:" + coutRestant + "/" + val);
                    if ((N - val.size()) > 0)
                    {
                        coutMoyen = Math.floorDiv(coutRestant, (N - val.size()));
                        lastCout = coutRestant % (N - val.size());
                    }

                    i++;
                }
                // tous ceux qui peuvent pas payer le cout moyen payent le cout max.
                // On enleve les fauchés
                if (i < budgets.size())
                    budgets = new ArrayList<Integer>(budgets.subList(i, budgets.size()));

                System.err.println("Après les " + val.size() + "/" + N + " fauchés, il reste à payer " + coutRestant
                        + " par " + budgets.size() + " et " + lastCout);

            }

            // Si tout le monde peut payer le cout moyen
            if (coutMoyen <= budgets.get(0))
            {
                System.err.println(budgets.size() + " peuvent payer : " + coutMoyen);
                int i = 0;
                while (i < budgets.size())
                {
                    // On répartit le modulo sur le reste des payeurs.
                    if (val.size() == N - lastCout)
                        coutMoyen += 1;

                    if (val.size() < N - 1)
                    {
                        val.add(coutMoyen);
                        coutRestant -= coutMoyen;
                    }
                    else // il reste lastCout à répartir sur les dernier
                    {
                        System.err.println(
                                "Le dernier doit payer " + coutRestant + "/" + budgets.get(budgets.size() - 1));
                        val.add(coutMoyen);
                        coutRestant = 0;
                    }
                    i++;
                }
            }

            // valdiation :
            int ss = 0;
            for (Integer k : val)
                ss += k;

            System.err.println("" + (ss == sum));

            for (Integer k : val)
                System.out.println(k);
        }

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        // System.out.println("IMPOSSIBLE");
    }
}
