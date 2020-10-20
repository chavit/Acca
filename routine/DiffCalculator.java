package routine;

import java.nio.channels.AsynchronousCloseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DiffCalculator {

    static public int last_new_rule_stat = -1;


    public static void calculate_number_of_modified_results(ArrayList<Rule> original, OptimizatorState test) {
        for (Rule r : original) {
            if (r.original_action == -1) {
                throw new AssertionError();
            }
        }
        int cnt = 0;
        for (int i = 0; i < original.size(); i++) {
            Rule r = test.rules.get(i);
            if (test.isRemoved[i]) {
                continue;
            }
            if (!r.toBitString().equals(original.get(i).toBitString())
                    || !r.actions.contains(original.get(i).original_action)
            ) {
                cnt++;
            }
        }
        last_new_rule_stat = cnt;

    }
}
