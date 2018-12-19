var body = document.getElementsByTagName("body")[0];

var sky = document.createElement("canvas");
sky.id = "sky";
sky.style = "z-index: 1000; position: absolute; top: 0;";
body.appendChild(sky);

//create canvas
var canvas = document.getElementById("sky");
var ctx = canvas.getContext("2d");


//generate snowflakes and atts
var mf = 100; //max flakes
var flakes = [];


//loop through empty flakes and apply atts
for(var i = 0; i < mf; i++){
    flakes.push({
        x: Math.random()*window.innerWidth, //set width of flake to random nr between 0 and 1 * width of screen
        y: Math.random()*window.innerHeight, //set height of flake to random nr between 0 and 1 * height of screen
        r: Math.random()*5+2, //set radius between 2 and 5
        d: Math.random() + 1
    })

}


//draw flakes
function drawFlakes(){
//set canvas fullscreen
    canvas.height = window.innerHeight;
    canvas.width = window.innerWidth;


    ctx.clearRect(0, 0, window.innerWidth, window.innerHeight);
    ctx.fillStyle = "#96c2ff";
    ctx.beginPath();
    for(var i = 0; i < mf; i++){
        var f = flakes[i];
        ctx.moveTo(f.x, f.y);
        ctx.arc(f.x, f.y, f.r, 0, Math.PI*2, true);
    }
    ctx.fill();
    moveFlakes();
}
var angle = 0;
//move flakes
function moveFlakes(){
    angle += 0.01;
    for(var i = 0; i < mf; i++){
        var f = flakes[i];
        f.y += Math.pow(f.d, 2) + 1;
        f.x += Math.cos(angle)*2;

        if(f.y > window.innerHeight){
            flakes[i] = {x: Math.random()*window.innerWidth, y: 0, r: f.r, d: f.d};
        }
    }
}

setInterval(drawFlakes, 25);
