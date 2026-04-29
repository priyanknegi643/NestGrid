package com.nestgrid.service;
import com.nestgrid.model.User;
import com.nestgrid.repository.UserRepository;
import com.nestgrid.util.JwtUtil;
import com.nestgrid.util.Json;
import com.nestgrid.util.PasswordUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class AuthService{
    private final UserRepository userRepo;
    public AuthService(UserRepository userRepo){
        this.userRepo=userRepo;
    }
    // signup
    public String signup(Map<String,Object> req,int[] status){
        String name=str(req,"name");
        String email=str(req,"email");
        String password=str(req,"password");
        String phone=str(req,"phone");
        String roleStr=str(req,"role");
        if(name.isEmpty()||email.isEmpty()||password.isEmpty()||roleStr.isEmpty()){
            status[0]=400;
            return Json.stringify(Map.of("error","Missing required fields"));
        }
        if(password.length()<6){
            status[0]=400;
            return Json.stringify(Map.of("error","Password must be at least 6 characters"));
        }
        User.Role role;
        try{
            role=User.Role.valueOf(roleStr.toUpperCase());
        }catch(IllegalArgumentException e){
            status[0]=400;
            return Json.stringify(Map.of("error","role must be USER or OWNER"));
        }
        if(userRepo.existsByEmail(email)){
            status[0]=409;
            return Json.stringify(Map.of("error","Email already registered"));
        }
        String salt=PasswordUtil.generateSalt();
        String hash=PasswordUtil.hash(password,salt);
        User user=new User(name,email,phone,hash,salt,role);
        userRepo.save(user);
        String token=JwtUtil.generateToken(user.getEmail(),user.getRole().name());
        status[0]=201;
        return authResponse(token,user);
    }
    // login
    public String login(Map<String,Object> req,int[] status){
        String email=str(req,"email");
        String password=str(req,"password");
        if(email.isEmpty()||password.isEmpty()){
            status[0]=400;
            return Json.stringify(Map.of("error","Missing email or password"));
        }
        Optional<User> found=userRepo.findByEmail(email);
        if(found.isEmpty()){
            status[0]=401;
            return Json.stringify(Map.of("error","Invalid credentials"));
        }
        User user=found.get();
        if(!PasswordUtil.matches(password,user.getPasswordHash(),user.getSalt())){
            status[0]=401;
            return Json.stringify(Map.of("error","Invalid credentials"));
        }
        String token=JwtUtil.generateToken(user.getEmail(),user.getRole().name());
        status[0]=200;
        return authResponse(token,user);
    }
    // response
    private String authResponse(String token,User user){
        Map<String,Object> m=new LinkedHashMap<>();
        m.put("token",token);
        m.put("name",user.getName());
        m.put("email",user.getEmail());
        m.put("role",user.getRole().name());
        return Json.stringify(m);
    }
    // safe string
    private String str(Map<String,Object> m,String key){
        Object v=m.get(key);
        return v==null?"":v.toString().trim();
    }
}
