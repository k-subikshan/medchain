function toast(msg){const t=document.querySelector(".toast");if(!t){alert(msg);return}t.textContent=msg;t.classList.add("show");setTimeout(()=>t.classList.remove("show"),2500)}
function toggleMenu(){document.querySelector(".sidebar")?.classList.toggle("open")}
function toggleUserMenu(){document.getElementById("userDropdown")?.classList.toggle("show")}
function logout(){location.href="../index.html"}
function copyHash(){navigator.clipboard?.writeText(document.querySelector(".hash")?.textContent||"");toast("Blockchain hash copied")}
function openModal(title,body){const m=document.querySelector(".modal-bg");if(!m)return;m.querySelector(".modal").innerHTML=`<div class="section-head"><h2>${title}</h2><button class="icon-btn" onclick="closeModal()">✕</button></div>${body}`;m.classList.add("show")}
function closeModal(){document.querySelector(".modal-bg")?.classList.remove("show")}
document.addEventListener("DOMContentLoaded",()=>{
 const login=document.getElementById("loginForm");
 if(login) login.addEventListener("submit",e=>{e.preventDefault();const role=document.getElementById("role").value;if(!role){alert("Please select your role.");return}const paths={doctor:"doctor/dashboard.html",pharmacy:"pharmacy/dashboard.html",manufacturer:"manufacturer/dashboard.html",patient:"patient/dashboard.html",admin:"admin/dashboard.html"};location.href=paths[role]});
 document.querySelectorAll("[data-toast]").forEach(b=>b.addEventListener("click",()=>toast(b.dataset.toast)));
 document.querySelectorAll("[data-copy]").forEach(b=>b.addEventListener("click",copyHash));
 document.querySelectorAll("form[data-demo]").forEach(f=>f.addEventListener("submit",e=>{e.preventDefault();toast(f.dataset.demo);f.reset()}));
});
document.addEventListener("click",e=>{const m=document.querySelector(".user-menu"),d=document.getElementById("userDropdown");if(d&&m&&!m.contains(e.target))d.classList.remove("show")});
