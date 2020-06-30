// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


// Show current date and time on every page
function showDate() {
    const date = new Date();
    const dateContainer = document.getElementById("date");
    dateContainer.style.color = "white";
    dateContainer.innerHTML = date;
} 

window.onload = (event) => {
    showDate();
}

function displayCalifornia() {
    document.getElementById("California").classList.toggle("show");
}

function displayEastCoast() {
    document.getElementById("EastCoast").classList.toggle("show");
}

function displayUK() {
    document.getElementById("UK").classList.toggle("show");
}

function displayPoland() {
    document.getElementById("Poland").classList.toggle("show");
}

function displaySA() {
    document.getElementById("SA").classList.toggle("show");
}

function displayThailand() {
    document.getElementById("Thailand").classList.toggle("show");
}

function displayGhana() {
    document.getElementById("Ghana").classList.toggle("show");
}

function displayTunis() {
    document.getElementById("Tunis").classList.toggle("show");
}

function displayRwanda() {
    document.getElementById("Rwanda").classList.toggle("show");
}

function displayRomania() {
    document.getElementById("Romania").classList.toggle("show");
}

// Remove all dropdowns whenever the user clicks outside of the map image 
window.onclick = function(event) {
  if (!event.target.matches('.dropbutton')) {
    const dropdowns = document.getElementsByClassName("dropdown-content");
    for (let i = 0; i < dropdowns.length; i++) {
      const openDropdown = dropdowns[i];
      if (openDropdown.classList.contains('show')) {
        openDropdown.classList.remove('show');
      }
    }
  }
}

function slideRight(n) {
  showSlides(slideIndex += n);
}

function slideLeft(n) {
  showSlides(slideIndex -= n);
}

function currentSlide(n) {
  showSlides(slideIndex = n);
}

function showSlides(n) {
  const slides = document.getElementsByClassName("mySlides");
  const dots = document.getElementsByClassName("dot");
  if (n >= slides.length) {slideIndex = 0}    
  if (n < 1) {slideIndex = slides.length - 1}
  for (let i = 0; i < slides.length; i++) {
      slides[i].style.display = "none";  
  }
  for (let i = 0; i < dots.length; i++) {
      dots[i].className = dots[i].className.replace(" active", "");
  }
  slides[slideIndex].style.display = "block";  
  dots[slideIndex].className += " active";
}

window.onload = (event) => {
    let slideIndex = 0;
    showSlides(slideIndex);
}
