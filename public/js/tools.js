
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
            t.minDistance = 2;

            t.onMouseDown = function(event) {
                path = new Path();
                path.add(event.point);
                path.strokeColor = '#AAA';
            };

            t.onMouseDrag = function(event) {
                var last = path.lastSegment.point;
                var diff = event.point.subtract(last);
                var closest = core.closestP(last);

                //console.log(diff.dot(dir), diff.getDistance() * dir.getDistance());


                if(diff.dot(dir)/(diff.mag() * dir.mag()+0.001) < 0.5 &&
                        (closest.dist > 1 || closest.dist <= core.thresh_overlap)) {
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


        let panTool = (function() {
            let t = new Tool();

            t.onMouseDown = function(event) {

            };

            t.onMouseDrag = function(event) {

            };

            t.onMouseUp = function(event) {
            };

            tool.onKeyUp = function(event) {
            };
            tool.onKeyDown = function(event) {

            };
        })();


        global.tools = { drawTool : drawTool };
    });

})(this); // closure to hide implementation details, register to global object