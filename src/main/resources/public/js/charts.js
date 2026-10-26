/* D3.js Visualizations */

/* Radar Chart for Topics */
function renderRadarChart(containerId, data) {
    var container = d3.select(containerId);
    // Clear the container for the new visualization
    container.selectAll("*").remove();
    if (!data || data.length === 0) {
        container.append("p").text("Keine Topic-Daten vorhanden.");
        return;
    }

    // Basic dimensions and SVG setup
    var size = 420;
    var margin = 60;
    var radius = size / 2 - margin;
    // Adds SVG-Element with ViewBox
    var svg = container.append("svg").attr("viewBox", "0 0 " + size + " " + size);
    // Group centered at (210,210)
    var g = svg.append("g").attr("transform", "translate(" + size/2 + "," + size/2 + ")");

    // Extract topics and determine maximum value for scaling
    // Topic names from the data
    var topics = data.map(function(d) { return d._id || ""; });
    // Maximum value for scaling
    var maxVal = d3.max(data, function(d) { return d.avgScore; }) || 1;
    var n = topics.length;
    if (n === 0) return;
    // Angle between the axes
    var angle = 2 * Math.PI / n;

    // 3 circles for Radar Chart
    [0.33, 0.66, 1].forEach(function(lvl) {
        g.append("circle").attr("r", radius * lvl)
            .attr("fill", "none").attr("stroke", "#e0e0e0");
    });

    // Draw axes and add topic labels
    topics.forEach(function(t, i) {
        var a = i * angle - Math.PI / 2;
        // Position of the axis
        var x = radius * Math.cos(a), y = radius * Math.sin(a);
        // Adds axis line from the center to the edge
        g.append("line").attr("x1",0).attr("y1",0).attr("x2",x).attr("y2",y)
            .attr("stroke", "#e0e0e0");
        // Positioning the topic label
        var lx = (radius + 14) * Math.cos(a), ly = (radius + 14) * Math.sin(a);
        // Text elements
        g.append("text").attr("x", lx).attr("y", ly)
            // Horizontal alignment
            .attr("text-anchor", lx > 1 ? "start" : lx < -1 ? "end" : "middle")
            // Vertically centered
            .attr("dominant-baseline", "central")
            .attr("font-size", "9px").attr("fill", "#555")
            // Text limit
            .text(t.length > 16 ? t.substring(0, 14) + ".." : t);
    });

    // Draw a data polygon from the topic scores
    var pts = data.map(function(d, i) {
        // Same angle as the axes
        var a = i * angle - Math.PI / 2;
        // Scale value to radius
        var v = (d.avgScore / maxVal) * radius;
        // Coordinates
        return [v * Math.cos(a), v * Math.sin(a)];
    });
    // Line generator
    var line = d3.line().x(function(d){return d[0];}).y(function(d){return d[1];}).curve(d3.curveLinearClosed);
    // Draw a polygon
    g.append("path").datum(pts).attr("d", line)
        .attr("fill", "rgba(55,90,180,0.2)").attr("stroke", "#375ab4").attr("stroke-width", 1.5);

    // Mark data points
    pts.forEach(function(p, i) {
        // Position and radius
        g.append("circle").attr("cx", p[0]).attr("cy", p[1]).attr("r", 3.5)
            .attr("fill", "#375ab4")
            // Mouse view
            .append("title").text(topics[i] + ": " + data[i].avgScore.toFixed(3));
    });
}


