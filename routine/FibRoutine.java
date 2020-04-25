package routine;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import static routine.Utils.*;

public class FibRoutine {

    public static ArrayList<Rule> parseIpClassifier(File f) throws FileNotFoundException, UnknownHostException {
        System.out.println(f.toString());
        Scanner in = new Scanner(f);
        ArrayList<Rule> ans = new ArrayList<>();
        HashMap<String, Integer> actionMap = new HashMap<String, Integer>();
        while (in.hasNext()) {
            String str = in.nextLine();
            String[] tokens = str.split("\\s+");
            String[] addrMask = tokens[0].split("/");

            byte[] parseBytes = InetAddress.getByName(addrMask[0]).getAddress();
            String ruleWithoutStar = "";
            for (int i = 0; i < parseBytes.length; i++) {
                String strK = Integer.toBinaryString(((int) parseBytes[i] & 0xFF) + 0x100).substring(1);
                ruleWithoutStar += strK;
            }
            String rule = "";
            for (int i = 0; i < ruleWithoutStar.length(); i++) {
                if (i < Integer.parseInt(addrMask[1])) {
                    rule += ruleWithoutStar.charAt(i);
                } else {
                    rule += '*';
                }
            }
            if (tokens[1].equals("drop")) {
                throw new AssertionError();
            }
            if (!actionMap.containsKey(tokens[1])) {
                actionMap.put(tokens[1], tokens[1].equals("drop") ? -1 : actionMap.size());
            }
            ans.add(new Rule(rule.toCharArray(), new HashSet<>(Arrays.asList(actionMap.get(tokens[1])))));
        }

        ans.sort(Comparator.comparingInt(o -> o.stars));
        return ans;
    }


    public static ArrayList<Rule> prepareClassifier(ArrayList<Rule> ans) {
        HashMap<String, Rule> was = new HashMap<>();
        boolean[] toRemove = new boolean[ans.size()];
        for (int i = ans.size() - 1; i >= 0; --i) {
            Rule r = ans.get(i);
            for (int j = ans.get(i).bits.length - r.stars - 1; j > 0; j--) {
                Rule k = was.get(r.toBitString().substring(0, j));
                if (k != null) {
                    toRemove[i] = hasCommonActions(r, k);
                    break;
                }
            }
            was.put(r.toBitString().substring(0, ans.get(i).bits.length - r.stars), r);
        }
        ans = clear(ans, toRemove);

        while (true) {
            HashSet<String> visited = new HashSet<>();
            toRemove = new boolean[ans.size()];
            for (int i = 0; i < ans.size(); i++) {
                String strK = ans.get(i).toBitString().substring(0, ans.get(i).bits.length- ans.get(i).stars);
                for (int j = 0; j <= strK.length(); j++) {
                    toRemove[i] |= visited.contains(strK.substring(0, j));
                }
                visited.add(strK);
            }
            ans = clear(ans, toRemove);
            int cntKR = 0;
            ans.sort(Comparator.comparingInt(o -> o.stars));
            HashMap<String, Rule> wasMapRule = new HashMap<>();
            for (int i = 0; i < ans.size(); i++) {
                String str = ans.get(i).toBitString().substring(0, ans.get(i).bits.length - ans.get(i).stars - 1);
                if (wasMapRule.containsKey(str)) {
                    if (hasCommonActions(wasMapRule.get(str), ans.get(i))) {
                        if (!isResoluteFilters(wasMapRule.get(str), ans.get(i))) {
                            throw new AssertionError();
                        } else {
                            wasMapRule.get(str).modifyBit('*', str.length());
                            wasMapRule.remove(str);
                            cntKR++;
                        }
                    }
                } else {
                    wasMapRule.put(str, ans.get(i));
                }
            }

            if (cntKR == 0) {
                break;
            }
        }

        was = new HashMap<>();
        toRemove = new boolean[ans.size()];
        for (int i = ans.size() - 1; i >= 0; --i) {
            Rule r = ans.get(i);
            for (int j = ans.get(i).bits.length - r.stars - 1; j > 0; j--) {
                Rule k = was.get(r.toBitString().substring(0, j));
                if (k != null) {
                    toRemove[i] = hasCommonActions(r, k);
                    break;
                }
            }
            was.put(r.toBitString().substring(0, ans.get(i).bits.length - r.stars), r);
        }
        ans = clear(ans, toRemove);
        return ans;
    }



    public static ArrayList<Rule> giveAlternativesForFibClassifier(ArrayList<Rule> classifier, double p, int numA) {

        ArrayList<Rule> ans3 = new ArrayList<>();
        for (Rule r : classifier) {
            Rule nw = new Rule(r.bits, r.actions);
            ans3.add(nw);
        }


        HashSet<Integer> diffActions = new HashSet<>();
        for (Rule r : classifier) {
            diffActions.addAll(r.actions);
        }
        diffActions.remove(-1);
        int[] dist = new int[diffActions.size()+100];
        for (Rule r : classifier) {
            if (r.actions.iterator().next() == -1)
            {
                continue;
            }
            dist[r.actions.iterator().next()]++;
        }
//        System.err.println(Arrays.toString(dist));

        if (ans3.size() == dist[0] + dist[1] + dist[2]) {
            numA = 1;
        }


        Random rnd = new Random();

        for (int i = 0; i < ans3.size(); i++) {
            if (rnd.nextDouble() > p || ans3.get(i).actions.contains(-1)) {
                continue;
            }

            Rule r = ans3.get(i);

            for (int it = 0; it < numA; it++) {
                int sum = 0;
                for (int j = 0; j < dist.length; j++) {
                    if (!r.actions.contains(j)) {
                        sum += dist[j];
                    }
                }

                int k = rnd.nextInt(sum);
                for (int j = 0; j < dist.length; j++) {
                    if (r.actions.contains(j)) {
                        continue;
                    }

                    if (k >= dist[j]) {
                        k -= dist[j];
                    } else {
                        ans3.get(i).actions.add(j);
                        break;
                    }
                }

            }
        }
        return ans3;
    }

}
