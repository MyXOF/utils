package myxof.git.date;

import java.util.Random;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        for(int i = 0;i < 20;i++){
        	Random random = new Random(System.nanoTime());
        	int num = Math.abs(random.nextInt()) % 10;
        	System.out.println(num+"---");
        }
    }
}
