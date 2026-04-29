package newdaa;
import java.util.*;

class Facility {
String name,type;
double lat,lon;
Facility(String name,String type,double lat,double lon){
this.name=name;
this.type=type;
this.lat=lat;
this.lon=lon;
}
}
class Accommodation {
String name,type;
double lat,lon,rent,rating;
List<Facility> nearby=new ArrayList<Facility>();
Accommodation(String name,double lat,double lon,String type,double rent,double rating){
this.name=name;
this.lat=lat;
this.lon=lon;
this.type=type;
this.rent=rent;
this.rating=rating;
}
void showDetails(double userDist,Map<String,Integer> weights){
double score=calculateScore(weights);
System.out.println("\n"+name+" ("+type+")");
System.out.println("Rent: "+rent+" | Rating: "+rating);
System.out.println("Distance: "+String.format("%.2f",userDist)+" km");
System.out.println("Score: "+String.format("%.2f",score));
if(nearby.size()==0){
System.out.println("No nearby facilities");
}else{
System.out.println("Nearby:");
for(Facility f:nearby){
double d=distance(lat,lon,f.lat,f.lon);
System.out.println("- "+f.name+" ("+f.type+") : "+String.format("%.2f",d)+" km");
}
}
}
double calculateScore(Map<String,Integer> weights){
double score=0;
for(Facility f:nearby){
double d=distance(lat,lon,f.lat,f.lon);
int w=weights.getOrDefault(f.type,0);
score+=w*(1/(d+0.1));
}
return score;
}
static double distance(double lat1,double lon1,double lat2,double lon2){
double R=6371;
double dLat=Math.toRadians(lat2-lat1);
double dLon=Math.toRadians(lon2-lon1);
double a=Math.sin(dLat/2)*Math.sin(dLat/2)
+Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
*Math.sin(dLon/2)*Math.sin(dLon/2);
double c=2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
return R*c;
}
}
class User {
String username,password;
User(String u,String p){
username=u;
password=p;
}
}
class UserDB {
static List users=new ArrayList<User>();
static boolean addUser(String u,String p){
for(User user:users){
if(user.username.equals(u)){
System.out.println("User already exists");
return false;
}
}
if(p.length()<6){
System.out.println("Password too short");
return false;
}
users.add(new User(u,p));
System.out.println("Signup done");
return true;
}
static boolean login(String u,String p){
for(User user:users){
if(user.username.equals(u)&&user.password.equals(p)){
System.out.println("Login success");
return true;
}
}
System.out.println("Wrong credentials");
return false;
}
}
public class MainApp {
public static void main(String[] args){
Scanner sc=new Scanner(System.in);
List stays=new ArrayList<Accommodation>();
List facilities=new ArrayList<Facility>();
System.out.println("Smart Accommodation Finder");
while(true){
System.out.println("\n1 Signup\n2 Login\n3 Exit");
int ch=sc.nextInt();sc.nextLine();
if(ch==3)break;
if(ch==1){
System.out.print("Username: ");
String u=sc.nextLine();
System.out.print("Password: ");
String p=sc.nextLine();
UserDB.addUser(u,p);
}
boolean logged=false;
while(!logged){
System.out.println("\nLogin:");
System.out.print("Username: ");
String u=sc.nextLine();
System.out.print("Password: ");
String p=sc.nextLine();
logged=UserDB.login(u,p);
if(!logged){
System.out.println("Try again? (1 yes / 0 no)");
if(sc.nextInt()==0)break;
sc.nextLine();
}
}
if(!logged)continue;
System.out.print("Number of stays: ");
int n=sc.nextInt();sc.nextLine();
for(int i=0;i< n;i++){
System.out.print("Type: ");
String type=sc.nextLine();
System.out.print("Rent: ");
double rent=sc.nextDouble();sc.nextLine();
System.out.print("Rating: ");
double rating=sc.nextDouble();sc.nextLine();
System.out.print("Distance from user: ");
double radius=sc.nextDouble();sc.nextLine();
System.out.print("Enter user's current latitude: ");
double userLat=sc.nextDouble();sc.nextLine();
System.out.print("Enter user's current longitude: ");
double userLon=sc.nextDouble();sc.nextLine();
System.out.print("Number of facilities: ");
int nf=sc.nextInt();sc.nextLine();
for(int j=0;j< nf;j++){
System.out.print("Facility name: ");
String name=sc.nextLine();
System.out.print("Facility type: ");
String ftype=sc.nextLine();
System.out.print("Facility latitude: ");
double lat=sc.nextDouble();sc.nextLine();
System.out.print("Facility longitude: ");
double lon=sc.nextDouble();sc.nextLine();
facilities.add(new Facility(name,ftype,lat,lon));
}
facilities.forEach(f->{
if(distance(userLat,userLon,f.lat,f.lon)<=2.0)stays.forEach(a->a.nearby.add(f));
});
Map<String,Integer> weights=new HashMap<>();
System.out.print("Gym weight: ");
int wg=sc.nextInt();sc.nextLine();
System.out.print("Hospital weight: ");
int wh=sc.nextInt();sc.nextLine();
System.out.print("Grocery weight: ");
int wgr=sc.nextInt();sc.nextLine();
weights.put("Gym",wg);
weights.put("Hospital",wh);
weights.put("Grocery",wgr);
List<Accommodation> result=new ArrayList<Accommodation>();
for(Accommodation a:stays){
double d=distance(userLat,userLon,a.lat,a.lon);
if(d<=radius&&a.type.equalsIgnoreCase(type))
result.add(a);
}
result.sort((a,b)->Double.compare(b.calculateScore(weights),a.calculateScore(weights)));
if(result.size()==0){
System.out.println("No results found");
}else{
for(Accommodation a:result){
double d=distance(userLat,userLon,a.lat,a.lon);
a.showDetails(d,weights);
}
}
}
sc.close();
}
}
