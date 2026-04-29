// config
const API='http://localhost:8080/api';

// check login
let currentUser=null,authToken=null;
try{
 currentUser=JSON.parse(sessionStorage.getItem('nestgrid_user')||'null');
 authToken=sessionStorage.getItem('nestgrid_token');
}catch(e){}
if(!currentUser||!authToken)window.location.href='auth.html';

// update navbar
const nav=document.getElementById('navUserName');
if(nav)nav.textContent='👤 '+(currentUser.name||currentUser.email);

function logout(){
 sessionStorage.clear();
 window.location.href='auth.html';
}

// api call helper
async function apiFetch(path,options={}){
 const res=await fetch(API+path,{
  ...options,
  headers:{
   'Content-Type':'application/json',
   'Authorization':`Bearer ${authToken}`,
   ...(options.headers||{})
  }
 });
 if(res.status===401){logout();return null;}
 return res.ok?res.json():null;
}

// map setup
const map=L.map('map').setView([28.61,77.20],12);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);

let userLat=28.61,userLng=77.20;
let markers=[],compareList=[];

// icons and colors
function getIcon(t){return{Gym:'🏋️',Hospital:'🏥',Grocery:'🛒',Metro:'🚇',Park:'🌳',School:'🏫'}[t]||'📍';}
function getColor(t){return{Gym:'#22C55E',Hospital:'#EF4444',Grocery:'#F59E0B',Metro:'#6366F1',Park:'#10B981',School:'#F59E0B'}[t]||'#6B7280';}

// search location
async function geocode(q){
 const res=await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(q)}`);
 const data=await res.json();
 if(data.length){
  userLat=+data[0].lat;
  userLng=+data[0].lon;
  map.setView([userLat,userLng],13);
 }
}

// use device location
function useMyLocation(){
 navigator.geolocation.getCurrentPosition(pos=>{
  userLat=pos.coords.latitude;
  userLng=pos.coords.longitude;
  map.setView([userLat,userLng],13);
 });
}

// show results
function render(data){
 const list=document.getElementById('listings');
 list.innerHTML='';
 markers.forEach(m=>map.removeLayer(m));
 markers=[];

 if(!data.length){
  list.innerHTML=`<div style="text-align:center;padding:40px 20px;color:#6B7280;font-size:13px;">
   No results found<br>Try changing filters
  </div>`;
  return;
 }

 data.forEach(pg=>{
  const dist=pg.distance!=null?pg.distance.toFixed(1):'—';
  const sc=pg.score!=null?pg.score.toFixed(2):'0.00';

  const marker=L.marker([pg.lat,pg.lng]).addTo(map);
  markers.push(marker);
  marker.bindPopup(`<b>${pg.name}</b><br>₹${pg.price}/mo<br>Score: ${sc}`);

  const card=document.createElement('div');
  card.className='pg-card';
  card.innerHTML=`
   <div class="pg-title">${pg.name}</div>
   <div class="pg-price">₹${pg.price.toLocaleString()}/mo</div>
   <div class="pg-meta">⭐ ${pg.rating} • ${dist} km</div>
   <div class="score">🔥 ${sc}
    <div class="score-bar"><div class="score-fill" style="width:${Math.min(+sc*10,100)}%"></div></div>
   </div>
   ${(pg.amenities||[]).map(f=>`
    <div class="facility">
     ${getIcon(f.type)} ${f.type}: ${f.d.toFixed(2)} km
     <div class="bar"><div class="fill" style="width:${Math.max(0,100-f.d*25)}%;background:${getColor(f.type)}"></div></div>
    </div>`).join('')}
   <button class="ghost-btn" onclick="toggleCompare(${pg.id},event)">Compare</button>`;
  list.appendChild(card);
 });
}

// run search
async function runSearch(){
 const input=document.getElementById('locationInput').value;
 if(input)await geocode(input);

 const body={
  lat:userLat,
  lng:userLng,
  radius:+document.getElementById('radius').value,
  type:document.getElementById('type').value,
  weightGym:+document.getElementById('wg').value,
  weightHospital:+document.getElementById('wh').value,
  weightGrocery:+document.getElementById('wgr').value
 };

 if(window.circle)map.removeLayer(window.circle);
 window.circle=L.circle([userLat,userLng],{
  radius:body.radius*1000,
  color:'#6366F1',
  fillOpacity:0.04
 }).addTo(map);

 const results=await apiFetch('/accommodations/search',{
  method:'POST',
  body:JSON.stringify(body)
 });

 render(results||[]);
}

// compare toggle
function toggleCompare(id,e){
 e.stopPropagation();
 if(compareList.includes(id))compareList=compareList.filter(i=>i!==id);
 else{if(compareList.length>=2)return alert('Max 2');compareList.push(id);}
 if(compareList.length===2)showCompare();
}

// show compare
async function showCompare(){
 const modal=document.getElementById('compareModal');
 const content=document.getElementById('compareContent');

 const all=await apiFetch('/accommodations')||[];
 const selected=all.filter(p=>compareList.includes(p.id));

 content.innerHTML=selected.map(pg=>`
  <div>
   <h3>${pg.name}</h3>
   <p>₹${pg.price.toLocaleString()}/mo</p>
   <p>⭐ ${pg.rating}</p>
   ${(pg.amenities||[]).map(f=>`<div>${getIcon(f.type)} ${f.type}: ${f.d.toFixed(2)} km</div>`).join('')}
  </div>
 `).join('');

 modal.style.display='block';
}

// close compare
function closeCompare(){
 document.getElementById('compareModal').style.display='none';
 compareList=[];
}

// start
runSearch();// config
const API='http://localhost:8080/api';

// check login
let currentUser=null,authToken=null;
try{
 currentUser=JSON.parse(sessionStorage.getItem('nestgrid_user')||'null');
 authToken=sessionStorage.getItem('nestgrid_token');
}catch(e){}
if(!currentUser||!authToken)window.location.href='auth.html';

// update navbar
const nav=document.getElementById('navUserName');
if(nav)nav.textContent='👤 '+(currentUser.name||currentUser.email);

function logout(){
 sessionStorage.clear();
 window.location.href='auth.html';
}

// api call helper
async function apiFetch(path,options={}){
 const res=await fetch(API+path,{
  ...options,
  headers:{
   'Content-Type':'application/json',
   'Authorization':`Bearer ${authToken}`,
   ...(options.headers||{})
  }
 });
 if(res.status===401){logout();return null;}
 return res.ok?res.json():null;
}

// map setup
const map=L.map('map').setView([28.61,77.20],12);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);

let userLat=28.61,userLng=77.20;
let markers=[],compareList=[];

// icons and colors
function getIcon(t){return{Gym:'🏋️',Hospital:'🏥',Grocery:'🛒',Metro:'🚇',Park:'🌳',School:'🏫'}[t]||'📍';}
function getColor(t){return{Gym:'#22C55E',Hospital:'#EF4444',Grocery:'#F59E0B',Metro:'#6366F1',Park:'#10B981',School:'#F59E0B'}[t]||'#6B7280';}

// search location
async function geocode(q){
 const res=await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(q)}`);
 const data=await res.json();
 if(data.length){
  userLat=+data[0].lat;
  userLng=+data[0].lon;
  map.setView([userLat,userLng],13);
 }
}

