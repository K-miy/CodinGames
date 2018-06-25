package tools;

import java.util.List;
import java.util.TreeMap;

/**
 * Best First Search with Branch and Bound
 *
 * @author C. Besse
 */
public class BFSalgo
{

    public static BFSAction solve(BFSState p_init)
    {

        TreeMap<BFSState, Integer> open = new TreeMap<>();

        for (BFSAction a : p_init.actions())
        {
            BFSState next = p_init.nextState(a);
            open.put(next, a.cost() + next.heuristic());
        }

        int lowerBound = p_init.lowerBound();

        while (!open.isEmpty() && !open.firstKey().isEndState())
        {
            BFSState current = open.pollFirstEntry().getKey();
            // db("BFS2 : " + Arrays.toString(this.generateActions(current.s)));
            for (BFSAction a : current.actions())
            {
                BFSState next = current.nextState(a);
                int c_lower = next.lowerBound();
                if (c_lower >= lowerBound)
                {
                    open.put(next, c_lower);
                    lowerBound = c_lower;
                }
            }
        }

        if (!open.isEmpty())// DONC open.firstKey().isEndState()
        {
            BFSState b = open.firstKey();
            if (b.prev == null)
                System.err.println("--- > " + b);
            else
                System.err.println(b + "---+>" + b.prev);

            while (b.prev.prev != null)
                b = b.prev;

            return b.prevA;
        }
        else
            System.err.println("I'm probably SCREWED ... ");

        return null;

    }

    public static abstract class BFSAction
    {
        public abstract int cost();
    }

    public static abstract class BFSState
    {
        BFSState prev = null;
        BFSAction prevA = null;

        public abstract BFSState next(BFSAction p_a);

        public BFSState nextState(BFSAction p_a)
        {
            BFSState next = this.next(p_a);
            next.prev = this;
            next.prevA = p_a;
            return next;
        }

        public abstract List<BFSAction> actions();

        public abstract boolean isEndState();

        public abstract int heuristic();

        public abstract int lowerBound();

    }

}
