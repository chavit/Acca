package routine;

import java.util.HashSet;

public class Rule {

    public Rule(char[] bits, HashSet<Integer> actions)
    {
        this.bits = bits.clone();
        this.actions = new HashSet<>(actions);
        calcStars();
    }


    public void modifyBit(char val, int index){
        bits[index] = val;
        calcStars();
    }

    private void calcStars()
    {
        stars = 0;
        zeros = 0;
        for (int i = 0; i < bits.length; i++)
        {
            if (bits[i] == '*')
            {
                stars++;
            }

            if (bits[i] == '0')
            {
                zeros++;
            }

        }
    }

    public String toString()
    {
        return toBitString() + " "+actions.toString();
    }

    public String toBitString()
    {
        String ans = "";
        for (int i = 0; i < bits.length; i++)
        {
            ans += bits[i];
        }
        return ans;
    }


    public char[] bits;
    public int stars, zeros;

    public HashSet<Integer> actions;
    int original_action;

}
