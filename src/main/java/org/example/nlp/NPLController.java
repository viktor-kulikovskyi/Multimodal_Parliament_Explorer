package org.example.nlp;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.example.nlpdata.*;
import org.hucompute.textimager.uima.type.category.CategoryCoveredTagged;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIDockerDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaSandbox;
import org.texttechnologylab.DockerUnifiedUIMAInterface.pipeline_storage.sqlite.DUUISqliteStorageBackend;
import org.texttechnologylab.annotation.NamedEntity;
import org.texttechnologylab.annotation.Sarcasm;
import org.texttechnologylab.uima.type.Sentiment;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NPLController {
    /**
     * The Compose.
     */
    DUUIComposer composer;

    /**
     * Instantiates a new NLP controller
     */

    public NPLController() throws Exception {
        initDockerDriver();
    }

    public void initDockerDriver() throws SQLException, CompressorException, IOException, URISyntaxException, InterruptedException, ClassNotFoundException, UIMAException, SAXException {
        int iWorkers = 1;

        DUUILuaContext ctx = new DUUILuaContext().withJsonLibrary();

        DUUISqliteStorageBackend sqliteStorageBackend = new DUUISqliteStorageBackend(
                "sqlite.db").withConnectionPoolSize(iWorkers);

        ctx.withSandbox(new DUUILuaSandbox().withAllJavaClasses(true));

        composer = new DUUIComposer()
                .withLuaContext(ctx)
                .withWorkers(iWorkers)
                .withStorageBackend(sqliteStorageBackend)
                .withSkipVerification(true);

        DUUIDockerDriver dockerDriver = new DUUIDockerDriver().withTimeout(6000);
        assert composer != null;
        composer.addDriver(dockerDriver);
        composer.add(
                new DUUIDockerDriver.Component("docker.texttechnologylab.org/textimager-duui-spacy-single-de_core_news_sm:0.1.4")
                .withScale(iWorkers)
                .withImageFetching(true));
        composer.add(
                new DUUIDockerDriver.Component("docker.texttechnologylab.org/duui-vader-sentiment:latest")
                        .withScale(iWorkers)
                        .withImageFetching(true)
                        .withParameter("selection","de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence")
        );
        composer.add(
                new DUUIDockerDriver.Component("docker.texttechnologylab.org/parlbert-topic-german:latest")
                        .withScale(iWorkers)
                        .withImageFetching(true)
                        .build()
        );
        composer.add(
                new DUUIDockerDriver.Component("docker.texttechnologylab.org/duui-sarcasm-multilingual-sarcasm-detector:latest")
                        .withScale(iWorkers)
                        .withImageFetching(true)
                        .withParameter("selection","de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence")
        );
    }

    /**
     * Pricess j cas
     *
     * @param jCas the j cas
     */

    public void processJCas(JCas jCas){
        try {
            composer.run(jCas,"GenericRunName");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public JCas prepareJCas(String redestring) throws ResourceInitializationException, CASException {
        JCas redejcas = JCasFactory.createJCas();
        redejcas.setDocumentLanguage("de");
        redejcas.setDocumentText(redestring);
        processJCas(redejcas);
        return redejcas;
    }

    public List<Tokendata> getTokenfromJCas(JCas jCasinput){
        List<Tokendata> tokens = new ArrayList<>();
        JCasUtil.select(jCasinput, Token.class).stream().forEach(token -> {
            Tokendata tokeninput = new Tokendata();
            tokeninput.setStart(token.getStart());
            tokeninput.setEnde(token.getEnd());
            tokeninput.setInhalt(token.getCoveredText());
            tokens.add(tokeninput);
        });
        return tokens;
    }

    public List<Lemmdata> getLemmdatafromJCas(JCas jCasinput){
        List<Lemmdata> lemmdata = new ArrayList<>();
        JCasUtil.select(jCasinput, Lemma.class).stream().forEach(lemm -> {
            Lemmdata lemminput = new Lemmdata();
            lemminput.setStart(lemm.getStart());
            lemminput.setEnd(lemm.getEnd());
            lemminput.setInhalt(lemm.getCoveredText());
            lemminput.setValue(lemm.getValue());
            lemmdata.add(lemminput);
        });
        return lemmdata;
    }

    public List<Satz> getSentencefromJCas(JCas jCasinput){
        List<Satz> sentences = new ArrayList<>();
        JCasUtil.select(jCasinput, Sentence.class).stream().forEach(sentence -> {
            Satz satzinput = new Satz();
            satzinput.setStart(sentence.getStart());
            satzinput.setEnd(sentence.getEnd());
            satzinput.setInhalt(sentence.getCoveredText());
            for(Sentiment sentiment: JCasUtil.selectCovered(Sentiment.class, sentence)){
                satzinput.sentiment=sentiment.getSentiment();
            }
            sentences.add(satzinput);
        });
        return sentences;
    }

    public List<NamedEnt> getNamedEntfromJCas(JCas jCasinput){
        List<NamedEnt> namedEntities = new ArrayList<>();
        for(NamedEntity entity: JCasUtil.select(jCasinput, NamedEntity.class)){
            NamedEnt neinput = new NamedEnt();
            if(entity.getValue().equals("PER")){

                neinput.start=entity.getBegin();
                neinput.end=entity.getEnd();
                neinput.inhalt=entity.getCoveredText();
                neinput.value=entity.getValue();
            }
            else if(entity.getValue().equals("LOC")){

                neinput.start=entity.getBegin();
                neinput.end=entity.getEnd();
                neinput.inhalt=entity.getCoveredText();
                neinput.value=entity.getValue();
            }
            else if(entity.getValue().equals("ORG")){

                neinput.start=entity.getBegin();
                neinput.end=entity.getEnd();
                neinput.inhalt=entity.getCoveredText();
                neinput.value=entity.getValue();
            }
            namedEntities.add(neinput);
        }
        return namedEntities;
    }

    public List<PartsOS> getPartsfromJCas(JCas jCasinput){
        List<PartsOS> partsos = new ArrayList<>();
        JCasUtil.select(jCasinput, POS.class).stream().forEach(pos->{
            PartsOS partinput = new PartsOS();
            //System.out.print(pos.getBegin()+"-"+pos.getEnd)+": "+pos.getCoveredText()+":"+pos.getPosValue());
            partinput.start=pos.getBegin();
            partinput.end=pos.getEnd();
            partinput.inhalt=pos.getCoveredText();
            partinput.value=pos.getPosValue();
            partsos.add(partinput);
        });
        return partsos;
    }
    public List<Depencedata> getDepencedatafromJCas(JCas jCasinput){
        List<Depencedata> depencedata = new ArrayList<>();
        JCasUtil.select(jCasinput, Dependency.class).stream().forEach(depence->{
            Depencedata depenceinput = new Depencedata();
            depenceinput.setBegin(depence.getBegin());
            depenceinput.setEnd(depence.getEnd());
            depenceinput.setGovernor(depence.getGovernor().getCoveredText());
            depenceinput.setDependent(depence.getDependent().getCoveredText());
            depenceinput.setDependencyType(depence.getDependencyType());
            depencedata.add(depenceinput);
        });
        return depencedata;
    }

    public List<Thema> getThemasfromJCas(JCas jCasinput){
        List<Thema> themas = new ArrayList<>();
        JCasUtil.select(jCasinput, CategoryCoveredTagged.class).stream().forEach(cct->{
            Thema themainput = new Thema();

            themainput.start=cct.getBegin();
            themainput.ende=cct.getEnd();
            themainput.value=cct.getValue();
            themainput.score=cct.getScore();
            themas.add(themainput);
        });
        return themas;
    }

    public List<Sarcasmdata> getSarcasmfromJCas(JCas jCasinput){
        List<Sarcasmdata> sarcasm = new ArrayList<>();

        JCasUtil.select(jCasinput, Sarcasm.class).forEach(s->{
            Sarcasmdata sarcasminput = new Sarcasmdata();

            sarcasminput.setStart(s.getBegin());
            sarcasminput.setEnde(s.getEnd());
            sarcasminput.setInhalt(s.getCoveredText());
            sarcasminput.setSarcasmScore(s.getSarcasm());
            sarcasminput.setNonSarcasmScore(s.getNonSarcasm());
            sarcasm.add(sarcasminput);
        });
        return sarcasm;
    }
}
