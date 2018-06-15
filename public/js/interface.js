$(function() {
    let crsf_token =  $('meta[name="csrf-token"]').attr('content');
    let $post = function (url, data, content_type) {
        return $.ajax({
            url: url,
            type: 'POST',
            contentType: content_type || 'application/json',
            beforeSend: function (xhr) {
                xhr.setRequestHeader('Csrf-Token', crsf_token);
            },
            data: data
        })
    };

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
            let $this = $(this);
            if ($this.is(":disabled"))
                return;

            let $cl = $this.find('.circle-loader');

            function show_go() {
                $this.find('svg').hide();
                $this.find('span.action-label').addClass("moved", 300);
                $this.prop('disabled', true).addClass('borderless', 200);
                $cl.removeClass('load-complete');
                $cl.show();
            }

            function show_reset(kind) {
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

            show_go();
            promise().done(function (data) {
                //$('#save-abutton').
                core.evolution_log.length = 0;
                show_reset('check');
            }).fail(function (err, a, b) {
                show_reset('cross');
            }).always(function () {
            });
        });
    }


    mkAction('.btn-outline-dark', function() {


        return $post( '/w/' + wid + '/evolve', JSON.stringify(core.evolution_log)).then(function() {
            let svg = paper.project.exportSVG();
            let b = paper.project.activeLayer.bounds;
            svg.setAttribute('viewBox', Math.floor(b.x) + ' '+Math.floor(b.y)+' '+ Math.ceil(b.width)+' '+ Math.ceil(b.height) );
            svg.setAttribute('width', '100%');
            svg.setAttribute('height', '10em');

            return $post('/w/'+wid+'/svg_update',  (new XMLSerializer()).serializeToString(svg), 'text/plain')
        })


    });



    function onFrame(event) {

    }

    paper.view.onFrame = onFrame;
});