package src1.repository;
import java.util.*;
import src1.model.Accomodation;
public class DataRepository {
        public static List<Accomodation> loadDataFromCSV(String filePath) {
        List<Accomodation> list = new ArrayList<>();

        try (Scanner sc = new Scanner(new java.io.File(filePath))) {

            sc.nextLine(); 

            while (sc.hasNextLine()) {
                String[] data = sc.nextLine().split(",");

                list.add(new Accomodation(
                        data[0],
                        Double.parseDouble(data[1]),
                        Double.parseDouble(data[2]),
                        data[3],
                        Double.parseDouble(data[4]),
                        Double.parseDouble(data[5])
                ));
            }

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

        return list;
    }

    public static List<Accomodation> loadDataFromDB() {
        return new ArrayList<>();
    }
}

