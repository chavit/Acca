package routine;

import java.util.*;

public class LPMSolver
{
    class Node
    {
        Node leftSon;
        Node rightSon;


        HashSet<Integer> availableActions;

        HashSet<Integer> actionsUnion;
        HashSet<Integer> actionsIntersection;

        Rule r;

        int type;
    }

    public Pair<Integer, HashSet<Integer>> dfs(Node cur, HashSet<Integer> up)
    {
        if (cur.availableActions != null)
        {
           up = cur.availableActions;
        }
        if (cur.leftSon == null)
        {
            if (cur.rightSon != null)
            {
                throw new AssertionError();
            }
            cur.actionsUnion = new HashSet<>(up);
            cur.actionsIntersection = new HashSet<>(up);
            return new Pair<>(1, new HashSet<>(up));
        }

        Pair<Integer, HashSet<Integer>> ansL = dfs(cur.leftSon, up);
        Pair<Integer, HashSet<Integer>> ansR = dfs(cur.rightSon, up);

        HashSet<Integer> ansActions = new HashSet<>(ansL.getValue());
        int ans = ansL.getKey() + ansR.getKey();

        if (ansL.getValue().stream().anyMatch(ansR.getValue()::contains))
        {
            ansActions.retainAll(ansR.getValue());
            ans--;

        } else
        {
            cur.type = 1;
            ansActions.addAll(ansR.getValue());
        }

        if (ansActions.size() == 0)
        {
            throw new AssertionError();
        }
        cur.actionsIntersection = new HashSet<>(ansActions);
        cur.actionsUnion = new HashSet<>();
        cur.actionsUnion.addAll(ansL.getValue());
        cur.actionsUnion.addAll(ansR.getValue());
        return new Pair<>(ans, ansActions);
    }


    public Node constructTree(ArrayList<Rule> rules) {
        Node root = new Node();
        for (Rule r : rules)
        {
            Node cur = root;
            for (int i = 0; i < r.bits.length; i++)
            {
                if (r.bits[i] == '*')
                {
                    for (int j = i; j < r.bits.length; j++)
                    {
                        if (r.bits[j] != '*')
                        {
                            throw new AssertionError();
                        }
                    }
                    break;
                }
                if (cur.leftSon == null)
                {
                    cur.leftSon = new Node();
                    cur.rightSon = new Node();
                }
                if (r.bits[i] == '0')
                {
                    cur = cur.leftSon;
                }
                else
                {
                    cur = cur.rightSon;
                }

            }
            if (cur.availableActions == null) {
                cur.availableActions = new HashSet<>(r.actions);
            } else {
                throw new AssertionError();
            }
        }
        return root;
    }

    public void validate(ArrayList<Rule> rules1, ArrayList<Rule> rules2)
    {
        validate_dfs(constructTree(rules1), constructTree(rules2), new HashSet<>(Collections.singletonList(-1)), new HashSet<>(Collections.singletonList(-1)));
    }


    private void validate_dfs(Node cur1, Node cur2, HashSet<Integer> up1, HashSet<Integer> up2) {
        if (cur1.availableActions != null)
        {
            up1 = cur1.availableActions;
        }
        if (cur2 != null && cur2.availableActions != null)
        {
            up2 = cur2.availableActions;
        }

        if (cur1.leftSon == null) {
            if (cur2!= null && cur2.leftSon != null) {
                throw new AssertionError();
            }
            if (!up1.containsAll(up2)) {
                throw new AssertionError();
            }
            return;
        }
        validate_dfs(cur1.leftSon, cur2 == null ? null : cur2.leftSon, up1, up2);
        validate_dfs(cur1.rightSon, cur2 == null ? null :  cur2.rightSon, up1, up2);
    }


    public ArrayList<Rule> minimize(ArrayList<Rule> rules)
    {
        Node root = constructTree(rules);
        int ans =  dfs(root, new HashSet<>(Collections.singleton(-1))).getKey();

        HashSet<Integer> allActions = new HashSet<>();
        for (Rule r : rules) {
            allActions.addAll(r.actions);
        }
        allActions.add(-1);

        if (allActions.contains(Integer.MAX_VALUE)) {
            throw new AssertionError();
        }

        ArrayList<Rule> recoverR = new ArrayList<>();
        recover(root, "", Integer.MAX_VALUE, recoverR);

        recoverAction(root, null, new HashSet<>(Arrays.asList(-1)), allActions);
        if (ans != recoverR.size()) {
            throw new AssertionError();
        }

        Collections.sort(recoverR, Comparator.comparingInt(o -> o.stars));
        validate(rules, recoverR);
        return recoverR;
    }

    private void recover(Node cur, String path, int actionAbove, ArrayList<Rule> recoverR) {
        if (cur == null) {
            return;
        }
        if (cur.type == 1 || cur.actionsUnion.contains(actionAbove)) {
            recover(cur.leftSon, path+"0", actionAbove, recoverR);
            recover(cur.rightSon, path+"1", actionAbove, recoverR);
            return;
        }

        int newC = cur.actionsIntersection.iterator().next();
        String str = path;
        while (str.length() < 32) {
            str += "*";
        }
        cur.r = new Rule(str.toCharArray(), new HashSet<>());
        recoverR.add(cur.r);
        recover(cur.leftSon, path+"0", newC, recoverR);
        recover(cur.rightSon, path+"1", newC, recoverR);
    }


    private void recoverAction(Node cur, Rule uprR, HashSet<Integer> up, HashSet<Integer> allActions) {
        if (cur.availableActions != null)
        {
            up = cur.availableActions;
        }
        if (cur.r != null) {
            uprR = cur.r;
            uprR.actions.addAll(allActions);
        }
        if (cur.leftSon == null) {
            uprR.actions.retainAll(up);
            if (uprR.actions.size() == 0) {
                throw new AssertionError();
            }
            return;
        }
        recoverAction(cur.leftSon, uprR, up, allActions);
        recoverAction(cur.rightSon, uprR, up, allActions);
   }

}
