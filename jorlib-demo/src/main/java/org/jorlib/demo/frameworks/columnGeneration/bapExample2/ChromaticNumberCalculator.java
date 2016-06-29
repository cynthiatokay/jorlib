/* ==========================================
 * jORLib : a free Java OR library
 * ==========================================
 *
 * Project Info:  https://github.com/jkinable/jorlib
 * Project Creator:  Joris Kinable (https://github.com/jkinable)
 *
 * (C) Copyright 2015, by Joris Kinable and Contributors.
 *
 * This program and the accompanying materials are licensed under GPLv3
 *
 */
/* -----------------
 * ChromaticNumberCalculator.java
 * -----------------
 * (C) Copyright 2016, by Joris Kinable and Contributors.
 *
 * Original Author:  Joris Kinable
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 *
 */
package org.jorlib.demo.frameworks.columnGeneration.bapExample2;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.alg.ChromaticNumber;
import org.jgrapht.graph.DefaultEdge;
import org.jorlib.demo.frameworks.columnGeneration.bapExample2.bap.BranchAndPrice;
import org.jorlib.demo.frameworks.columnGeneration.bapExample2.bap.branching.BranchOnVertexPair;
import org.jorlib.demo.frameworks.columnGeneration.bapExample2.cg.ChromaticNumberPricingProblem;
import org.jorlib.demo.frameworks.columnGeneration.bapExample2.cg.ExactPricingProblemSolver;
import org.jorlib.demo.frameworks.columnGeneration.bapExample2.cg.IndependentSet;
import org.jorlib.demo.frameworks.columnGeneration.bapExample2.cg.master.Master;
import org.jorlib.demo.frameworks.columnGeneration.bapExample2.model.ColoringGraph;
import org.jorlib.frameworks.columnGeneration.branchAndPrice.AbstractBranchCreator;
import org.jorlib.frameworks.columnGeneration.io.SimpleBAPLogger;
import org.jorlib.frameworks.columnGeneration.pricing.AbstractPricingProblemSolver;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A column generation solution to calculate the chromatic number of a graph (graph coloring). A solution to a graph coloring problem
 * can be interpreted as the smallest set of disjoint independent sets, such that the union of those sets contain all vertices in the graph.<p>
 *
 * The implementation is based on: Mehrotra, A. Trick, M.A., A column Generation Approach for Graph Coloring. INFORMS Journal on Computing, volume 8, p.344--354, 1995<p>
 *
 * Note: this is an example class to demonstrate features of the Column Generation framework. This class is not
 * intended as a high-performance Graph Coloring solver!
 *
 * @author Joris Kinable
 * @version 29-6-2016
 */
public final class ChromaticNumberCalculator {

    private final ColoringGraph coloringGraph;

