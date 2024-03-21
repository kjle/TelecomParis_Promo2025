function send() {
    var xhr = new XMLHttpRequest();
    var req = document.getElementById("textedit").value;
    if(req == "") return;
    req = "chat.php?phrase=" + req;
    xhr.onreadystatechange = function() {
        if (this.readyState == 4) {
            if(this.status == 200) {
                console.log("Success");
            } else {
                console.log("Failed");
            }
        }
    };
    xhr.open("GET", req, true);
    xhr.send();
    document.getElementById("textedit").value = "";
}

function load() {
    var xhr = new XMLHttpRequest();
    xhr.onload = function() {
        var div = document.getElementById("tarea");
        var lines = this.responseText.split("\n").reverse();
        // for (var i = 0; i < div.childElementCount; i++) {
        while (div.firstChild) {
            div.removeChild(div.firstChild);
        }
        for (var i = 0; i < lines.length; i++) {
            if (lines[i] == "") continue;
            var p = document.createElement("p");
            p.textContent = lines[i];
            div.appendChild(p);
            if (div.childElementCount >= 10) {
                break;
            }
        }
    }
    
    xhr.open("GET", "chatlog.txt", true);
    xhr.send();
}

setInterval(load, 1000);