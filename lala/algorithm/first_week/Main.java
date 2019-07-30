import java.util.ArrayList;
import java.util.Scanner;

class Game {
    int number;
    ArrayList<Integer> card;
    public Game(int number, ArrayList card) {
        this.number = number;
        this.card = card;
    }
    public void Qsort(Game game, int low, int high) {
        int mid;
        if(low < high) {
            mid = game.Partition(game, low, high);
            game.Qsort(game, low, mid - 1);
            game.Qsort(game, mid + 1, high);
        }
    }
    public int Partition(Game game, int low, int high) {
        int mid = (int)game.card.get(low);
        while(low < high) {
            while(low < high && (int)game.card.get(high) <= mid) {
                high--;
            }
            //交换位置
            game.swap(game, low, high);
            while(low < high && (int)game.card.get(low) >= mid) {
                low++;
            }
            game.swap(game, low, high);
        }
        return low;
    }
    public void swap(Game game, int low, int high) {
        int mid = game.card.set(high, game.card.get(low));
        game.card.set(low, mid);
    }
    public int Game_sum(Game game, int j) {
        int sum = 0;
        for(int i = j; i < game.card.size(); i+=2) {
            sum += game.card.get(i);
        }
        return sum;
    }
}
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int number1 = sc.nextInt();
        ArrayList<Integer> card1 = new ArrayList();
        for(int i = 0; i < number1; i++) {
            card1.add(sc.nextInt());
        }
        Game game = new Game(number1, card1);
        game.Qsort(game, 0, game.card.size() - 1);
        int sum_jia, sum_yi;
        sum_jia = game.Game_sum(game, 0);
        sum_yi = game.Game_sum(game, 1);
        System.out.println(sum_jia - sum_yi);
    }
}
