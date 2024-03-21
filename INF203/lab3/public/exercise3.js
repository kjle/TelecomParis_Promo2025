var slides = [];

function loadSlides() {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "slides.json", true);
    xhr.onload = function() {
        slides = JSON.parse(this.responseText);
    }
    xhr.send();
}

function playSlides() {
    var main = document.getElementById("MAIN");
    var i = 0;
    function nextSlide() {
        if(i < slides.slides.length) {
            main.innerHTML = '';
            var iframe = document.createElement("iframe");
            iframe.style.width = "100%";
            iframe.style.height = "800px";
            iframe.src = slides.slides[i].url;
            main.appendChild(iframe);
            console.debug("curSlideNum: " + i);
            setTimeout(nextSlide, 2000);
            i++;
        }
    }
    nextSlide();
}

loadSlides();