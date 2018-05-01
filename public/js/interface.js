$(function() {

    $.getJSON('/w/'+window.diagram_id+'/get').done( function(data) {
        if("0-cells" in data) data["0-cells"].forEach(c => new core.Vert(c) );
        if("1-cells" in data) data["1-cells"].forEach(c => new core.Seg(c) );
        if("2-cells" in data) data["2-cells"].forEach(c => new core.Area(c) );
    });

    const $toolbuttons = $("button.tool");

    $toolbuttons.click(function(event){
        let $this_button = $(this);
        $toolbuttons.not($this_button)
            .removeClass("active")
            .find(".tool-text").hide();
        let tt =  $this_button.find(".tool-text");

        let active = tt.is(":hidden");
        tt.toggle(100);
        if(active) {
            $this_button.addClass("active");
            tools[$this_button.attr("data-tool")].activate();
        } else {
            $this_button.removeClass("active");
            $this_button.blur();
            tools.dud.activate();
        }

    });

    let toggle = false;
    $('#dual_button').click(function (event) {
        toggle = !toggle;
        if(toggle) {
            core.dualify();
            core.dual.layer.opacity = 1;
            core.standard.layer.opacity = 0.1;
        }
        else {
            core.dual.layer.opacity = 0;
            core.standard.layer.opacity = 1;
        }
    });


    tools.dud.activate();


    function onFrame(event) {

    }

    paper.view.onFrame = onFrame;
});