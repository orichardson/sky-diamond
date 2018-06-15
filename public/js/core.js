
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

        core.evolution_log = [];

        core.Geom = versor.create({metric:[1,1]});

        // view parameters (will change with viewport updates)
        var scale = 50;
        var offset = new Point(200,200);
        var dot_size = scale / 3;
        var dual_dot_size = dot_size * 1.3;

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
        //let colors_predef = ['red', 'blue', 'green', '#FF7F50', 'Aquamarine' ];
        let colors_predef = ['#1b322a','#326659','#84b6ab','#c5e786','#ebe7ca','#de7c55'];


        let colorQ = {
            pop: function() {
              if(colors_predef.length === 0)
                  return new Color([Math.random(), Math.random(), Math.random()]);
                return colors_predef.pop();
            },
            push: function(c) {
                colors_predef.push(c);
            }
        };

        core.colorQ = colorQ;


        function default_cell_data(me, opts) {
            return {dim : me.dim,
                workspace: window.wid,
                mag: "",
                sup: [],
                sub: [],
                extrajson: JSON.stringify(opts || {}) }
        }


        function toPix(array_of_values) {
            // for now, this projects onto first two dimensions.
            return toPixD(array_of_values).add(offset);
        }
        function toPixD (array_of_values) {
            return new Point(Vec.scale(array_of_values, scale));  }

        function fromPix(point) {
            return Vec.scale(point.subtract(offset).toArray(), 1 / scale);          }
        function fromPixD(point) {
            return Vec.scale(point.toArray(), 1 / scale);          }

        function dual_blade(blade) {
            // this is really dumb.
            if(blade.length === 2)
                return [-blade[1], blade[0]];

            return blade;
        }

        core.toPix = toPix;
        core.toPixD = toPixD;
        core.fromPix = fromPix;
        core.fromPixD = fromPixD;

        core.new_pos = function () {
            var vs = paper.view.size;
            return new Point(Math.random() * vs.x / scale - offset.x * 2,
                Math.random() * vs.y / scale - offset.y * 2);
        };

        core.points = {};
        core.segments = {};
        core.areas = {};

        core.dual.v = {};
        core.dual.e = {};
        core.dual.f = {};

        Object.defineProperty(core, 'cells', {
            get: function() {
                let toRet = [];
                var n;

                for ( n in core.points)
                    toRet.push(core.points[n]);

                for ( n in core.segments)
                    toRet.push(core.segments[n]);

                for ( n in  core.areas)
                    toRet.push(core.areas[n]);

                return toRet;
            }
        });



        const cell_proto = {
            dset : function(key, val) { this.data[key] = val; this.log_changes(key); },
            get name() { return this.data.name; },
            get pos() { return this.data.pos; },
            get sub() { return this.data.sub; },
            get sup() { return this.data.sup; },
            get flipped() {return this._my_type[this.data.flipped] },
            set flipped(f) { this.dset('flipped', f.name); },

            get blade() {
                if("_blade" in this) return this._blade;

                //todo: actual blades.
                //return new core.Geom.
            },
            neighbors_above : function() {
                let toReturn = Set([]);

                for(s of this.sup)
                    for( s2 of this._over_type[s].sub)
                        toReturn[i].add(s2)

                return toReturn;
            },

            remove : function() {
                if(!(this.name in this._my_type))
                    return;

                this.path.remove();
                if(this.ptext)
                    this.ptext.remove();

                // update connections to things above by removing those things.
                for( ns of this.sup.slice()) {
                    if(ns in this._sup_type)
                        this._sup_type[ns].remove();
                }

                // update connections to things below by removing dependencies.
                for (ns of this.sub)
                    if(ns in this._sub_type) {
                        let ss = this._sub_type[ns];
                        removeFrom(ss.data.sup, this.name);
                        ss.log_changes("sup");
                        //core.evolution_log.push({type : "update", name : ns, dim: ss.dim, field: "sup", data: ss.sup.slice() });
                    }

                core.evolution_log.push({type : "delete", name : this.name, dim: this.dim, old: $.extend({}, this.data) });
                delete this._my_type[this.name];

                if(this.flipped)
                    this.flipped.remove();

            },
            log_changes : function (fields) {/* assume fields are sent as varargs */
                let n = this.name; let d = this.dim;
                let datacopy =  $.extend(true, {}, this.data);

                if(arguments.length === 0) {
                    // no arguments => we wanted to update the whole row
                    // compression:
                    removeWhere(core.evolution_log, e => (e.name === n && e.dim === d));
                    core.evolution_log.push( {type : "overwrite", name: n, dim : d, data:datacopy} )
                } else {
                    for(f of arguments)
                        core.evolution_log.push({type: "update", name: n, dim: d, field: f, data: datacopy[f] })
                }
            },
            update_display : function () {
            }
        };

        const dual_data = {
            get: function() {
                return {
                    pos : this.dopelganger.pos,
                    blade: dual_blade(this.dopelganger.data.blade), // TODO: this is actually the dual blade.
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
            this.path.cell = this;
            core.evolution_log.push({type: "new", data:$.extend(default_cell_data(this), p)});
            core.points[p.name] = this;
        }
        Vert.prototype = Object.create(cell_proto, {});
        Vert.prototype._sub_type = [];
        Vert.prototype._my_type = core.points;
        Vert.prototype._sup_type = core.segments;
        Vert.prototype.dim = 0;

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
            }

            let path = new Path();
            path.segments = [new Segment(start, null, handle),
                new Segment(start.interp(end,0.5), handle.multiply(-1), null)];

            path.strokeWidth = 2;
            path.strokeColor = '#555';

            path.sendToBack();
            //l.path.segments[0].selected = true;
            //l.path.segments[1].selected = true;

            this.path = path;

            this.data = l;
            this.path.cell = this;
            core.evolution_log.push({type: "new", data: $.extend(default_cell_data(this, options), l) });
            core.segments[l.name] = this;
        }
        Seg.prototype = Object.create(cell_proto, {});
        Seg.prototype._sub_type = core.points;
        Seg.prototype._my_type = core.segments;
        Seg.prototype._sup_type = core.areas;
        Seg.prototype.dim = 1;



        function Area( a, options ) {
            let segs = a.sub;
            removeFrom(a_namesQ, a.name);
            removeFrom(colors_predef, options.color);

            options = options || { };
            core.standard.layer.activate();

            let path = new Path(options);
            path.segments = flatMap(x => core.segments[x].path.segments, segs);
            path.fillColor = options.color;
            path.opacity = 0.3;
            path.sendToBack();

            if(options.insert !== false) {

                let ptext = new PointText(("pos" in a ? toPix(a.pos) : path.position).add([0, 7]));
                ptext.content = a.name;
                ptext.style = {
                    fontFamily: 'Courier New',
                    fontWeight: 'bold',
                    fontSize: 24,
                    fillColor: new Color(options.color).darker(0.5),
                    justification: 'center'
                };

                this.ptext = ptext;
            }

            this.color = options.color;
            this.path = path;
            this.data = a;
            this.path.cell = this;

            core.evolution_log.push({type: "new", data:
                    $.extend(default_cell_data(this, options), a)});
            core.areas[a.name] = this;
        }

        Area.prototype = Object.create(cell_proto, {});
        Area.prototype._sub_type = core.segments;
        Area.prototype._my_type = core.areas;
        Area.prototype._sup_type = [];
        Area.prototype.dim = 2;


        function DualVert ( area ) { // {pos, name, ...dbvars...}
            this.dopelganger = area;

            let options = {insert: area.path.isInserted()};

            core.dual.layer.activate();
            var pos = core.toPix(area.data.pos);
            var new_dot = new Path.Circle({
                center: pos,
                radius: dual_dot_size,
                insert: options.insert
            });

            if (options.insert !== false) {
                new_dot.opacity = 0.8;
                new_dot.fillColor = '#000';
                new_dot.strokeColor = area.color;
                new_dot.strokeWidth = 2;

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
            this.path = new_dot;
            this.path.cell = this;
            core.dual.v[area.name] = this;
        }
        DualVert.prototype = Object.create(cell_proto, {
            _sub_type : [],
            _my_type : core.dual.v,
            _sup_type : core.dual.e
        });
        Object.defineProperty(DualVert.prototype, 'data', dual_data);


        function DualEdge (l) {
            let endpoints = l.sup;
            // should be two of these

            let options = {insert:l.path.isInserted()};

            this.dopelganger = l;
            core.dual.layer.activate();


            let handleDir = core.toPixD(dual_blade(l.data.blade)).normalize();

            let mid = l.path.lastSegment.point;

            let [v1, v2] = endpoints.map(e => core.dual.v[e]);
            let avgC = new Color(v1.color).interp(new Color(v2 ? v2.color : '#FFFFF'), 0.5);

            let start, startC;

            // TODO: VERY 2D specific orientation
            // TODO: sometimes these won't have positions!
            if (v1.data.blade[0] > 0) {
                start = v1.path.position.add(handleDir.multiply(dual_dot_size));
                startC = v1.color;
            } else {
                if (typeof v2 === "undefined") {
                    start = mid.add(handleDir.multiply(-100));
                    startC = '#FFFFFF';

                } else {
                    start = v2.path.position.add(handleDir.multiply(dual_dot_size));
                    startC = v2.color;
                }
            }

            if( options.insert && false ) {
                let offset = handleDir.perp().multiply(14).add([0, 6]);

                let ptext = new PointText(("pos" in l ? toPix(l.pos) : start.interp(mid, 0.43)).add(offset));
                ptext.content = l.name;
                ptext.style = {
                    fontFamily: 'Courier New',
                    fontWeight: 'normal',
                    fontSize: 16,
                    fillColor: '#AAA',
                    justification: 'center'
                };
                ptext.rotation = Math.abs(handleDir.angle) <= 95 ? handleDir.angle : handleDir.angle + 180;
                this.ptext = ptext;
            }

            let dist = start.subtract(mid).getDistance();
            let handle = handleDir.multiply(dist/3);

            let path = new Path();
            path.segments = [new Segment(start, null, handle.multiply(1)),
                new Segment(mid, handle.multiply(-1), null)
            ];
            path.strokeWidth = 4;
            path.strokeColor = {
                gradient: {
                    stops: [startC, avgC ]
                },
                origin: start,
                destination: mid
            };

            path.sendToBack();
            this.path = path;
            this.path.cell = this;

            core.dual.e[l.name] = this;
        }
        DualEdge.prototype = Object.create(cell_proto, {
            _sub_type : core.dual.v,
            _my_type : core.dual.e,
            _sup_type : core.dual.f
        });
        Object.defineProperty(DualEdge.prototype, 'data', dual_data);

        core.Vert = Vert;
        core.Seg = Seg;
        core.Area = Area;

        /**********************************************************************
         *          FINISHED WITH THE CONSTRUCTORS.
         *     the below are methods to integerate with them.
         * ******************************************************************/

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
                mag : "",
                name: v_namesQ.pop(),
                sup: [],
                sub: [],
                blade: []
            });


           // tests for colinearity with lines, and split.
            var seg, ints;
            for(s in core.segments) {
                 seg = core.segments[s];
                 if(seg.path.intersects(pt.path)) {
                     var loc = seg.getNearestLocation(pt.path.position);

                     seg.path.remove();
                     seg.ptext.remove();

                     new Segment({
                         name: p1.data.name + p2.data.name,
                         sup: [],
                         sub: [p1.data.name, p2.data.name],
                         blade: blade,
                         mag : mag,
                         pos: Vec.interp(p1.data.pos, p2.data.pos, 0.33)
                     });


                     delete core.segments[s];
                 }
            }

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

            var blade, mag, l, l2;


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

            [p1,p2].forEach( function(pi) {
                pi.data.sup.push(l.name, l2.name);
                pi.log_changes('sup')
            });

            // determine if we made an area, and if so, add it
            // trace with + and - angle. Each has to be in the same plane as its predecessors.


            function find_path( oriented_seg ){ // segment is oriented.
                let at_v = core.points[oriented_seg.sub[1]];
                let prev_seg = oriented_seg.name;
                let prev_blade = oriented_seg.data.blade;
                let trail = [ prev_seg ];

                let angle_sum = 0;

                var limit = 200;
                //console.log(prev_seg,at_v.name , oriented_seg.sub[1]);

                while(at_v.name !== oriented_seg.sub[0]) {
                    let bestV = undefined, bestS = undefined, bestB = undefined, best_angle = undefined;
                    //console.log("\n\nAT: "+at_v.name+" (looking for "+oriented_seg.sub[0]+")");

                    limit -= 1;
                    if( limit <= 0)
                        return [prev_seg, 0];

                    for ([n, b, s] of at_v.z_out(/*{exclude: prev_seg}*/)) {
                        //TODO: fix this so it works for not just 2D
                        let y = b[0]*prev_blade[1] - b[1]*prev_blade[0];
                        let x =  -Vec.dot(b, prev_blade);

                        let angle = (Math.atan2(y,x) + 2*Math.PI) % (2* Math.PI);

                        if(angle === 0)
                            angle = 2*Math.PI;


                        if(!(angle  >= best_angle )) {
                            bestV = n;
                            bestS = s;
                            bestB = b;
                            best_angle = angle;
                        }
                        //console.log(s, angle);

                    }

                    angle_sum += (best_angle);
                    at_v = bestV;
                    prev_seg = bestS;
                    prev_blade = bestB;


                    trail.push(prev_seg);
                }

                return [trail, angle_sum];
            }

            var paths = [find_path(l), find_path(l2)];
            paths.sort( (a,b) => a[1] - b[1]);


            //console.log("FROM "+p2.name+" TO "+p1.name, paths[0][0].map(x => x  ),
            //    paths[0][1], "\nand", paths[1][0].map(x => x),  paths[1][1], '\n\n');

            let path = paths[0][0];
            if( path[path.length-1] !== l.name && path[path.length-1] !== l2.name)
                core.auto_a(path.map(s => core.segments[s]));


            return l;
        };

        core.auto_a = function( segs ) {
            let midpoint = Vec.scale(segs.map(s => s.pos).reduce( (a,b) => Vec.plus(a,b) ), 1/segs.length);
            let color = colorQ.pop();
            let name = a_namesQ.pop();

            let a = new Area({
                name : name,
                sub: segs.map(s => s.name),
                sup: [],
                blade: [ 1 ],
                mag: undefined,
                pos: midpoint
            },  {color : color} );

            let a2 = new Area({
                name : "~"+name,
                sub: segs.slice().reverse().map(s => s.flipped.name),
                sup: [],
                blade: [ -1 ],
                mag: undefined,
                pos: midpoint
            },  {color:color, insert:false});

            a.flipped = a2;
            a2.flipped = a;


            segs.forEach( function(s){
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