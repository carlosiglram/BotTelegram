package org.example.entidades;

/**
 * Clase para mapear información del envio
 */
public class Envio {

    private long chat_id;
    private String text;

    public long getChat_id() {
        return chat_id;
    }

    public void setChat_id(long chat_id) {
        this.chat_id = chat_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    /**
     * @return Un string del objeto
     */
    public String toString() {
        return "Envio{" +
                "chat_id=" + chat_id +
                ", text='" + text + '\'' +
                '}';
    }
}