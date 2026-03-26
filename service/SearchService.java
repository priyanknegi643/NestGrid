package src1.service;
import java.util.*;
import src1.model.Accomodation;
public class SearchService {
    public static List<Accomodation> search(List<Accomodation> list,
                                             double campusLat,
                                             double campusLon,
                                             double radius,
                                             String type) {

        List<Accomodation> result = new ArrayList<>();

        for (Accomodation a : list) {

            double distance = DistanceService.calculate(campusLat, campusLon, a.lat, a.lon);

            if (distance <= radius && a.type.equalsIgnoreCase(type)) {
                result.add(a);
            }
        }

        return result;
    }
}
