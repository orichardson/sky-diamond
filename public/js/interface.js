paper.Point.prototype.perp = function() {
    return new paper.Point(-this.y, this.x);
};

$(function() {

    /****************** SETUP ******************************/
    // paper installation
    paper.setup(document.getElementById('DRAW'));
    paper.install(window);

    // view parameters (will change with viewport updates)
    var scale = 50;
    var offsetX = 200;
    var offsetY = 200;
    var dot_size = scale/2;

    // just a sandbox-y thing
    var namesQ = [];
    for(var i = 0;  i < 26; i++)
        namesQ.push(String.fromCharCode(i + 97))
    namesQ.reverse();


    function toPix( array_of_values ) {
        // for now, this projects onto first two dimensions.
        return new Point(array_of_values[0] * scale + offsetX, array_of_values[1] * scale + offsetY);
    }

    function fromPix( x, y )  {
        return [(x-offsetX)/scale, (y-offsetY)/scale]
    }

    function new_pos() {
        var vs = paper.view.size;
        return new Point(Math.random()* vs.x / scale - offsetX*2,
                        Math.random() * vs.y /scale -offsetY*2 );
    }

    window.points = {};
    window.segments = {};
    window.areas = {};

    function create_point(p) {
        // todo: this assumes points are two-dimensional.
        var pos = pixCoords(("pos" in p) ? p.pos : new_pos());

        var ptext = new PointText(pos);
        ptext.justification = 'center';
        ptext.fillColor = 'black';
        ptext.content = p.name;

        var new_dot = new Path.Circle(pos, dot_size);
        new_dot.fillColor = 'black';

        p.path = new_dot;
        p.pt_text = ptext;

        window.points[p.name] = p
    }

    function create_segment(l) {
        var endpoints = l.sub;
        // should be two of these

        var start = points[endpoints[0]];
        var end = points[endpoints[1]];

        var dist = start.getDistance(end);
        var handle = toPix(l.blade).multiply(0.5);

        l.ptext = new PointText(("pos") in l ? l.pos : start.add(end).multiply(0.5));

        l.path = new Path(  new Segment(start, null, handle),
            new Segment(end, handle.multiply(-1), null) );
        l.path.strokeColor = 'black';
        window.segments[l.name] = l
    }

    function create_area(a) {
        var segs = a.sub;

        a.path = new CompoundPath({children: segs.map( x => window.segments[x].path )});
        a.ptext = new PointText(("pos") in l ? l.pos : start.add(end).multiply(0.5));

        window.areas[a.name] = a    }

    $.getJSON('/w/'+window.diagram_id+'/get').done( function(data) {
        if("0-cells" in data) data["0-cells"].forEach( create_point );
        if("1-cells" in data) data["1-cells"].forEach( create_segment );
        if("2-cells" in data) data["2-cells"].forEach( create_area );
    });

    var tool = new Tool();

    tool.onMouseDown = function (event) {

    };


    function onFrame(event) {

    }


    paper.view.onFrame = onFrame;
});