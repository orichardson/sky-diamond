
(function(global) {

    $(function() {
        console.log('running tools.js setup, registering to', global);
        let core = global.core;

        let drawTool = (function() {
            let t = new Tool();

            var path;
            var lastPointMade = null;
            var dir = new Point(0, 0);
            var shift  = false;
            t.minDistance = 5;

            t.onMouseDown = function(event) {
                path = new Path();
                path.add(event.point);
                path.strokeColor = '#AAA';

                lastPointMade = core.auto_pt(event.point);
            };

            t.onMouseDrag = function(event) {
                var last = path.lastSegment.point;
                var diff = event.point.subtract(last);
                var closest = core.closestP(last);

                //console.log(diff.dot(dir), diff.getDistance() * dir.getDistance());


                if(closest.dist <= core.thresh_overlap ||
                    (diff.dot(dir)/(diff.mag() * dir.mag()+0.001) < 0.5 &&  closest.dist > 1)) {
                    //console.log("^^^^^^^^^")
                    var p = core.auto_pt(last);
                    if(lastPointMade)
                        core.auto_ln(lastPointMade, p);

                    lastPointMade = p;
                    path.remove();

                    path = new Path();
                    path.strokeColor = '#AAA';

                    dir = diff;
                } else {
                    dir = dir.interp(diff , 0.1)
                }
                path.add(event.point)
            };

            t.onMouseUp = function(event) {
                dir = new Point(0,0);
                t.onMouseDrag(event);
                path.remove();
                lastPointMade = null;
            };

            tool.onKeyUp = function(event) {
                if (event.key == 'shift')
                    shift = true;
            };
            tool.onKeyDown = function(event) {
                if (event.key == 'shift')
                    shift = false;
            };

            return t
        })();


        let cell_hit_options = {
            fill: true,
            stroke: true,
            segments: true,
            tolerance: 4,
            match: hr => ("cell" in hr.item)
        };
        function pickCell(pt, f){
            let hit = paper.project.hitTest(pt, cell_hit_options );
            if(hit) {
                let c = hit.item.cell;
                return f ? f(c): c;
            }
        }


        let eraseTool = (function() {
            let t = new Tool();


            t.minDistance = 1;
            function eraseAt(pt) {
                pickCell(pt, x => x.remove());
            }


            t.onMouseDown = function(event) {
                eraseAt(event.point);
            };

            t.onMouseDrag = function(event) {
                eraseAt(event.midPoint);
                eraseAt(event.point);
            };

            t.onMouseUp = function(event) {};
            t.onKeyUp = function(event) {} ;
            t.onKeyDown = function(event) {};
            return t;
        })();


        let panTool = (function() {
            let t = new Tool();

            t.onMouseDown = function(event) {

            };

            t.onMouseDrag = function(event) {

            };

            t.onMouseUp = function(event) {
            };

            t.onKeyUp = function(event) {
            };
            t.onKeyDown = function(event) {

            };
            return t;
        })();

        let manualTool = (function() {
            let t = new Tool();

            var start;

            t.onMouseDown = function(event) {
                start = event.point;
            };

            t.onMouseDrag = function(event) {

            };

            t.onMouseUp = function(event) {
                start = undefined;
            };

            t.onKeyUp = function(event) {
            };
            t.onKeyDown = function(event) {

            };
            return t;
        })();

        let moveTool = (function() {
            let t = new Tool();

            var activeStart, dragStart, active, last;

            function update(event) {


                if(active) {
                    active.dset('pos', Vec.plus(diff, activeStart));
                    active.path.translate( event.delta );
                    active.ptext.translate( event.delta );
                }
            }

            t.onMouseDown = function(event) {
                active = pickCell(event.point);
                if(active) {
                    activeStart = active.pos;
                    dragStart = event.point;
                }
            };

            t.onMouseDrag = function(event) {
                let diffPix = event.point.subtract(dragStart);

                if(active) {
                    active.path.translate( event.delta );
                    active.ptext.translate( event.delta );
                    //active.flipped.path
                    last = diffPix;
                }
            };

            t.onMouseUp = function(event) {
                let diffPix = event.point.subtract(dragStart);
                let diff = core.fromPixD(diffPix);

                if(active) {
                    active.dset('pos', Vec.plus(diff, activeStart));
                    active.path.translate( event.delta.subtract(last) );
                    active.ptext.translate( event.delta.subtract(last) );
                }

                active = undefined;
                activeStart = undefined;
                dragStart = undefined;
            };

            t.onKeyUp = function(event) {
            };
            t.onKeyDown = function(event) {

            };
            return t;
        })();


        global.tools = { draw : drawTool,
            erase : eraseTool,
            manual: manualTool,
            move : moveTool,
            dud : new Tool()};
    });

})(this); // closure to hide implementation details, register to global object