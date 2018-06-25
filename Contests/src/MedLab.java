import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Bring data on patient samples from the diagnosis machine to the laboratory with enough molecules to produce medicine!
 **/
class MedLab
{
    static Scanner in = new Scanner(System.in);
    final static boolean DEBUG = true;
    final static Random RAND = new Random();

    final static int MAX_MOL = 10;
    final static int MAX_SAMPLE = 3;

    static int tour = 0;
    static Project[] proj;
    static LabState orig = new LabState();

    public static void sep(Object s)
    {
        if (DEBUG)
            System.err.println(tour + ": " + s);
    }

    public static void read(Robot[] r, LabState l, HashMap<Integer, Sample> s)
    {
        // Robot State

        for (int i = 0; i < 2; i++)
        {
            r[i].toMake.clear();

            r[i].upd(in.next(), in.nextInt(), in.nextInt());
            // String target = in.next();
            // int eta = in.nextInt();
            // int score = in.nextInt();
            r[i].updMolS(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
            // int storageA = in.nextInt();
            // int storageB = in.nextInt();
            // int storageC = in.nextInt();
            // int storageD = in.nextInt();
            // int storageE = in.nextInt();
            r[i].updMolE(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
            // int expertiseA = in.nextInt();
            // int expertiseB = in.nextInt();
            // int expertiseC = in.nextInt();
            // int expertiseD = in.nextInt();
            // int expertiseE = in.nextInt();
        }
        // Lab State
        l.updMol(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
        if (tour == 1)
            orig.updMol(l.s[0], l.s[1], l.s[2], l.s[3], l.s[4]);

        // sep(l);
        // int availableA = in.nextInt();
        // int availableB = in.nextInt();
        // int availableC = in.nextInt();
        // int availableD = in.nextInt();
        // int availableE = in.nextInt();

        // Samples State
        // Complete state ... no need to keep anything.
        s.clear();
        int sampleCount = in.nextInt();
        for (int i = 0; i < sampleCount; i++)
        {
            int sampleId = in.nextInt();

            Sample ss = s.get(sampleId);
            if (ss == null)
            {
                ss = new Sample(sampleId);
                s.put(sampleId, ss);
            }

            ss.upd(in.nextInt(), in.nextInt(), in.next(), in.nextInt());

            // Upd Sample in possession of robots
            if (ss.o != -1)
                r[ss.o].toMake.put(ss.id, ss);

            // int carriedBy = in.nextInt();
            // int rank = in.nextInt();
            // String expertiseGain = in.next();
            // int health = in.nextInt();
            ss.updMol(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
            // int costA = in.nextInt();
            // int costB = in.nextInt();
            // int costC = in.nextInt();
            // int costD = in.nextInt();
            // int costE = in.nextInt();
            // sep(ss);
        }

        sep(Arrays.toString(proj));
        sep(r[0]);
        sep(r[1]);

    }

    public static void main(String args[])
    {
        int projectCount = in.nextInt();
        proj = new Project[projectCount];
        for (int i = 0; i < projectCount; i++)
        {
            proj[i] = new Project();
            proj[i].updMol(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
        }
        // int a = in.nextInt();
        // int b = in.nextInt();
        // int c = in.nextInt();
        // int d = in.nextInt();
        // int e = in.nextInt();

        Robot[] rob = { new Robot(), new Robot() };
        LabState lab = new LabState();
        HashMap<Integer, Sample> s = new HashMap<>();

        IStrategy strat = new Greedy();

        tour = 0;
        // game loop
        while (true)
        {
            tour++;
            read(rob, lab, s);

            Action a = strat.decide(rob, lab, s);

            System.out.println(a);
        }
    }

    static class Greedy implements IStrategy
    {
        @Override
        public Action decide(Robot[] p_r, LabState p_l, HashMap<Integer, Sample> p_s)
        {
            Action a = null;

            Robot me = p_r[0];
            Robot him = p_r[1];

            // Not on any Module
            if (me.eta > 1)
                return new Action();

            // Count available Samples
            List<Sample> cloud = p_s.values().stream().filter(ss -> ss.o == -1).collect(Collectors.toList());
            List<Sample> decloudable = p_s.values().stream().filter(ss -> ss.o == -1 && ss.sumActualCost(me) == 0)
                    .collect(Collectors.toList());
            List<Sample> mine = p_s.values().stream().filter(ss -> ss.o == 0).collect(Collectors.toList());
            List<Sample> his = p_s.values().stream().filter(ss -> ss.o == 1).collect(Collectors.toList());

            if (mine.size() != me.toMake.size())
                sep("FUCK !!! mine & me.toMake != ---------------------------------------------- > ");

            // for (Sample ss : cloud)
            // sep(" ===> " + ss + " : " + Arrays.toString(ss.actualCostTo(me)));
            for (Sample ss : decloudable)
                sep(" ===> " + ss + " : " + Arrays.toString(ss.actualCostTo(me)));
            for (Sample ss : mine)
                sep(" +++> " + ss + " : " + Arrays.toString(ss.actualCostTo(me)));
            for (Sample ss : his)
                sep(" ---> " + ss + " : " + Arrays.toString(ss.actualCostTo(him)));

            boolean needDiag = me.needDiag();
            boolean allUndiag = me.allUndiag();
            boolean enoughForOne = me.enoughForOne();
            boolean needMol = me.needMol();
            boolean molAvailable = me.molAvailable(p_l);
            int cloudIt = me.cloudIt(p_l);
            int deCloudIt = me.deCloudIt(cloud);

            sep("Enough ? " + enoughForOne + " | need Mol ? " + needMol + " | mol avail ? " + molAvailable
                    + " | needDiag ? " + needDiag + " | allUndiag ? " + allUndiag);
            sep("--- Cloud one ? " + cloudIt);
            sep("- deCloud one ? " + deCloudIt);

            // Rien a faire et pas aux samples
            if (me.toMake.size() == 0 && !me.pos.equals(Module.SAMPLES))
                a = new Action(Module.SAMPLES);
            // Pas assez a faire mais aux samples
            else if (me.toMake.size() < MAX_SAMPLE && me.pos.equals(Module.SAMPLES))
                // a = new Action(rank);
                a = new Action(1);
            else if (cloudIt != -1 && me.pos.equals(Module.DIAGNOSIS))
                a = new Action(cloudIt);
            // qq chose est store cloudé et peut être réalisé
            else if (deCloudIt != -1 && me.toMake.size() < MAX_SAMPLE && me.pos.equals(Module.DIAGNOSIS))
                a = new Action(deCloudIt);
            // Au moins un sample non diagnostiqué et à la bonne place
            else if (needDiag && me.pos.equals(Module.DIAGNOSIS))
                a = new Action(me.diagFirst());
            // A faire mais pas de matériel pour au moins une et à la bonne place
            else if (needMol && molAvailable && me.pos.equals(Module.MOLECULES) && me.storeSize() < MAX_MOL)
                a = new Action(me.chooseMol(p_l, him));
            // A Faire mais assez de matériel pour au moins une et à la bonne place
            else if (enoughForOne && me.pos.equals(Module.LABORATORY))
                a = new Action(me.doFirst());
            // ---------------------------------------------
            // NOT ON PLACE
            // ---------------------------------------------
            // A Faire mais assez de matériel pour au moins une mais pas à la bonne place
            else if (enoughForOne && !me.pos.equals(Module.LABORATORY))
                a = new Action(Module.LABORATORY);
            // A faire mais pas de matériel pour au moins une mais pas à la bonne place
            else if (!enoughForOne && needMol && molAvailable && !me.pos.equals(Module.MOLECULES))
                a = new Action(Module.MOLECULES);
            // A faire, mais le materiel n'est pas dispo donc on doit devoir clouder
            else if (cloudIt != -1 && !enoughForOne && !molAvailable && !me.pos.equals(Module.DIAGNOSIS))
                a = new Action(Module.DIAGNOSIS);
            // tous les Samples non diagnostiqué mais pas à la bonne place
            else if (allUndiag && !me.pos.equals(Module.DIAGNOSIS))
                a = new Action(Module.DIAGNOSIS);
            else
                a = new Action(Module.DIAGNOSIS);

            return a;
        }

    }

    static class SampleComparator implements Comparator<Sample>
    {

        Robot r;

        SampleComparator(Robot p_r)
        {
            this.r = p_r;
        }

        @Override
        public int compare(Sample p_o1, Sample p_o2)
        {
            int d1 = p_o1.distToCost(this.r), d2 = p_o2.distToCost(this.r);

            // Maximize availability ?
            return (int) ((p_o2.h * 1000d / d2) - (p_o1.h * 1000d / d1));
        }

    }

    static interface IStrategy
    {
        Action decide(Robot[] p_r, LabState p_l, HashMap<Integer, Sample> p_s);
    }

    enum ActionType
    {
        WAIT, GOTO, CONNECT;
    }

    static class Action
    {
        ActionType t;
        Module m; // module for goto;
        Mol mm; // mol for connection;
        int c; // id for connection;

        Action()
        {
            this.t = ActionType.WAIT;
        }

        Action(Module p_m)
        {
            this.t = ActionType.GOTO;
            this.m = p_m;
            this.c = -1;
        }

        Action(int p_c)
        {
            this.t = ActionType.CONNECT;
            this.c = p_c;
            this.mm = null;
        }

        Action(Mol p_m)
        {
            this.t = ActionType.CONNECT;
            this.mm = p_m;
            this.c = -1;
        }

        @Override
        public String toString()
        {
            switch (this.t)
            {
                case WAIT:
                    return this.t + "";
                case GOTO:
                    return this.t + " " + this.m;
                case CONNECT:
                    if (this.mm == null)
                        return this.t + " " + this.c;
                    else
                        return this.t + " " + this.mm;
            }
            return "";
        }

    }

    enum Mol
    {
        A, B, C, D, E;

        public static Mol fromString(String p_s)
        {
            for (Mol m : values())
                if (p_s.equals(m.toString()))
                    return m;

            return null;
        }
    }

    enum Module
    {
        START_POS, DIAGNOSIS, MOLECULES, LABORATORY, SAMPLES;

        public static Module fromString(String p_s)
        {
            for (Module m : values())
                if (p_s.equals(m.toString()))
                    return m;

            return null;
        }

    }

    static class Robot
    {
        Module pos;
        int eta;
        int sc;
        int[] s = new int[5];
        int[] e = new int[5];

        HashMap<Integer, Sample> toMake = new HashMap<>();

        /**
         * La somme de chaque molécule nécessaire pour reussir à produire les toMake
         *
         * @return une valeur positive poir chaque molécule.
         */
        public int[] needed()
        {
            int[] n = new int[5];

            for (int j = 0; j < n.length; j++)
                for (Sample s : this.toMake.values())
                    if (s.cost[j] > 0) // Undiagnosed are -1
                        // expertise remove need of mol
                        n[j] += Math.max(0, s.cost[j] - this.e[j]);

            for (int j = 0; j < n.length; j++)
                n[j] = Math.max(0, n[j] - this.s[j]);

            return n;
        }

        public boolean molAvailable(LabState p_l)
        {
            boolean avail = false;

            int[] n = this.needed();
            for (int i = 0; i < n.length; i++)
                avail |= (n[i] > 0 && p_l.s[i] > 0);

            return avail;
        }

        public int cloudIt(LabState p_l)
        {
            int c = -1;

            // not enough space in storage to store all mol for one sample
            // List<Sample> l = this.toMake.values().stream().filter(s -> s.totalCost(this) > MAX_MOL)
            // .collect(Collectors.toList());
            //
            // if (!l.isEmpty())
            // return l.get(0).id;

            List<Sample> l = new ArrayList<>();
            for (Sample s : this.toMake.values())
            {
                int[] cost = s.costTo(this);
                int[] actualCost = s.actualCostTo(this);

                int totalCost = 0;
                for (int i = 0; i < cost.length; i++)
                {
                    // not enough mol in the entire game to do this without expertise
                    if (cost[i] > orig.s[i])
                        l.add(s);
                    // not enough mol in lab storage and my storage to do it (ie the enemy got it).
                    if (actualCost[i] > p_l.s[i])
                        l.add(s);
                    // not enough space in storage to store all mol for one sample expertise and storage included
                    totalCost += cost[i];
                }
                if (totalCost > MAX_MOL)
                    l.add(s);

            }

            for (Sample s : l)
                sep("To Cloud : " + s);

            if (!l.isEmpty())
                return l.get(0).id;

            return c;
        }

        /**
         * Savoir quelle Sample sortir du cloud lorsque celui ci devient dispo
         *
         * @return
         */
        public int deCloudIt(List<Sample> p_cloud)
        {
            int c = -1;
            List<Sample> l = new ArrayList<>();
            for (Sample s : p_cloud)
            {

                int[] cost = s.costTo(this);
                int[] actualCost = s.actualCostTo(this);
                int totalCost = 0;
                int totalActualCost = 0;
                for (int i = 0; i < cost.length; i++)
                {
                    totalCost += cost[i];
                    totalActualCost += actualCost[i];
                }
                // Si le cout ou le cout reel est négatif
                if (totalCost <= 0 || totalActualCost <= 0)
                    l.add(s);
            }

            for (Sample s : l)
                sep("To Cloud : " + s);

            if (!l.isEmpty())
                return l.get(0).id;

            return c;
        }

        /**
         * Choisis le prochain sample à produire en lab
         *
         * @return
         */
        public int doFirst()
        {
            List<Sample> l = this.enoughForSample().entrySet().stream().filter(e -> e.getValue())
                    .map(HashMap.Entry::getKey).collect(Collectors.toList());
            l.sort(new SampleComparator(this));

            return l.get(0).id;
        }

        /**
         * Retourne l'id du prochain à diagnostiquer
         *
         * @return dans n'importe quel ordre pour le moment.
         */
        public int diagFirst()
        {
            List<Sample> l = this.toMake.values().stream().filter(s -> !s.diag()).collect(Collectors.toList());
            return l.get(0).id;
        }

        /**
         * Choisis la prochaine Molécule à retirer en fonction des supply dispo et des besoins de "l'ennemi"
         *
         * @param p_l
         *            Supplies
         * @param p_o
         *            Ennemi
         * @return
         */
        public Mol chooseMol(LabState p_l, Robot p_o)
        {
            int[] reasons = new int[5];

            int[] n = this.needed();
            int[] on = p_o.needed();

            // JUST BECAUSE I NEED IT
            for (int i = 0; i < n.length; i++)
                if (n[i] > 0)
                    reasons[i] += 2;

            // JUST BECAUSE IT'S RARE
            int[] demand = new int[5];
            int[] offer = p_l.s;
            double[] rarity = new double[5];
            double min = 10000;
            int imin = 0;

            for (int i = 0; i < n.length; i++)
            {
                demand[i] = n[i] + on[i];
                rarity[i] = (1d * offer[i]) / demand[i];
                // most rare mol available and needed.
                if (rarity[i] < min && offer[i] > 0 && n[i] > 0)
                {
                    min = rarity[i];
                    imin = i;
                }
            }
            Mol rareMol = Mol.values()[imin];
            //
            sep("--- Need : " + Arrays.toString(n));
            sep("- Demand : " + Arrays.toString(demand));
            sep("LabState : " + Arrays.toString(offer));
            sep("- Rarity : " + Arrays.toString(rarity) + " --> " + rareMol);

            reasons[imin] += 1;

            // JUST BECAUSE I GOT INTEREST IN PROJECT
            HashMap<Project, int[]> dist = new HashMap<>();

            for (Project p : proj)
                dist.put(p, p.distTo(this));

            for (Sample s : this.toMake.values())
            {
                int[] actCost = s.actualCostTo(this);
                for (int[] ss : dist.values())
                    if (s.g != null && ss[s.g.ordinal()] > 0)
                        for (int i = 0; i < ss.length; i++)
                            if (actCost[i] > 0)
                                reasons[i] += 2; // PROJECT IS WAY MORE IMPORTANT THAN RARITY

            }

            // BUT CANNOT ASK FOR SOMETHING THERE IS NOT
            HashMap<Mol, Integer> r = new HashMap<>();
            for (int i = 0; i < n.length; i++)
                if (p_l.s[i] > 0)
                    r.put(Mol.values()[i], reasons[i]);

            LinkedHashMap<Mol, Integer> m = sortByValue(r);

            sep("Ordered Choice of Mols : " + m);

            return m.keySet().iterator().next();
        }

        /**
         * Si au moins une molécule est nécessaire
         *
         * @return oui si <code>needed()</code> contient au moins une valeur non-nulle
         * @see #Robot.needed()
         */
        public boolean needMol()
        {
            return (Arrays.stream(this.needed()).anyMatch(i -> i > 0));
        }

        /**
         * Si au moins un sample need diagnostique
         *
         * @return vrai si au moins un non diagnostiqué
         */
        public boolean needDiag()
        {
            return this.toMake.values().stream().anyMatch(s -> !s.diag());
        }

        /**
         * Si tous les samples need diagnostique
         *
         * @return vrai tous sont non dignostiqués
         */
        public boolean allUndiag()
        {
            return this.toMake.values().stream().allMatch(s -> !s.diag());
        }

        /**
         * Si au moins un sample est réalisable avec le contenu du storage
         *
         * @return oui si au moins un sample est réalisable
         */
        public boolean enoughForOne()
        {
            // boolean forAll = false;
            //
            // for (Sample s : this.toMake.values())
            // {
            // boolean forOne = true;
            // for (int i = 0; i < s.cost.length; i++)
            // forOne &= s.cost[i] <= this.s[i];
            //
            // forAll |= forOne;
            // }
            // return forAll;

            return this.enoughForSample().values().stream().anyMatch(b -> b);
        }

        /**
         * Le nombre de molécule en storage
         *
         * @return
         */
        public int storeSize()
        {
            return Arrays.stream(this.s).reduce(0, Integer::sum);
        }

        /**
         * Dis pour chaque Sample toMake, si le storage est suffisant ou pas.
         *
         * @return une map des sample et leur faisabilité.
         */
        public HashMap<Sample, Boolean> enoughForSample()
        {
            HashMap<Sample, Boolean> m = new HashMap<>();
            for (Sample s : this.toMake.values())
            {
                boolean forOne = s.diag();
                for (int i = 0; i < s.cost.length; i++)
                    forOne &= s.cost[i] <= (this.s[i] + this.e[i]);

                m.put(s, forOne);
            }

            return m;
        }

        public void upd(String p_p, int p_e, int p_s)
        {
            this.pos = Module.fromString(p_p);
            this.eta = p_e;
            this.sc = p_s;
        }

        public void updMolS(int p_a, int p_b, int p_c, int p_d, int p_e)
        {
            this.s[Mol.A.ordinal()] = p_a;
            this.s[Mol.B.ordinal()] = p_b;
            this.s[Mol.C.ordinal()] = p_c;
            this.s[Mol.D.ordinal()] = p_d;
            this.s[Mol.E.ordinal()] = p_e;
        }

        public void updMolE(int p_a, int p_b, int p_c, int p_d, int p_e)
        {
            this.e[Mol.A.ordinal()] = p_a;
            this.e[Mol.B.ordinal()] = p_b;
            this.e[Mol.C.ordinal()] = p_c;
            this.e[Mol.D.ordinal()] = p_d;
            this.e[Mol.E.ordinal()] = p_e;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "Robot [p=" + this.pos + ", eta=" + this.eta + ", sc=" + this.sc + ", s=" + Arrays.toString(this.s)
                    + ", e=" + Arrays.toString(this.e) + ", toMake=" + this.toMake + "]";
        }

    }

    static class LabState
    {
        int[] s = new int[5];

        public void updMol(int p_a, int p_b, int p_c, int p_d, int p_e)
        {
            this.s[Mol.A.ordinal()] = p_a;
            this.s[Mol.B.ordinal()] = p_b;
            this.s[Mol.C.ordinal()] = p_c;
            this.s[Mol.D.ordinal()] = p_d;
            this.s[Mol.E.ordinal()] = p_e;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "LabState [s=" + Arrays.toString(this.s) + "]";
        }

    }

    static class Project extends LabState
    {

        public int sumDistTo(Robot p_r)
        {
            int d = 0;
            for (int i = 0; i < this.s.length; i++)
                d += Math.max(0, this.s[i] - p_r.e[i]);

            return d;
        }

        public int[] distTo(Robot p_r)
        {
            int[] c = new int[5];

            for (int i = 0; i < this.s.length; i++)
                c[i] = Math.max(0, this.s[i] - p_r.e[i]);

            return c;
        }

        @Override
        public String toString()
        {
            return "Project [s=" + Arrays.toString(this.s) + "]";
        }

    }

    static class Sample
    {
        int id;
        int o;
        int r;
        Mol g;
        int h;
        int[] cost = new int[5];

        public Sample(int p_sampleId)
        {
            this.id = p_sampleId;
        }

        public boolean isMine()
        {
            return this.o == 0;
        }

        public boolean isHis()
        {
            return this.o == 1;
        }

        public boolean diag()
        {
            return this.h != -1;
        }

        public int totalCost(Robot p_r)
        {
            // return Arrays.stream(this.cost).reduce(0, Integer::sum);
            int sum = 0;
            for (int i = 0; i < this.cost.length; i++)
                sum += Math.max(0, (this.cost[i] - p_r.e[i]));
            return sum;
        }

        public void upd(int p_o, int p_r, String p_g, int p_h)
        {
            this.o = p_o;
            this.r = p_r;
            this.g = Mol.fromString(p_g);
            this.h = p_h;
        }

        public void updMol(int p_a, int p_b, int p_c, int p_d, int p_e)
        {
            this.cost[Mol.A.ordinal()] = p_a;
            this.cost[Mol.B.ordinal()] = p_b;
            this.cost[Mol.C.ordinal()] = p_c;
            this.cost[Mol.D.ordinal()] = p_d;
            this.cost[Mol.E.ordinal()] = p_e;
        }

        public int distToCost(Robot p_r)
        {
            int d = 0;
            for (int i = 0; i < this.cost.length; i++)
                d += Math.max(0, this.cost[i] - p_r.e[i] - p_r.s[i]);

            return d;
        }

        public int[] costTo(Robot p_r)
        {
            int[] c = new int[5];

            for (int i = 0; i < this.cost.length; i++)
                c[i] = Math.max(0, this.cost[i] - p_r.e[i]);

            return c;
        }

        public int[] actualCostTo(Robot p_r)
        {
            int[] c = new int[5];

            for (int i = 0; i < this.cost.length; i++)
                c[i] = Math.max(0, this.cost[i] - p_r.e[i] - p_r.s[i]);

            return c;
        }

        public int sumActualCost(Robot p_r)
        {
            int c = 0;

            for (int i = 0; i < this.cost.length; i++)
                c += Math.max(0, this.cost[i] - p_r.e[i] - p_r.s[i]);

            return c;
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "Sample [id=" + this.id + ", o=" + this.o + ", r=" + this.r + ", g=" + this.g + ", h=" + this.h
                    + ", c=" + Arrays.toString(this.cost) + "]";
        }

    }

    public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue(Map<K, V> map)
    {
        return map.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}
