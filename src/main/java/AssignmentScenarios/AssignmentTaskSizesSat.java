package AssignmentScenarios;

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
public class AssignmentTaskSizesSat {
    public static void main(String[] args) {
        Loader.loadNativeLibraries();
        // Data
        int[][] costs = {
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
        final int numWorkers = costs.length;
        final int numTasks = costs[0].length;

        final int[] allWorkers = IntStream.range(0, numWorkers).toArray();
        final int[] allTasks = IntStream.range(0, numTasks).toArray();

        final int[] taskSizes = {10, 7, 3, 12, 15, 4, 11, 5};
        // Maximum total of task sizes for any worker
        final int totalSizeMax = 15;

        // Model
        CpModel model = new CpModel();

        // Variables
        Literal[][] x = new Literal[numWorkers][numTasks];
        for (int worker : allWorkers) {
            for (int task : allTasks) {
                x[worker][task] = model.newBoolVar("x[" + worker + "," + task + "]");
            }
        }

        // Constraints
        // Each worker has a maximum capacity.
        for (int worker : allWorkers) {
            LinearExprBuilder expr = LinearExpr.newBuilder();
            for (int task : allTasks) {
                expr.addTerm(x[worker][task], taskSizes[task]);
            }
            model.addLessOrEqual(expr, totalSizeMax);
        }

        // Each task is assigned to exactly one worker.
        for (int task : allTasks) {
            List<Literal> workers = new ArrayList<>();
            for (int worker : allWorkers) {
                workers.add(x[worker][task]);
            }
            model.addExactlyOne(workers);
        }

        // Objective
        LinearExprBuilder obj = LinearExpr.newBuilder();
        for (int worker : allWorkers) {
            for (int task : allTasks) {
                obj.addTerm(x[worker][task], costs[worker][task]);
            }
        }
        model.minimize(obj);

        // Solve
        CpSolver solver = new CpSolver();
        CpSolverStatus status = solver.solve(model);

        // Print solution.
        // Check that the problem has a feasible solution.
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            System.out.println("Total cost: " + solver.objectiveValue() + "\n");
            for (int worker : allWorkers) {
                for (int task : allTasks) {
                    if (solver.booleanValue(x[worker][task])) {
                        System.out.println("Worker " + worker + " assigned to task " + task
                                + ".  Cost: " + costs[worker][task]);
                    }
                }
            }
        } else {
            System.err.println("No solution found.");
        }
    }

    private AssignmentTaskSizesSat() {}
}
