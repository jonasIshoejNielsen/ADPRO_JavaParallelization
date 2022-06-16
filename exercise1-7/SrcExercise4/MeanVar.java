
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Scanner;


class MeanVar {
    public static void main(String[] args) {

        double[] data = new double[0];

        if (args.length > 0) {
            var parsed = parseData(args[0]);
            data = new double[parsed.size()];
            int i = 0;
            for (var d : parsed) {
                data[i] = d;
                i++;
            }
        }
        else {
            data = new double[] { 30.7, 100.2, 30.1, 30.7, 20.2, 30.4, 2, 30.3, 30.5, 5.4, 25};
        }

        var st = 0;
        var sst = 0;
        var n = data.length;

        for (int j= 0; j<n; j++) {
            var time = data[j];
            st += time;
            sst += time * time;
        }

        double mean = st/n;
        double sdev = Math.sqrt((sst - mean*mean*n)/(n-1));
        System.out.printf("%6.1f ns +/- %6.3f%n", mean, sdev);
    }

    public static ArrayList<Double> parseData(String filepath){
        Scanner scan;
        File file = new File(filepath);
        try {
            scan = new Scanner(file);

            while(scan.hasNextDouble())
            {
                System.out.println( scan.nextDouble() );
            }

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        return new ArrayList<Double>(); //parse the data
    }
}