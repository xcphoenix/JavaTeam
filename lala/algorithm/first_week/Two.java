import java.util.HashSet;
import java.util.Scanner;

public class Two {
    public static void main(String[] args) {
        int flag = 0;
        Scanner sc = new Scanner(System.in);
        String s1 = sc.nextLine();
        String s2 = sc.nextLine();
        HashSet<String> set = new HashSet<>();
        for(int i = 0; i <= s1.length() - s2.length(); i++) {
            flag = 0;
            String s3 = s1.substring(i, i + s2.length());
            for(int j = 0; j < s2.length(); j++) {
                if(s2.charAt(j) != '?' && s2.charAt(j) != s3.charAt(j)) {
                    flag = 1;
                    break;
                }
            }
            if(flag == 0)
                set.add(s3);
        }

        System.out.println(set.size());
    }
}
