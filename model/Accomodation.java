package src1.model;

public class Accomodation {
    public String name;
    public double lat,lon;
    public String type;
    public double rent;
    public double rating;

    public Accomodation(String name, double lat, double lon, String type, double rent, double rating){
        this.name=name;
        this.lat=lat;
        this.lon = lon;
        this.type = type;
        this.rent = rent;
        this.rating = rating;
    }

    public void display(double distance) {
        System.out.println(name + " | " + type + " | ₹" + rent +
                " | Rating: " + rating +
                " | Distance: " + String.format("%.2f", distance) + " km");
    }
}