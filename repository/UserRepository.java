package src1.repository;
import java.util.*;
import src1.model.User;
public class UserRepository {
      private static List<User> users = new ArrayList<>();

    public static void save(User user) {
        users.add(user);
    }

    public static User find(String username) {
        for (User u : users) {
            if (u.username.equals(username)) {
                return u;
            }
        }
        return null;
    }
}
