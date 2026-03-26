package src1.service;
import src1.model.User;
import src1.repository.UserRepository;
public class AuthService {
    public static boolean signup(String username, String password) {
        if (UserRepository.find(username) != null) {
            System.out.println("User already exists!");
            return false;
        }

        UserRepository.save(new User(username, password));
        System.out.println("Signup successful!");
        return true;
    }

    public static boolean login(String username, String password) {
        User u = UserRepository.find(username);

        if (u != null && u.password.equals(password)) {
            System.out.println("Login successful!");
            return true;
        }

        System.out.println("Invalid credentials!");
        return false;
    }
}
