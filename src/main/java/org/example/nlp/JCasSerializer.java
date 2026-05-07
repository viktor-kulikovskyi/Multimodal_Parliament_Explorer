package org.example.nlp;

import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

/**
 * This class represent J cas serializer
 */
public class JCasSerializer {
    private JCasSerializer() {}

    /**
     * Serialize string
     * @param jCas the j cas
     * @return the string
     */
    public static String serialize(JCas jCas) {
        try {
            ByteArrayOutputStream bytestream = new ByteArrayOutputStream();

            try (GZIPOutputStream outstream = new GZIPOutputStream(bytestream)) {
                XmiCasSerializer.serialize(jCas.getCas(), outstream);
            }
            return Base64.getEncoder().encodeToString(bytestream.toByteArray());

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
