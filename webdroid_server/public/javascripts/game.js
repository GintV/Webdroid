$( document ).ready(function() {

    /* Variables ================================================ */
    var server = webSocketClient("ws://218.gaikaz.tk", "");
    var theGame = $("#theGame");
    var gameContext = theGame.get(0).getContext("2d");
    var gameWidth = window.innerWidth,
        gameHeight = window.innerHeight;
    var bgColor = '#ccc';
    var players = [];
    var colors = ['#FFA500','#008000', '#800080', '#0000FF', '#FFFF00', '#808000', '#00FFFF', '#00FF00'];
    var colorsRGBA = ['255 165 0','0 128 0', '128 0 128', '0 0 255', '255 255 0', '128 128 0', '0 255 255', '0 255 0'];
    var fps = 0, mps = 0;
    var speed = 5;
    var running = false, starting = false;
    var timeLeft, preTimeLeft;
    var timer, preTimer;

    /* Functions ================================================ */
    function launchFullScreen(element) {
        if(element.requestFullScreen) {
            element.requestFullScreen();
        } else if(element.mozRequestFullScreen) {
            element.mozRequestFullScreen();
        } else if(element.webkitRequestFullScreen) {
            element.webkitRequestFullScreen();
        }
    }
    function resizeCanvas() {
        gameWidth = window.innerWidth;
        gameHeight = window.innerHeight;
        gameContext.canvas.width  = gameWidth;
        gameContext.canvas.height = gameHeight;

        /*gameContext.fillStyle = "#ccc";
        gameContext.fillRect(0, 0, gameWidth, gameHeight);*/
    }
    function toPx(pos) {
        return {
            x: gameWidth*(pos.x+1)/2,
            y: gameHeight*(pos.y+1)/2
        };
    }
    function rgbToHex(r, g, b) {
        return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
    }
    function sToTime(ms) {
        var min, sec;

        min = parseInt(ms / 60, 10);
        sec = parseInt(ms % 60, 10);

        min = min < 10 ? "0" + min : min;
        sec = sec < 10 ? "0" + sec : sec;

        return min + ":" + sec;
    }
    function clearScr() {
        gameContext.fillStyle = bgColor;
        gameContext.fillRect(0, 0, gameWidth, gameHeight);
    }
    function drawPlayer(player) {
        gameContext.beginPath();
        gameContext.arc(player.lastPos.x, player.lastPos.y, 20, 0, 2 * Math.PI, false);
        gameContext.fillStyle = player.color;
        gameContext.fill();
        gameContext.lineWidth = 6;
        //gameContext.strokeStyle = '#003300';
        gameContext.strokeStyle = '#000000';
        gameContext.stroke();
        gameContext.font = "bold 14px Arial";
        gameContext.fillStyle = '#000000';
        gameContext.fillText(
            player.initials,
            player.lastPos.x - gameContext.measureText(player.initials).width/2,
            player.lastPos.y + 4);
    }
    function drawLine(pos0, pos1, color) {
        gameContext.beginPath();
        gameContext.moveTo(pos0.x, pos0.y);
        gameContext.lineTo(pos1.x, pos1.y);
        gameContext.lineWidth = 50;
        gameContext.lineCap = 'round';
        gameContext.strokeStyle = color;
        gameContext.stroke();
    }
    function gameDraw() {
        var np = {};

        if(!running) {
            clearScr(); // Isvalo ekrana, jei ne linijos modas
            players.forEach(function(pl, pID) {

                pl.lastPos.x = pl.vec.x * speed + pl.lastPos.x;
                pl.lastPos.y = pl.vec.y * speed + pl.lastPos.y;

                if(pl.lastPos.x > gameWidth) pl.lastPos.x = gameWidth;
                if(pl.lastPos.x < 0) pl.lastPos.x = 0;
                if(pl.lastPos.y > gameHeight) pl.lastPos.y = gameHeight;
                if(pl.lastPos.y < 0) pl.lastPos.y = 0;

                drawPlayer(pl);
            });
        } else {
            players.forEach(function (pl, pID) {
                np.x = pl.vec.x * speed + pl.lastPos.x;
                np.y = pl.vec.y * speed + pl.lastPos.y;

                if(np.x > gameWidth) np.x = gameWidth;
                if(np.x < 0) np.x = 0;
                if(np.y > gameHeight) np.y = gameHeight;
                if(np.y < 0) np.y = 0;

                drawLine(pl.lastPos, np, pl.color);
                pl.lastPos.x = np.x;
                pl.lastPos.y = np.y;

                drawPlayer(pl);
            });
        }


        fps++;
        window.requestAnimationFrame(gameDraw);
    }
    function runGame() {
        clearInterval(timer);
        clearInterval(preTimer);

        starting = true;

        preTimeLeft = 5; // 5s
        timeLeft = 45; // 45s

        $("#preTimer .preTime").text(sToTime(preTimeLeft));
        $("#timer").text(sToTime(timeLeft));

        $("#timer").removeClass('sharper1');
        $("#timer").removeClass('sharper2');
        $("#timer").removeClass('sharper3');

        bgColor = '#ccc';
        clearScr();

        $("#QR").addClass('hidden');
        $("#preTimer").removeClass('hidden');

        preTimer = setInterval(function(){
            if(--preTimeLeft < 0) {
                clearInterval(preTimer);
                $("#preTimer").addClass('hidden');
                running = true;
                server.send('{"type":"gameStatus","data":"running"}');
                starting = false;

                timer = setInterval(function(){
                    if(--timeLeft < 0) {
                        running = false;
                        server.send('{"type":"gameStatus","data":"lobby"}');

                        $("#timer").text("");
                        $("#QR").removeClass('hidden');
                        clearInterval(timer);
                        findWinner();
                    } else {
                        $("#time").text("Time left: " + sToTime(timeLeft));
                        $("#timer").text(sToTime(timeLeft));
                        if(timeLeft == 15) $("#timer").addClass('sharper1');
                        if(timeLeft == 10) $("#timer").addClass('sharper2');
                        if(timeLeft == 5) $("#timer").addClass('sharper3');
                    }
                }, 1000);

            } else {
                $("#preTimer .preTime").text(sToTime(preTimeLeft));
            }
        }, 1000);


    }
    function findWinner(){
        var image = gameContext.getImageData(0, 0, gameWidth, gameHeight);
        var winColors = [];
        var r, g, b;
        var best = 0;
        var bestColor = '#ccc';
        for(var i = 0; i < gameHeight*gameWidth*4; i+=4){
            r = image.data[i+0];
            g = image.data[i+1];
            b = image.data[i+2];
            //console.error(r + " " + g + " " + b);
            if(colorsRGBA.indexOf(r + " " + g + " " + b) != -1) {
                if(winColors[r*256*256 + g*256 + b] !== undefined) {
                    winColors[r*256*256 + g*256 + b]++;
                } else {
                    winColors[r*256*256 + g*256 + b] = 1;
                }

                if(winColors[r*256*256 + g*256 + b] > best) {
                    best = winColors[r*256*256 + g*256 + b];
                    bestColor = rgbToHex(r, g, b);//"rgb("+r+","+g+","+b+")";
                }
            }
        }

        bgColor = bestColor;
        clearScr();

        players.forEach(function(pl){
            if(pl.color.toUpperCase() == bestColor.toUpperCase()) {
                $("#timer").text(pl.name);
            }
        });
    }

    /* Game Logic =============================================== */
    resizeCanvas();

    $(document).on('webkitfullscreenchange mozfullscreenchange fullscreenchange ', resizeCanvas);
    $(window).resize(resizeCanvas);


    server.onmessage = function (e) {

        var obj = {};
        try { obj = jQuery.parseJSON(e.data); } catch (e) { console.warn("JSON parse error: " + e.toString()); }

        if(obj.type !== undefined && obj.data !== undefined) {
            if(obj.type != "joystick") console.log(e.data);
            switch (obj.type) {
                case "newSession":
                    $("#QR").qrcode({
                        "size": 250,
                        "color": "#3a3",
                        radius: 0.4,
                        fontcolor: '#444',
                        fill: '#444',
                        mSize: 0.2,
                        mode: 2,
                        ecLevel: 'H',
                        label: obj.data ? obj.data : "",
                        "text": obj.data ? obj.data : ""
                    });
                    break;

                case "newConnection":
                    console.log('Pridetas zaidejas: ' + obj.data);
                    players[obj.data] = { color: "#cccccc", vec: {x: 0, y: 0}, lastPos: {x: gameWidth/2, y: gameHeight/2} };
                    break;

                case "changePlayer":
                    if(obj.data.pid !== undefined && players[obj.data.pid]) {
                        players[obj.data.pid].color = obj.data.inf.color;
                        players[obj.data.pid].initials = obj.data.inf.initials;
                        players[obj.data.pid].name = obj.data.inf.name;
                        players[obj.data.pid].ready = obj.data.inf.ready;

                        if(!players[obj.data.pid].ready) {
                            players[obj.data.pid].vec = {x: 0, y: 0};
                        }
                    }
                    break;

                case "dropPlayer":
                    console.log('Atjungtas zaidejas: ' + obj.data);
                    players.splice(obj.data, 1);
                    break;

                case "joystick":
                    if(players[obj.data.playerID]) {
                        players[obj.data.playerID].vec = obj.data;
                        mps++;
                    }
                    break;
            }
        }
    };


    $("#fullScreenPrompt .button.yes").click(function(e){
        launchFullScreen(document.documentElement);
        $("#fullScreenPrompt").addClass("hidden");
        $("#QR").removeClass("hidden");
        //setTimeout(resizeCanvas, 500);
    });

    $("#fullScreenPrompt .button.no").click(function(e){
        $("#fullScreenPrompt").addClass("hidden");
        $("#QR").removeClass("hidden");
    });

    $(document).keypress(function(e) {
        if (e.which == 13) runGame();
        if (e.which == 96) $('#menu').toggleClass('hidden');
        //console.error("Key: " + e.which);
        //if (e.which == 13) resizeCanvas();
    });

    setInterval(function() {
        $("#fps").text("FPS: " + fps); fps = 0;
        $("#mps").text("MPS: " + mps); mps = 0;

        if(!running && !starting) {
            var rCount = 0,
                pCount = 0;
            players.forEach(function(pl) {
                pCount++;
                if(pl.ready) rCount++;
            });
            if ((pCount > 3 && rCount > 3) || (pCount > 0 && pCount <= 3 && rCount == pCount)) runGame();
        }
    }, 1000);

    window.requestAnimationFrame(gameDraw);

});