

package retrospector.util;

import java.math.BigInteger;
import java.util.Comparator;


/**
 * Compares strings in a 'natural' way. The ordering is as follows:
 * + Non-Alphanumeric characters are first
 *   - Ordered by their ASCII values
 * + Numeric characters come second
 *   - Consecutive numerics are group
 *   - Then ordered by the resulting integer value
 * + Alphabetic characters come third
 *   - Alphabetically ordered
 * + Length comes last
 *   - The shorter comes first
 * 
 * For example, {A,C,03,1,b,002,$} would be sorted into {$,1,002,03,A,b,C}. 
 * @author NonlinearFruit
 */
public class NaturalOrderComparator implements Comparator<String> {
    /**
     * This enum represents the three types of characters:
     *  + DIGIT - [0-9]+
     *  + ALPHA - [A-Za-z]
     *  + OTHER - [^0-9A-Za-z]
     */
    private static enum Type{ 
        DIGIT(1), ALPHA(2), OTHER(0);
        
        /**
         * Takes a char and returns the Type it belongs to
         * @param x
         * @return 
         */
        public static Type getType(char x){
            if(Character.isDigit(x))
                return DIGIT;
            if(Character.isAlphabetic(x))
                return ALPHA;
            return OTHER;
        }
        
        /**
         * Compares two Types based on natural order. Returns:
         *  + Negative if `a` is before `b`
         *  + Zero if `a` and `b` are the same Type
         *  + Positive if `a` is after `b`
         * @param a
         * @param b
         * @return a negative integer, zero, or a positive integer as the
         *         first argument is less than, equal to, or greater than the
         *         second.
         */
        public static int compare(Type a, Type b){
            return Integer.compare(a.getValue(), b.getValue());
        }
        
        private int value;
        
        Type(int v){ 
            value = v;
        }
        
        private int getValue(){
            return value;
        }
    }
    
    /**
     * Takes two integer strings and compares the integer value of them. For
     * example, "03" and "1" would result in 1
     * @param a 
     * @param b
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second.
     */
    private int compareDigitUnits(String a, String b)
    {
        BigInteger aInt = new BigInteger(a);
        BigInteger bInt = new BigInteger(b);
        return aInt.compareTo(bInt);
    }
    
    /**
     * Takes two characters and compares them based on Alphabetical ordering. 
     * This also works for ASCIIbetical sorting of non-letters.
     * @param a
     * @param b
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second.
     */
    private int compareNonDigitUnits(char a, char b)
    {
        char lowA = Character.toLowerCase(a);
        char lowB = Character.toLowerCase(b);
        
        if(lowA == lowB)
            return Character.compare(a, b);
        
        return Character.compare(lowA, lowB);
    }

    /**
     * This takes two strings and compares them based on natural ordering/
     * @param s1 String of characters
     * @param s2 String of characters
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second.
     */
    @Override
    public int compare(String s1, String s2)
    {
        StringBuilder a = new StringBuilder(s1);
        StringBuilder b = new StringBuilder(s2);
        
        while(a.length()>0 && b.length()>0){
            
            String nextAUnit = getNextUnit(a.toString());
            String nextBUnit = getNextUnit(b.toString());
            
            a.delete(0, nextAUnit.length());
            b.delete(0, nextBUnit.length());
            
            Type aUnitType = Type.getType(nextAUnit.charAt(0));
            Type bUnitType = Type.getType(nextBUnit.charAt(0));
            
            int result = Type.compare(aUnitType, bUnitType);
            if(result != 0)
                return result;
            
            if(aUnitType == Type.DIGIT)
                result = compareDigitUnits(nextAUnit, nextBUnit);
            else
                result = compareNonDigitUnits(nextAUnit.charAt(0), nextBUnit.charAt(0));
            
            if(result != 0)
                return result;
            
        }
        
        // All things being equal, let the shorter win
        return s1.length()-s2.length();
    }
    
    /**
     * Takes a string of characters and returns the 1st character unless it is
     * a digit. If the 1st character is a digit, it returns the 1st character 
     * plus all of the consecutive digits.
     * @param s
     * @return a character or a string of digits
     */
    private String getNextUnit(String s){
        char firstChar = s.charAt(0);
        if(!Character.isDigit(firstChar))
            return String.valueOf(firstChar);
        
        StringBuilder bob = new StringBuilder();
        char[] chars = s.toCharArray();
        for (char aChar : chars) {
            if(Character.isDigit(aChar))
                bob.append(aChar);
            else
                break;
        }
        return bob.toString();
    }
}
