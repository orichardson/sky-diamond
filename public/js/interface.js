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

    function mkAction(btn_selector, promise) {
        $(btn_selector).click( function() {
            let $this = $(this)
            let $cl = $this.find('.circle-loader');
            if ($this.is(":disabled"))
                return;

            let token = $('meta[name="csrf-token"]').attr('content');
            $this.find('svg').hide();
            $this.find('span.action-label').addClass("moved", 300);
            $this.prop('disabled', true).addClass('borderless', 200);
            $cl.removeClass('load-complete');
            $cl.show();

            function reset(kind) {
                $this.delay(2100).removeClass('borderless', 500).queue(function () {
                    $this.prop('disabled', false);
                    $this.find('svg').show();

                    $(this).dequeue();
                });
                $this.addClass('rslt-' + kind);


                $cl.delay(500, "classes").queue("classes", function () {
                    $cl.addClass('load-complete');
                    $this.find('.markspan').show();
                    $(this).dequeue("classes");
                }).delay(1800, "classes").queue("classes", function () {
                    $this.find('.markspan').hide();
                    $this.removeClass('rslt-' + kind);
                    $cl.fadeOut(200);
                    $this.find('span.action-label').removeClass("moved", 300);
                    $(this).dequeue("classes");
                }).dequeue('classes');//.removeClass('load-complete', 0);
            }

            promise(token).done(function (data) {
                //$('#save-abutton').
                core.evolution_log.length = 0;
                reset('check');
            }).fail(function (err, a, b) {
                reset('cross');
            }).always(function () {
            });
        });
    }


    mkAction('.btn-outline-dark', function(token) {
        return $.ajax({
            url: '/w/' + wid + '/evolve',
            type: 'POST',
            contentType: 'application/json',
            beforeSend: function (xhr) {
                //xhr.setRequestHeader('X-CSRF-Token', token);
                xhr.setRequestHeader('Csrf-Token', token);
            },
            data: JSON.stringify(core.evolution_log)
        })
    });



    function onFrame(event) {

    }

    paper.view.onFrame = onFrame;
});