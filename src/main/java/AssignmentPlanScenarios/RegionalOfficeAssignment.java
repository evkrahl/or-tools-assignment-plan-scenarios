package AssignmentPlanScenarios;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

/** MIP example that solves an assignment problem. */
public class RegionalOfficeAssignment {
    public static void main(String[] args) {

        Loader.loadNativeLibraries();

        // Data
        int[][] capacities = {
                {90, 80, 75, 70},
                {70, 85, 55, 65},
                {125, 95, 100, 95}, // 1 highest capacity
                {45, 80, 95, 115},
                {100, 95, 95, 100}, // 2 highest capacity
                {60, 50, 90, 65},
        };

        int numRegionalOffices = capacities.length;
        int numClaims = capacities[0].length;

        // Solver
        // Create the linear solver with the SCIP backend.
        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            System.out.println("Could not create solver SCIP");
            return;
        }

        // Variables
        // x[i][j] is an array of 0-1 variables, which will be 1
        // if regional office i is assigned to task j.
        MPVariable[][] x = new MPVariable[numRegionalOffices][numClaims];
        for (int i = 0; i < numRegionalOffices; ++i) {
            for (int j = 0; j < numClaims; ++j) {
                x[i][j] = solver.makeIntVar(0, 1, "");
            }
        }

        // Constraints
        // Each claim authority is assigned to at most one claim.
        for (int i = 0; i < numRegionalOffices; ++i) {
            MPConstraint constraint = solver.makeConstraint(0, 1, "");
            for (int j = 0; j < numClaims; ++j) {
                constraint.setCoefficient(x[i][j], 1);
            }
        }

        // Each claim is assigned to exactly one claim authority.
        for (int j = 0; j < numClaims; ++j) {
            MPConstraint constraint = solver.makeConstraint(1, 1, "");
            for (int i = 0; i < numRegionalOffices; ++i) {
                constraint.setCoefficient(x[i][j], 1);
            }
        }

        // Objective
        MPObjective objective = solver.objective();
        for (int i = 0; i < numRegionalOffices; ++i) {
            for (int j = 0; j < numClaims; ++j) {
                objective.setCoefficient(x[i][j], capacities[i][j]);
            }
        }
        objective.setMinimization();

        // Solve
        MPSolver.ResultStatus resultStatus = solver.solve();

        // Print solution.
        // Check that the problem has a feasible solution.
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL
                || resultStatus == MPSolver.ResultStatus.FEASIBLE) {
//            System.out.println("Total capacity: " + objective.value() + "\n");
            for (int i = 0; i < numRegionalOffices; ++i) {
                for (int j = 0; j < numClaims; ++j) {
                    // Test if x[i][j] is 0 or 1 (with tolerance for floating point
                    // arithmetic).
                    if (x[i][j].solutionValue() > 0.5) {
                        System.out.println(
                                "Regional Office " + i + " assigned to claim " + j + ".  Capacity = " + capacities[i][j]);
                    }
                }
            }
        } else {
            System.err.println("No solution found.");
        }
    }

    private RegionalOfficeAssignment() {}
}



