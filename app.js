// ===== AUTH GUARD =====
let currentUser = null;
try { currentUser = JSON.parse(sessionStorage.getItem('nestgrid_user') || 'null'); } catch(e) {}
if (!currentUser) { window.location.href = 'auth.html'; }
else {
  const nav = document.getElementById('navUserName');
  if (nav) nav.textContent = '👤 ' + (currentUser.name || currentUser.email);
}
function logout() {
  sessionStorage.removeItem('nestgrid_user');
  window.location.href = 'auth.html';
}

// ===== BASE DATA =====
const baseFacilities = [
 {name:"Gym", type:"Gym", lat:28.62, lng:77.21},
 {name:"Hospital", type:"Hospital", lat:28.60, lng:77.20},
 {name:"Grocery", type:"Grocery", lat:28.61, lng:77.22}
];

const basePgs = [
 {id:1,name:"Sunrise PG",type:"PG",price:8000,rating:4.2,lat:28.61,lng:77.20},
 {id:2,name:"Elite Flat",type:"Flat",price:15000,rating:4.5,lat:28.65,lng:77.23},
 {id:3,name:"Comfort PG",type:"PG",price:10000,rating:4.0,lat:28.58,lng:77.30}
];

// ===== MERGE WITH OWNER LISTINGS =====
function getAllPgs() {
  let ownerPgs = [];
  try { ownerPgs = JSON.parse(localStorage.getItem('nestgrid_global_pgs') || '[]'); } catch(e) {}
  // Merge: owner listings override base by id
  const merged = [...basePgs];
  ownerPgs.forEach(op => {
    if (!merged.find(p => p.id === op.id)) merged.push(op);
  });
  return merged;
}

function getAllFacilities(pgs) {
  const extra = [];
  pgs.forEach(pg => {
    (pg.amenities || []).forEach(a => {
      extra.push({ name: a.type, type: a.type, lat: pg.lat + (Math.random()-0.5)*0.01, lng: pg.lng + (Math.random()-0.5)*0.01 });
    });
  });
  return [...baseFacilities, ...extra];
}

// ===== MAP =====
const map = L.map('map').setView([28.61,77.20],12);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);

let userLat=28.61,userLng=77.20;
let markers=[],compareList=[];

// ===== UTILS =====
function distance(a,b,c,d){
 const R=6371;
 const dLat=(c-a)*Math.PI/180;
 const dLon=(d-b)*Math.PI/180;
 const x=Math.sin(dLat/2)**2+
  Math.cos(a*Math.PI/180)*Math.cos(c*Math.PI/180)*
  Math.sin(dLon/2)**2;
 return R*(2*Math.atan2(Math.sqrt(x),Math.sqrt(1-x)));
}

function getIcon(t){
  return {Gym:"🏋️",Hospital:"🏥",Grocery:"🛒",Metro:"🚇",Park:"🌳",School:"🏫"}[t] || "📍";
}
function getColor(t){
  return {Gym:"#22C55E",Hospital:"#EF4444",Grocery:"#F59E0B",Metro:"#6366F1",Park:"#10B981",School:"#F59E0B"}[t] || "#6B7280";
}

// ===== LOGIC =====
function findFacilities(pg, allFacilities){
 pg.nearby=[];
 allFacilities.forEach(f=>{
  // Use declared distance if available from amenities
  const declared = (pg.amenities||[]).find(a=>a.type===f.type);
  const d = declared ? declared.d : distance(pg.lat,pg.lng,f.lat,f.lng);
  if(d<=2) pg.nearby.push({...f,d});
 });
}

function score(pg,w){
 let s=0;
 pg.nearby.forEach(f=>{
  s+=(w[f.type]||0)*(1/(f.d+0.1));
 });
 return s;
}