// use device location
function useMyLocation(){
 navigator.geolocation.getCurrentPosition(pos=>{
  userLat=pos.coords.latitude;
  userLng=pos.coords.longitude;
  map.setView([userLat,userLng],13);
 });
}

// show results
function render(data){
 const list=document.getElementById('listings');
 list.innerHTML='';
 markers.forEach(m=>map.removeLayer(m));
 markers=[];

 if(!data.length){
  list.innerHTML=`<div style="text-align:center;padding:40px 20px;color:#6B7280;font-size:13px;">
   No results found<br>Try changing filters
  </div>`;
  return;
 }

 data.forEach(pg=>{
  const dist=pg.distance!=null?pg.distance.toFixed(1):'—';
  const sc=pg.score!=null?pg.score.toFixed(2):'0.00';

  const marker=L.marker([pg.lat,pg.lng]).addTo(map);
  markers.push(marker);
  marker.bindPopup(`<b>${pg.name}</b><br>₹${pg.price}/mo<br>Score: ${sc}`);

  const card=document.createElement('div');
  card.className='pg-card';
  card.innerHTML=`
   <div class="pg-title">${pg.name}</div>
   <div class="pg-price">₹${pg.price.toLocaleString()}/mo</div>
   <div class="pg-meta">⭐ ${pg.rating} • ${dist} km</div>
   <div class="score">🔥 ${sc}
    <div class="score-bar"><div class="score-fill" style="width:${Math.min(+sc*10,100)}%"></div></div>
   </div>
   ${(pg.amenities||[]).map(f=>`
    <div class="facility">
     ${getIcon(f.type)} ${f.type}: ${f.d.toFixed(2)} km
     <div class="bar"><div class="fill" style="width:${Math.max(0,100-f.d*25)}%;background:${getColor(f.type)}"></div></div>
    </div>`).join('')}
   <button class="ghost-btn" onclick="toggleCompare(${pg.id},event)">Compare</button>`;
  list.appendChild(card);
 });
}

// run search
async function runSearch(){
 const input=document.getElementById('locationInput').value;
 if(input)await geocode(input);

 const body={
  lat:userLat,
  lng:userLng,
  radius:+document.getElementById('radius').value,
  type:document.getElementById('type').value,
  weightGym:+document.getElementById('wg').value,
  weightHospital:+document.getElementById('wh').value,
  weightGrocery:+document.getElementById('wgr').value
 };

 if(window.circle)map.removeLayer(window.circle);
 window.circle=L.circle([userLat,userLng],{
  radius:body.radius*1000,
  color:'#6366F1',
  fillOpacity:0.04
 }).addTo(map);

 const results=await apiFetch('/accommodations/search',{
  method:'POST',
  body:JSON.stringify(body)
 });

 render(results||[]);
}

// compare toggle
function toggleCompare(id,e){
 e.stopPropagation();
 if(compareList.includes(id))compareList=compareList.filter(i=>i!==id);
 else{if(compareList.length>=2)return alert('Max 2');compareList.push(id);}
 if(compareList.length===2)showCompare();
}

// show compare
async function showCompare(){
 const modal=document.getElementById('compareModal');
 const content=document.getElementById('compareContent');

 const all=await apiFetch('/accommodations')||[];
 const selected=all.filter(p=>compareList.includes(p.id));

 content.innerHTML=selected.map(pg=>`
  <div>
   <h3>${pg.name}</h3>
   <p>₹${pg.price.toLocaleString()}/mo</p>
   <p>⭐ ${pg.rating}</p>
   ${(pg.amenities||[]).map(f=>`<div>${getIcon(f.type)} ${f.type}: ${f.d.toFixed(2)} km</div>`).join('')}
  </div>
 `).join('');

 modal.style.display='block';
}

// close compare
function closeCompare(){
 document.getElementById('compareModal').style.display='none';
 compareList=[];
}

// start
runSearch();
