package newdaa;
import java.util.*;

//================= MODEL =================
class Accommodation {
 String name;
 double lat, lon;
 String type;
 double rent;
 double rating;

 Accommodation(String name, double lat, double lon, String type, double rent, double rating) {
     this.name = name;
     this.lat = lat;
     this.lon = lon;
     this.type = type;
     this.rent = rent;
     this.rating = rating;
 }

 void display(double distance) {
     System.out.println(name + " | " + type + " | ₹" + rent +
             " | Rating: " + rating +
             " | Distance: " + String.format("%.2f", distance) + " km");
 }
}

class User {
 String username, password;
 User(String u, String p) {
     username = u;
     password = p;
 }
}

//================= REPOSITORY =================
class UserRepository {
 static List<User> users = new ArrayList<>();

 static void save(User u) {
     users.add(u);
 }

 static User find(String username) {
     for (User u : users) {
         if (u.username.equals(username)) return u;
     }
     return null;
 }
}

//================= SERVICES =================
class AuthService {
 static boolean signup(String u, String p) {
     if (UserRepository.find(u) != null) {
         System.out.println("User already exists!");
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

class SearchService {
 static List<Accommodation> search(List<Accommodation> list,
                                   double campusLat,
                                   double campusLon,
                                   double radius,
                                   String type) {

     List<Accommodation> result = new ArrayList<>();

     for (Accommodation a : list) {
         double dist = DistanceService.calculate(campusLat, campusLon, a.lat, a.lon);

         if (dist <= radius && a.type.equalsIgnoreCase(type)) {
             result.add(a);
         }
     }
     return result;
 }
}

//================= MAIN =================
public class MainApp {

 public static void main(String[] args) {

     Scanner sc = new Scanner(System.in);

     System.out.println("===== Accommodation Finder (Manual Data) =====");

     while (true) {

         // -------- MENU --------
         System.out.println("\n1. Signup");
         System.out.println("2. Login");
         System.out.println("3. Exit");
         System.out.print("Enter choice: ");

         int choice = sc.nextInt();
         sc.nextLine();

         if (choice == 3) break;

         // -------- SIGNUP --------
         if (choice == 1) {
             System.out.print("Username: ");
             String u = sc.nextLine();
             System.out.print("Password: ");
             String p = sc.nextLine();

             AuthService.signup(u, p);

             System.out.println("1. Continue to Login\n2. Back");
             int next = sc.nextInt();
             sc.nextLine();
             if (next == 2) continue;
         }

         // -------- LOGIN LOOP --------
         boolean loggedIn = false;
         while (!loggedIn) {
             System.out.print("Username: ");
             String u = sc.nextLine();
             System.out.print("Password: ");
             String p = sc.nextLine();

             loggedIn = AuthService.login(u, p);

             if (!loggedIn) {
                 System.out.println("1. Retry\n2. Exit");
                 int retry = sc.nextInt();
                 sc.nextLine();
                 if (retry == 2) return;
             }
         }

         // -------- ADD DATA --------
         List<Accommodation> list = new ArrayList<>();

         System.out.print("Enter number of accommodations: ");
         int n = sc.nextInt();
         sc.nextLine();

         for (int i = 0; i < n; i++) {
             System.out.println("\nEnter details for Accommodation " + (i + 1));

             System.out.print("Name: ");
             String name = sc.nextLine();

             System.out.print("Latitude: ");
             double lat = sc.nextDouble();

             System.out.print("Longitude: ");
             double lon = sc.nextDouble();
             sc.nextLine();

             System.out.print("Type (PG/Flat): ");
             String type = sc.nextLine();

             System.out.print("Rent: ");
             double rent = sc.nextDouble();

             System.out.print("Rating: ");
             double rating = sc.nextDouble();
             sc.nextLine();

             list.add(new Accommodation(name, lat, lon, type, rent, rating));
         }

         // -------- SEARCH INPUT --------
         System.out.print("\nEnter Radius (km): ");
         double radius = sc.nextDouble();
         sc.nextLine();

         System.out.print("Enter Type (PG/Flat): ");
         String type = sc.nextLine();

         double campusLat = 30.3165;
         double campusLon = 78.0322;

         // -------- SEARCH --------
         List<Accommodation> result =
                 SearchService.search(list, campusLat, campusLon, radius, type);

         // -------- OUTPUT --------
         System.out.println("\nResults:");

         if (result.isEmpty()) {
             System.out.println("No accommodations found.");
         } else {
             for (Accommodation a : result) {
                 double d = DistanceService.calculate(campusLat, campusLon, a.lat, a.lon);
                 a.display(d);
             }
         }

         // -------- POST MENU --------
         System.out.println("\n1. Search Again");
         System.out.println("2. Logout");
         System.out.println("3. Exit");

         int post = sc.nextInt();
         sc.nextLine();

         if (post == 2) continue;
         else if (post == 3) break;
     }

     sc.close();
 }
}
