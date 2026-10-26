<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Export - Parlaments-Explorer</title>
    <link rel="stylesheet" href="/css/style.css">
    <script src="/js/jquery.min.js"></script>
</head>
<body>
    <nav class="navbar">
        <div class="nav-brand">Parlaments-Explorer</div>
        <div class="nav-links">
            <a href="/">Start</a>
            <a href="/visualisierung">Visualisierung</a>
            <a href="/suche">Suche</a>
            <a href="/export" class="active">Export</a>
        </div>
    </nav>

    <div class="container">
        <h1>Protokoll-Export</h1>

        <!-- Export Filter -->
        <div class="filter-panel">
            <h3>Export-Auswahl</h3>
            <div class="filter-row">
                <div class="filter-group">
                    <label for="exportType">Modus:</label>
                    <select id="exportType">
                        <option value="single">Einzelnes Protokoll</option>
                        <option value="multiple">Mehrere Protokolle</option>
                        <option value="all">Alle Protokolle</option>
                        <option value="redner">Nach Redner</option>
                        <option value="fraktion">Nach Fraktion</option>
                        <option value="topic">Nach Topic</option>
                    </select>
                </div>

                <!-- Protokoll-Auswahl (Einzeln) -->
                <div class="filter-group" id="singleProtoFilter">
                    <label for="singleProtoSelect">Protokoll:</label>
                    <select id="singleProtoSelect">
                        <option value="">Laden...</option>
                    </select>
                </div>

                <!-- Protokoll-Auswahl (Mehrere) -->
                <div class="filter-group" id="multiProtoFilter" style="display:none;">
                    <label for="multiProtoSelect">Protokolle (Strg+Klick):</label>
                    <select id="multiProtoSelect" multiple size="5" style="min-width:260px;">
                        <option value="">Laden...</option>
                    </select>
                </div>

                <!-- Redner-Auswahl -->
                <div class="filter-group" id="rednerFilter" style="display:none;">
                    <label for="rednerSelect">Redner:</label>
                    <select id="rednerSelect">
                        <option value="">Laden...</option>
                    </select>
                </div>

                <!-- Fraktions-Auswahl -->
                <div class="filter-group" id="fraktionFilter" style="display:none;">
                    <label for="fraktionSelect">Fraktion:</label>
                    <select id="fraktionSelect">
                        <option value="">Laden...</option>
                    </select>
                </div>

                <!-- Topic-Auswahl -->
                <div class="filter-group" id="topicFilter" style="display:none;">
                    <label for="topicSelect">Topic:</label>
                    <select id="topicSelect">
                        <option value="">Laden...</option>
                    </select>
                </div>

                <button class="btn btn-primary" id="generateBtn">
                    PDF generieren
                </button>
            </div>
        </div>

        <!-- Status / Fehler -->
        <div id="exportStatus" style="display:none;" class="result-card">
            <span id="exportStatusText"></span>
        </div>

        <!-- PDF Viewer -->
        <div id="pdfViewerSection" style="display:none;">
            <div class="filter-panel" style="padding:0; overflow:hidden;">
                <div style="display:flex; justify-content:space-between; align-items:center; padding:1rem 1.5rem; border-bottom:1px solid #e0e0e0;">
                    <h3 style="margin:0;">Generiertes PDF</h3>
                    <a id="pdfDownloadBtn" href="#" download="protokoll_export.pdf" class="btn btn-sm">
                        Download
                    </a>
                </div>
                <iframe id="pdfEmbed" src="" style="width:100%; height:700px; border:none; display:block;"></iframe>
            </div>
        </div>

        <!-- Template Editor -->
        <div class="filter-panel" style="margin-top:2rem;">
            <h3>LaTeX-Templates bearbeiten</h3>
            <div class="filter-row" style="margin-bottom:1rem;">
                <button class="btn" id="loadTemplateBtn">Laden</button>
                <button class="btn btn-primary" id="saveTemplateBtn">Speichern</button>
            </div>
            <div class="filter-group" style="width:100%;">
                <label for="templateEditor">Template-Inhalt:</label>
                <textarea id="templateEditor" rows="12"
                    style="width:100%; font-family:monospace; font-size:0.85rem;
                           padding:10px; border:1px solid #ccc; border-radius:4px;
                           resize:vertical; background:#fafafa;"></textarea>
            </div>
            <div id="templateStatus" style="margin-top:0.5rem; font-size:0.85rem; color:#666;"></div>
        </div>
    </div>

    <script>
    $(document).ready(function() {

        // --- Filter-Dropdowns laden ---

        // Protokolle (Sitzungen) laden
        $.getJSON('/api/sitzungen', function(data) {
            var single = $('#singleProtoSelect').empty();
            var multi = $('#multiProtoSelect').empty();
            single.append($('<option>').val('').text('-- Protokoll w\u00e4hlen --'));
            data.forEach(function(d) {
                var wp = d._id.wahlperiode;
                var sNr = d._id.sitzung;
                var key = wp + '-' + sNr;
                var label = 'Sitzung ' + sNr + ' (' + (d.datum || '?') + ')';
                single.append($('<option>').val(key).text(label));
                multi.append($('<option>').val(key).text(label));
            });
        });

        // Redner laden
        $.getJSON('/api/reden/list', function(data) {
            var rednerSeen = {};
            var rednerList = [];
            data.forEach(function(d) {
                var rid = d.rednerId || d._id;
                if (d.sprecherName && d.sprecherName.trim() && !rednerSeen[rid]) {
                    rednerSeen[rid] = true;
                    rednerList.push({ id: rid, name: d.sprecherName.trim(), fraktion: d.fraktion || '' });
                }
            });
            rednerList.sort(function(a, b) { return a.name.localeCompare(b.name); });
            var rSel = $('#rednerSelect').empty();
            rSel.append($('<option>').val('').text('-- Redner w\u00e4hlen --'));
            rednerList.forEach(function(r) {
                var label = r.name + (r.fraktion ? ' (' + r.fraktion + ')' : '');
                rSel.append($('<option>').val(r.id).text(label));
            });
        });

        // Fraktionen laden
        $.getJSON('/api/fraktionen', function(data) {
            var sel = $('#fraktionSelect').empty();
            sel.append($('<option>').val('').text('-- Fraktion w\u00e4hlen --'));
            data.forEach(function(d) {
                if (d._id) sel.append($('<option>').val(d._id).text(d._id));
            });
        });

        // Topics laden
        $.getJSON('/api/topics/list', function(data) {
            var sel = $('#topicSelect').empty();
            sel.append($('<option>').val('').text('-- Topic w\u00e4hlen --'));
            data.forEach(function(d) {
                if (d._id) sel.append($('<option>').val(d._id).text(d._id));
            });
        });

        // --- Filter-Gruppen ein/ausblenden ---

        $('#exportType').on('change', function() {
            var val = $(this).val();
            $('#singleProtoFilter, #multiProtoFilter, #rednerFilter, #fraktionFilter, #topicFilter').hide();
            if (val === 'single') $('#singleProtoFilter').show();
            else if (val === 'multiple') $('#multiProtoFilter').show();
            else if (val === 'redner') $('#rednerFilter').show();
            else if (val === 'fraktion') $('#fraktionFilter').show();
            else if (val === 'topic') $('#topicFilter').show();
        });

        // --- PDF generieren ---

        $('#generateBtn').on('click', function() {
            var type = $('#exportType').val();
            var params = '?type=' + type;

            if (type === 'single') {
                var proto = $('#singleProtoSelect').val();
                if (!proto) { showStatus('Bitte ein Protokoll ausw\u00e4hlen.'); return; }
                params += '&sitzung=' + encodeURIComponent(proto);
            } else if (type === 'multiple') {
                var selected = $('#multiProtoSelect').val();
                if (!selected || selected.length === 0) { showStatus('Bitte mindestens ein Protokoll ausw\u00e4hlen.'); return; }
                selected.forEach(function(s) { params += '&sitzung=' + encodeURIComponent(s); });
            } else if (type === 'redner') {
                var redner = $('#rednerSelect').val();
                if (!redner) { showStatus('Bitte einen Redner ausw\u00e4hlen.'); return; }
                params += '&rednerId=' + encodeURIComponent(redner);
            } else if (type === 'fraktion') {
                var frak = $('#fraktionSelect').val();
                if (!frak) { showStatus('Bitte eine Fraktion ausw\u00e4hlen.'); return; }
                params += '&fraktion=' + encodeURIComponent(frak);
            } else if (type === 'topic') {
                var topic = $('#topicSelect').val();
                if (!topic) { showStatus('Bitte ein Topic ausw\u00e4hlen.'); return; }
                params += '&topic=' + encodeURIComponent(topic);
            }

            showStatus('PDF wird generiert...');
            $('#pdfViewerSection').hide();
            $('#generateBtn').prop('disabled', true).text('Generiere...');

            // PDF als Blob laden und inline anzeigen
            var xhr = new XMLHttpRequest();
            xhr.open('GET', '/api/export/pdf' + params);
            xhr.responseType = 'blob';
            xhr.onload = function() {
                $('#generateBtn').prop('disabled', false).text('PDF generieren');
                if (xhr.status === 200) {
                    var blob = new Blob([xhr.response], { type: 'application/pdf' });
                    var url = URL.createObjectURL(blob);
                    $('#pdfEmbed').attr('src', url);
                    $('#pdfDownloadBtn').attr('href', url);
                    $('#pdfViewerSection').show();
                    showStatus('PDF erfolgreich generiert.');
                } else {
                    showStatus('Fehler beim Generieren (Status ' + xhr.status + ').');
                }
            };
            xhr.onerror = function() {
                $('#generateBtn').prop('disabled', false).text('PDF generieren');
                showStatus('Verbindungsfehler beim Generieren.');
            };
            xhr.send();
        });

        function showStatus(msg) {
            $('#exportStatusText').text(msg);
            $('#exportStatus').show();
        }

        // --- Template Editor ---

        $('#loadTemplateBtn').on('click', function() {
            $('#templateStatus').text('Laden...');
            $.getJSON('/api/templates/preamble', function(data) {
                $('#templateEditor').val(data.content || '');
                $('#templateStatus').text('Preamble geladen.');
            }).fail(function() {
                $('#templateEditor').val('');
                $('#templateStatus').text('Kein Template in der DB gefunden. Standardwert wird beim Export verwendet.');
            });
        });

        $('#saveTemplateBtn').on('click', function() {
            var content = $('#templateEditor').val();
            $('#templateStatus').text('Speichern...');
            $.ajax({
                url: '/api/templates/preamble',
                method: 'PUT',
                contentType: 'application/json',
                data: JSON.stringify({ content: content }),
                success: function() {
                    $('#templateStatus').text('Preamble gespeichert.');
                },
                error: function() {
                    $('#templateStatus').text('Fehler beim Speichern.');
                }
            });
        });

        // Template beim Start laden
        $('#loadTemplateBtn').click();
    });
    </script>
</body>
</html>
