
var start = 0;
var numFound = 0;
var qchanged = false;

String.prototype.capitalize = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
}

function demo_main() {
    $("#searchform").submit(function() {
        do_search();
        return false;
    });
    
    $("#q").change(function() {
        qchanged = true;        
    });
    
    $(".pager").hide();
        
    $("#didyoumean").click(function() {
        $("#q").val($("#didyoumean a").text());
        do_search();
    }).hide();    
    
    $('a[href="#first"]').click(function() {
        start = 0;
        do_search();
        return false;
    });

    $('a[href="#prev"]').click(function() {
        start -= 10;
        if (start < 0) start = 0;
        do_search();
        return false;
    });

    $('a[href="#next"]').click(function() {
        if (start + 10 < numFound) {
            start += 10;
        }
        do_search();
        return false;
    });
    
    $('.facrevd').click(function() {
        // ages facets are exclusive
        var on = $(this).hasClass('facon');
        $('.facrevd').removeClass('facon');
        if (! on) {
            $(this).addClass('facon');
        }
        do_search();
        return false;
    });    
}

function do_search() {
    if (qchanged) start = 0;
    qchanged = false;
    
    var filters = [];
    $(".facon").each(function() {
        filters.push($(this).attr('href'));
    });
    
//    console.log('filters: ' + filters);
    
    $.ajax({
        'type': 'GET',
        'url': 'http://192.168.1.34:8983/solr/morris',
        'data': { 'wt': 'json', 'q': $('#q').val(), 'start': start,
                'fq': filters },
        'dataType': 'jsonp',
        'jsonp': 'json.wrf',
        'traditional': true,
        'success': function(data) { handle_response(data, filters) },
        'failure': function(data) { alert('oh noes!') }
    });
}        

function enable_link(el, enabled) {
    if (enabled) {
        el.removeClass("disabled");
    } else {
        el.addClass("disabled");
    }
}

function handle_response(data, filters) {
    console.log('handle_response');
    $("#results").empty();
    if (data.response.docs.length) {
        numFound = data.response.numFound;
        $("#summary").html("Displaying " + (start + 1) + 
            " to " + (start + data.response.docs.length) + 
            " of " + numFound + " matching pages");
        
        for (var i = 0; i < data.response.docs.length; i++) {
            var doc = data.response.docs[i];
            $("#results").append(format_hit(doc, data));
        }
        
        // pager links
        enable_link($(".first"), start > 0);
        enable_link($(".prev"), start > 0);
        enable_link($(".next"), numFound > start + data.response.docs.length);
        $(".pager").show();

        // facets - reviewdate
        var fq = data.facet_counts.facet_queries;
        $("#fac_1dy span").text('(' + fq['created_at:[NOW/DAY-1DAYS TO NOW/DAY]'] + ')');
        $("#fac_7dy span").text('(' + fq['created_at:[NOW/DAY-7DAYS TO NOW/DAY]'] + ')');
        $("#fac_1mn span").text('(' + fq['created_at:[NOW/DAY-30DAYS TO NOW/DAY]'] + ')');
        $("#fac_1yr span").text('(' + fq['created_at:[NOW/DAY-365DAYS TO NOW/DAY]'] + ')');
        $("#fac_2yr span").text('(' + fq['created_at:[NOW/DAY-730DAYS TO NOW/DAY]'] + ')');

        $("#factweeter").empty()
        var facets = data.facet_counts.facet_fields.user_full_name;
        for (var i = 0; i < facets.length; i += 2) {
            $("#factweeter").append(
                '<div><a class="facfield" href="user_full_name:&quot;' + facets[i] + 
                '&quot;">' + facets[i] + '</a> (' + facets[i+1] + ')</div>');

            if (filters.indexOf('user_full_name:"' + facets[i] + '"') >= 0) {
                $("#factweeter a").last().addClass('facon');
            }
        }

        $("#facmentions").empty()
        var facets = data.facet_counts.facet_fields.ent_mentions_full_name;
        for (var i = 0; i < facets.length; i += 2) {
            $("#facmentions").append(
                '<div><a class="facfield" href="ent_mentions_full_name:&quot;' + facets[i] + 
                '&quot;">' + facets[i] + '</a> (' + facets[i+1] + ')</div>');

            if (filters.indexOf('ent_mentions_full_name:"' + facets[i] + '"') >= 0) {
                $("#facmentions a").last().addClass('facon');
            }
        }
                
        $(".facfield").click(function() {
            console.log("facfield: " + $(this).attr('href'));
            if ($(this).hasClass('facon')) {
                $(this).removeClass('facon');
            } else {
                $(this).addClass('facon');
            }
            do_search();
            return false;
        });

    } else {
        $(".pager").hide();
        $("#summary").html("No matching pages found");
    }    
}

function format_hit(doc, data) {
    var sample = '[description not available]';
    try {
        sample = data.highlighting[doc.id].text.join('...');
    } catch (err) { }
    
    return '<div class="hit"><div class="hittext"><i>' +
        doc.user_screen_name + 
        ' : ' + doc.created_at + 
        '</i><br/>' +
        sample + 
        '</i></div></div>';
}

