package routine;

import java.lang.reflect.Array;
import java.util.*;

import static routine.Utils.isCover;

public class HeuristicOnO {

    public static ArrayList<Pair<Integer, Integer>> find_max_non_intersecting(OptimizatorState state, ArrayList<Pair<Integer, Integer>> pairs) {
        ArrayList<Rule> rules = state.rules;
        ArrayList<Integer> arr[] = new ArrayList[rules.size()];
        int[] color = new int[rules.size()];
        int[] prev = new int[rules.size()];

        for (int i = 0; i < rules.size(); i++) {
            arr[i] = new ArrayList<>();
            color[i] = rules.get(i).zeros % 2;
            prev[i] = -1;
        }

        for (Pair<Integer, Integer> e : pairs) {
            arr[e.getKey()].add(e.getValue());
            arr[e.getValue()].add(e.getKey());
        }

        Collections.sort(pairs, (o1, o2) -> o1.getValue().equals(o2.getValue()) ? Integer.compare(o2.getKey(), o1.getKey()) : Integer.compare(o1.getValue(), o2.getValue()));
        HashSet<Integer> ussed = new HashSet<>();
        for (Pair<Integer, Integer> e : pairs) {
            if (ussed.contains(e.getKey()) || ussed.contains(e.getValue())) {
                continue;
            }
            if (color[e.getValue()] == 1) {
                prev[e.getValue()] = e.getKey();
            } else {
                prev[e.getKey()] = e.getValue();
            }
            ussed.add(e.getKey());
            ussed.add(e.getValue());
        }


        for (int i = 0; i < rules.size(); i++) {
            if (arr[i].size() == 0 || color[i] == 1) {
                continue;
            }
            boolean[] used = new boolean[rules.size()];

            class dfs {
                public boolean dfs(int v) {
                    if (used[v]) {
                        return false;
                    }
                    used[v] = true;

                    for (int to : arr[v]) {
                        if (prev[to] == -1 || dfs(prev[to])) {
                            prev[to] = v;
                            return true;
                        }
                    }
                    return false;
                }
            }

            if (!ussed.contains(i)) {
                new dfs().dfs(i);
            }
        }

        ArrayList<Pair<Integer, Integer>> resolutions = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++) {
            if (prev[i] != -1) {
                resolutions.add(new Pair<>(Math.min(i, prev[i]), Math.max(i, prev[i])));
            }
        }
        Collections.sort(resolutions, Comparator.comparingInt(Pair::getKey));
        return resolutions;
    }


    private static ArrayList<Pair<Integer, Integer>> apply_resolutions(OptimizatorState optimizatorState, ArrayList<Pair<Integer, Integer>> operations) {
        operations = new ArrayList<>(operations);
        operations.removeIf(o1 -> optimizatorState.isRemoved[o1.getKey()] || optimizatorState.isRemoved[o1.getValue()] || optimizatorState.getActionListForResolution(o1.getKey(), o1.getValue()).size() == 0);
        ArrayList<Pair<Integer, Integer>> resolutions = find_max_non_intersecting(optimizatorState, operations);
        for (Pair<Integer, Integer> res : resolutions) {
            optimizatorState.applyResolution(res.getKey(), res.getValue());
        }
        return resolutions;
    }


    static class SequenceOptimal {
        ArrayList<Pair<Pair<Integer, Integer>, Integer>> operations = new ArrayList<>();
        OptimizatorState start;

        public SequenceOptimal(OptimizatorState start) {
            this.start = new OptimizatorState(start);
        }

        public void putOperation(Pair<Pair<Integer, Integer>, Integer> operation) {
            operations.add(operation);
        }

        public OptimizatorState applyAll() {
            TreeSet<Integer> bses = new TreeSet<>((o1, o2) -> Integer.compare(o2, o1));
            OptimizatorState ans = new OptimizatorState(start);
            for (Pair<Pair<Integer, Integer>, Integer> operation : operations) {
                if (operation.getValue() == 1) {
                    ans.applyForwardSubsumptionsUp(operation.getKey().getValue());
                }
                if (operation.getValue() == 2) {
                    bses.add(operation.getKey().getKey());
                }
                if (operation.getValue() == 3) {
                    ans.applyResolution(operation.getKey().getKey(), operation.getKey().getValue());
                    ArrayList<Integer> covering = ans.getCoveringFor(operation.getKey().getKey(), true);
                    for (Integer t : covering) {
                        if (bses.contains(t)) {
                            ans.applyForwardSubsumptionsUp(t);
                            bses.remove(t);
                        }
                    }
                }
            }
            for (int x : bses) {
                ans.applyBackwardSubsumption(x);
            }
            return ans;
        }

    }

    public static ArrayList<Integer> select_maximum_backward_subsumptions(OptimizatorState optimizatorState) {
        ArrayList<Integer> ans = new ArrayList<>();

        HashSet<Integer> cands = new HashSet<>();
        HashSet<Integer>[] actionLists = new HashSet[optimizatorState.rules.size()];

        for (int i = 0; i < optimizatorState.rules.size(); i++) {
            actionLists[i] = new HashSet<>(optimizatorState.rules.get(i).actions);
            if (optimizatorState.isRemoved[i]) {
                continue;
            }

            ArrayList<Integer> possibleMistakes = new ArrayList<>();
            for (int t : optimizatorState.intersectWith[i]) {
                if (cands.contains(t)) {
                    possibleMistakes.add(t);
                }
            }


            int actionBest = -1;
            int max = -1;
            for (int action : optimizatorState.rules.get(i).actions) {
                int cnt = 0;
                for (int rule : possibleMistakes) {
                    if (actionLists[rule].contains(action)) {
                        cnt++;
                    }
                }
                if (cnt > max) {
                    max = cnt;
                    actionBest = action;
                }
            }

            for (int rule : possibleMistakes) {
                if (!actionLists[rule].contains(actionBest)) {
                    cands.remove(rule);
                }
            }
            possibleMistakes.removeIf(o1 -> !cands.contains(o1));


            if (optimizatorState.findBackwardSubsumption(i) != null) {
                cands.add(i);
            }

            for (int t : possibleMistakes) {
                if (isCover(optimizatorState.rules.get(i), optimizatorState.rules.get(t))) {
                    ans.add(t);
                    cands.remove(t);
                    actionLists[i].retainAll(actionLists[t]);
                    if (actionLists[i].size() == 0) {
                        throw new AssertionError();
                    }
                }
            }
        }


        Collections.sort(ans);
        Collections.reverse(ans);
        return ans;
    }

    public static ArrayList<Rule> optimizeEfficient(ArrayList<Rule> ans, boolean isFib, boolean isSingleIter) {
        System.err.println("=====");
        OptimizatorState optimizatorState = new OptimizatorState(ans, isFib);
        SequenceOptimal sequenceOptimal = new SequenceOptimal(optimizatorState);
        System.err.println("Start minimization: " + optimizatorState.calculateSize());


        int it = 0;
        while (true) {
            it++;
            int old = optimizatorState.calculateSize();
            ArrayList<Pair<Integer, Integer>> resolutionsInO = new ArrayList<>();
            for (int i = 0; i < optimizatorState.rules.size(); i++) {
                ArrayList<Pair<Integer, Integer>> arr = optimizatorState.getResolutionsForSecondRule(i);
                Collections.reverse(arr);
                resolutionsInO.addAll(arr);
            }

            for (int i = 0; i < optimizatorState.rules.size(); i++) {
                ArrayList<Integer> covering = optimizatorState.getCoveringBy(i);
                if (covering.size()>0) {
                    sequenceOptimal.putOperation(new Pair<>(new Pair<>(covering.get(0), i), 1));
                }
            }
            optimizatorState.applyAllForwardSubsumptions();

            ArrayList<Integer> backwardSubsumptions = select_maximum_backward_subsumptions(optimizatorState);
            for (int i : backwardSubsumptions) {
                sequenceOptimal.putOperation(new Pair<>(optimizatorState.findBackwardSubsumption(i), 2));
                optimizatorState.applyBackwardSubsumption(i);
            }


            System.err.println("After bs: "+optimizatorState.calculateSize());
            ArrayList<Pair<Integer, Integer>> resolutions = apply_resolutions(optimizatorState, resolutionsInO);
            System.err.println("After rs: "+optimizatorState.calculateSize());
            if (isSingleIter || old == optimizatorState.calculateSize()) {
                break;
            }
            for (Pair<Integer, Integer> resolution : resolutions) {
                sequenceOptimal.putOperation(new Pair<>(new Pair<>(resolution.getKey(), resolution.getValue()), 3));
            }
            optimizatorState = sequenceOptimal.applyAll();
        }
        return Utils.clear(optimizatorState.rules, optimizatorState.isRemoved);

    }

}
