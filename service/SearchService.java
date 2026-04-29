package com.nestgrid.service;
import com.nestgrid.model.Accommodation;
import com.nestgrid.model.Accommodation.Amenity;
import com.nestgrid.model.User;
import com.nestgrid.repository.AccommodationRepository;
import com.nestgrid.repository.UserRepository;
import com.nestgrid.util.Json;
import java.util.*;
import java.util.stream.Collectors;

public class AccommodationService{
    private final AccommodationRepository repo;
    private final UserRepository userRepo;
    private final DistanceService distanceService;
    public AccommodationService(AccommodationRepository repo,UserRepository userRepo,DistanceService distanceService){
        this.repo=repo;
        this.userRepo=userRepo;
        this.distanceService=distanceService;
    }
    // create
    public String create(Map<String,Object> req,User owner,int[] status){
        String name=str(req,"name");
        String type=str(req,"type");
        if(name.isEmpty()||type.isEmpty()){
            status[0]=400;
            return Json.stringify(Map.of("error","name and type are required"));
        }
        Accommodation acc=Accommodation.create(name,type,toDouble(req.get("lat")),toDouble(req.get("lng")),str(req,"address"),toDouble(req.get("price")),toDouble(req.get("rating")),str(req,"description"),parseAmenities(req.get("amenities")),owner.getId());
        status[0]=201;
        return toJson(repo.save(acc),null,null);
    }
    // update
    public String update(long id,Map<String,Object> req,User owner,int[] status){
        Optional<Accommodation> found=repo.findById(id);
        if(found.isEmpty()||found.get().getOwnerId()!=owner.getId()){
            status[0]=403;
            return Json.stringify(Map.of("error","Not found or forbidden"));
        }
        Accommodation a=found.get();
        a.setName(str(req,"name"));
        a.setType(str(req,"type"));
        a.setLat(toDouble(req.get("lat")));
        a.setLng(toDouble(req.get("lng")));
        a.setAddress(str(req,"address"));
        a.setPrice(toDouble(req.get("price")));
        a.setRating(toDouble(req.get("rating")));
        a.setDescription(str(req,"description"));
        a.setAmenities(parseAmenities(req.get("amenities")));
        status[0]=200;
        return toJson(repo.save(a),null,null);
    }
    // delete
    public String delete(long id,User owner,int[] status){
        Optional<Accommodation> found=repo.findById(id);
        if(found.isEmpty()||found.get().getOwnerId()!=owner.getId()){
            status[0]=403;
            return Json.stringify(Map.of("error","Not found or forbidden"));
        }
        repo.delete(id);
        status[0]=204;
        return "";
    }
    // my listings
    public String getMyListings(User owner,int[] status){
        List<String> list=repo.findByOwnerId(owner.getId()).stream().map(a->toJson(a,null,null)).collect(Collectors.toList());
        status[0]=200;
        return "["+String.join(",",list)+"]";
    }
    // all
    public String getAll(int[] status){
        List<String> list=repo.findAll().stream().map(a->toJson(a,null,null)).collect(Collectors.toList());
        status[0]=200;
        return "["+String.join(",",list)+"]";
    }
    // search
    public String search(Map<String,Object> req,int[] status){
        double lat=toDouble(req.get("lat"));
        double lng=toDouble(req.get("lng"));
        double rawRadius=toDouble(req.get("radius"));
        final double radius=rawRadius<=0?10:rawRadius;
        String type=str(req,"type");
        Map<String,Integer> weights=Map.of(
            "Gym",toInt(req.get("weightGym")),
            "Hospital",toInt(req.get("weightHospital")),
            "Grocery",toInt(req.get("weightGrocery")),
            "Metro",toInt(req.get("weightMetro")),
            "Park",toInt(req.get("weightPark")),
            "School",toInt(req.get("weightSchool"))
        );
        List<Accommodation> candidates=type.isEmpty()?repo.findAll():repo.findByType(type);
        List<String> results=candidates.stream()
            .map(a->{
                double dist=distanceService.calculate(lat,lng,a.getLat(),a.getLng());
                if(dist>radius) return null;
                double sc=computeScore(a,weights);
                return new Object[]{a,dist,sc};
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingDouble(o->-((double)((Object[])o)[2])))
            .map(o->toJson((Accommodation)o[0],(Double)o[1],(Double)o[2]))
            .collect(Collectors.toList());
        status[0]=200;
        return "["+String.join(",",results)+"]";
    }
    // score
    private double computeScore(Accommodation a,Map<String,Integer> weights){
        if(a.getAmenities()==null) return 0;
        return a.getAmenities().stream().mapToDouble(am->weights.getOrDefault(am.getType(),0)*(1.0/(am.getD()+0.1))).sum();
    }
    // to json
    private String toJson(Accommodation a,Double dist,Double sc){
        Map<String,Object> m=new LinkedHashMap<>();
        m.put("id",a.getId());
        m.put("name",a.getName());
        m.put("type",a.getType());
        m.put("lat",a.getLat());
        m.put("lng",a.getLng());
        m.put("address",a.getAddress()!=null?a.getAddress():"");
        m.put("price",a.getPrice());
        m.put("rating",a.getRating());
        m.put("description",a.getDescription()!=null?a.getDescription():"");
        String ownerEmail=userRepo.findById(a.getOwnerId()).map(User::getEmail).orElse("");
        m.put("ownerEmail",ownerEmail);
        List<Object> amenArr=new ArrayList<>();
        if(a.getAmenities()!=null){
            for(Amenity am:a.getAmenities()){
                Map<String,Object> am2=new LinkedHashMap<>();
                am2.put("type",am.getType());
                am2.put("d",am.getD());
                amenArr.add(am2);
            }
        }
        m.put("amenities",amenArr);
        m.put("distance",dist);
        m.put("score",sc);
        return Json.stringify(m);
    }
    // parse amenities
    @SuppressWarnings("unchecked")
    private List<Amenity> parseAmenities(Object raw){
        List<Amenity> result=new ArrayList<>();
        if(!(raw instanceof List)) return result;
        for(Object item:(List<Object>)raw){
            Map<String,Object> m=(Map<String,Object>)item;
            result.add(new Amenity(str(m,"type"),toDouble(m.get("d"))));
        }
        return result;
    }
    // string
    private String str(Map<String,Object> m,String key){
        Object v=m.get(key);
        return v==null?"":v.toString().trim();
    }
    // to double
    private double toDouble(Object n){
        if(n==null) return 0;
        if(n instanceof Double d) return d;
        if(n instanceof Long l) return l.doubleValue();
        if(n instanceof Integer i) return i.doubleValue();
        try{return Double.parseDouble(n.toString());}catch(Exception e){return 0;}
    }
    // to int
    private int toInt(Object n){
        if(n==null) return 0;
        if(n instanceof Long l) return l.intValue();
        if(n instanceof Integer i) return i;
        if(n instanceof Double d) return d.intValue();
        try{return Integer.parseInt(n.toString());}catch(Exception e){return 0;}
    }
}
