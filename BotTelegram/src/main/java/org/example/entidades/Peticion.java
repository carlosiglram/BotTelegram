package org.example.entidades;

/**
 * Clase para mapear información de la peticion
 */

public class Peticion {

    private long update_id;
    private Mensaje message;

    public long getUpdate_id() {
        return update_id;
    }

    public void setUpdate_id(long update_id) {
        this.update_id = update_id;
    }

    public Mensaje getMessage() {
        return message;
    }

    public void setMessage(Mensaje message) {
        this.message = message;
    }

    @Override
    /**
     * @return Un string del objeto
     */
    public String toString() {
        return "Peticion{" +
                "update_id=" + update_id +
                ", message=" + message +
                '}';
    }
}