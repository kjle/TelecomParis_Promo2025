function showTxt() {
    document.getElementById("PIESHOW").style.display = "none";
    document.getElementById("MAINSHOW").style.display = "block";
    document.getElementById("inputFields").style.display = "none";
    document.getElementById("inputFields2").style.display = "none";

    let xhr = new XMLHttpRequest();
    xhr.open("GET", "../../Items", true);
    xhr.onload = function() {
        if (xhr.readyState == 4 && xhr.status == 200) {
            var mainShow = document.getElementById('MAINSHOW');
            mainShow.textContent = xhr.responseText;
        }
    }
    xhr.send();
}

function addElement() {
    document.getElementById("inputFields").style.display = "block";
    document.getElementById("inputFields2").style.display = "none";
    document.getElementById("MAINSHOW").style.display = "none";
    document.getElementById("PIESHOW").style.display = "none";
}

function removeElement() {
    document.getElementById("inputFields2").style.display = "block";
    document.getElementById("inputFields").style.display = "none";
    document.getElementById("MAINSHOW").style.display = "none";
    document.getElementById("PIESHOW").style.display = "none";
}

function clear1() {
    document.getElementById("inputFields").style.display = "none";
    document.getElementById("inputFields2").style.display = "none";
    document.getElementById("MAINSHOW").style.display = "none";
    document.getElementById("PIESHOW").style.display = "none";
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "../../clear", true);
    xhr.send();
}

function restore() {
    document.getElementById("inputFields").style.display = "none";
    document.getElementById("inputFields2").style.display = "none";
    document.getElementById("MAINSHOW").style.display = "none";
    document.getElementById("PIESHOW").style.display = "none";
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "../../restore", true);
    xhr.send();
}

function add() {
    let title = document.getElementById('titleTF').value;
    let value = document.getElementById('valueTF').value;
    let color = document.getElementById('colorTF').value;
    if (title != "" && value != "" && color != "") {
        let xhr = new XMLHttpRequest();
        xhr.open("GET", `../../add?title=${title}&value=${value}&color=${color}`, true);
        xhr.send();
    }
}

function remove() {
    let index = document.getElementById('indexTF').value
    console.log("index=" + index);
    if (!isNaN(index) && index != "") {
        let xhr = new XMLHttpRequest();
        xhr.open("GET", `../../remove?index=${index}`, true);
        xhr.send();
    }
}

function showPiechart() {
    document.getElementById("inputFields").style.display = "none";
    document.getElementById("inputFields2").style.display = "none";
    document.getElementById("MAINSHOW").style.display = "none";
    document.getElementById("PIESHOW").style.display = "block";
    let xhr = new XMLHttpRequest();
    xhr.open("GET", "../../PChart", true);
    xhr.onload = function() {
        if (xhr.readyState == 4 && xhr.status == 200) {
            var pieShow = document.getElementById('PIESHOW');
            pieShow.innerHTML = xhr.responseText;
        }
    }
    xhr.send();
}

function showLocalPiechart() {
    document.getElementById("inputFields").style.display = "none";
    document.getElementById("inputFields2").style.display = "none";
    document.getElementById("MAINSHOW").style.display = "none";
    document.getElementById("PIESHOW").style.display = "block";
    let xhr = new XMLHttpRequest();
    xhr.open("GET", "../../Items", true);
    xhr.onload = function() {
        if (xhr.readyState == 4 && xhr.status == 200) {
            var pieShow = document.getElementById('PIESHOW');
            let json = JSON.parse(xhr.responseText);
            let sum = json.reduce((total, item) => total + item.value, 0);
            let currentAngle = 0;
            let svgData = json.map((item, i) => {
                let percentage = (item.value / sum) * 100;
                let largeArcFlag = percentage > 50 ? 1 : 0;
                let x = Math.cos(2 * Math.PI * currentAngle / 100);
                let y = Math.sin(2 * Math.PI * currentAngle / 100);
                let xEnd = Math.cos(2 * Math.PI * (currentAngle + percentage) / 100);
                let yEnd = Math.sin(2 * Math.PI * (currentAngle + percentage) / 100);
                currentAngle += percentage;
                return `
                    <path fill="${item.color}" d="M ${x} ${y} A 1 1 0 ${largeArcFlag} 1 ${xEnd} ${yEnd} L 0 0 Z">
                        <text x="${(x + xEnd) / 2}" y="${(y + yEnd) / 2}" fill="#000">${item.title}</text>
                    </path>
                `;
            }).join("");
            let svg = `
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="-1 -1 2 2">
                    ${svgData}
                </svg>
            `;
            pieShow.innerHTML = svg;
        }
    }
    xhr.send();
}