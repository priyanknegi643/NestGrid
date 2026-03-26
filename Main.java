package src1;
import java.util.List;
import java.util.Scanner;

import src1.controller.*;
import src1.model.*;
import src1.service.AuthService;
import src1.util.Input;
import src1.service.DistanceService;
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

        boolean auth = AuthService.login(
                Input.getUsername(),
                Input.getPassword()
        );

        if (!auth) return;

        double radius = Input.getRadius();
        String type = Input.getType();

        // Predefined campus
        double campusLat = 30.3165;
        double campusLon = 78.0322;

        List<Accomodation> result =
                MainController.process(campusLat, campusLon, radius, type);

        System.out.println("\nResults:\n");

        if (result.isEmpty()) {
            System.out.println("No accommodations found.");
        } else {
            for (Accomodation a : result) {
                double dist = DistanceService.calculate(campusLat, campusLon, a.lat, a.lon);
                a.display(dist);
            }
        }
    }
    
} 
