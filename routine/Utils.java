package routine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Utils {

    public static ArrayList<Rule> clear(ArrayList<Rule> classifier, boolean[] isRemove) {
        ArrayList<Rule> resultClassifier = new ArrayList<>();
        for (int i = 0; i < isRemove.length; i++) {
            if (!isRemove[i]) {
                resultClassifier.add(classifier.get(i));
            }
        }
        return resultClassifier;
    }

    public static ArrayList<Rule> makeSameAction(ArrayList<Rule> classifier) {
        ArrayList<Rule> ans = new ArrayList<>();
        for (Rule r : classifier)
        {
            ans.add(new Rule(r.bits, new HashSet<>(Arrays.asList(0))));
        }
        return ans;
    }


    public static boolean isIntersect(Rule r1, Rule r2)
    {
        for (int i = 0; i < r1.bits.length; i++)
        {
            if (r1.bits[i] != r2.bits[i] && r1.bits[i] != '*' && r2.bits[i] != '*')
            {
                return false;
            }
        }
        return true;
    }


    public static boolean isCover(Rule r1, Rule r2) {
        if (r1.stars < r2.stars)
        {
            return false;
        }
        if (r1.zeros > r2.zeros)
        {
            return false;
        }
        for (int i = 0; i < r1.bits.length; i++)
        {
            if (r1.bits[i] != r2.bits[i] && r1.bits[i] != '*')
            {
                return false;
            }
        }
        return true;
    }

    public static boolean isResoluteFilters(Rule r1, Rule r2)
    {
        if (r1.stars != r2.stars)
        {
            return false;
        }
        if (Math.abs(r1.zeros -r2.zeros) != 1)
        {
            return false;
        }

        int cnt = 0;
        for (int i = 0; i < r1.bits.length; i++)
        {
            if (r1.bits[i] != r2.bits[i] && (r1.bits[i] == '*' || r2.bits[i] == '*'))
            {
                return false;
            }
            if (r1.bits[i] != r2.bits[i])
            {
                cnt++;
            }
            if (cnt > 1)
            {
                return false;
            }
        }
        return cnt==1;
    }

    public static boolean hasCommonActions(Rule r1, Rule r2)
    {
        return r1.actions.stream().anyMatch(r2.actions::contains);
    }

    public static Rule createRuleByResolutionWithoutActions(Rule r1, Rule r2)
    {
        if (!isResoluteFilters(r1, r2))
        {
            throw new AssertionError();
        }

        Rule result = new Rule(r1.bits, new HashSet<>());
        for (int i = 0; i < r2.bits.length; i++)
        {
            if (r1.bits[i] != r2.bits[i])
            {
                result.modifyBit('*', i);
            }
        }
        return result;
    }
//
//    public void calculateIntersections(ArrayList<>) {
//        for (int i = 0; i < rules.size(); i++) {
//            for (int j = 0; j < i; j++) {
//                if (isIntersect(rules.get(i), rules.get(j))) {
//                    intersectWith[j].add(i);
//                    intersectWith[i].add(j);
//                }
//            }
//        }
//    }


}
