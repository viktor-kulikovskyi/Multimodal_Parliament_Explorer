package org.example.nlp;

import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

/**
 * This class represents the type J cas deserializer.
 */
public class JCasDeSerializer {
    private JCasDeSerializer(){}


    /**
     * Deserialize J cas
     */

    public static JCas deSerialize(String b64){

        try{
            byte[] inbytes = Base64.getDecoder().decode(b64);
            try (GZIPInputStream instream =  new GZIPInputStream(new ByteArrayInputStream(inbytes))){
                JCas jCas = JCasFactory.createJCas();
                XmiCasDeserializer.deserialize(instream, jCas.getCas());
                return jCas;
            }
        } catch (Exception e){
            throw new RuntimeException("Error",e);
        }

    }
}
