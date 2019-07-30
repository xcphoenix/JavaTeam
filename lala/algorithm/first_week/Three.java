import java.util.Scanner;

public class Three {
    public static void main(String[] args) {
        int k = 0;
        Scanner sc = new Scanner(System.in);
        StringBuilder str1 = new StringBuilder(sc.nextLine());
        StringBuilder str2 = new StringBuilder(sc.nextLine());
        for(int i = 0; i < str2.length(); i++) {
            for(int j = 0; j < str2.length() - 1; j++) {
                if(str2.charAt(j) < str2.charAt(j + 1)) {
                    char ptr = str2.charAt(j);//交换
                    str2.setCharAt(j, str2.charAt(j + 1));
                    str2.setCharAt(j + 1, ptr);
                }
            }
        }
        for(int i = 0; i < str1.length(); i++) {
            if(str2.charAt(k) > str1.charAt(i)) {
                //替换
                str1.setCharAt(i, str2.charAt(k));
                if(k < str2.length())
                    k++;
                if(k == str2.length())
                    break;
            }
        }
        System.out.println(str1);
    }
}
