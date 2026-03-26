package src1.controller;
import java.util.*;
import src1.model.Accomodation;
import src1.repository.DataRepository;
import src1.service.SearchService;

public class MainController {
    public static List<Accomodation> process(double campusLat,
                                              double campusLon,
                                              double radius,
                                              String type) {

        List<Accomodation> list =
                DataRepository.loadDataFromCSV("data.csv");

        return SearchService.search(list, campusLat, campusLon, radius, type);
    }
}