    public ChromaticNumberCalculator(ColoringGraph coloringGraph) {
        this.coloringGraph = coloringGraph;

        //Create Pricing problem
        ChromaticNumberPricingProblem pricingProblem = new ChromaticNumberPricingProblem(coloringGraph, "chromaticNumberPricingProblem");

        //Create the Master Problem
        Master master = new Master(coloringGraph, pricingProblem);

        //Define which solvers to use for the pricing problem
        List<Class<? extends AbstractPricingProblemSolver<ColoringGraph, IndependentSet, ChromaticNumberPricingProblem>>> solvers = Collections.singletonList(ExactPricingProblemSolver.class);

        //Optional: Get an initial solution
        List<IndependentSet> initSolution = this.getInitialSolution(pricingProblem);
        int upperBound=initSolution.size();
        System.out.println("Init solution: ");
        for(IndependentSet is : initSolution)
            System.out.println(is);


        //Optional: Get a lower bound on the optimum solution, e.g. largest clique in the graph
        double lowerBound=this.calculateLowerBound();

        //Define Branch creators
        List<? extends AbstractBranchCreator<ColoringGraph, IndependentSet, ChromaticNumberPricingProblem>> branchCreators = Collections.singletonList(new BranchOnVertexPair(coloringGraph, pricingProblem));

        //Create a Branch-and-Price instance, and provide the initial solution as a warm-start
        BranchAndPrice bap = new BranchAndPrice(coloringGraph, master, pricingProblem, solvers, branchCreators, lowerBound, upperBound);
        bap.warmStart(upperBound, initSolution);

        //OPTIONAL: Attach a debugger
        //SimpleDebugger debugger = new SimpleDebugger(bap, true);
        MyDebugger debugger = new MyDebugger(bap, true);

        //OPTIONAL: Attach a logger to the Branch-and-Price procedure.
        SimpleBAPLogger logger = new SimpleBAPLogger(bap, new File("./output/coloring.log"));

        System.out.println("Instance: "+coloringGraph.getName());
        System.out.println("Vertices: "+coloringGraph.getNrVertices()+" edges: "+coloringGraph.edgeSet().size());
        System.out.println("Lower bound: "+lowerBound+" upper bound: "+upperBound);

        //Solve the Graph Coloring problem through Branch-and-Price
        bap.runBranchAndPrice(System.currentTimeMillis() + 8000000L);


        //Print solution:
        System.out.println("================ Solution ================");
        System.out.println("BAP terminated with objective (chromatic number): " + bap.getObjective());
        System.out.println("Total Number of iterations: " + bap.getTotalNrIterations());
        System.out.println("Total Number of processed nodes: " + bap.getNumberOfProcessedNodes());
        System.out.println("Total Time spent on master problems: " + bap.getMasterSolveTime() + " Total time spent on pricing problems: " + bap.getPricingSolveTime());
        if (bap.hasSolution()) {
            System.out.println("Solution is optimal: " + bap.isOptimal());
            System.out.println("Columns (only non-zero columns are returned):");
            List<IndependentSet> solution = bap.getSolution();
            for (IndependentSet column : solution)
                System.out.println(column);
        }

        //Clean up:
        bap.close(); //Close master and pricing problems

    }

    public static void main(String[] args) throws IOException {
//        ColoringGraph coloringGraph=new ColoringGraph("./data/graphColoring/myciel3.col"); //Optimal: 4
//        ColoringGraph coloringGraph=new ColoringGraph("./data/graphColoring/myciel4.col"); //Optimal: 5
//        ColoringGraph coloringGraph=new ColoringGraph("./data/graphColoring/queen5_5.col"); //Optimal: 5
        ColoringGraph coloringGraph=new ColoringGraph("./data/graphColoring/queen6_6.col"); //Optimal: 7
        new ChromaticNumberCalculator(coloringGraph);
    }


    //------------------ Helper methods -----------------

    /**
     * Calculate a feasible graph coloring using a greedy algorithm.
     * @param pricingProblem Pricing problem
     * @return Feasible coloring.
     */
    public List<IndependentSet> getInitialSolution(ChromaticNumberPricingProblem pricingProblem) {
        List<IndependentSet> initialSolution=new ArrayList<>();
        Map<Integer, Set<Integer>> coloredGroups=ChromaticNumber.findGreedyColoredGroups((UndirectedGraph<Integer, DefaultEdge>)coloringGraph);
        for(Integer color : coloredGroups.keySet()){
            initialSolution.add(new IndependentSet(pricingProblem, false, "initialColumn", coloredGroups.get(color), 1));
        }
        return initialSolution;
    }

    /**
     * Calculate a lower bound on the chromatic number of a graph, by calculating the largest clique in the graph.
     * @return lower bound
     */
    private int calculateLowerBound() {
        BronKerboschCliqueFinder<Integer, DefaultEdge> cliqueFinder=new BronKerboschCliqueFinder<>(coloringGraph);
        Collection<Set<Integer>> cliques=cliqueFinder.getBiggestMaximalCliques();
        return cliques.iterator().next().size();
    }
}