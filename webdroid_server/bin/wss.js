var websocket = require('ws');
var hashids = require("hashids");
var hashSalt = "GVEMGK" + Date.now();

var ws = {
    hasher: new hashids(hashSalt, 4),
    sessions: [],
    nextSessionID: 1,
    //session: { screens: [], players: [] },

    start: function(options){
        this.server = new websocket.Server(options);
        this.server.on('connection', onWSConnection);
    }
};

function onWSConnection(cl) {
    console.log('=== WS connected ===');

    cl.on('message', onMessage);
    cl.on('close', onClose);
}

function onMessage(message) {

    var obj = {};
    try { obj = JSON.parse(message); } catch (e) { console.warn("JSON parse error: " + e.toString()); }

    if(obj.type && obj.data) {
        if(obj.type != "joystick") console.log(message);
        switch(obj.type) {
            case "newSession":
                this.sessionID = ws.hasher.encode(ws.nextSessionID++);
                ws.sessions[this.sessionID] = {
                    status: "lobby",
                    colors: ['#FFA500','#008000', '#800080', '#0000FF', '#FFFF00', '#808000', '#00FFFF', '#00FF00'],
                    screen:  this,
                    nextPID: 0,
                    players: []
                };
                //sendTo(this, '{"type":"newSession","data":"' + this.sessionID + '"}');
                sendTo(this, JSON.stringify({
                    type: "newSession",
                    data: this.sessionID
                }));
                console.log('New session: ' + this.sessionID);
                break;

            case "newConnection":
                if(obj.data.sessionID && ws.sessions[obj.data.sessionID]){
                    // Priskiriamas sessionID i ws
                    this.sessionID = obj.data.sessionID;

                    // Isiunciamos zaidimo spalvos
                    sendTo(this, JSON.stringify({
                        type: "colors",
                        data: ws.sessions[this.sessionID].colors
                    }));

                    // Isiunciamas zaidimo statusas ir esami zaidejai
                    sendTo(this, JSON.stringify({
                        type: "gameInfo",
                        data: gameInfo(this.sessionID)
                    }));

                    this.playerInfo = {
                        // TODO: Cia gali reiket connected, nes vienam atsijungus, jis nebetaps ready ir neprasides zaidimas
                        ready: false,
                        color: obj.data.playerColor ? obj.data.playerColor : '#ffffff',
                        name: obj.data.playerName ? obj.data.playerName : '',
                        initials: obj.data.playerInitials ? obj.data.playerInitials : ''
                    }
                    //ws.sessions[this.sessionID].players.push(this);
                    ws.sessions[this.sessionID].players[ws.sessions[this.sessionID].nextPID++] = this;
                    this.playerID = ws.sessions[this.sessionID].players.indexOf(this);

                    sendTo(ws.sessions[this.sessionID].screen, JSON.stringify({
                        type: "newConnection",
                        data: this.playerID
                    }));

                    console.log('Player at session: ' + obj.data.sessionID + ' PID: ' + this.playerID);
                } else {
                    sendTo(this, JSON.stringify({
                        type: "error",
                        data: {
                            errorID: 1,
                            errorText: "Wrong sessionID"
                        }
                    }));
                }
                break;

            case "changePlayer":
                if((obj.data.playerColor || obj.data.playerInitials || obj.data.playerName || obj.data.playerIsReady !== undefined) && this.sessionID && ws.sessions[this.sessionID]) {
                    this.playerInfo.color = obj.data.playerColor ? obj.data.playerColor : this.playerInfo.color;
                    this.playerInfo.initials = obj.data.playerInitials ? obj.data.playerInitials : this.playerInfo.initials;
                    this.playerInfo.name = obj.data.playerName ? obj.data.playerName : this.playerInfo.name;
                    this.playerInfo.ready = obj.data.playerIsReady !== undefined ? obj.data.playerIsReady : this.playerInfo.ready;

                    sendTo(ws.sessions[this.sessionID].screen, JSON.stringify({
                        type: "changePlayer",
                        data: {
                            pid: this.playerID,
                            inf: this.playerInfo
                        }
                    }));

                    informPlayers(this.sessionID);
                }
                break;

            case "gameStatus":
                if(this.sessionID && ws.sessions[this.sessionID] && ws.sessions[this.sessionID].screen == this && obj.data && (obj.data == "lobby" || obj.data == "running")) {
                    ws.sessions[this.sessionID].status = obj.data;
                    informPlayers(this.sessionID);
                }
                break;

            case "joystick":
                if(obj.data.x !== undefined && obj.data.y !== undefined && this.sessionID && ws.sessions[this.sessionID]) {
                    sendTo(ws.sessions[this.sessionID].screen, JSON.stringify({
                        type: "joystick",
                        data: {
                            playerID: this.playerID,
                            x: obj.data.x,
                            y: obj.data.y
                        }
                    }));
                }
                break;
        }
    }
}

function informPlayers(sessionID){
    ws.sessions[sessionID].players.forEach(function(pl){
        sendTo(pl, JSON.stringify({
            type: "gameInfo",
            data: gameInfo(sessionID)
        }));
    });
}

function gameInfo(sessionID) {
    if(ws.sessions[sessionID]){
        var ssn = ws.sessions[sessionID];
        var gameObj = {
            status: ssn.status,
            players: []
        };

        ssn.players.forEach(function(player) {
            gameObj.players.push(player.playerInfo);
        });

        return gameObj;

    } else
        return {
            status: "notFound",
            players: []
        };
}

function sendTo(to, m) {
    if(to.readyState == websocket.OPEN) {
        try {
            to.send(m);
        } catch (e) {
            console.error("sendTo([to], "+m+"):" + e);
        }
    }
}

function onClose() {
    console.log('=== WS disconnected ===');

    if(this.sessionID && ws.sessions[this.sessionID]){
        if(ws.sessions[this.sessionID].screen == this) {
            console.log('Session ended: ' + this.sessionID);
            ws.sessions.splice(this.sessionID, 1);
        } else if(this.playerID !== undefined) {
            //this.playerInfo.connected = false;
            //this.playerInfo.ready = true;
            ws.sessions[this.sessionID].players.splice(this.playerID, 1);
            sendTo(ws.sessions[this.sessionID].screen, JSON.stringify({
                type: "dropPlayer",
                data: this.playerID
            }));
            informPlayers(this.sessionID);
        }
    }
}

module.exports = ws;