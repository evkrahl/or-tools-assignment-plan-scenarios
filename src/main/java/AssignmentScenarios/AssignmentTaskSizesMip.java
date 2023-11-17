package AssignmentScenarios;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import java.util.stream.IntStream;

/** MIP example that solves an assignment problem. */
public class AssignmentTaskSizesMip {
    public static void main(String[] args) {
        Loader.loadNativeLibraries();
        // Data
        double[][] costs = {
                {90, 76, 75, 70, 50, 74, 12, 68},
                {35, 85, 55, 65, 48, 101, 70, 83},
                {125, 95, 90, 105, 59, 120, 36, 73},
                {45, 110, 95, 115, 104, 83, 37, 71},
                {60, 105, 80, 75, 59, 62, 93, 88},
                {45, 65, 110, 95, 47, 31, 81, 34},
                {38, 51, 107, 41, 69, 99, 115, 48},
                {47, 85, 57, 71, 92, 77, 109, 36},
                {39, 63, 97, 49, 118, 56, 92, 61},
                {47, 101, 71, 60, 88, 109, 52, 90},
        };
        int numWorkers = costs.length;
        int numTasks = costs[0].length;

        final int[] allWorkers = IntStream.range(0, numWorkers).toArray();
        final int[] allTasks = IntStream.range(0, numTasks).toArray();

        final int[] taskSizes = {10, 7, 3, 12, 15, 4, 11, 5};
        // Maximum total of task sizes for any worker
        final int totalSizeMax = 15;

        // Solver
        // Create the linear solver with the SCIP backend.
        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            System.out.println("Could not create solver SCIP");
            return;
        }

        // Variables
        // x[i][j] is an array of 0-1 variables, which will be 1
        // if worker i is assigned to task j.
        MPVariable[][] x = new MPVariable[numWorkers][numTasks];
        for (int worker : allWorkers) {
            for (int task : allTasks) {
                x[worker][task] = solver.makeBoolVar("x[" + worker + "," + task + "]");
            }
        }

        // Constraints
        // Each worker is assigned to at most max task size.
        for (int worker : allWorkers) {
            MPConstraint constraint = solver.makeConstraint(0, totalSizeMax, "");
            for (int task : allTasks) {
                constraint.setCoefficient(x[worker][task], taskSizes[task]);
            }
        }
        // Each task is assigned to exactly one worker.
        for (int task : allTasks) {
            MPConstraint constraint = solver.makeConstraint(1, 1, "");
            for (int worker : allWorkers) {
                constraint.setCoefficient(x[worker][task], 1);
            }
        }

        // Objective
        MPObjective objective = solver.objective();
        for (int worker : allWorkers) {
            for (int task : allTasks) {
                objective.setCoefficient(x[worker][task], costs[worker][task]);
            }
        }
        objective.setMinimization();

        // Solve
        MPSolver.ResultStatus resultStatus = solver.solve();

        // Print solution.
        // Check that the problem has a feasible solution.
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL
                || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
            System.out.println("Total cost: " + objective.value() + "\n");
            for (int worker : allWorkers) {
                for (int task : allTasks) {
                    // Test if x[i][j] is 0 or 1 (with tolerance for floating point
                    // arithmetic).
                    if (x[worker][task].solutionValue() > 0.5) {
                        System.out.println("Worker " + worker + " assigned to task " + task
                                + ".  Cost: " + costs[worker][task]);
                    }
                }
            }
        } else {
            System.err.println("No solution found.");
        }
    }

    private AssignmentTaskSizesMip() {}
}

