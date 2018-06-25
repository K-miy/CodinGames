package tools;

import java.util.Arrays;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;

public class AlgoGenetik
{

    private static Random r = new Random();
    private static final int numberOfAction = 4;

    /* GA parameters */
    private static final double uniformRate = 0.5;
    private static final double alpha = 0.7;
    private static final double mutationRate = 0.05;

    private static final int tournamentSize = 5;
    private static final int popSize = 15;
    private static final boolean elitism = true;
    State s = null;
    Action[] a = null;

    public boolean checkTime()
    {
        return true;
    }

    // Evolve a population
    public Population evolvePopulation(Population pop)
    {
        Population newPopulation = new Population(this.s, this.a, pop.size(), false);

        // Keep our best individual
        if (elitism)
            newPopulation.add(pop.getFittest());

        Iterator<Individual> it1 = pop.iterator();
        if (elitism)
            it1.next();

        for (; it1.hasNext();)
            newPopulation.add(it1.next());

        // Crossover population
        // Loop over the population size and create new individuals
        // for (int i = 0; i < pop.size() - 1; i++)
        // {
        // Individual indiv1 = this.tournamentSelection(pop);
        // Individual indiv2 = this.tournamentSelection(pop);
        // Individual newIndiv = indiv1.crossover(indiv2);
        // newPopulation.add(newIndiv);
        // }

        // Mutate population
        Iterator<Individual> it = newPopulation.iterator();
        if (elitism)
            it.next();

        for (; it.hasNext();)
            (it.next()).mutate();

        return newPopulation;
    }

    public Individual run(State ps, Action[] pp, Individual seed)
    {

        this.s = ps;
        this.a = Arrays.copyOf(pp, numberOfAction);

        // Create an initial population
        Population myPop = new Population(this.s, this.a, seed, popSize);

        // Evolve our population until we reach an optimum solution
        int generationCount = 0;
        while (this.checkTime())
        {
            generationCount++;
            // if (generationCount % 100 == 0)
            System.err.println("Generation: " + generationCount + " Fittest: " + myPop.getFittest().getFitness());
            myPop = this.evolvePopulation(myPop);
        }
        System.err.println("Generation: " + generationCount);
        System.err.println("Genes:" + myPop.getFittest());

        return myPop.getFittest();
    }

    // Select individuals for crossover
    private Individual tournamentSelection(Population pop)
    {
        // Create a tournament population
        Population tournament = new Population(this.s, this.a, tournamentSize, false);
        // For each place in the tournament get a random individual
        for (int i = 0; i < tournamentSize; i++)
        {
            int randomId = this.r.nextInt(pop.size());
            tournament.add(pop.get(randomId));
        }
        // Get the fittest
        Individual fittest = tournament.getFittest();
        return fittest;
    }

    interface Action
    {

    }

    class Individual implements Comparable<Individual>
    {
        // un individu est une séquence de 8 actions, 1 action est un triplet Action, col, rot
        // donc un gène est 8 * (2+1+1) Mais les Actions sont fixes et l'ordre aussi ...
        // la seule chose qui bouge est donc les 8 col et rot.
        // Donc les indices ActionS : col
        // Et les indices IMActionS : rot

        int defaultGeneLength = numberOfAction * 2;
        private byte[] genes = new byte[this.defaultGeneLength];
        // Cache
        int defaultFitnessVal = -1;
        private int fitness = this.defaultFitnessVal;

        State o = null;
        Action[] a = null;

        private Individual(Individual copy)
        {
            this.o = copy.o.clone();
            this.a = copy.a;
        }

        // Create a random individual
        public Individual(State orig, Action[] pp, boolean random)
        {
            this.o = orig.clone();
            this.a = Arrays.copyOf(pp, numberOfAction);

            if (random)
            {
                Action[] a = new Action[this.a.length];
                State[] ss = new State[a.length + 1];
                ss[0] = this.o;
                for (int i = 0; i < a.length; i++)
                {
                    Action[] c = ss[i].probableActionSet(this.a[i]);
                    a[i] = c[AlgoGenetik.this.r.nextInt(c.length)];
                    ss[i + 1] = ss[i].next(a[i]);

                    // this.genes[i * 2] = (byte) a[i].c;
                    // this.genes[i * 2 + 1] = (byte) a[i].r.ordinal();
                }

            }

        }

        public Individual(State orig, Action[] pp, Individual prec)
        {
            this.o = orig.clone();
            this.a = pp;

            for (int i = 0; i < this.defaultGeneLength - 2; i++)
                this.genes[i] = prec.genes[i + 2];

            // this.genes[this.defaultGeneLength - 2] = (byte) AlgoGenetik.this.r.nextInt(WizState.State_WIDTH);
            // Rotation[] ra = Rotation.availableRots(this.genes[this.defaultGeneLength - 2]);
            // this.genes[this.defaultGeneLength - 1] = (byte) ra[AlgoGenetik.this.r.nextInt(ra.length)].ordinal();

        }

