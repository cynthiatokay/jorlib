/* Copyright 2015 Joris Kinable
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package org.jorlib.demo.io.tsplibreader;

import java.io.File;
import java.io.IOException;

import org.jorlib.io.tsplibreader.TSPLibInstance;

/**
 * Simple class which reads a TSPLib instance
 * 
 * @author Joris Kinable
 * @since April 24, 2015
 *
 */
public final class TSPDemo
{

    public TSPDemo()
        throws IOException
    {
        // Read a TSP instance from the TSPLib, as well as a TSP tour
        File directory = new File("./data/tspLib/tsp/");
        File instanceData = new File(directory, "ulysses16.tsp");
        File optimalTour = new File(directory, "ulysses16.opt.tour");

        // Create a TSP instance, thereby parsing the TSPLib file
        TSPLibInstance problem = new TSPLibInstance(instanceData);
        // Add the tour
        problem.addTour(optimalTour);

        // Print some information about the problem
        System.out.println("Name of TSP problem: " + problem.getName());
        System.out.println("Number of vertices: " + problem.getDimension());
        System.out.println("Number of registered tours: " + problem.getTours().size());
        System.out
            .println("Length of registered tour: " + problem.getTours().get(0).distance(problem));
    }

    public static void main(String[] args)
    {
        try {
            new TSPDemo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
