package newdaa;

import java.util.*;

class Facility {
    String name, type;
    double lat, lon;

    Facility(String name, String type, double lat, double lon) {
        this.name = name;
        this.type = type;
        this.lat = lat;
        this.lon = lon;
    }
}

class Accommodation {
    String name, type;
    double lat, lon, rent, rating;
    List<Facility> nearbyFacilities = new ArrayList<>();

    Accommodation(String name, double lat, double lon, String type, double rent, double rating) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.type = type;
        this.rent = rent;
        this.rating = rating;
    }

    void display(double distance, Map<String, Integer> weights) {
        double score = RecommendationService.calculateScore(this, weights);

        System.out.println("\n" + name + " | " + type + " | ₹" + rent +
                " | Rating: " + rating +
                " | Distance: " + String.format("%.2f", distance) + " km" +
                " | Score: " + String.format("%.2f", score));

        System.out.println("Map: https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon);

        if (nearbyFacilities.isEmpty()) {
            System.out.println("  No nearby facilities.");
        } else {
            System.out.println("  Nearby Facilities:");
            for (Facility f : nearbyFacilities) {
                double d = DistanceService.calculate(lat, lon, f.lat, f.lon);
                System.out.println("   - " + f.name + " (" + f.type + ") : " +
                        String.format("%.2f", d) + " km");
            }
        }
    }
}

class User {
    String username, password;
    User(String u, String p) { username = u; password = p; }
}

class UserRepository {
    static List<User> users = new ArrayList<>();

    static void save(User u) { users.add(u); }

    static User find(String username) {
        for (User u : users) if (u.username.equals(username)) return u;
        return null;
    }
}

class AuthService {
    static boolean signup(String u, String p) {
        if (UserRepository.find(u) != null) {
            System.out.println("User already exists!");
            return false;
        }
        if (p.length() < 6) {
            System.out.println("Password must be at least 6 characters!");
            return false;
        }
        UserRepository.save(new User(u, p));
        System.out.println("Signup successful!");
        return true;
    }

    static boolean login(String u, String p) {
        User user = UserRepository.find(u);
        if (user != null && user.password.equals(p)) {
            System.out.println("Login successful!");
            return true;
        }
        System.out.println("Invalid credentials!");
        return false;
    }
}

class DistanceService {
    static double calculate(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2)*Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}

class FacilityService {
    static void findNearbyFacilities(Accommodation acc, List<Facility> facilities, double maxDist) {
        for (Facility f : facilities) {
            double d = DistanceService.calculate(acc.lat, acc.lon, f.lat, f.lon);
            if (d <= maxDist) acc.nearbyFacilities.add(f);
        }
    }
}

class RecommendationService {
    static double calculateScore(Accommodation a, Map<String, Integer> weights) {
        double score = 0;
        for (Facility f : a.nearbyFacilities) {
            double d = DistanceService.calculate(a.lat, a.lon, f.lat, f.lon);
            int w = weights.getOrDefault(f.type, 0);
            score += w * (1 / (d + 0.1));
        }
        return score;
    }
}

class SearchService {
    static List<Accommodation> search(List<Accommodation> list, double lat, double lon, double radius, String type) {
        List<Accommodation> res = new ArrayList<>();
        for (Accommodation a : list) {
            double d = DistanceService.calculate(lat, lon, a.lat, a.lon);
            if (d <= radius && a.type.equalsIgnoreCase(type)) res.add(a);
        }
        return res;
    }
}

public class MainApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("===== Smart Accommodation Finder =====");

        while (true) {
            System.out.println("\n1. Signup\n2. Login\n3. Exit");
            int ch = sc.nextInt(); sc.nextLine();
            if (ch == 3) break;

            if (ch == 1) {
                System.out.print("Username: ");
                String u = sc.nextLine();
                System.out.print("Password: ");
                String p = sc.nextLine();
                AuthService.signup(u, p);
            }

            boolean loggedIn = false;
            while (!loggedIn) {
                System.out.println("\n1. Login\n2. Exit to Main Menu");
                int loginChoice = sc.nextInt(); sc.nextLine();

                if (loginChoice == 2) break;

                System.out.print("Username: ");
                String u = sc.nextLine();
                System.out.print("Password: ");
                String p = sc.nextLine();

                loggedIn = AuthService.login(u, p);

                if (!loggedIn) {
                    System.out.println("1. Retry\n2. Exit to Main Menu");
                    int retry = sc.nextInt(); sc.nextLine();
                    if (retry == 2) break;
                }
            }

            if (!loggedIn) continue;

            List<Accommodation> list = new ArrayList<>();
            List<Facility> facilities = new ArrayList<>();

            System.out.print("Enter number of accommodations: ");
            int n = sc.nextInt(); sc.nextLine();

            for (int i = 0; i < n; i++) {
                System.out.print("Name: ");
                String name = sc.nextLine();
                System.out.print("Lat: "); double lat = sc.nextDouble();
                System.out.print("Lon: "); double lon = sc.nextDouble(); sc.nextLine();
                System.out.print("Type: "); String type = sc.nextLine();
                System.out.print("Rent: "); double rent = sc.nextDouble();
                System.out.print("Rating: "); double rating = sc.nextDouble(); sc.nextLine();

                list.add(new Accommodation(name, lat, lon, type, rent, rating));
            }

            System.out.print("Enter number of facilities: ");
            int f = sc.nextInt(); sc.nextLine();

            for (int i = 0; i < f; i++) {
                System.out.print("Name: "); String name = sc.nextLine();
                System.out.print("Type: "); String type = sc.nextLine();
                System.out.print("Lat: "); double lat = sc.nextDouble();
                System.out.print("Lon: "); double lon = sc.nextDouble(); sc.nextLine();

                facilities.add(new Facility(name, type, lat, lon));
            }

            System.out.print("Enter your latitude: "); double userLat = sc.nextDouble();
            System.out.print("Enter your longitude: "); double userLon = sc.nextDouble();

            System.out.print("Enter radius (km): "); double radius = sc.nextDouble(); sc.nextLine();
            System.out.print("Enter type (PG/Flat): "); String type = sc.nextLine();

            System.out.print("Weight Gym: "); int wg = sc.nextInt();
            System.out.print("Weight Hospital: "); int wh = sc.nextInt();
            System.out.print("Weight Grocery: "); int wgr = sc.nextInt();

            Map<String, Integer> weights = new HashMap<>();
            weights.put("Gym", wg);
            weights.put("Hospital", wh);
            weights.put("Grocery", wgr);

            for (Accommodation a : list) {
                FacilityService.findNearbyFacilities(a, facilities, 2.0);
            }

            List<Accommodation> result = SearchService.search(list, userLat, userLon, radius, type);

            Collections.sort(result, (a, b) -> Double.compare(
                    RecommendationService.calculateScore(b, weights),
                    RecommendationService.calculateScore(a, weights)));

            if (result.isEmpty()) {
                System.out.println("No accommodations found.");
            } else {
                for (Accommodation a : result) {
                    double d = DistanceService.calculate(userLat, userLon, a.lat, a.lon);
                    a.display(d, weights);
                }
            }
        }
        sc.close();
    }
}
