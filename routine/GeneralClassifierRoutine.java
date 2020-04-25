package routine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class GeneralClassifierRoutine {

    public static ArrayList<Rule> createQoSRandScenario(ArrayList<Rule> rules, int k, boolean isShuffle) {
        ArrayList<Rule> answer = new ArrayList<>();
        for (Rule r : rules) {
            answer.add(new Rule(r.bits.clone(), new HashSet<>()));
        }

        ArrayList<Rule> answer2 = new ArrayList<>(answer);

        if (isShuffle) {
            Collections.shuffle(answer, new Random(239));
        }

        for (int i = 0; i < rules.size(); i++) {
            int action = 0;
            if (i > 0.33 * rules.size()) {
                action = 1;
            }
            if (i > 0.66 * rules.size()) {
                action = 2;
            }
            answer.get(i).actions.add(action);
        }

        Random rnd = new Random(17);
        for (int i = 0; i < answer.size() / 100 * k; i++) {
            int rn = rnd.nextInt(answer.size());
            answer.get(rn).actions.add(0);
            answer.get(rn).actions.add(1);
            answer.get(rn).actions.add(2);
        }
        return answer2;
    }


    public static ArrayList<Rule> createQoSBorderScenario(ArrayList<Rule> rules, double p, boolean isShuffle) {
        ArrayList<Rule> answer = new ArrayList<>();
        for (Rule r : rules) {
            answer.add(new Rule(r.bits, new HashSet<>()));
        }

        ArrayList<Rule> answer2 = new ArrayList<>(answer);

        if (isShuffle) {
            Collections.shuffle(answer, new Random(239));
        }

        for (int i = 0; i < rules.size(); i++) {
            int action = 0;
            if (i > 0.33 * rules.size()) {
                action = 1;
            }
            if (i > 0.66 * rules.size()) {
                action = 2;
            }
            answer.get(i).actions.add(action);
        }

        if (p < 1e-6)
        {
            return answer2;
        }

        int l1 = (int) (0.33 * rules.size() - 0.33 * 0.5 * p * rules.size());
        int r1 = (int) (0.33 * rules.size() + 0.33 * 0.5 * p * rules.size());

        int l2 = (int) (0.66 * rules.size() - 0.33 * 0.5 * p * rules.size());
        int r2 = (int) (0.66 * rules.size() + 0.33 * 0.5 * p * rules.size());

        for (int i = l1; i < r1; i++) {
            answer.get(i).actions.add(0);
            answer.get(i).actions.add(1);
        }

        for (int i = l2; i < r2; i++) {
            answer.get(i).actions.add(1);
            answer.get(i).actions.add(2);
        }

        return answer2;
    }


    public static ArrayList<Rule> cutWithManyStars(ArrayList<Rule> classifier) {
        int manyStars = 69;
//        manyStars = 32*2-10;
        for (int i = classifier.size() - 1; i >= 0; --i) {
            if (classifier.get(i).stars > manyStars) {
                classifier.remove(i);
            }
        }
        return classifier;
    }

    public static String transformAdress(String address) {
        StringTokenizer str = new StringTokenizer(address, "/.");
        ArrayList<Integer> values = new ArrayList<>();
        while (str.hasMoreTokens()) {
            String st = str.nextToken();
            if (st.startsWith("0x")) {
                values.add(Integer.decode(st));
            } else {
                values.add(Integer.parseInt(st));
            }
        }

        String resExact = "";

        for (int i = 0; i < values.size() - 1; i++) {
            int k = values.get(i);
            String cur = Integer.toBinaryString(k + (1 << 8));
            resExact += cur.substring(1);
        }

        String res = "";
        for (int i = 0; i < resExact.length(); i++) {
            res += (i < values.get(values.size() - 1)) ? resExact.charAt(i) : "*";
        }
        return res;
    }

    public static String transformField(String str) {
        String[] tokens = str.split("/");
        String a = Integer.toBinaryString(Integer.decode(tokens[0]) + (1 << 16)).substring(1);
        String b = Integer.toBinaryString(Integer.decode(tokens[1]) + (1 << 16)).substring(1);
        String ans = "";
        for (int i = 0; i < a.length(); i++) {
            if (b.charAt(i) == '1') {
                ans += a.charAt(i);
            } else {
                ans += "*";
            }
        }
        return ans;
    }

    public static ArrayList<Rule> parseClassifier(File f) throws FileNotFoundException {
        Scanner in = new Scanner(f);
        ArrayList<Rule> ans = new ArrayList<>();
        int cntK = 0;
        while (in.hasNext()) {
            String str = in.nextLine().substring(1);
            if (str.length() == 0) {
                continue;
            }

            String[] tokens = str.split("\\s+");

            String prefix = transformAdress(tokens[0]) + transformAdress(tokens[1]) + transformField(tokens[2]);
            // transform(tokens[2]+"/8");


            ArrayList<String> inExact = new ArrayList<>();
            ArrayList<String> outExact = new ArrayList<>();


            boolean isOutToken = false;
            for (int i = 3; i < tokens.length; i++) {
                String token = new StringTokenizer(tokens[i], "[] \t\',").nextToken();
                if (!isOutToken) {
                    inExact.add(token);
                } else {
                    outExact.add(token);
                }
                isOutToken |= tokens[i].contains("]");
            }

//            inExact.add("");
//            outExact.add("");



            cntK++;
            for (String inE : inExact) {
                for (String outE : outExact) {
                    ans.add(new Rule((prefix + inE + outE).toCharArray(), new HashSet<>()));
                }
            }
        }
        return ans;
    }

}
