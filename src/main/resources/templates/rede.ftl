<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Rede - Parlaments-Explorer</title>
    <link rel="stylesheet" href="/css/style.css">
    <script src="/js/d3.v7.min.js"></script>
    <script src="/js/jquery.min.js"></script>
</head>
<body>
    <nav class="navbar">
        <div class="nav-brand">Parlaments-Explorer</div>
        <div class="nav-links">
            <a href="/">Start</a>
            <a href="/visualisierung">Visualisierung</a>
            <a href="/suche">Suche</a>
            <a href="/export">Export</a>
        </div>
    </nav>

    <div class="container">
        <a href="/suche" class="btn btn-back">&larr; Zur&uuml;ck zur Suche</a>

        <!-- Speaker Info -->
        <div class="speaker-card">
            <#if (sprecher.bildDatei)?? && sprecher.bildDatei != "">
                <img src="/bilder/${sprecher.bildDatei}" alt="Foto" class="speaker-photo"
                     onerror="this.style.display='none';this.nextElementSibling.style.display='flex';">
                <div class="speaker-photo-placeholder" style="display:none;"></div>
            <#elseif (sprecher.fotoUrl)?? && sprecher.fotoUrl != "">
                <img src="${sprecher.fotoUrl}" alt="Foto" class="speaker-photo"
                     onerror="this.style.display='none';this.nextElementSibling.style.display='flex';">
                <div class="speaker-photo-placeholder" style="display:none;"></div>
            <#else>
                <div class="speaker-photo-placeholder"></div>
            </#if>
            <div class="speaker-info">
                <h2>${(sprecher.vorname)!"Unbekannt"} ${(sprecher.nachname)!""}</h2>
                <span class="badge">${(sprecher.fraktion)!"-"}</span>
                <div class="speech-meta">
                    ${(rede.datum)!"?"} |
                    Sitzung ${(rede.sitzung)!"?"} |
                    Wahlperiode ${(rede.wahlperiode)!"?"}
                </div>
            </div>
        </div>

        <!-- Annotation Controls -->
        <div class="annotation-controls">
            <h3>Annotationen</h3>
            <div class="toggle-group">
                <label><input type="checkbox" id="toggleNE" checked> Named Entities anzeigen</label>
                <label><input type="checkbox" id="toggleSentiment" checked> Sentiment anzeigen</label>
                <label><input type="checkbox" id="toggleAmpel" checked> Sentiment-Ampel anzeigen</label>
                <label><input type="checkbox" id="toggleSarcasm"> Sarkasmus anzeigen</label>
            </div>
            <div class="sarcasm-threshold" id="sarcasmControls" style="display:none;">
                <label for="sarcasmSlider">Sarkasmus-Schwellwert: <strong id="sarcasmValue">0.50</strong></label>
                <input type="range" id="sarcasmSlider" min="0" max="1" step="0.05" value="0.5">
            </div>
            <div class="ne-legend">
                <span class="ne-badge ne-per">Person (PER)</span>
                <span class="ne-badge ne-loc">Ort (LOC)</span>
                <span class="ne-badge ne-org">Organisation (ORG)</span>
            </div>
            <div class="ampel-legend" style="margin-top:0.8rem;">
                <span class="ampel-dot ampel-positive"></span> Positiv
                <span class="ampel-dot ampel-neutral" style="margin-left:1rem;"></span> Neutral
                <span class="ampel-dot ampel-negative" style="margin-left:1rem;"></span> Negativ
            </div>
        </div>

        <!-- Video + Speech Container -->
        <div class="video-speech-container" id="videoSpeechContainer">

            <!-- Video Player (shown only if video available) -->
            <div class="video-panel" id="videoPanel" style="display:none;">
                <h3>Video der Rede</h3>
                <video id="redeVideo" controls preload="metadata" style="width:100%; border-radius:8px;">
                    Ihr Browser unterst&uuml;tzt kein HTML5 Video.
                </video>
                <div class="video-sentence-display" id="videoSentenceDisplay">
                    <div class="current-sentence-label">Aktueller Satz:</div>
                    <div class="current-sentence-text" id="currentSentenceText">-</div>
                    <div class="current-sentence-ampel" id="currentSentenceAmpel">
                        <span class="ampel-dot ampel-neutral"></span>
                    </div>
                </div>

            </div>

            <!-- Speech Text with Sentiment Ampel -->
            <div class="speech-content" id="redeContainer" data-rede-id="${rede["_id"]}">
                <div id="redeText" class="rede-text">
                    <p class="loading">Annotationen werden geladen...</p>
                </div>
            </div>
        </div>

        <!-- Hinweis wenn kein Video vorhanden -->
        <div id="noVideoMessage" style="display:none;" class="result-card">
            <span>Kein NLP-verarbeitetes Video f&uuml;r diese Rede vorhanden.</span>
        </div>

        <!-- Comments -->
        <#if rede.kommentare?? && (rede.kommentare?size > 0)>
        <div class="comments-section">
            <h3>Kommentare (${rede.kommentare?size})</h3>
            <#list rede.kommentare as kommentar>
                <div class="comment-card">
                    <div class="comment-author">

                        <strong>${(kommentar.autor)!"Unbekannt"}</strong>
                        <#if (kommentar.fraktion)?? && kommentar.fraktion != "">
                            <span class="badge badge-small">${kommentar.fraktion}</span>
                        </#if>
                    </div>
                    <div class="comment-text">${(kommentar.text)!""}</div>
                </div>
            </#list>
        </div>
        </#if>

        <!-- Per-Rede Visualisierung (Mini-Charts) -->
        <div class="charts-grid" style="margin-top:1.5rem;">
            <div class="chart-card">
                <h3>Sentiment dieser Rede</h3>
                <div id="redeSentimentChart" class="chart-container"></div>
            </div>
        </div>
    </div>



    <script src="/js/charts.js"></script>
    <script>
    var nlpData = null;
    var rawText = '';
    var currentVideoId = '${(videoId)!""}';
    var sentencesGlobal = [];

    $(document).ready(function() {
        var redeId = $('#redeContainer').data('rede-id');

        // --- Video Setup ---
        initVideo(redeId);

        // Fetch NLP data via API
        $.getJSON('/api/nlpdata/' + encodeURIComponent(redeId), function(data) {
            nlpData = data;
            // Also need raw text from speech API
            $.getJSON('/api/reden/' + encodeURIComponent(redeId), function(rede) {
                rawText = rede.text || '';
                renderAnnotations();
                // Render per-sentence sentiment line chart
                if (nlpData && nlpData.sentence) {
                    var sentData = nlpData.sentence.map(function(s, i) {
                        return { idx: i, sentiment: s.sentiment || 0, text: s.inhalt || '' };
                    });
                    renderSentimentLineChart('#redeSentimentChart', sentData);
                }
            });
        }).fail(function() {
            // No NLP data available - show raw text
            $.getJSON('/api/reden/' + encodeURIComponent(redeId), function(rede) {
                rawText = rede.text || '';
                $('#redeText').html('<p>' + escapeHtml(rawText).replace(/\n\n/g, '</p><p>').replace(/\n/g, '<br>') + '</p>');
                $('#redeSentimentChart').html('<p class="loading">Keine NLP-Daten verf&uuml;gbar.</p>');
            }).fail(function() {
                $('#redeText').html('<p class="loading">Fehler beim Laden der Rede.</p>');
            });
        });

        // Toggle handlers
        $('#toggleNE, #toggleSentiment, #toggleSarcasm, #toggleAmpel').on('change', function() {
            renderAnnotations();
        });
        $('#sarcasmSlider').on('input', function() {
            $('#sarcasmValue').text(parseFloat($(this).val()).toFixed(2));
            renderAnnotations();
        });
        $('#toggleSarcasm').on('change', function() {
            if ($(this).is(':checked')) {
                $('#sarcasmControls').show();
            } else {
                $('#sarcasmControls').hide();
            }
        });
    });

    // --- Video Functions ---

    function initVideo(redeId) {
        if (currentVideoId) {
            showVideoPlayer(currentVideoId);
        } else {
            // No video assigned - show assignment option
            $('#noVideoMessage').show();
        }
    }

    function showVideoPlayer(videoId) {
        var video = document.getElementById('redeVideo');
        video.src = '/videos/' + videoId + '.mp4';
        $('#videoPanel').show();
        $('#noVideoMessage').hide();

        // Add class for side-by-side layout
        $('#videoSpeechContainer').addClass('has-video');

        // Video time update for sentence sync
        video.addEventListener('timeupdate', function() {
            syncSentenceWithVideo(video.currentTime, video.duration);
        });
    }


    function syncSentenceWithVideo(currentTime, duration) {
        if (!sentencesGlobal || sentencesGlobal.length === 0 || !duration) return;

        // Estimate which sentence is current based on linear time mapping
        var progress = currentTime / duration;
        var sentIdx = Math.min(Math.floor(progress * sentencesGlobal.length), sentencesGlobal.length - 1);
        var sent = sentencesGlobal[sentIdx];

        if (sent) {
            var sentText = sent.text || '';
            var sentiment = sent.sentiment || 0;
            $('#currentSentenceText').text(sentText.substring(0, 120) + (sentText.length > 120 ? '...' : ''));

            var ampelDot = $('#currentSentenceAmpel');
            ampelDot.html(getAmpelDotHtml(sentiment));

            // Highlight current sentence in text
            $('.sentence').removeClass('sentence-active');
            $('.sentence[data-sent-idx="' + sentIdx + '"]').addClass('sentence-active');
        }
    }

    function getAmpelDotHtml(sentiment) {
        if (sentiment > 0.05) return '<span class="ampel-dot ampel-positive"></span> <span class="ampel-label">Positiv (' + sentiment.toFixed(3) + ')</span>';
        if (sentiment < -0.05) return '<span class="ampel-dot ampel-negative"></span> <span class="ampel-label">Negativ (' + sentiment.toFixed(3) + ')</span>';
        return '<span class="ampel-dot ampel-neutral"></span> <span class="ampel-label">Neutral (' + sentiment.toFixed(3) + ')</span>';
    }

    function getAmpelClass(sentiment) {
        if (sentiment > 0.05) return 'ampel-positive';
        if (sentiment < -0.05) return 'ampel-negative';
        return 'ampel-neutral';
    }

    function renderAnnotations() {
        if (!rawText) return;
        if (!nlpData) {
            $('#redeText').html('<p>' + escapeHtml(rawText).replace(/\n\n/g, '</p><p>').replace(/\n/g, '<br>') + '</p>');
            return;
        }

        var showNE = $('#toggleNE').is(':checked');
        var showSentiment = $('#toggleSentiment').is(':checked');
        var showAmpel = $('#toggleAmpel').is(':checked');
        var showSarcasm = $('#toggleSarcasm').is(':checked');
        var sarcasmThreshold = parseFloat($('#sarcasmSlider').val());

        var sentences = (nlpData.sentence || []).slice().sort(function(a, b) { return a.start - b.start; });
        var namedEntities = nlpData.namedentity || [];
        var sarcasmList = nlpData.sarkasmus || [];

        // Store globally for video sync
        sentencesGlobal = sentences.map(function(s) {
            return {
                text: rawText.substring(s.start, Math.min(s.ende, rawText.length)),
                sentiment: s.sentiment || 0
            };
        });

        if (sentences.length === 0) {
            $('#redeText').html('<p>' + escapeHtml(rawText).replace(/\n\n/g, '</p><p>').replace(/\n/g, '<br>') + '</p>');
            return;
        }

        var html = '';
        sentences.forEach(function(sentence, idx) {
            var sentStart = sentence.start;
            var sentEnd = sentence.ende;
            if (sentEnd > rawText.length) sentEnd = rawText.length;
            if (sentStart >= sentEnd) return;
            var sentText = rawText.substring(sentStart, sentEnd);
            var sentiment = sentence.sentiment || 0;

            // Sentiment background
            var bgStyle = '';
            if (showSentiment) {
                bgStyle = 'background-color:' + getSentimentColor(sentiment) + ';';
            }

            // Ampel (traffic light) dot
            var ampelHtml = '';
            if (showAmpel) {
                ampelHtml = '<span class="ampel-dot ' + getAmpelClass(sentiment) + '" title="Sentiment: ' + sentiment.toFixed(4) + '"></span>';
            }

            // Build sentence HTML with NE highlights
            var sentHtml;
            if (showNE) {
                sentHtml = buildAnnotatedSentence(sentText, sentStart, namedEntities);
            } else {
                sentHtml = escapeHtml(sentText);
            }

            // Sarcasm indicator
            var sarcasmHtml = '';
            if (showSarcasm) {
                for (var si = 0; si < sarcasmList.length; si++) {
                    var s = sarcasmList[si];
                    if (s.start >= sentStart && s.start < sentEnd && s.sarcasmScore > sarcasmThreshold) {
                        sarcasmHtml = ' <span class="sarcasm-indicator" title="Sarkasmus: ' +
                            (s.sarcasmScore * 100).toFixed(1) + '%">&#127917;</span>';
                        break;
                    }
                }
            }

            html += '<span class="sentence" data-sentiment="' + sentiment.toFixed(4) + '" data-sent-idx="' + idx + '" style="' + bgStyle + '">' +
                ampelHtml + sentHtml + sarcasmHtml + '</span> ';
        });

        $('#redeText').html(html || '<p>' + escapeHtml(rawText).replace(/\n/g, '<br>') + '</p>');
    }

    function buildAnnotatedSentence(sentText, sentOffset, entities) {
        // Filter entities within this sentence
        var relevant = entities.filter(function(e) {
            return e.start >= sentOffset && e.end <= sentOffset + sentText.length && e.value;
        }).sort(function(a, b) { return a.start - b.start; });

        if (relevant.length === 0) return escapeHtml(sentText);

        var result = '';
        var lastPos = 0;

        relevant.forEach(function(e) {
            var relStart = e.start - sentOffset;
            var relEnd = e.end - sentOffset;
            if (relStart < lastPos || relStart < 0 || relEnd > sentText.length) return;

            result += escapeHtml(sentText.substring(lastPos, relStart));
            var entityClass = 'ne-' + e.value.toLowerCase();
            result += '<span class="ne-highlight ' + entityClass + '" title="' + e.value + '">' +
                escapeHtml(sentText.substring(relStart, relEnd)) + '</span>';
            lastPos = relEnd;
        });

        result += escapeHtml(sentText.substring(lastPos));
        return result;
    }

    function getSentimentColor(val) {
        if (val > 0.05) return 'rgba(76, 175, 80, ' + (Math.min(Math.abs(val), 1) * 0.35).toFixed(2) + ')';
        if (val < -0.05) return 'rgba(244, 67, 54, ' + (Math.min(Math.abs(val), 1) * 0.35).toFixed(2) + ')';
        return 'rgba(158, 158, 158, 0.05)';
    }

    function escapeHtml(text) {
        if (!text) return '';
        return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#039;');
    }
    </script>
</body>
</html>
