<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Visualisierung - Parlaments-Explorer</title>
    <link rel="stylesheet" href="/css/style.css">
    <script src="/js/d3.v7.min.js"></script>
    <script src="/js/jquery.min.js"></script>
</head>
<body>
    <nav class="navbar">
        <div class="nav-brand">Parlaments-Explorer</div>
        <div class="nav-links">
            <a href="/">Start</a>
            <a href="/visualisierung" class="active">Visualisierung</a>
            <a href="/suche">Suche</a>
            <a href="/export">Export</a>
        </div>
    </nav>

    <div class="container">
        <h1>Visualisierung der Parlamentsdebatten</h1>

        <!-- Filter Controls -->
        <div class="filter-panel">
            <h3>Filter</h3>
            <div class="filter-row">
                <div class="filter-group">
                    <label for="filterType">Anzeige:</label>
                    <select id="filterType">
                        <option value="all">Alle Reden</option>
                        <option value="single">Einzelne Rede</option>
                        <option value="fraktion">Nach Fraktion</option>
                        <option value="topic">Nach Topic</option>
                    </select>
                </div>
                <div class="filter-group" id="fraktionFilter" style="display:none;">
                    <label for="fraktionSelect">Fraktion:</label>
                    <select id="fraktionSelect"><option value="">Laden...</option></select>
                </div>
                <div class="filter-group" id="topicFilter" style="display:none;">
                    <label for="topicSelect">Topic:</label>
                    <select id="topicSelect"><option value="">Laden...</option></select>
                </div>
                <div class="filter-group" id="singleFilter" style="display:none;">
                    <label for="redeSelect">Rede:</label>
                    <select id="redeSelect"><option value="">Laden...</option></select>
                </div>
                <button class="btn btn-primary" id="applyFilter">
                    Anwenden
                </button>
            </div>
        </div>

        <!-- Charts -->
        <div class="charts-grid">
            <div class="chart-card">
                <h3>Topic-Verteilung (Radar Chart)</h3>
                <div id="radarChart" class="chart-container">
                    <p class="loading">Laden...</p>
                </div>
            </div>
            <div class="chart-card">
                <h3>POS-Verteilung (Sunburst)</h3>
                <div id="sunburstChart" class="chart-container">
                    <p class="loading">Laden...</p>
                </div>
            </div>
            <div class="chart-card">
                <h3>Sentiment-Verlauf (Line Chart)</h3>
                <div id="sentimentChart" class="chart-container">
                    <p class="loading">Laden...</p>
                </div>
            </div>
            <div class="chart-card">
                <h3>Named Entities (Bar Chart)</h3>
                <div id="neBarChart" class="chart-container">
                    <p class="loading">Laden...</p>
                </div>
            </div>
        </div>
    </div>



    <script src="/js/charts.js"></script>
    <script>
    var _pendingRequests = [];

    $(document).ready(function() {
        // Load filter options - use jQuery DOM creation for safe value handling
        $.getJSON('/api/fraktionen', function(data) {
            var sel = $('#fraktionSelect').empty();
            sel.append($('<option>').val('').text('-- Fraktion w\u00e4hlen --'));
            data.forEach(function(d) {
                if (d._id) sel.append($('<option>').val(d._id).text(d._id));
            });
        });
        $.getJSON('/api/topics/list', function(data) {
            var sel = $('#topicSelect').empty();
            sel.append($('<option>').val('').text('-- Topic w\u00e4hlen --'));
            data.forEach(function(d) {
                if (d._id) sel.append($('<option>').val(d._id).text(d._id));
            });
        });
        $.getJSON('/api/reden/list', function(data) {
            var sel = $('#redeSelect').empty();
            sel.append($('<option>').val('').text('-- Rede w\u00e4hlen --'));
            data.forEach(function(d) {
                var label = (d.sprecherName || 'Unbekannt').trim() + ' - ' + (d.datum || '?') + ' (Sitzung ' + (d.sitzung || '?') + ')';
                sel.append($('<option>').val(d._id).text(label));
            });
        });

        // Show/hide filter groups
        $('#filterType').on('change', function() {
            $('#fraktionFilter, #topicFilter, #singleFilter').hide();
            var val = $(this).val();
            if (val === 'fraktion') $('#fraktionFilter').show();
            else if (val === 'topic') $('#topicFilter').show();
            else if (val === 'single') $('#singleFilter').show();
        });

        $('#applyFilter').on('click', function() { loadAllCharts(); });

        // Initial load
        loadAllCharts();
    });

    function getFilterParams() {
        var ft = $('#filterType').val();
        var params = '?filter=' + ft;
        if (ft === 'single') params += '&redeId=' + encodeURIComponent($('#redeSelect').val() || '');
        else if (ft === 'fraktion') params += '&value=' + encodeURIComponent($('#fraktionSelect').val() || '');
        else if (ft === 'topic') params += '&value=' + encodeURIComponent($('#topicSelect').val() || '');
        return params;
    }

    function loadAllCh  arts() {
        // Abort any pending requests to prevent race conditions
        _pendingRequests.forEach(function(xhr) { if (xhr && xhr.abort) xhr.abort(); });
        _pendingRequests = [];

        var params = getFilterParams();
        console.log('[Visualisierung] Filter params:', params);

        // Show loading
        $('.chart-container').html('<p class="loading">Laden...</p>');

        _pendingRequests.push($.ajax({
            url: '/api/visualisierung/topics' + params, dataType: 'json', cache: false,
            success: function(data) { renderRadarChart('#radarChart', data); },
            error: function(xhr) { if (xhr.statusText !== 'abort') $('#radarChart').html('<p class="loading">Fehler beim Laden der Topic-Daten.</p>'); }
        }));

        _pendingRequests.push($.ajax({
            url: '/api/visualisierung/pos' + params, dataType: 'json', cache: false,
            success: function(data) { renderSunburst('#sunburstChart', data); },
            error: function(xhr) { if (xhr.statusText !== 'abort') $('#sunburstChart').html('<p class="loading">Fehler beim Laden der POS-Daten.</p>'); }
        }));

        _pendingRequests.push($.ajax({
            url: '/api/visualisierung/sentiments' + params, dataType: 'json', cache: false,
            success: function(data) { renderSentimentLineChart('#sentimentChart', data); },
            error: function(xhr) { if (xhr.statusText !== 'abort') $('#sentimentChart').html('<p class="loading">Fehler beim Laden der Sentiment-Daten.</p>'); }
        }));

        _pendingRequests.push($.ajax({
            url: '/api/visualisierung/namedentities' + params, dataType: 'json', cache: false,
            success: function(data) { renderNamedEntityBarChart('#neBarChart', data); },
            error: function(xhr) { if (xhr.statusText !== 'abort') $('#neBarChart').html('<p class="loading">Fehler beim Laden der NE-Daten.</p>'); }
        }));
    }
    </script>
</body>
</html>