// ===== LOCATION =====
async function geocode(q){
 const res=await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${q}`);
 const data=await res.json();
 if(data.length){
  userLat=+data[0].lat;
  userLng=+data[0].lon;
  map.setView([userLat,userLng],13);
 }
}

function useMyLocation(){
 navigator.geolocation.getCurrentPosition(pos=>{
  userLat=pos.coords.latitude;
  userLng=pos.coords.longitude;
  map.setView([userLat,userLng],13);
 });
}

// ===== RENDER =====
function render(data,w){
 const list=document.getElementById("listings");
 list.innerHTML="";
 markers.forEach(m=>map.removeLayer(m));
 markers=[];

 if (!data.length) {
   list.innerHTML=`<div style="text-align:center;padding:40px 20px;color:#6B7280;font-size:13px;">
     No results found in this area.<br>Try adjusting the radius or type.
   </div>`;
   return;
 }

 data.forEach(pg=>{
  const d=distance(userLat,userLng,pg.lat,pg.lng);
  const sc=score(pg,w);

  const marker=L.marker([pg.lat,pg.lng]).addTo(map);
  markers.push(marker);

  marker.bindPopup(`<b>${pg.name}</b><br>₹${pg.price}/mo<br>Score: ${sc.toFixed(2)}`);

  const card=document.createElement("div");
  card.className="pg-card";

  card.innerHTML=`
    <div class="pg-title">${pg.name}</div>
    <div class="pg-price">₹${pg.price.toLocaleString()}/mo</div>
    <div class="pg-meta">⭐ ${pg.rating} • ${d.toFixed(1)} km away</div>

    <div class="score">
      🔥 Score: ${sc.toFixed(2)}
      <div class="score-bar">
        <div class="score-fill" style="width:${Math.min(sc*10,100)}%"></div>
      </div>
    </div>

    ${pg.nearby.map(f=>`
      <div class="facility">
        ${getIcon(f.type)} ${f.type}: ${f.d.toFixed(2)} km
        <div class="bar">
          <div class="fill" style="width:${Math.max(0,100-f.d*25)}%;background:${getColor(f.type)}"></div>
        </div>
      </div>
    `).join("")}

    <button class="ghost-btn" onclick="toggleCompare(${pg.id},event)">Compare</button>
  `;

  list.appendChild(card);
 });
}

// ===== SEARCH =====
async function runSearch(){
 const input=document.getElementById("locationInput").value;
 if(input) await geocode(input);

 const radius=+document.getElementById("radius").value;
 const selectedType=document.getElementById("type").value;

 const w={
  Gym:+document.getElementById("wg").value,
  Hospital:+document.getElementById("wh").value,
  Grocery:+document.getElementById("wgr").value
 };

 const allPgs = getAllPgs();
 const allFacilities = getAllFacilities(allPgs);

 let res=allPgs.filter(p=>{
  return distance(userLat,userLng,p.lat,p.lng)<=radius &&
    (p.type===selectedType || selectedType==='All');
 });

 res.forEach(pg => findFacilities(pg, allFacilities));
 res.sort((a,b)=>score(b,w)-score(a,w));

 if(window.circle) map.removeLayer(window.circle);
 window.circle=L.circle([userLat,userLng],{
  radius:radius*1000,
  color:"#6366F1",
  fillOpacity: 0.04
 }).addTo(map);

 render(res,w);
}

// ===== COMPARE =====
function toggleCompare(id,e){
 e.stopPropagation();
 if(compareList.includes(id)){
  compareList=compareList.filter(i=>i!==id);
 } else {
  if(compareList.length>=2) return alert("Max 2");
  compareList.push(id);
 }
 if(compareList.length===2) showCompare();
}

function showCompare(){
 const modal=document.getElementById("compareModal");
 const content=document.getElementById("compareContent");

 const w={
   Gym:+document.getElementById("wg").value,
   Hospital:+document.getElementById("wh").value,
   Grocery:+document.getElementById("wgr").value
 };

 const allPgs = getAllPgs();
 const allFacilities = getAllFacilities(allPgs);
 const selected=allPgs.filter(p=>compareList.includes(p.id));
 selected.forEach(pg => findFacilities(pg, allFacilities));

 content.innerHTML=selected.map(pg=>{
  const sc=score(pg,w);
  return `
    <div>
      <h3>${pg.name}</h3>
      <p>₹${pg.price.toLocaleString()}/mo</p>
      <p>⭐ ${pg.rating}</p>
      <p>🔥 Score: ${sc.toFixed(2)}</p>
      ${pg.nearby.map(f=>`
        <div>${getIcon(f.type)} ${f.type}: ${f.d.toFixed(2)} km</div>
      `).join("")}
    </div>
  `;
 }).join("");

 modal.style.display="block";
}

function closeCompare(){
 document.getElementById("compareModal").style.display="none";
 compareList=[];
}

// init
runSearch();
