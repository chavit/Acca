package routine;

import java.util.*;

public class Optimizator {

    public static int optimize(ArrayList<Rule> ans, boolean isFib)
    {
        OptimizatorState optimizatorState = new OptimizatorState(ans, isFib);
        System.err.println("Aga: "+optimizatorState.calculateSize());

        for (int it = 0;; it++)
        {
            int old = optimizatorState.calculateSize();
            optimizatorState.applyAllForwardSubsumptions();
            HashSet<Integer> used = new HashSet<>();
            for (int i = 0; i < ans.size(); i++) {
                ArrayList<Pair<Integer, Integer>> op = optimizatorState.getResolutionsForSecondRule(i);
                op.removeIf(o->used.contains(o.getKey()));
                if (op.size() > 0) {
                    Pair<Integer, Integer> cur = op.get(op.size()-1);
                    optimizatorState.applyResolution(cur.getKey(), cur.getValue());
                    optimizatorState.applyForwardSubsumptionsFor(cur.getKey());
//                    used.add(cur.getKey());
                }
            }

            if (optimizatorState.calculateSize() == old)
            {
                break;
            }

            System.err.println(optimizatorState.calculateSize());
        }

        for (int i = optimizatorState.rules.size() - 1; i >= 0; --i) {
            if (optimizatorState.findBackwardSubsumption(i) != null) {
                optimizatorState.applyBackwardSubsumption(i);
            }
        }

        System.err.println(optimizatorState.calculateSize());
        return optimizatorState.calculateSize();
    }


//
//    public static int optimizeClassifierOld(ArrayList<Rule> rules, boolean isFib)
//    {
//        OptimizatorState optimizatorState = new OptimizatorState(rules, isFib);
//        System.err.println("Before operations: "+optimizatorState.calculateSize());
//
//        for (int t = 0; t < 2; t++) {
//            int old = optimizatorState.calculateSize();
//            optimizatorState.applyAllForwardSubsumptions();
//            System.err.println("After fs: " + optimizatorState.calculateSize());
//            TreeSet<Integer> traversing = new TreeSet<>();
//            for (int i = 0; i < rules.size(); i++) {
//                traversing.add(i);
//            }
//
//            System.err.println("Well: "+optimizatorState.calculateSize());
//
//            while (traversing.size() > 0) {
//                int cur = traversing.first();
//                traversing.remove(cur);
//                ArrayList<Pair<Integer, Integer>> ops = optimizatorState.getResolutionsForFirstRule(cur);
//                if (ops.size() == 0) {
//                    continue;
//                }
//                traversing.add(cur);
//                Pair<Integer, Integer> operation = ops.get(0);
//                HashSet<Integer> toCheck = new HashSet<>();
//                toCheck.addAll(optimizatorState.intersectWith[operation.getKey()]);
//                toCheck.addAll(optimizatorState.intersectWith[operation.getValue()]);
//                toCheck.add(cur);
//                optimizatorState.applyResolution(operation.getKey(), operation.getValue());
//                ArrayList<Integer> removed = optimizatorState.getCoveringFor(cur);
//                for (int r : removed) {
//                    toCheck.addAll(optimizatorState.intersectWith[r]);
//                }
//                optimizatorState.applyForwardSubsumptionsFor(cur);
//                for (int temp : toCheck) {
//                    if (temp >= cur && !optimizatorState.isRemoved[temp]) {
//                        ArrayList<Pair<Integer, Integer>> newOps = optimizatorState.getResolutionsForSecondRule(cur);
//                        for (Pair<Integer, Integer> pair : newOps) {
//                            traversing.add(pair.getKey());
//                        }
//                    }
//                }
//            }
//
//            System.err.println("After resolution:" + optimizatorState.calculateSize());
//
//
//            for (int i = optimizatorState.rules.size() - 1; i >= 0; --i) {
//                if (optimizatorState.findBackwardSubsumption(i) != null) {
//                    optimizatorState.applyBackwardSubsumption(i);
//                }
//            }
//            System.err.println("After bs: " + optimizatorState.calculateSize());
//
//            if (t == 1 && optimizatorState.calculateSize() != old) {
//                throw new AssertionError();
//            }
//
//        }
//        return optimizatorState.calculateSize();
//    }

}
