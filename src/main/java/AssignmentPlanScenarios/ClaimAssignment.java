package AssignmentPlanScenarios;
// CP-SAT example that solves an assignment problem.
import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import com.google.ortools.sat.Literal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/** Assignment problem. */
public class ClaimAssignment {
    public static void main(String[] args) {
        Loader.loadNativeLibraries();
        // Data
        int[][] capacities = {
                {90, 76, 75, 70, 50, 74, 12, 68},       // 64.38
                {35, 85, 55, 65, 48, 101, 70, 83},      // 67.75
                {125, 95, 90, 105, 79, 120, 36, 73},    // 87.88  *** 1 Worst Case
                {45, 110, 95, 115, 104, 83, 37, 71},    // 82.50  *** 2 Worst Case2
                {60, 105, 80, 75, 59, 62, 93, 88},      // 77.75
                {45, 65, 110, 95, 47, 31, 81, 34},      // 63.50  *** 1 Best Case
                {38, 51, 107, 41, 69, 99, 115, 48},     // 71.00
                {47, 85, 57, 71, 92, 77, 109, 36},      // 71.75
                {39, 63, 97, 49, 118, 56, 92, 61},      // 71.88
                {47, 101, 71, 60, 88, 109, 52, 90},     // 77.25
        };

        final int numRegionalOffices = capacities.length;
        final int numClaims = capacities[0].length;

        final int[] allRegionalOffices = IntStream.range(0, numRegionalOffices).toArray();
        final int[] allClaims = IntStream.range(0, numClaims).toArray();

        final int[] taskSizes = {10, 7, 3, 12, 15, 4, 11, 5};
        // Maximum total of claims for any RO
        final int totalSizeMax = 15;

        // Model
        CpModel model = new CpModel();

        // Variables
        Literal[][] x = new Literal[numRegionalOffices][numClaims];
        for (int regionalOffice : allRegionalOffices) {
            for (int claim : allClaims) {
                x[regionalOffice][claim] = model.newBoolVar("x[" + regionalOffice + "," + claim + "]");
            }
        }

        // Constraints
        // Each regional office has a maximum capacity.
        for (int regionalOffice : allRegionalOffices) {
            LinearExprBuilder expr = LinearExpr.newBuilder();
            for (int claim : allClaims) {
                expr.addTerm(x[regionalOffice][claim], taskSizes[claim]);
            }
            model.addLessOrEqual(expr, totalSizeMax);
        }

        // Each claim is assigned to exactly one regional office.
        for (int claim : allClaims) {
            List<Literal> regionalOffices = new ArrayList<>();
            for (int regionalOffice : allRegionalOffices) {
                regionalOffices.add(x[regionalOffice][claim]);
            }
            model.addExactlyOne(regionalOffices);
        }

        // Objective
        LinearExprBuilder obj = LinearExpr.newBuilder();
        for (int regionalOffice : allRegionalOffices) {
            for (int claim : allClaims) {
                obj.addTerm(x[regionalOffice][claim], capacities[regionalOffice][claim]);
            }
        }
        model.minimize(obj);

        // Solve
        CpSolver solver = new CpSolver();
        CpSolverStatus status = solver.solve(model);

        // Print solution.
        // Check that the problem has a feasible solution.
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
//            System.out.println("Total capacity: " + solver.objectiveValue() + "\n");
            for (int regionalOffice : allRegionalOffices) {
                for (int claim : allClaims) {
                    if (solver.booleanValue(x[regionalOffice][claim])) {
                        System.out.println("Regional Office " + regionalOffice + " assigned to claim " + claim
                                + ".  Capacity: " + capacities[regionalOffice][claim]);
                    }
                }
            }
        } else {
            System.err.println("No solution found.");
        }
    }

    private ClaimAssignment() {}
}

