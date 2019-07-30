import java.util.Scanner;

public class Four {
    public static void Find(String str) {
        int max = 0;
        char ptr = 'a';
        for(int i = 0; i < str.length(); i++) {
            if(ptr <= str.charAt(i)) {
                ptr = str.charAt(i);
            }
        }
//System.out.println("ptr = " + ptr);
        String split_str = "" + ptr;
        String[] str1 = str.split(split_str);
//System.out.println("str1.length = " + str1.length);
        if(str1.length == 0) {
            System.out.println(str);
        } else if(str.lastIndexOf(ptr) == str.length() - 1) {
            max = str1.length;
            for(int i = 0; i < max; i++) {
                System.out.print(ptr);
            }
            System.out.println();
        } else {
            max = str1.length - 1;
            for(int i = 0; i < max; i++) {
                System.out.print(ptr);
            }
            if(str1[str1.length - 1].length() == 1) {
                System.out.println(str1[str1.length - 1]);
            } else {
                Find(str1[str1.length - 1]);
            }
        }

    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String str = sc.nextLine();

        Find(str);
    }
}
