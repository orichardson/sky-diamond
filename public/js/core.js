
paper.Point.prototype.perp = function() {
    return new paper.Point(-this.y, this.x);
};
paper.Point.prototype.mag = function() {
    return this.getDistance();
};
paper.Point.prototype.interp = function(other, amt) {
    return new paper.Point(this.x * (1-amt) + other.x * amt, this.y * (1-amt) + other.y * amt);
};
paper.Point.prototype.toArray = function() { return [this.x, this.y];};

(function(global) {
    $(function () {
        // assume that all other functions are also wrapped in a $(document).ready(...) block,
        // and that they are registered after this one.

        /****************** SETUP ******************************/
        // paper installation
        paper.setup(document.getElementById('DRAW'));
        paper.install(window);

        var core = {};
        core.dual = {};
        core.standard = {};

        core.standard.layer = paper.project.activeLayer;
        core.dual.layer = paper.project.insertLayer(0, new Layer());

        core.Geom = versor.create({metric:[1,1]});

        // view parameters (will change with viewport updates)
        var scale = 50;
        var offsetX = 200;
        var offsetY = 200;
        var dot_size = scale / 3;

        ////////// vertex names
        var v_namesQ = [];
        for (var i = 0; i < 26; i++)
            v_namesQ.push(String.fromCharCode(i + 'a'.charCodeAt(0)))
        for (var i = 0; i < 25; i++)
            v_namesQ.push(String.fromCharCode(i + 'α'.charCodeAt(0)))
        v_namesQ.reverse();

        ///////// area names
        var a_namesQ = [];
        for (var i = 0; i < 26; i++)
            a_namesQ.push(String.fromCharCode(i + 'A'.charCodeAt(0)))
        a_namesQ.reverse();


        //////////// colors
        var colorsQ = ['red', 'blue', 'green', '#FF7F50'];




        function toPix(array_of_values) {
            // for now, this projects onto first two dimensions.
            return new Point(array_of_values[0] * scale + offsetX, array_of_values[1] * scale + offsetY);
        }
        function toPixD (array_of_values) {
            return new Point(array_of_values[0] * scale, array_of_values[1] * scale);  }

        function fromPix(point) {
            return [(point.x - offsetX) / scale, (point.y - offsetY) / scale]
        }

        function dual_blade(blade) {
            // this is really dumb.
            if(blade.length === 2)
                return [-blade[1], blade[0]];

            return blade;
        }

        core.toPix = toPix;
        core.toPixD = toPixD;
        core.fromPix = fromPix;

        core.new_pos = function () {
            var vs = paper.view.size;
            return new Point(Math.random() * vs.x / scale - offsetX * 2,
                Math.random() * vs.y / scale - offsetY * 2);
        };

        core.points = {};
        core.segments = {};
        core.areas = {};

        core.dual.v = {};
        core.dual.e = {};
        core.dual.f = {};

        const cell_proto = {
            get name() { return this.data.name; },
            get pos() { return this.data.pos; },
            get sub() { return this.data.sub; },
            get sup() { return this.data.sup; },

            neighbors_above : function() {
                let toReturn = Set([]);

                for(s of this.sup)
                    for( s2 of this._over_type[s].sub)
                        toReturn[i].add(s2)

                return toReturn;
            },
            get blade() {
                if("_blade" in this) return this._blade;

                //return new core.Geom.
            }
        };

        const dual_data = {
            get: function() {
                return {
                    pos : this.dopelganger.pos,
                    blade: dual_blade(this.dopelganger.blade), // TODO: this is actually the dual blade.
                    sub: this.dopelganger.sup,
                    sup: this.dopelganger.sub,
                    name: this.dopelganger.name
                }
            },
            set: function(dat) {
                if("pos" in dat) this.dopelganger.pos  = dat.pos;
                if("sub" in dat) this.dopelganger.sup  = dat.sub;
                if("sup" in dat) this.dopelganger.sub  = dat.sup;
                if("name" in dat) this.dopelganger.name  = dat.name;
                if("blade" in dat) this.dopelganger.blade  = dual_blade(dat.blade);
            }
        };

        function Vert (p) { // {pos, name, ...dbvars...}
            core.standard.layer.activate();
            removeFrom(v_namesQ, p.name);

            var pos = core.toPix(("pos" in p) ? p.pos : new_pos());

            var new_dot = new Path.Circle(pos, dot_size);
            new_dot.fillColor = '#AAA';
            new_dot.strokeColor = '#555';

            var ptext = new PointText(pos);
            ptext.content = p.name;
            ptext.style = {
                fontFamily: 'Courier New',
                fontWeight: 'bold',
                fontSize: 20,
                fillColor: '#000',
                justification: 'center'
            };
            ptext.translate([0, ptext.getBounds().height / 4]);

            this.data = p;
            this.path = new_dot;
            this.ptext = ptext;

            /*/ for debugging convenience///
            for(prop in p) {
                this[prop] = p[prop];
            }*/

            core.points[p.name] = this;
        }
        Vert.prototype = Object.create(cell_proto, {});
        Vert.prototype._sub_type = [];
        Vert.prototype._my_type = core.points;
        Vert.prototype._sup_type = core.segments;

        //Object.defineProperty(Vert.prototype, 'z_out', { get: ..., set: ... })
        //todo: extends this to all cell complexes
        // This will require defining the "correct direction" for planes, etc.
        Vert.prototype.z_out = function( options ) {
            options = options || {};

            let es = this.data.sup;
            let to_return = [];

            for( e of es ){
                if(e === options.exclude)
                    continue;

                let seg = core.segments[e];
                let other = core.points[seg.sub.find( x => x !== this.name)];

                if( Vec.dot(seg.data.blade, Vec.minus(other.pos, this.pos)) < 0)
                    continue; // there will be another edge going the other way.

                to_return.push([other, seg.data.blade, seg.name]);
            }

            return to_return;
        };


        function DualVert ( area ) { // {pos, name, ...dbvars...}
            this.dopelganger = area;

            let options = {insert: area.path.isInserted()};

            core.dual.layer.activate();
            var pos = core.toPix(area.data.pos);

            if (options.insert !== false) {
                var new_dot = new Path.Circle(pos, dot_size);
                new_dot.fillColor = '#000';
                new_dot.strokeColor = area.color;
                new_dot.strokeWidth = 2;
                this.path = new_dot;

                var ptext = new PointText(pos, options);
                ptext.content = area.name;
                ptext.style = {
                    fontFamily: 'Courier New',
                    fontWeight: 'bold',
                    fontSize: 24,
                    fillColor: '#FFF',
                    justification: 'center'
                };
                ptext.translate([0, ptext.getBounds().height / 4]);
                this.ptext = ptext;
            }

            this.color = area.color;

            core.dual.v[area.name] = this;
        }
        function DV() {}
        DualVert.prototype = Object.create(cell_proto, {});
        DualVert.prototype._sub_type = [];
        DualVert.prototype._my_type = core.dual.v;
        DualVert.prototype._sup_type = core.dual.e;
        Object.defineProperty(DV.prototype, 'data', dual_data);


        function Seg (l, options) {
            options = options || {};

            let endpoints = l.sub;
            core.standard.layer.activate();

            let start = core.points[endpoints[0]].path.position;
            let end = core.points[endpoints[1]].path.position;
            // TODO: sometimes these won't have positions..?

            //TODO: for arbitrary blades.
            let handle = core.toPixD(l.blade).multiply(0.5);

            let offset = handle.perp().normalize().multiply(14).add([0, 6]);

            var path;

            if(options.insert !== false) {
                let ptext = new PointText(("pos" in l ? toPix(l.pos) : start.interp(end, 0.33)).add(offset));
                ptext.content = l.name;
                ptext.style = {
                    fontFamily: 'Courier New',
                    fontWeight: 'bold',
                    fontSize: 16,
                    fillColor: '#CCC',
                    justification: 'center'
                };
                ptext.rotation = Math.abs(handle.angle) <= 95 ? handle.angle : handle.angle + 180;
                this.ptext = ptext;
                path = new Path();
            }else {
                path = new Path({insert:false});
            }
            path.segments = [new Segment(start, null, handle),
                new Segment(end, handle.multiply(-1), null)];

            path.strokeWidth = 2;
            path.strokeColor = '#555';

            path.sendToBack();
            //l.path.segments[0].selected = true;
            //l.path.segments[1].selected = true;

            this.path = path;

            this.data = l;
            core.segments[l.name] = this;
        }
        Seg.prototype = Object.create(cell_proto, {});
        Seg.prototype._sub_type = core.points;
        Seg.prototype._my_type = core.segments;
        Seg.prototype._sup_type = core.areas;

        function DualEdge (l) {
            let endpoints = l.sup;
            // console.log(endpoints);
            // should be two of these

            let options = {insert:l.path.isInserted()};

            this.dopelganger = l;
            core.dual.layer.activate();

            console.log(l.name, endpoints);

            let handle = core.toPixD(dual_blade(l.data.blade)).multiply(0.5);

            let start = core.dual.v[endpoints[0]].path.position;
            let end = endpoints.length > 1 ? core.dual.v[endpoints[1]].path.position : start.add(handle.multiply(3));

            let startC = core.dual.v[endpoints[0]].color;
            let endC = endpoints.length > 1 ? core.dual.v[endpoints[1]].color : '#FFF';
            // TODO: sometimes these won't have positions!

            let offset = handle.perp().normalize().multiply(14).add([0, 6]);

            if( options.insert !== false ) {
                let ptext = new PointText(("pos" in l ? toPix(l.pos) : start.interp(end, 0.33)).add(offset));
                ptext.content = l.name;
                ptext.style = {
                    fontFamily: 'Courier New',
                    fontWeight: 'normal',
                    fontSize: 16,
                    fillColor: '#AAA',
                    justification: 'center'
                };
                ptext.rotation = Math.abs(handle.angle) <= 95 ? handle.angle : handle.angle + 180;
                this.ptext = ptext;
            }

            let path = new Path(options);
            path.segments = [new Segment(start, null, handle),
                new Segment(end, handle.multiply(-1), null)];
            path.strokeWidth = 4;
            path.strokeColor = {
                gradient: {
                    stops: [startC, endC]
                },
                origin: start,
                destination: end
            };

            path.sendToBack();
            //l.path.segments[0].selected = true;
            //l.path.segments[1].selected = true;

            this.path = path;


            /* for debugging convenience
            for(prop in p) {
                this[prop] = p[prop];
            }*/

            core.dual.e[l.name] = this;
        }
        DualEdge.prototype = Object.create(cell_proto, {});
        DualEdge.prototype._sub_type = core.dual.v;
        DualEdge.prototype._my_type = core.dual.e;
        DualEdge.prototype._sup_type = core.dual.f;
        //Object.defineProperty(DualEdge.prototype, 'data', dual_data);



        function Area( a, color, options ) {
            let segs = a.sub;

            options = options || {};
            core.standard.layer.activate();

            let path = new Path(flatMap(x => core.segments[x].path.segments, segs), options);
            path.fillColor = color;
            path.opacity = 0.3;
            path.sendToBack();

            if(options.insert !== false) {

                let ptext = new PointText(("pos" in a ? toPix(a.pos) : path.position).add([0, 7]));
                ptext.content = a.name;
                ptext.style = {
                    fontFamily: 'Courier New',
                    fontWeight: 'bold',
                    fontSize: 24,
                    fillColor: new Color(color).darker(0.5),
                    justification: 'center'
                };

                this.ptext = ptext;
            }

            this.color = color;
            this.path = path;
            this.data = a;

            core.areas[a.name] = this;
        }

        Area.prototype = Object.create(cell_proto, {});
        Area.prototype._sub_type = core.segments;
        Area.prototype._my_type = core.areas;
        Area.prototype._sup_type = [];



        core.Vert = Vert;
        core.Seg = Seg;
        core.Area = Area;

        core.closestP = function(p) { // p is in pixels
            let mindist, min_name , worldp, dist;
            worldp = core.fromPix(p);
            mindist = 100000;
            min_name = undefined;

            for(name in core.points) {
                dist = Vec.dist(core.points[name].data.pos, worldp);
                if(dist < mindist) {
                    mindist = dist;
                    min_name = name;
                }
            }

            return {name:min_name, dist : mindist};
        };

        core.thresh_overlap = dot_size/scale;
        core.auto_pt = function ( point ) {
            let worldp = core.fromPix(point);

            for(let name in core.points) {
                if(Vec.dist(core.points[name].data.pos, worldp) <= core.thresh_overlap)
                    return core.points[name];
            }

            let pt = new Vert({
                pos: worldp,
                name: v_namesQ.pop(),
                sup: [],
                sub: [],
                blade: []
            });


           // tests for colinearity with lines, and split.
            /*var seg, ints;
            for(s in core.segments) {
                 seg = core.segments[s];
                 if(seg.path.intersects(pt.path)) {
                     var loc = seg.getNearestLocation(pt.path.position);

                     seg.path.remove();
                     seg.ptext.remove();

                     new Segment()


                     delete core.segments[s];
                 }
             }*/

            return pt;
        };

        core.find_all_paths = function(n1, n2) {
            // console.log(n1,n2);
            let v1 = core.points[n1]; // : Vertex
            let v2 = core.points[n2]; // : Vertex

            let diff = Vec.normalized(v1.pos.minus(v2.pos));

            //var paths = core.find_all_paths();
            //paths.

        };

        core.auto_ln = function (p1, p2) {
            if(p1.name === p2.name)
                return;

            var blade, mag, l;


            if ("pos" in p1.data && "pos" in p2.data) {
                blade = Vec.minus(p2.data.pos, p1.data.pos);
            }

            // console.log(p1.data.name, p2.data.name);

            l = new Seg({
                name: p1.data.name + p2.data.name,
                sup: [],
                sub: [p1.data.name, p2.data.name],
                blade: blade,
                mag : mag,
                pos: Vec.interp(p1.data.pos, p2.data.pos, 0.33)
            });

            l2 = new Seg({
               name : p2.name + p1.name,
               sup: [],
               sub: [p2.name, p1.name],
               blade: Vec.neg(blade),
               mag: mag,
               pos: Vec.interp(p2.data.pos, p1.data.pos, 0.33)
            }, {insert:false});

            l.flipped = l2;
            l2.flipped = l;

            p1.data.sup.push(l.name);
            p1.data.sup.push(l2.name);
            p2.data.sup.push(l.name);
            p2.data.sup.push(l2.name);

            // determine if we made an area, and if so, add it
            // trace with + and - angle. Each has to be in the same plane as its predecessors.


            function find_path( plane ){ // plane is oriented.
                let at_v = p2;
                let prev_seg = l.name;
                let prev_blade = l.data.blade;
                let trail = [ prev_seg ];

                let angle_sum = 0;

                while(at_v.name !== p1.name) {
                    let bestV = undefined, bestS = undefined, bestB = undefined, best_angle = undefined;
                    //console.log("\n\nAT: "+at_v.name);

                    for ([n, b, s] of at_v.z_out(/*{exclude: prev_seg}*/)) {
                        //TODO: fix this so it works for not just 2D
                        let y = b[0]*prev_blade[1] - b[1]*prev_blade[0];
                        let x =  -Vec.dot(b, prev_blade);

                        let angle = (Math.atan2(y,x) + 2*Math.PI) % (2* Math.PI);

                        if(angle === 0 && plane > 0)
                            angle = 2*Math.PI;


                        if(!(plane*angle  >= plane*best_angle )) {
                            bestV = n;
                            bestS = s;
                            bestB = b;
                            best_angle = angle;
                        }
                        //console.log(bestS, angle);

                    }

                    angle_sum += (best_angle);
                    at_v = bestV;
                    prev_seg = bestS;
                    prev_blade = bestB;


                    trail.push(prev_seg);
                }

                return [trail, angle_sum];
            }

            var paths = [find_path(1), find_path(-1)];
            paths.sort( (a,b) => a[1] - b[1]);


            console.log("FROM "+p2.name+" TO "+p1.name, paths[0][0].map(x => x  ),
                paths[0][1], "\nand", paths[1][0].map(x => x),  paths[1][1], '\n\n');

            let path = paths[0][0];
            if( path[path.length-1] !== l.name && path[path.length-1] !== l2.name)
                core.auto_a(path.map(s => core.segments[s]));


            return l;
        };

        core.auto_a = function( segs ) {
            let midpoint = Vec.scale(segs.map(s => s.pos).reduce( (a,b) => Vec.plus(a,b) ), 1/segs.length);
            let color = colorsQ.pop();
            let name = a_namesQ.pop();

            let a = new Area({
                name : name,
                sub: segs.map(s => s.name),
                sup: [],
                blade: [],
                mag: undefined,
                pos: midpoint
            },  color);

            let a2 = new Area({
                name : "~"+name,
                sub: segs.splice().reverse().map(s => s.name),
                sup: [],
                blade: [],
                mag: undefined,
                pos: midpoint
            },  color, {insert:false});


            segs.forEach( function(s){
                console.log(s.flipped);
                s.data.sup.push(a.name);
                s.flipped.data.sup.push(a2.name);
            });

            return a;
        };

        core.dualify = function() {
            core.standard.layer.opacity = 0.3;

            for(name in core.areas) {
                new DualVert(core.areas[name]);
            }

            for (name in core.segments) {
                new DualEdge(core.segments[name]);
            }
        };

        global.core = core;
    });
})(this);