        @Override
        public int compareTo(Individual p_o)
        {
            return p_o.getFitness() - this.getFitness();
        }

        // Crossover individuals
        private Individual crossover(Individual indiv)
        {
            Individual newSol = new Individual(this);
            // Loop through genes
            for (int i = 0; i < this.defaultGeneLength; i += 2)
                // Crossover
                if (AlgoGenetik.this.r.nextDouble() < uniformRate)
                {
                    newSol.genes[i] = this.genes[i];
                    newSol.genes[i + 1] = this.genes[i + 1];
                }
                else
                {
                    newSol.genes[i] = indiv.genes[i];
                    newSol.genes[i + 1] = indiv.genes[i + 1];
                }

            return newSol;
        }

        public Action[] fromAction()
        {
            Action[] r = new Action[this.a.length];

            for (int i = 0; i < this.defaultGeneLength; i += 2)
            {
                // int k = i / 2;
                // r[k] = new Action(this.a[k], this.genes[i], Rotation.values()[this.genes[i + 1]]);
            }

            return r;
        }

        public int getFitness()
        {
            if (this.fitness == this.defaultFitnessVal)
            {
                double d = 0;

                Action[] a = this.fromAction();
                State[] ss = new State[a.length + 1];
                ss[0] = this.o;
                for (int i = 0; i < a.length; i++)
                    ss[i + 1] = ss[i].next(a[i]);

                for (int i = a.length; i >= 0; i--)
                    d = ss[i].v + alpha * d;

                this.fitness = (int) d;
            }
            return this.fitness;
        }

        // Mutate an individual
        private void mutate()
        {
            // Loop through genes

            Action[] a = this.fromAction();
            State[] ss = new State[a.length + 1];
            ss[0] = this.o;

            boolean fromHere = false;

            for (int i = 0; i < a.length; i++)
            {
                if (AlgoGenetik.this.r.nextDouble() < mutationRate)
                    fromHere = true;

                if (fromHere)
                {
                    Action[] c = ss[i].probableActionSet(this.a[i]);
                    a[i] = c[AlgoGenetik.this.r.nextInt(c.length)];

                    // this.genes[i * 2] = (byte) a[i].c;
                    // this.genes[i * 2 + 1] = (byte) a[i].r.ordinal();
                }
                ss[i + 1] = ss[i].next(a[i]);

            }

            if (fromHere)
                this.fitness = this.defaultFitnessVal;
        }

        /* Getters and setters */
        // Use this if you want to create individuals with different gene lengths
        public void setDefaultGeneLength(int length)
        {
            this.defaultGeneLength = length;
        }

        @Override
        public String toString()
        {
            return this.fitness + ":" + Arrays.toString(this.fromAction());
        }
    }

    class Population implements Iterable<Individual>
    {
        State o;
        Action[] p;

        PriorityQueue<Individual> individuals;

        /*
         * Constructors
         */
        // Create a population
        public Population(State orig, Action[] pp, Individual pre_fittest, int populationSize)
        {
            this.o = orig.clone();
            this.p = pp;
            this.individuals = new PriorityQueue<Individual>(populationSize);
            // Initialise population
            // Loop and create individuals
            Individual seed = new Individual(orig, pp, pre_fittest);
            this.individuals.add(seed);

            for (int i = 0; i < populationSize - 1; i++)
            {
                Individual newIndividual = new Individual(this.o, this.p, true);
                this.individuals.add(newIndividual);
            }
        }

        /*
         * Constructors
         */
        // Create a population
        public Population(State orig, Action[] pp, int populationSize, boolean initialise)
        {
            this.o = orig.clone();
            this.p = pp;
            this.individuals = new PriorityQueue<Individual>(populationSize);
            // Initialise population
            if (initialise)
                // Loop and create individuals
                for (int i = 0; i < populationSize; i++)
                {
                Individual newIndividual = new Individual(this.o, this.p, true);
                this.individuals.add(newIndividual);
                }
        }

        public void add(Individual p_newIndiv)
        {
            this.individuals.add(p_newIndiv);
        }

        public Individual get(int p_i)
        {
            return (Individual) this.individuals.toArray()[p_i];
        }

        public Individual getFittest()
        {
            return this.individuals.peek();
        }

        @Override
        public Iterator<Individual> iterator()
        {
            return this.individuals.iterator();
        }

        /* Public methods */
        // Get population size
        public int size()
        {
            return this.individuals.size();
        }

        /**
         * Redéfinition.
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            for (Individual i : this)
                sb.append(i + "\n");

            return sb + "";
        }
    }

    interface State
    {

        double v = 0;

        State clone();

        State next(Action p_action);

        Action[] probableActionSet(Action p_action);

    }
}
