$(function() {

    $.getJSON('/w/'+window.diagram_id+'/get').done( function(data) {
        if("0-cells" in data) data["0-cells"].forEach(c => new core.Vert(c) );
        if("1-cells" in data) data["1-cells"].forEach(c => new core.Seg(c) );
        if("2-cells" in data) data["2-cells"].forEach(c => new core.Area(c) );
    });


    tools.drawTool.activate();


    function onFrame(event) {

    }

    paper.view.onFrame = onFrame;
});