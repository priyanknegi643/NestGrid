package src1;
import java.util.List;
import java.util.Scanner;
import src1.controller.MainController;
import src1.model.Accomodation;
import src1.service.AuthService;
import src1.service.DistanceService;
import src1.util.Input;
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("1. Signup");
        System.out.println("2. Login");
        int choice = sc.nextInt();
        sc.nextLine();
        if (choice == 1) {
            AuthService.signup(
                Input.getUsername(),
                Input.getPassword()
            );
        }
        boolean isAuthenticated = AuthService.login(
            Input.getUsername(),
            Input.getPassword()
        );
        if (!isAuthenticated) return;
        double radius = Input.getRadius();
        String type = Input.getType();
        // Default location (campus)
        double campusLat = 30.3165;
        double campusLon = 78.0322;
        List<Accomodation> results =
            MainController.process(campusLat, campusLon, radius, type);
        System.out.println("\nResults:\n");
        if (results.isEmpty()) {
            System.out.println("No accommodations found.");
            return;
        }
        for (Accomodation acc : results) {
            double distance = DistanceService.calculate(
                campusLat, campusLon,
                acc.lat, acc.lon
            );
            acc.display(distance);
        }
    }
}
