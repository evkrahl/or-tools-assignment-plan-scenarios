package AssignmentScenarios;

import com.google.ortools.Loader;
import com.google.ortools.graph.LinearSumAssignment;
import java.util.stream.IntStream;

/** Minimal Linear Sum Assignment problem. */
public class AssignmentLinearSumAssignment {
    public static void main(String[] args) {
        Loader.loadNativeLibraries();
        LinearSumAssignment assignment = new LinearSumAssignment();

        final int[][] costs = {
                {90, 76, 75, 70},
                {35, 85, 55, 65},
                {125, 95, 90, 105},
                {45, 110, 95, 115},
        };
        final int numWorkers = 4;
        final int numTasks = 4;

        final int[] allWorkers = IntStream.range(0, numWorkers).toArray();
        final int[] allTasks = IntStream.range(0, numTasks).toArray();

        // Add each arc.
        for (int w : allWorkers) {
            for (int t : allTasks) {
                if (costs[w][t] != 0) {
                    assignment.addArcWithCost(w, t, costs[w][t]);
                }
            }
        }

        LinearSumAssignment.Status status = assignment.solve();

        if (status == LinearSumAssignment.Status.OPTIMAL) {
            System.out.println("Total cost: " + assignment.getOptimalCost());
            for (int worker : allWorkers) {
                System.out.println("Worker " + worker + " assigned to task "
                        + assignment.getRightMate(worker) + ". Cost: " + assignment.getAssignmentCost(worker));
            }
        } else {
            System.out.println("Solving the min cost flow problem failed.");
            System.out.println("Solver status: " + status);
        }
    }

    private AssignmentLinearSumAssignment() {}
}

