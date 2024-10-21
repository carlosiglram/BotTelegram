package org.example.entidades;

import com.google.gson.annotations.Expose;

/**
 * Clase para determinar el objeto Palabra
 */

public class Palabra {

    @Expose
    private String palabra;

    /**
     * Método que define el objeto Palabra
     *
     * @param palabra
     */
    public Palabra(String palabra) {
        this.palabra = palabra;
    }

    public Palabra() {
    }

    public String getPalabra() {
        return palabra;
    }

    public void setPalabra(String palabra) {
        this.palabra = palabra;
    }
}