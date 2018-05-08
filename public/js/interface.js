$(function() {

    $.getJSON('/w/'+wid+'/get').done( function(data) {
        function make(Kind) {
            return c => new Kind(c, JSON.parse(c.extrajson))
        }

        if("0-cells" in data) data["0-cells"].forEach(make(core.Vert));
        if("1-cells" in data) data["1-cells"].forEach(make(core.Seg));
        if("2-cells" in data) data["2-cells"].forEach(make(core.Area));

        // a hacky way to clear the log after loading things.
        core.evolution_log.length = 0;
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

    $('#dual_button').change(function () {
        let toggle = $(this).prop("checked");
        console.log(toggle);
        if(toggle) {
            core.dualify();
            core.dual.layer.opacity = 1;
            core.standard.layer.opacity = 0.3;
        }
        else {
            core.dual.layer.opacity = 0;
            core.standard.layer.opacity = 1;
        }
    });

    tools.draw.activate();
    $('#draw-button').click();


    $('#save-abutton').click(function () {
        let $this = $(this);
        let $cl = $('.circle-loader');
        if($this.is(":disabled"))
            return;

        console.log(core.evolution_log);
        let token = $('meta[name="csrf-token"]').attr('content');

        $this.prop('disabled', true).addClass('borderless');
        $cl.show();

        $.ajax({
            url: '/w/'+wid+'/evolve',
            type: 'POST',
            contentType : 'application/json',
            beforeSend: function (xhr) {
                //xhr.setRequestHeader('X-CSRF-Token', token);
                xhr.setRequestHeader('Csrf-Token', token);
            },
            data : JSON.stringify(core.evolution_log)
        }).done(function(data) {
            //$('#save-abutton').
            core.evolution_log.length = 0;

            $this.delay(1200).removeClass('borderless',500).queue(function() {
                $this.prop('disabled', false);
                $cl.hide();
                $('.checkmark').hide();
                $( this ).dequeue();
            });


            $cl.delay( 500).queue("classes", function(){
                $cl.addClass('load-complete');
                console.log("YO");
                $('.checkmark').show();
                $(this).dequeue("classes");
            }).delay(1800).queue("classes", function(){
                $(this).dequeue("classes");
            }).dequeue('classes');//.removeClass('load-complete', 0);//.fadeOut(300)
            //$('.checkmark').show().delay(1800);//.fadeOut(100);

        }).fail(function(err, a, b) {
            console.log("FAILURE", err, a, b)
        }).always( function() { });
    });


    function onFrame(event) {

    }

    paper.view.onFrame = onFrame;
});