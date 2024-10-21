package org.example.entidades;

import java.io.Serializable;

/**
 * Clase para determinar el objeto PalabraXML
 */

public class PalabraXML implements Serializable {

    private String word;

    /**
     * Método que define el objeto PalabraXML
     *
     * @param word
     */
    public PalabraXML(String word) {
        this.word = word;
    }

    public PalabraXML() {
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
