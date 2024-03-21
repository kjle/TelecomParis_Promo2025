var slides = [];
var main;
var curSlideNum = -1; // counter
var pauseFlag = false;
var timeoutId;

function loadSlides() {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "slides.json", true);
    xhr.onload = function() {
        slides = JSON.parse(this.responseText);
    }
    xhr.send();
}

function playSlides() {
    curSlideNum ++;
    main = document.getElementById("MAIN");
    // clear slides in the main
    while(main.firstChild) {
        main.removeChild(main.firstChild);
    }
    var iframe = document.createElement("iframe");
    iframe.style.width = "100%";
    iframe.style.height = "800px";
    iframe.src = slides.slides[curSlideNum].url;
    main.appendChild(iframe);
    console.debug("curSlideNum: " + curSlideNum);
    if (curSlideNum < slides.slides.length && !pauseFlag) {  
        timeoutId = setTimeout(playSlides, 2000);
    }
}

function pauseSlides() {
    if (pauseFlag) {
        pauseFlag = false;
        playSlides();
    } else {
        clearTimeout(timeoutId);
        pauseFlag = true;
    }
    console.debug("pauseFlag: " + pauseFlag);
}

function nextSlide() {
    pauseFlag = true;
    if (curSlideNum < slides.slides.length) {
        curSlideNum ++;
        main = document.getElementById("MAIN");
        // clear slides in the main
        while(main.firstChild) {
            main.removeChild(main.firstChild);
        }
        var iframe = document.createElement("iframe");
        iframe.style.width = "100%";
        iframe.style.height = "800px";
        iframe.src = slides.slides[curSlideNum].url;
        main.appendChild(iframe);
        console.debug("curSlideNum: " + curSlideNum);
    } 
}

function previousSlide() {
    pauseFlag = true;
    if (curSlideNum > 0) {
        curSlideNum --;
        main = document.getElementById("MAIN");
        // clear slides in the main
        while(main.firstChild) {
            main.removeChild(main.firstChild);
        }
        var iframe = document.createElement("iframe");
        iframe.style.width = "100%";
        iframe.style.height = "800px";
        iframe.src = slides.slides[curSlideNum].url;
        main.appendChild(iframe);
        console.debug("curSlideNum: " + curSlideNum);
    }
}

loadSlides();