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
public class AssignmentTeamsSat {
    public static void main(String[] args) {
        Loader.loadNativeLibraries();
        // Data
        int[][] costs = {
                {90, 76, 75, 70},
                {35, 85, 55, 65},
                {125, 95, 90, 105},
                {45, 110, 95, 115},
                {60, 105, 80, 75},
                {45, 65, 110, 95},
        };
        final int numWorkers = costs.length;
        final int numTasks = costs[0].length;

        final int[] allWorkers = IntStream.range(0, numWorkers).toArray();
        final int[] allTasks = IntStream.range(0, numTasks).toArray();

        final int[] team1 = {0, 2, 4};
        final int[] team2 = {1, 3, 5};
        // Maximum total of tasks for any team
        final int teamMax = 2;

        // Model
        CpModel model = new CpModel();

        // Variables
        Literal[][] x = new Literal[numWorkers][numTasks];
        // Variables in a 1-dim array.
        for (int worker : allWorkers) {
            for (int task : allTasks) {
                x[worker][task] = model.newBoolVar("x[" + worker + "," + task + "]");
            }
        }

        // Constraints
        // Each worker is assigned to at most one task.
        for (int worker : allWorkers) {
            List<Literal> tasks = new ArrayList<>();
            for (int task : allTasks) {
                tasks.add(x[worker][task]);
            }
            model.addAtMostOne(tasks);
        }

        // Each task is assigned to exactly one worker.
        for (int task : allTasks) {
            List<Literal> workers = new ArrayList<>();
            for (int worker : allWorkers) {
                workers.add(x[worker][task]);
            }
            model.addExactlyOne(workers);
        }

        // Each team takes at most two tasks.
        LinearExprBuilder team1Tasks = LinearExpr.newBuilder();
        for (int worker : team1) {
            for (int task : allTasks) {
                team1Tasks.add(x[worker][task]);
            }
        }
        model.addLessOrEqual(team1Tasks, teamMax);

        LinearExprBuilder team2Tasks = LinearExpr.newBuilder();
        for (int worker : team2) {
            for (int task : allTasks) {
                team2Tasks.add(x[worker][task]);
            }
        }
        model.addLessOrEqual(team2Tasks, teamMax);

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

    private AssignmentTeamsSat() {}
}