/* Sunburst for POS */
function renderSunburst(containerId, data) {
    // Clear the container for the new visualization
    var container = d3.select(containerId);
    container.selectAll("*").remove();
    if (!data || data.length === 0) {
        container.append("p").text("Keine POS-Daten vorhanden.");
        return;
    }

    // Dimensions and scales
    var size = 360;
    var r = size / 2;
    var color = d3.scaleOrdinal(d3.schemeSet2);
    // Hierarchy of data: Root node, Child nodes
    var root = d3.hierarchy({name:"POS", children: data.map(function(d){return {name:d._id, value:d.count};})})
        // Summing the values
        .sum(function(d){return d.value||0;})
        // Descending sort
        .sort(function(a,b){return b.value-a.value;});
    // Partition layout
    d3.partition().size([2*Math.PI, r])(root);

    // Adds SVG-Element with ViewBox
    var svg = container.append("svg").attr("viewBox","0 0 "+size+" "+size);
    // Start in center of SVG
    var g = svg.append("g").attr("transform","translate("+r+","+r+")");

    // Arc generator for the segments
    var arc = d3.arc()
        .startAngle(function(d){return d.x0;}).endAngle(function(d){return d.x1;})
        .innerRadius(function(d){return d.y0 * 0.4;}).outerRadius(function(d){return d.y1-1;});

    // Draw segments
    g.selectAll("path")
        .data(root.descendants().filter(function(d){return d.depth;}))
        // Generate path
        .join("path").attr("d", arc)
        .attr("fill", function(d){return color(d.data.name);})
        .attr("stroke","#fff").attr("stroke-width",0.5)
        .append("title").text(function(d){return d.data.name+": "+(d.value||0).toLocaleString();});

    // Adds label
    g.selectAll("text")
        // Adds only if there is sufficient space
        .data(root.descendants().filter(function(d){return d.depth && (d.x1-d.x0)>0.2;}))
        .join("text")
        .attr("transform",function(d){
            // Central angle in degrees
            var a=(d.x0+d.x1)/2*180/Math.PI;
            // Medium radius
            var rr=(d.y0*0.4+d.y1-1)/2;
            return "rotate("+(a-90)+") translate("+rr+",0) rotate("+(a<180?0:180)+")";
        })
        .attr("dy","0.35em").attr("text-anchor","middle")
        .attr("font-size","8px").attr("fill","#333")
        .text(function(d){return d.data.name;});
}


/* Line Chart for Sentiments */
function renderSentimentLineChart(containerId, data) {
    // Clear the container for the new visualization
    var container = d3.select(containerId);
    container.selectAll("*").remove();
    if (!data || data.length === 0) {
        container.append("p").text("Keine Sentiment-Daten vorhanden.");
        return;
    }
    // Width and height
    var w = 560, h = 320;
    // Margins for axes and labels
    var m = {top:15, right:20, bottom:50, left:50};
    // Inner drawing area
    var iw = w-m.left-m.right, ih = h-m.top-m.bottom;

    // Adds SVG-Element with ViewBox
    var svg = container.append("svg").attr("viewBox","0 0 "+w+" "+h);
    // Start in center of SVG
    var g = svg.append("g").attr("transform","translate("+m.left+","+m.top+")");
    // Checks sentence data or speech data
    var isSatz = data[0].hasOwnProperty("idx") || data[0].hasOwnProperty("sentiment");
    // Cheks wich field use for Y values
    var key = isSatz ? "sentiment" : "avgSentiment";

    // X-Skala
    var x = d3.scaleLinear().domain([0, data.length-1]).range([0, iw]);
    var ext = d3.extent(data, function(d){return d[key]||0;});
    // Y-Skala
    var y = d3.scaleLinear().domain([Math.min(ext[0]||-1,-0.3), Math.max(ext[1]||1,0.3)]).range([ih,0]);

    // X-Achsen with maximal 8 Ticks
    var xAx = d3.axisBottom(x).ticks(Math.min(data.length, 8));
    // Labels according to data type
    if(!isSatz) {
        // Speech data with date
        xAx.tickFormat(function(d){
            var it=data[Math.round(d)];
            return it&&it.datum?it.datum:"";});
    }
    else {
        // Sentence data
        xAx.tickFormat(function(d){return "S"+(Math.round(d)+1);});
    }
    // Draw the x-axis
    g.append("g").attr("transform","translate(0,"+ih+")").call(xAx)
        // Labels around 35 Grad
        .selectAll("text").attr("transform","rotate(-35)").attr("text-anchor","end").attr("font-size","8px");
    // Draw the y-axis
    g.append("g").call(d3.axisLeft(y).ticks(5).tickFormat(d3.format(".1f")));

    // Nulllinie
    g.append("line").attr("x1",0).attr("x2",iw).attr("y1",y(0)).attr("y2",y(0))
        .attr("stroke","#bbb").attr("stroke-dasharray","3,3");

    // Linie generator
    // Indexposition = x
    // Sentiment value = y
    // Monotone curve
    var line = d3.line().x(function(d,i){return x(i);}).y(function(d){return y(d[key]||0);}).curve(d3.curveMonotoneX);
    // Draw a line
    g.append("path").datum(data).attr("d",line)
        .attr("fill","none").attr("stroke","#375ab4").attr("stroke-width",1.5);

    // Y-axis label
    g.append("text").attr("transform","rotate(-90)").attr("y",-38).attr("x",-ih/2)
        .attr("text-anchor","middle").attr("font-size","11px").attr("fill","#666").text("Sentiment");
    // X-axis label with text
    g.append("text").attr("x",iw/2).attr("y",ih+42)
        .attr("text-anchor","middle").attr("font-size","11px").attr("fill","#666")
        .text(isSatz?"Saetze":"Reden");
}


