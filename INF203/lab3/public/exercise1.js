function loadDoc() {
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            document.getElementById("tarea").textContent = this.responseText;
        }
    };
    xhr.open("GET", "text.txt", true);
    xhr.send();
}

function loadDoc2() {
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            var lines = this.responseText.split("\n");
            var colors = ["red", "blue", "green", "yellow", "purple"];
            var tarea2 = document.getElementById("tarea2");
            tarea2.innerHTML = "";
            for (var i = 0; i < lines.length; i++) {
                var p = document.createElement("p");
                p.textContent = lines[i];
                p.style.color = colors[i % colors.length];
                tarea2.appendChild(p);
            }
        }
    };
    xhr.open("GET", "text.txt", true);
    xhr.send();
}