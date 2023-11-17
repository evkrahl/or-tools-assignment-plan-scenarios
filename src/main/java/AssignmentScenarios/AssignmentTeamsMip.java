package AssignmentScenarios;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import java.util.stream.IntStream;

/** MIP example that solves an assignment problem. */
public class AssignmentTeamsMip {
    public static void main(String[] args) {
        Loader.loadNativeLibraries();
        // Data
        double[][] costs = {
                {90, 76, 75, 70},
                {35, 85, 55, 65},
                {125, 95, 90, 105},
                {45, 110, 95, 115},
                {60, 105, 80, 75},
                {45, 65, 110, 95},
        };
        int numWorkers = costs.length;
        int numTasks = costs[0].length;

        final int[] allWorkers = IntStream.range(0, numWorkers).toArray();
        final int[] allTasks = IntStream.range(0, numTasks).toArray();

        final int[] team1 = {0, 2, 4};
        final int[] team2 = {1, 3, 5};
        // Maximum total of tasks for any team
        final int teamMax = 2;

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
        // Each worker is assigned to at most one task.
        for (int worker : allWorkers) {
            MPConstraint constraint = solver.makeConstraint(0, 1, "");
            for (int task : allTasks) {
                constraint.setCoefficient(x[worker][task], 1);
            }
        }
        // Each task is assigned to exactly one worker.
        for (int task : allTasks) {
            MPConstraint constraint = solver.makeConstraint(1, 1, "");
            for (int worker : allWorkers) {
                constraint.setCoefficient(x[worker][task], 1);
            }
        }

        // Each team takes at most two tasks.
        MPConstraint team1Tasks = solver.makeConstraint(0, teamMax, "");
        for (int worker : team1) {
            for (int task : allTasks) {
                team1Tasks.setCoefficient(x[worker][task], 1);
            }
        }

        MPConstraint team2Tasks = solver.makeConstraint(0, teamMax, "");
        for (int worker : team2) {
            for (int task : allTasks) {
                team2Tasks.setCoefficient(x[worker][task], 1);
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

    private AssignmentTeamsMip() {}
}

