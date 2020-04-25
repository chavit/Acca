package routine;

import java.util.*;
import static routine.Utils.*;

public class OptimizatorState {


    public ArrayList<Rule> rules;

    HashMap<String, HashSet<Integer>> forResolutionPairs = new HashMap<>();

    TreeSet<Integer>[] intersectWith;
    HashSet<Integer>[] resolutedWith;


    public boolean[] isRemoved;


    int size;

    public OptimizatorState(ArrayList<Rule> rules, boolean isFib) {
        intersectWith = new TreeSet[rules.size()];
        resolutedWith = new HashSet[rules.size()];
        isRemoved = new boolean[rules.size()];
        this.rules = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++) {
            this.rules.add(new Rule(rules.get(i).bits, rules.get(i).actions));
            intersectWith[i] = new TreeSet<>();
            resolutedWith[i] = new HashSet<>();
        }
        size = rules.size();
        if (isFib) {
            initIntersectionsFib();
        } else {
            initIntersectionsRegular();
        }
        initResolutions();
    }

    public OptimizatorState(OptimizatorState old) {
        this.rules = new ArrayList<>();
        intersectWith = new TreeSet[old.rules.size()];
        resolutedWith = new HashSet[old.rules.size()];
        isRemoved = old.isRemoved.clone();

        for (int i = 0; i < old.rules.size(); i++) {
            this.rules.add(new Rule(old.rules.get(i).bits, old.rules.get(i).actions));
            intersectWith[i] = new TreeSet<>(old.intersectWith[i]);
            resolutedWith[i] = new HashSet<>(old.resolutedWith[i]);
        }
        size = rules.size();

        for (Map.Entry<String, HashSet<Integer>> entry : old.forResolutionPairs.entrySet()) {
            forResolutionPairs.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
    }

    public void initIntersectionsRegular() {
        for (int i = 0; i < rules.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (isIntersect(rules.get(i), rules.get(j))) {
                    intersectWith[j].add(i);
                    intersectWith[i].add(j);
                }
            }
        }
    }


    public void initIntersectionsFib() {
        HashMap<String, ArrayList<Integer>> map = new HashMap<>();
        for (int i = rules.size()-1; i>=0; --i) {
            String str = rules.get(i).toBitString();
            for (int j = str.length()-1; j>=0; j--) {
                str = str.substring(0,j)+"*"+str.substring(j+1);
                if (map.containsKey(str)) {
                    ArrayList<Integer> inter = map.get(str);
                    for (int t : inter) {
                        intersectWith[t].add(i);
                        intersectWith[i].add(t);
                    }
                }
            }
            str = rules.get(i).toBitString();
            if (!map.containsKey(str)) {
                map.put(str, new ArrayList<>());
            }
            map.get(str).add(i);
        }
    }


    public void initResolutions() {
        for (int i = 0; i < rules.size(); i++) {
            updateResolutionListAfterInsert(i);
        }
    }

    private void updateResolutionListAfterInsert(int no_rule) {
        Rule r = rules.get(no_rule);
        if (forResolutionPairs.containsKey(r.toBitString())) {
            for (int t : forResolutionPairs.get(r.toBitString())) {
                if (t == no_rule) {
                    throw new AssertionError();
                }
                if (!isResoluteFilters(rules.get(t), rules.get(no_rule))) {
                    throw new AssertionError();
                }
                resolutedWith[no_rule].add(t);
                resolutedWith[t].add(no_rule);
            }
        }
        String str = r.toBitString();
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '*') {
                continue;
            }
            String str2 = str.substring(0, i) + (char) ('0' + (1 - (str.charAt(i) - '0'))) + str.substring(i + 1);
            if (!forResolutionPairs.containsKey(str2)) {
                forResolutionPairs.put(str2, new HashSet<>());
            }
            forResolutionPairs.get(str2).add(no_rule);
        }
    }


    private void updateResolutionListAfterRemove(int no_rule) {
        for (int t : resolutedWith[no_rule]) {
            resolutedWith[t].remove(no_rule);
        }
        resolutedWith[no_rule].clear();
        String str = rules.get(no_rule).toBitString();
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '*') {
                continue;
            }
            String str2 = str.substring(0, i) + (char) ('0' + (1 - (str.charAt(i) - '0'))) + str.substring(i + 1);
            forResolutionPairs.get(str2).remove(no_rule);
        }
    }

    private void updateIntersetcionsAfterRemove(int no_rule) {
        for (int t : intersectWith[no_rule]) {
            intersectWith[t].remove(no_rule);
        }
        intersectWith[no_rule].clear();
    }


    public void removeRule(int cur) {
        if (isRemoved[cur]) {
            throw new AssertionError();
        }
        isRemoved[cur] = true;
        size--;
        updateResolutionListAfterRemove(cur);
        updateIntersetcionsAfterRemove(cur);
    }

    private void setIntersections(int i, TreeSet<Integer> intersecting) {
        intersectWith[i] = intersecting;
        for (int no_rule : intersecting) {
            intersectWith[no_rule].add(i);
        }
    }

    private void insertRule(int i, Rule rule, TreeSet<Integer> intersecting) {
        if (!isRemoved[i]) {
            throw new AssertionError();
        }
        setIntersections(i, intersecting);
        rules.set(i, rule);
        isRemoved[i] = false;
        size++;
        updateResolutionListAfterInsert(i);
    }


    public ArrayList<Integer> getCoveringFor(int cur, boolean isForward) {
        if (isRemoved[cur]) {
            throw new AssertionError();
        }
        ArrayList<Integer> covered = new ArrayList<>();
        for (int t : intersectWith[cur]) {
            if (isForward && t > cur && isCover(rules.get(cur), rules.get(t))) {
                covered.add(t);
            }
            if (!isForward && t < cur && isCover(rules.get(cur), rules.get(t))) {
                covered.add(t);
            }
        }
        return covered;
    }


    public ArrayList<Integer> getCoveringBy(int cur) {
        if (isRemoved[cur]) {
            return new ArrayList<>();
        }
        ArrayList<Integer> coveredBy = new ArrayList<>();
        for (int t : intersectWith[cur]) {
            if (t < cur && isCover(rules.get(t), rules.get(cur))) {
                coveredBy.add(t);
            }
        }
        return coveredBy;
    }



    public void applyForwardSubsumptionsFor(int cur) {
        ArrayList<Integer> toRemove = getCoveringFor(cur, true);
        for (int t : toRemove) {
            removeRule(t);
        }
    }

    public void applyForwardSubsumptionsUp(int cur) {
        if (getCoveringBy(cur).size() == 0) {
            throw new AssertionError();
        }
        removeRule(cur);
    }

    public void applyAllForwardSubsumptions() {
        for (int i = 0; i < rules.size(); i++) {
            if (!isRemoved[i]) {
                applyForwardSubsumptionsFor(i);
            }
        }
    }


    public int calculateSize() {
        return size;
    }


    public HashSet<Integer> getActionListForResolution(int i, int j) {
        HashSet<Integer> res = new HashSet<>(rules.get(i).actions);
        res.retainAll(rules.get(j).actions);
        for (int t : intersectWith[j]) {
            if (t > i && t < j) {
                res.retainAll(rules.get(t).actions);
            }
        }
        return res;
    }

    public ArrayList<Pair<Integer, Integer>> getResolutionsForRule(int j, boolean isFirst) {
        if (isRemoved[j]) {
            return new ArrayList<>();
        }
        ArrayList<Pair<Integer, Integer>> ans = new ArrayList<>();
        for (int i : resolutedWith[j]) {
            if (isRemoved[i] || isRemoved[j]) {
                throw new AssertionError();
            }
            if (isFirst && j < i && getActionListForResolution(j,i).size()>0) {
                ans.add(new Pair<>(j, i));
            }
            if (!isFirst && i < j && getActionListForResolution(i,j).size()>0) {
                ans.add(new Pair<>(i, j));
            }
        }
        Collections.sort(ans, (o1, o2)-> o1.getKey().equals(o2.getKey()) ? Integer.compare(o1.getValue(), o2.getValue()) : Integer.compare(o1.getKey(), o2.getKey()));
        return ans;
    }

    public ArrayList<Pair<Integer, Integer>> getResolutionsForSecondRule(int j) {
       return getResolutionsForRule(j, false);
    }


    public ArrayList<Pair<Integer, Integer>> getResolutionsForFirstRule(int j) {
        return getResolutionsForRule(j, true);
    }


    public void applyResolution(int i, int j) {
        TreeSet<Integer> intersecting = new TreeSet<>(intersectWith[i]);
        intersecting.addAll(intersectWith[j]);

        removeRule(j);
        removeRule(i);
        insertRule(i, new Rule(createRuleByResolutionWithoutActions(rules.get(i), rules.get(j)).toBitString().toCharArray(), getActionListForResolution(i,j)), intersecting);
    }


    public Pair<Integer, Integer> findBackwardSubsumption(int i) {
        if (isRemoved[i]) {
            return null;
        }
//        int cost = 0;
        for (int j : intersectWith[i]) {
            if (j>i && !hasCommonActions(rules.get(i), rules.get(j))) {
                return null;
            }
//            if (j > i) {
//                HashSet<Integer> intersect = new HashSet<>(rules.get(i).actions);
//                intersect.retainAll(rules.get(j).actions);
//                cost += rules.get(j).actions.size() - intersect.size();
//            }
            if (isCover(rules.get(j), rules.get(i))) {
                if (j < i) {
                    throw new AssertionError();
                }
                return new Pair<>(i,j);
            }
        }
        return null;
    }

    public void applyBackwardSubsumption(int i) {
        for (int j : intersectWith[i]) {
            if (j>i) {
                rules.get(j).actions.retainAll(rules.get(i).actions);
                if (!hasCommonActions(rules.get(i), rules.get(j))) {
                    throw new AssertionError();
                }
            }
            if (isCover(rules.get(j), rules.get(i))) {
                if (j < i) {
                    System.err.println(i+" "+j);
                    throw new AssertionError();
                }
                removeRule(i);
                return;
            }
        }
        throw new AssertionError();
    }

}
