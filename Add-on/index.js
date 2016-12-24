// CHANGE AREA
var CLIENT_PORT = 6371;
var DEBUG = true;
// END

const Stremio = require("stremio-addons");
const nameToImdb = require("name-to-imdb");
const request = require("request");
const net = require('net');

var manifest = { 
    "name": "PROStremio",
    "description": "Choose torrents for stremio on the fly.",
    "icon": "URL to 256x256 monochrome png icon", 
    "background": "https://raw.githubusercontent.com/butterproject/butter-desktop/master/src/app/images/bg-header.jpg",	// Not mine. Copied from: https://goo.gl/VkPYFs
    "id": "net.jlxip.prostremio",
    "version": "1.0.0",
    "types": ["movie", "series"],
    "filter": { "query.imdb_id": { "$exists": true }, "query.type": { "$in":["series","movie"] } }
};


var client = new Stremio.Client();
client.add("http://cinemeta.strem.io/stremioget");


var methods = { };
var addon = new Stremio.Server(methods, { stremioget: true }, manifest);

var server = require("http").createServer(function (req, res) {
    addon.middleware(req, res, function() { res.end() });
}).on("listening", function()
{
    console.log("PROStremio Addon listening on "+server.address().port);
}).listen(process.env.PORT || 7666);

// Streaming
methods["stream.find"] = function(args, callback) {
    if (! args.query) return callback();
    if (! args.query.imdb_id) return callback();
    
    var m;
    (function(next) {
        m = nameToImdb.byImdb[args.query.imdb_id];
        if (m) return next();
        else client.meta.get({ projection: "lean", query: { imdb_id: args.query.imdb_id } }, function(err, res) {
            m = res;
            next();
        });
    })(function() {
        if (! m) return callback(new Error("unable to resolve meta"));
        
        if (args.query.type === "series") {
            doQueries([
                m.name+" s"+pad(args.query.season)+"e"+pad(args.query.episode) 
            ], args, callback); 
        } else { 
            doQueries([ m.name+" "+m.year ], args, callback); 
        }
    });
};

/*
DATA:
movie_or_series|infohash|mapidx|quality@seeds|trackers

TRACKERS:
tracker_a(B64) & tracker_b(B64)
*/

function doQueries(queries, args, callback) {
	if(DEBUG) console.log("[DEBUG] "+queries[0]);

	var results = [];

	const client = net.connect({port: CLIENT_PORT}, () => {
		if(DEBUG) console.log('Connected.');
		client.write(queries[0]);
	});
	client.on('data', (data) => {
		if(DEBUG) console.log(data.toString());
		client.end();

		if(DEBUG) console.log("Finished!");

		var toReturnArrays = new Array();		

		var DATA_options = data.toString().split("\n");
		for(var i=0;i<DATA_options.length;i++) {
			var OPTION_fields = DATA_options[i].split("|");

			var OPTION_trackers = OPTION_fields[4].split("&");
			var TRACKERS = new Array();
			for(var j=0;j<OPTION_trackers.length;j++) {
				TRACKERS.push(new Buffer(OPTION_trackers[j], 'base64').toString('ascii'));
			}

			if(OPTION_fields[0] == "0")	{	// MOVIE
				var toReturnArray = {
					infoHash: OPTION_fields[1],
					title: OPTION_fields[3],
					isFree: true,
					source: TRACKERS
				};
				toReturnArrays.push(toReturnArray);
			} else {	// SERIES
				var toReturnArray = {
					infoHash: OPTION_fields[1],
					mapIdx: OPTION_fields[2],
					title: OPTION_fields[3],
					isFree: true,
					source: TRACKERS
				};
				toReturnArrays.push(toReturnArray);
			}
		}

		return callback(null, toReturnArrays);
	});
}

function pad(n) {
    return ("00"+n).slice(-2)
}