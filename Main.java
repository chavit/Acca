import routine.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.*;


public class Main {


    public int[] calculateAllGeneralAlgorithms(ArrayList<Rule> classifier) {
        int[] res = new int[3];
        res[0] = Optimizator.optimize(classifier, false);
        res[1] = HeuristicOnO.optimizeEfficient(classifier, false, false).size();
        res[2] = HeuristicOnO.optimizeEfficient(classifier, false, true).size();
        if (res[0] < res[1]) {
            //throw new AssertionError();
        }
        return res;
    }


    public void runGeneral(String filename) throws FileNotFoundException {
        ArrayList<Rule> classifier = GeneralClassifierRoutine.parseClassifier(new File(filename));
        classifier = GeneralClassifierRoutine.cutWithManyStars(classifier);
        PrintWriter out = new PrintWriter("ans_" + filename);
        out.println(classifier.size());
        out.flush();
        ArrayList<Rule> classifierNotShuffled = GeneralClassifierRoutine.createQoSBorderScenario(classifier, 0, false);
        ArrayList<Rule> classifierShuffled = GeneralClassifierRoutine.createQoSBorderScenario(classifier, 0, true);
        ArrayList<Rule> classifierBest = Utils.makeSameAction(classifier);
        int[] exactNotShuffeled = calculateAllGeneralAlgorithms(classifierNotShuffled);
        int[] exactShuffeled = calculateAllGeneralAlgorithms(classifierShuffled);
        int[] best = calculateAllGeneralAlgorithms(classifierBest);
        out.println(exactNotShuffeled[0]+" "+exactNotShuffeled[1]+" "+exactShuffeled[0]+" "+exactShuffeled[1]+" "+best[0]+" "+best[1]);
        out.flush();

        for (int k = 0; k <= 10; k++) {
            int[] notShuffledBorder = calculateAllGeneralAlgorithms(GeneralClassifierRoutine.createQoSBorderScenario(classifier, 0.1 * k, false));
            int[] shuffledBorder = calculateAllGeneralAlgorithms(GeneralClassifierRoutine.createQoSBorderScenario(classifier, 0.1 * k, true));
            int[] notShuffleRand = calculateAllGeneralAlgorithms(GeneralClassifierRoutine.createQoSRandScenario(classifier, 3 * k, false));
            out.print(k + " : ");
            for (int[] arr : new int[][]{notShuffledBorder, shuffledBorder, notShuffleRand}) {
                for (int t = 0; t < 2; t++) {
                    int[] exact = arr == shuffledBorder ? exactShuffeled : exactNotShuffeled;
                    out.printf("%d %d %.3f ", arr[t], classifier.size()-arr[t], 1.0*(exact[t] - arr[t])/(exact[t] - best[t]) );
                }
            }
            out.println();
            out.flush();
        }
        out.close();
    }

    public int[] calculateAllFibAlgorithms(ArrayList<Rule> classifier) {
        int[] res = new int[5];
        ArrayList<Rule> lpmMin = new LPMSolver().minimize(classifier);
        res[0] = lpmMin.size();
        res[1] = Optimizator.optimize(classifier, true);
        res[2] = HeuristicOnO.optimizeEfficient(classifier, true,false).size();
        if (res[1] < res[2]) {
            throw new AssertionError();
        }
        res[3] = HeuristicOnO.optimizeEfficient(lpmMin, true,false).size();
        res[4] = HeuristicOnO.optimizeEfficient(classifier, true, true).size();
        return res;
    }


    public void runFib(File f) throws FileNotFoundException, UnknownHostException {
        PrintWriter out = new PrintWriter("ans_"+f.getName());
        System.err.println(f.getName());
        ArrayList<Rule> classifer = FibRoutine.parseIpClassifier(f);
        classifer = FibRoutine.prepareClassifier(classifer);
        System.err.println(classifer.size());
        int[] exact = calculateAllFibAlgorithms(classifer);
        out.println(exact[0]+" "+exact[1]+" "+exact[2]+" "+exact[3]+" "+exact[4]);
        out.flush();
        int[] best = calculateAllFibAlgorithms(Utils.makeSameAction(classifer));
        out.println(best[0]+" "+best[1]+" "+best[2]+" "+best[3]+" "+best[4]);
        out.flush();
        for (int i = 0; i <= 10; i++)
        {
            out.print(i+" : ");
            for (int it = 1; it <= 3; it++)
            {
                int[] cur = calculateAllFibAlgorithms(FibRoutine.giveAlternativesForFibClassifier(classifer, i*0.1,it));
                for (int j = 0; j < 4; j++) {
                    String ansik = String.format("%d %.3f %.3f", cur[j], 1.0*(exact[j]-cur[j])/exact[j], 1.0*(exact[j]-cur[j])/(exact[j] - best[j]));
                    out.print(ansik+" ");
                }
                out.print(cur[4]+" ");
                out.flush();
            }
            out.println();
        }
        out.close();
    }


    public static void main(String[] args) throws FileNotFoundException, UnknownHostException {
        if (args[0].equals("fib")) {
            new Main().runFib(new File(args[1]));
        }
        if (args[0].equals("gen")) {
          new Main().runGeneral(args[1]);
        }
    }


}
