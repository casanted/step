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
    dateContainer.innerHTML = date;
} 

window.addEventListener('DOMContentLoaded', (event) => {
    showDate();
});

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

// Remove all dropdowns when a user clicks outside the map image

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

function slideRight() {
  showSlides(slideIndex += 1);
}

function slideLeft() {
  showSlides(slideIndex -= 1);
}

function currentSlide(n) {
  showSlides(slideIndex = n);
}

function showSlides(n) {
  const slides = document.getElementsByClassName("mySlides");
  const dots = document.getElementsByClassName("dot");

  slideIndex = n;
  if (n >= slides.length) {slideIndex = 0}    
  if (n < 0) {slideIndex = slides.length - 1}
  for (let i = 0; i < slides.length; i++) {
      slides[i].style.display = "none";  
  }
  for (let i = 0; i < dots.length; i++) {
      dots[i].className = dots[i].className.replace(" active", "");
  }
  slides[slideIndex].style.display = "block";  
  dots[slideIndex].className += " active";
}

window.addEventListener('DOMContentLoaded', (event) => {
    let slideIndex = 0;
    showSlides(slideIndex);
});

function getComments() {
  const limit = document.getElementById('input').value;
  fetch('/data?limit-input=' + limit).then(response => response.json()).then((comments) => {
    const commentElement = document.getElementById('comments-container');
    commentElement.innerHTML = "";
    comments.forEach((comment) => {
      commentElement.appendChild(createCommentElement(comment));
    })
  });
}

function createCommentElement(comment) {
  const commElement = document.createElement('p');
  commElement.className = 'comment';

  const mainElement = document.createElement('span');
  mainElement.innerText = comment.name + ": " + comment.comment;

  commElement.appendChild(mainElement);
  return commElement;
}

function deleteComments() {
  fetch('/delete-data', {method: 'POST'}).then(() => getComments());
}

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);
google.charts.setOnLoadCallback(drawGenreChart);
/** Creates a chart and adds it to the page. */

function drawChart() {
  const data = new google.visualization.DataTable();
  data.addColumn('string', 'Artist');
  data.addColumn('number', 'Hours Played');
  data.addColumn('number', 'Number of songs in my top 100 most played songs');
  data.addColumn('string', 'Genre');
        data.addRows([
          ['Beyonce', 28, 9, 'Pop'],
          ['Burna Boy', 11, 11, 'Afrobeats'],
          ['Joe Mettle', 7, 1, 'Gospel'],
          ['Joyful Way Incorporated', 6, 2, 'Gospel'],
          ['Joeboy', 5, 4, 'Worldwide'],
          ['Fireboy DML', 4, 3, 'Worldwide'],
          ['Chloe x Halle', 4, 0, 'R&B/Soul'],
          ['Sauti Sol', 11, 3, 'Afro-Pop']
        ]);

  const options = {
    'title': 'My Most Played Artists',
    'hAxis': {'title': 'Hours Played'},
    'vAxis': {'title': 'Number of songs on my top 100 most played songs'},
    'width': 900,
    'height': 500,
    'bubble': {'textStyle': {'fontSize': 11}}, 
    'animation': {'startup': 'true', 'duration': 6000}
  };

  const chart = new google.visualization.BubbleChart(
      document.getElementById('chart-container'));
  chart.draw(data, options);
}
 
/** Fetches genre data and uses it to create a chart. */
function drawGenreChart() {
  fetch('/genre-data').then(response => response.json())
  .then((genreVotes) => {
    const data = new google.visualization.DataTable();
    data.addColumn('string', 'genre');
    data.addColumn('number', 'Votes');
    Object.keys(genreVotes).forEach((genre) => {
      data.addRow([genre, genreVotes[genre]]);
    });
 
    const options = {
      'title': 'Favorite Genres',
      'width':600,
      'height':500
    };
 
    const chart = new google.visualization.BarChart(
        document.getElementById('genre-container'));
    chart.draw(data, options);
  });
}
 
window.addEventListener('DOMContentLoaded', (event) => {
    getLoginStatus();
});
 
function getLoginStatus() {
  fetch('/authorize').then(response => response.json())
  .then((loginMap) => {
      if (loginMap.loginStatus == "loggedIn") {
          console.log("Im in");
          document.getElementById('login').style.display = "block";
          document.getElementById('login-container').innerHTML = "<a href=\"" + loginMap.URL + "\">Logout here!</a>";
      } else {
          console.log("Im out");
          document.getElementById('login-container').innerHTML = "<a href=\"" + loginMap.URL + "\">Login here to put your vote in!</a>";
      }
  });
} 
