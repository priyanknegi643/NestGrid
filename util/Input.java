package src1.util;
import java.util.*;
public class Input {
    static Scanner sc = new Scanner(System.in);

    public static String getUsername() {
        System.out.print("Username: ");
        return sc.nextLine();
    }

    public static String getPassword() {
        System.out.print("Password: ");
        return sc.nextLine();
    }

    public static double getRadius() {
        System.out.print("Enter Radius (km): ");
        return sc.nextDouble();
    }

    public static String getType() {
        sc.nextLine();
        System.out.print("Enter Type (PG/Flat): ");
        return sc.nextLine();
    }
}