/* Horizontal Bar Chart for Named Entities */
function renderNamedEntityBarChart(containerId, data) {
    // Clear the container for the new visualization
    var container = d3.select(containerId);
    container.selectAll("*").remove();
    if (!data || data.length === 0) {
        container.append("p").text("Keine Named-Entity-Daten vorhanden.");
        return;
    }

    // Names for the entity types
    var labels = {"PER":"Personen","LOC":"Orte","ORG":"Organisationen","MISC":"Sonstiges"};
    // Colors for each entity type
    var farben = {"PER":"#4a7fc4","LOC":"#5a9e5f","ORG":"#d47a2b","MISC":"#8e6bbf"};

    // Dimensions
    var barH = 32;
    var w = 480, h = Math.max(140, data.length*(barH+8)+50);
    // Margins for axes and labels
    var m = {top:10, right:50, bottom:25, left:100};
    // Inner drawing
    var iw = w-m.left-m.right, ih = h-m.top-m.bottom;

    // Adds SVG-Element with ViewBox
    var svg = container.append("svg").attr("viewBox","0 0 "+w+" "+h);
    // Start in SVG
    var g = svg.append("g").attr("transform","translate("+m.left+","+m.top+")");

    // X-Scale with linear range
    var x = d3.scaleLinear().domain([0, d3.max(data,function(d){return d.count;})]).nice().range([0,iw]);
    // Y-Scale for discrete categories
    var y = d3.scaleBand().domain(data.map(function(d){return d._id;})).range([0,ih]).padding(0.25);

    // Y-axis
    g.append("g").call(d3.axisLeft(y).tickFormat(function(d){return labels[d]||d;}))
        .selectAll("text").attr("font-size","10px");
    // X-axis
    g.append("g").attr("transform","translate(0,"+ih+")").call(d3.axisBottom(x).ticks(4));

    // Draw beams
    // Binding data
    g.selectAll("rect").data(data).join("rect")
        // Bars begin on left
        .attr("x",0).attr("y",function(d){return y(d._id);})
        // Width scaled value
        .attr("width",function(d){return x(d.count);})
        // Height range of the scale
        .attr("height",y.bandwidth())
        // Color based on entity type
        .attr("fill",function(d){return farben[d._id]||"#999";})
        // Rounded corners
        .attr("rx",2)
        .append("title").text(function(d){return (labels[d._id]||d._id)+": "+d.count.toLocaleString();});

    // Labels value
    // Selection with class
    g.selectAll(".val").data(data).join("text")
        // Right of the beam
        .attr("x",function(d){return x(d.count)+4;})
        // Vertically centered
        .attr("y",function(d){return y(d._id)+y.bandwidth()/2;})
        .attr("dy","0.35em").attr("font-size","10px").attr("fill","#555")
        // Formatted number with separator
        .text(function(d){return d.count.toLocaleString();});
}
