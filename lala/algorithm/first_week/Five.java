import java.util.ArrayList;
import java.util.Scanner;

public class Five {
    public static void main(String[] args) {

        int single_number;
        Scanner sc = new Scanner(System.in);
        int sum_number = sc.nextInt();
        for(int i = 0; i < sum_number; i++) {
            int num_four = 0;
            int num_two = 0;
            ArrayList<Integer> array1 = new ArrayList<>();
            single_number = sc.nextInt();
            for(int j = 0; j < single_number; j++) {
                int flag = sc.nextInt();
                array1.add(flag);
                if(flag % 4 == 0)
                    num_four += 1;
                else if (flag % 2 == 0)
                    num_two += 1;
            }
            if(num_four >= single_number / 2) {
                System.out.println("Yes");
            } else if(num_two == single_number) {
                System.out.println("Yes");
            } else if((2 * num_four + num_two ) >= single_number) {
                System.out.println("Yes");
            } else {
                System.out.println("No");
            }
        }
    }
}
