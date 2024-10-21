package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.entidades.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Clase con métodos para utilizar el bot de Telegram
 *
 * @author Carlos Iglesias
 * @version 1.0 17/10/23
 */

// Clase con métodos para utilizar el bot de Telegram
public class BotUtils {
    static final String BOT_TOKEN = "***_TELEGRAM_BOT_TOKEN_***";
    static final String API_TELEGRAM = "https://api.telegram.org/bot";
    static final String METHOD_GET = "GET";
    static final String METHOD_POST = "POST";
    static final String PROPERTY_ACCEPT = "Accept";
    static final String PROPERTY_APPLICATION_JSON = "application/json";
    static final String PROPERTY_CONTENT_TYPE = "Content-Type";
    static final String LOG_ERROR_SOLICITUD = "Error en la solicitud. Código de respuesta: ";
    static final String LOG_ENVIO_CORRECTO = "Envío correcto";
    static final int HTTP_CODE_OK = 200;
    static long offset = 0;

    // Método que invocará al API de Telegram para ver si hay peticiones de usuarios pendientes de procesar
    public static void procesarPeticiones() {

        try {
            // Crear petición HTTP
            String apiUrl = API_TELEGRAM + BOT_TOKEN + "/getUpdates?offset=" + offset;
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(METHOD_GET);
            conn.setRequestProperty(PROPERTY_ACCEPT, PROPERTY_APPLICATION_JSON);

            // Obtiene el código de respuesta
            int responseCode = conn.getResponseCode();
            // Si es un 200 la petición se ha realizado con éxito
            if (responseCode == HTTP_CODE_OK) {

                // Lee la respuesta JSON
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parsea la respuesta
                Gson gson = new Gson();
                Respuesta respuesta = gson.fromJson(response.toString(), Respuesta.class);

                // Recorremos todas las peticiones y las procesamos
                for (Peticion peticion : respuesta.getResult()) {
                    procesarPeticion(peticion);
                }
            } else {
                System.out.println(LOG_ERROR_SOLICITUD + responseCode);
            }

            // Cierra la conexión
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método que evaluará una petición enviada por un usuario al bot
    // @param peticion Datos de la petición del usuario
    // @throws IOException Excepción en envío de mensaje

    /**
     * Evaluará una petición enviada por un usuario al bot
     *
     * @param peticion Datos de la petición del usuario
     * @throws IOException                  Excepción en envío de mensaje
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TransformerException
     */
    public static void procesarPeticion(Peticion peticion) throws IOException, ParserConfigurationException, SAXException, TransformerException {

        // Obtener datos del mensaje recibido

        long destinatario = 0;
        String texto = "";

//        TRY-CATCH PARA CONTROLAR QUE, AL EDITAR UN MENSAJE DESDE TELEGRAM, EL BOT NO DEJE DE FUNCIONAR
        try {
            destinatario = peticion.getMessage().getFrom().getId();
            texto = peticion.getMessage().getText();
        } catch (Exception exception) {
            System.out.println("Se ha detectado una edición en un mensaje previo.");
        }

//        SI EL MENSAJE RECIBIDO POR EL BOT ES UNA IMAGEN, UN AUDIO, UN GIF O UN STICKER DEVOLVERA EL SIGUIENTE TEXTO
        if (texto == null) {
            Envio envio = new Envio();
            envio.setChat_id(destinatario);
            envio.setText("Formato de mensaje no permitido.\nEscribe /help para ver las funcionalidades de este bot.");
            enviarMensaje(envio);
            offset = peticion.getUpdate_id() + 1;

//        SI EL MENSAJE RECIBIDO POR EL BOT ENCAJA CON EL PATRON /word palabra, DEVOLVERA UNA PALABRA QUE EMPIECE POR LA ULTIMA LETRA DE LA PALABRA ENVIADA POR EL USUARIO
        } else if (texto.matches("^/word .*$")) {

//        DIVIDIMOS EL MENSAJE CON UN SPLIT PARA SEPARAR EL COMANDO Y LA PALABRA INTRODUCIDA Y PODER DETERMINAR CUAL HA SIDO
            String[] word = texto.split(" ");
//        COGEMOS LA ULTIMA POSICION DE LA PALABRA PARA SABER CUAL ES LA LETRA POR LA QUE TERMINA
            String letra = word[1].substring(word[1].length() - 1);

//        SEGUN CUAL SEA ESA ULTIMA LETRA, EL BOT LEERA EL DOCUMENTO CORRESPONDIENTE
            if (letra.equals("a")) {
//        EN EL CASO DE LA a, QUE ES UN TXT, SE USARAN LAS FUNCIONES leerTXT Y enviarpalabraTXT_XML. Y ASI CON TODOS LOS DOCUMENTOS DE ESTE TIPO
                ArrayList<String> listaA = leerTXT("src\\txt\\a.txt");
                enviarPalabraTXT_XML(listaA, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("b")) {
//        EN EL CASO DE LA b, QUE ES UN XML, SE USARAN LAS FUNCIONES leerXML Y enviarpalabraTXT_XML. Y ASI CON TODOS LOS DOCUMENTOS DE ESTE TIPO
                ArrayList<String> listaB = leerXML("src\\xml\\b.xml");
                enviarPalabraTXT_XML(listaB, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("c")) {
//        EN EL CASO DE LA c, QUE ES UN JSON, SE USARAN LAS FUNCIONES leerJSON Y enviarpalabra. Y ASI CON TODOS LOS DOCUMENTOS DE ESTE TIPO
                ArrayList<Palabra> listaC = leerJSON("src\\json\\c.json");
                enviarPalabra(listaC, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("d")) {
                ArrayList<String> listaD = leerTXT("src\\txt\\d.txt");
                enviarPalabraTXT_XML(listaD, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("e")) {
                ArrayList<String> listaE = leerXML("src\\xml\\e.xml");
                enviarPalabraTXT_XML(listaE, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("f")) {
                ArrayList<Palabra> listaF = leerJSON("src\\json\\f.json");
                enviarPalabra(listaF, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("g")) {
                ArrayList<String> listaG = leerTXT("src\\txt\\g.txt");
                enviarPalabraTXT_XML(listaG, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("h")) {
                ArrayList<String> listaH = leerXML("src\\xml\\h.xml");
                enviarPalabraTXT_XML(listaH, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("i")) {
                ArrayList<Palabra> listaI = leerJSON("src\\json\\i.json");
                enviarPalabra(listaI, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("j")) {
                ArrayList<String> listaJ = leerTXT("src\\txt\\j.txt");
                enviarPalabraTXT_XML(listaJ, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("l")) {
                ArrayList<String> listaL = leerXML("src\\xml\\l.xml");
                enviarPalabraTXT_XML(listaL, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("m")) {
                ArrayList<Palabra> listaM = leerJSON("src\\json\\m.json");
                enviarPalabra(listaM, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("n")) {
                ArrayList<String> listaM = leerTXT("src\\txt\\n.txt");
                enviarPalabraTXT_XML(listaM, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("o")) {
                ArrayList<String> listaO = leerXML("src\\xml\\o.xml");
                enviarPalabraTXT_XML(listaO, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("p")) {
                ArrayList<Palabra> listaP = leerJSON("src\\json\\p.json");
                enviarPalabra(listaP, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("q")) {
                ArrayList<String> listaP = leerTXT("src\\txt\\q.txt");
                enviarPalabraTXT_XML(listaP, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("r")) {
                ArrayList<String> listaR = leerXML("src\\xml\\r.xml");
                enviarPalabraTXT_XML(listaR, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("s")) {
                ArrayList<Palabra> listaS = leerJSON("src\\json\\s.json");
                enviarPalabra(listaS, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("t")) {
                ArrayList<String> listaT = leerTXT("src\\txt\\t.txt");
                enviarPalabraTXT_XML(listaT, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("u")) {
                ArrayList<String> listaU = leerXML("src\\xml\\u.xml");
                enviarPalabraTXT_XML(listaU, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("v")) {
                ArrayList<Palabra> listaV = leerJSON("src\\json\\v.json");
                enviarPalabra(listaV, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("y")) {
                ArrayList<String> listaY = leerTXT("src\\txt\\y.txt");
                enviarPalabraTXT_XML(listaY, destinatario);
                offset = peticion.getUpdate_id() + 1;

            } else if (letra.equals("z")) {
                ArrayList<String> listaZ = leerXML("src\\xml\\z.xml");
                enviarPalabraTXT_XML(listaZ, destinatario);
                offset = peticion.getUpdate_id() + 1;

//        SI LA LETRA NO ESTA CONTEMPLADA POR EL BOT, ESTE DEVOLVERA EL SIGUIENTE MENSAJE
            } else {
                Envio envio = new Envio();
                envio.setChat_id(destinatario);
                envio.setText("La letra '" + letra + "' no está contemplada por el bot.");
                enviarMensaje(envio);
                offset = peticion.getUpdate_id() + 1;
            }

//        SI EL MENSAJE RECIBIDO POR EL BOT ENCAJA CON EL PATRON /addword palabra -caracter, SE ALMACENARA LA PALABRA INDICADA EN LA LISTA CORRESPONDIENTE AL CARACTER INTRODUCIDO
        } else if (texto.matches("^/addword .* -.$")) {

//        DIVIDIMOS EL MENSAJE RECIBIDO PARA SACAR LA PALABRA Y EL CARACTER
            String[] nueva = texto.split(" ");

//        EN CASO DE QUE SE INTRODUZCAN VARIOS ESPACIOS EN EL MENSAJE, EL BOT CONTROLARA ESA EXCEPCION Y DEVOLVERA UN ERROR,
//        YA QUE DESDE LA PRIMERA DIVISION EN ADELANTE, EL MENSAJE SE CONSIDERARA VACIO
            if (nueva[1].isEmpty()) {
                Envio envio = new Envio();
                envio.setChat_id(destinatario);
                envio.setText("No se reconoce el comando.\nEscribe /help para ver la lista de comandos a los que responde este bot.");
                enviarMensaje(envio);
                offset = peticion.getUpdate_id() + 1;
            } else {

//        EL STRING nuevaPalabra CONTENDRA LA PALABRA INTRODUCIDA y nuevaLetra EL CARACTER
                String nuevaPalabra = nueva[1];
                String nuevaLetra = String.valueOf(nueva[2].charAt(1));

//        EL STRING letra CONTENDRA LA PRIMERA LETRA DE LA PALABRA PARA COMPROBAR QUE COINCIDA CON EL CARACTER
                String letra = String.valueOf(nueva[1].charAt(0));

                if (letra.matches(nuevaLetra)) {

//        SI letra y nuevaLetra COINCIDEN, SE USARAN LAS FUNCIONES addPalabraTXT, addPalabraXML O addPalabra, DEPENDIENDO DE SI EL DOCUMENTO ES UN TXT, UN XML O UN JSON
//        nuevaPalabra SE AÑADIRA AL LISTADO CORRESPONDIENTE
                    if (nuevaLetra.matches("a")) {
                        addPalabraTXT("src\\txt\\a.txt", nuevaPalabra);
                    } else if (nuevaLetra.matches("b")) {
                        addPalabraXML("src\\xml\\b.xml", nuevaPalabra);
                    } else if (nuevaLetra.matches("c")) {
                        addPalabra("src\\json\\c.json", nuevaPalabra);
                    } else if (nuevaLetra.matches("d")) {
                        addPalabraTXT("src\\txt\\d.txt", nuevaPalabra);
                    } else if (nuevaLetra.matches("e")) {
                        addPalabraXML("src\\xml\\e.xml", nuevaPalabra);
                    } else if (nuevaLetra.matches("f")) {
                        addPalabra("src\\json\\f.json", nuevaPalabra);
                    } else if (nuevaLetra.matches("g")) {
                        addPalabraTXT("src\\txt\\g.txt", nuevaPalabra);
                    } else if (nuevaLetra.matches("h")) {
                        addPalabraXML("src\\xml\\h.xml", nuevaPalabra);
                    } else if (nuevaLetra.matches("i")) {
                        addPalabra("src\\json\\i.json", nuevaPalabra);
                    } else if (nuevaLetra.matches("j")) {
                        addPalabraTXT("src\\txt\\j.txt", nuevaPalabra);
                    } else if (nuevaLetra.matches("l")) {
                        addPalabraXML("src\\xml\\l.xml", nuevaPalabra);
                    } else if (nuevaLetra.matches("m")) {
                        addPalabra("src\\json\\m.json", nuevaPalabra);
                    } else if (nuevaLetra.matches("n")) {
                        addPalabraTXT("src\\txt\\n.txt", nuevaPalabra);
                    } else if (nuevaLetra.matches("o")) {
                        addPalabraXML("src\\xml\\o.xml", nuevaPalabra);
                    } else if (nuevaLetra.matches("p")) {
                        addPalabra("src\\json\\p.json", nuevaPalabra);
                    } else if (nuevaLetra.matches("q")) {
                        addPalabraTXT("src\\txt\\q.txt", nuevaPalabra);
                    } else if (nuevaLetra.matches("r")) {
                        addPalabraXML("src\\xml\\r.xml", nuevaPalabra);
                    } else if (nuevaLetra.matches("s")) {
                        addPalabra("src\\json\\s.json", nuevaPalabra);
                    } else if (nuevaLetra.matches("t")) {
                        addPalabraTXT("src\\txt\\t.txt", nuevaPalabra);
                    } else if (nuevaLetra.matches("u")) {
                        addPalabraXML("src\\xml\\u.xml", nuevaPalabra);
                    } else if (nuevaLetra.matches("v")) {
                        addPalabra("src\\json\\v.json", nuevaPalabra);
                    } else if (nuevaLetra.matches("y")) {
                        addPalabraTXT("src\\txt\\y.json", nuevaPalabra);
                    } else if (nuevaLetra.matches("z")) {
                        addPalabraXML("src\\xml\\z.xml", nuevaPalabra);
                    }

                    Envio envio = new Envio();
                    envio.setChat_id(destinatario);
                    envio.setText("Palabra " + nuevaPalabra + " añadida.");
                    enviarMensaje(envio);
                    offset = peticion.getUpdate_id() + 1;

//        SI letra y nuevaLetra NO COINCIDEN, SE LE DEVOLVERA UN MENSAJE AL USUARIO Y NO SE AÑADIRA LA PALABRA
                } else {
                    Envio envio = new Envio();
                    envio.setChat_id(destinatario);
                    envio.setText("La palabra introducida no comienza por la letra indicada.");

                    enviarMensaje(envio);
                    offset = peticion.getUpdate_id() + 1;
                }
            }
//        SI EL USUARIO ESCRIBE EL COMANDO /help SE LE INFORMARA DEL FUNCIONAMIENTO DEL BOT
        } else if (texto.equals("/help")) {
            Envio envio = new Envio();
            envio.setChat_id(destinatario);
            envio.setText("Lista de comandos:\n/word *palabra*: el bot devuelve una palabra que empieza por la letra por la que termina la palabra introducida por el usuario." +
                    "\n/addword *palabra* -*letra*: el bot añade la palabra introducida (palabra) a la lista de palabras indicada (letra)." +
                    "\n\nEjemplos:\n/word sabor\n/addword sabor -s" +
                    "\n\nNota: el bot no devolverá palabras que empiecen por K, Ñ, W y X.");
            enviarMensaje(envio);
            offset = peticion.getUpdate_id() + 1;

//        RESPUESTA AL COMANDO /start, GENERADO POR TELEGRAM AL ABRIR CHAT CON UN BOT
        } else if (texto.equals("/start")) {
            Envio envio = new Envio();
            envio.setChat_id(destinatario);
            envio.setText("Escribe /help para ver las funcionalidades de este bot.");
            enviarMensaje(envio);
            offset = peticion.getUpdate_id() + 1;

//        SI EL MENSAJE INTRODUCIDO POR EL USUARIO NO ENCAJA CON NINGUNO DE LOS COMANDOS ESPECIFICADOS, EL BOT DEVOLVERA EL SIGUIENTE MENSAJE
        } else {
            Envio envio = new Envio();
            envio.setChat_id(destinatario);
            envio.setText("No se reconoce el comando.\nEscribe /help para ver la lista de comandos a los que responde este bot.");
            enviarMensaje(envio);
            offset = peticion.getUpdate_id() + 1;

        }
    }

    // Método que enviará un mensaje a un usuario
    // @param envio Datos de destinatario del mensaje y mensaje a enviar
    // @throws IOException Excepción en envío de mensaje

    /**
     * Enviará un mensaje a un usuario
     *
     * @param envio Datos de destinatario del mensaje y mensaje a enviar
     * @throws IOException Excepción en envío de mensaje
     */
    public static void enviarMensaje(Envio envio) throws IOException {

        // Crear petición HTTP
        String apiUrl = API_TELEGRAM + BOT_TOKEN + "/sendMessage";
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(METHOD_POST);
        conn.setRequestProperty(PROPERTY_CONTENT_TYPE, PROPERTY_APPLICATION_JSON);
        conn.setDoOutput(true);

        // Utilizar Gson para convertir el objeto JSON en una cadena
        String jsonMessage = new Gson().toJson(envio);

        // Crear un OutputStreamWriter para escribir en el OutputStream que se ligará a la conexión
        try (OutputStream os = conn.getOutputStream(); OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            osw.write(jsonMessage);
            osw.flush();
        }

        // Enviar peticion HTTP
        int responseCode = conn.getResponseCode();

        // Evaluar respuesta petición HTTP
        if (responseCode == HTTP_CODE_OK) {
            System.out.println(LOG_ENVIO_CORRECTO);
        } else {
            System.out.println(LOG_ERROR_SOLICITUD + responseCode);
        }
    }

    // Leer TXT (usando BUFFEREDREADER)

    /**
     * Lee los ficheros .txt
     *
     * @param ruta Ruta del fichero a leer
     * @return ArrayList de strings con los datos del fichero leído
     * @throws IOException
     */
    public static ArrayList<String> leerTXT(String ruta) throws IOException {
        ArrayList<String> palabraTXT = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            while (br.ready()) {
                palabraTXT.add(br.readLine());
            }
        }
        return palabraTXT;
    }

    // Escribir TXT (usando FILEOUTPUTSTREAM)

    /**
     * Edita los ficheros .txt
     *
     * @param ruta         Ruta del fichero a editar
     * @param nuevaPalabra Palabra introducida por el usuario
     * @throws IOException
     */
    public static void addPalabraTXT(String ruta, String nuevaPalabra) throws IOException {
        File file = new File(ruta);
        FileOutputStream fOut = new FileOutputStream(file, true);
        OutputStreamWriter osw = new OutputStreamWriter(fOut);
        osw.write("\n" + nuevaPalabra);
        osw.flush();
        osw.close();
    }

    // Enviar palabra TXT
    // Enviar palabra XML

    /**
     * El bot envía una palabra de un fichero .txt o .xml
     *
     * @param arrayList    Lista de palabras recogidas al leer un TXT o un XML
     * @param destinatario ID del usuario que está interactuando con el bot
     * @throws IOException
     */
    public static void enviarPalabraTXT_XML(ArrayList<String> arrayList, long destinatario) throws IOException {
        Envio envio = new Envio();
        envio.setChat_id(destinatario);
        int random = (int) Math.floor(Math.random() * ((arrayList.size() - 1) - 0 + 1) + 0);
        envio.setText(arrayList.get(random));
        enviarMensaje(envio);
    }

    // Leer XML (usando SAX)

    /**
     * Lee los ficheros .xml
     *
     * @param ruta Ruta del fichero a leer
     * @return ArrayList de strings con los datos del fichero leído
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static ArrayList<String> leerXML(String ruta) throws
            IOException, ParserConfigurationException, SAXException {
        ArrayList<PalabraXML> coleccionPalabras = new ArrayList<PalabraXML>();
        ArrayList<String> arrayPalabras = new ArrayList<>();

        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        DocumentBuilder creadorDocumento = fabrica.newDocumentBuilder();
        Document documento = creadorDocumento.parse(ruta);

        Element raiz = documento.getDocumentElement();
        NodeList listaPalabras = raiz.getElementsByTagName("palabra");

        for (int i = 0; i < listaPalabras.getLength(); i++) {
            Node palabra = listaPalabras.item(i);

            coleccionPalabras.add(new PalabraXML());

            NodeList datosPalabra = palabra.getChildNodes();

            for (int j = 0; j < datosPalabra.getLength(); j++) {
                Node dato = datosPalabra.item(j);

                if (dato.getNodeType() == Node.ELEMENT_NODE) {

                    Node datoContenido = dato.getFirstChild();

                    if (datoContenido != null && datoContenido.getNodeType() == Node.TEXT_NODE) {
                        arrayPalabras.add(datoContenido.getNodeValue());

                        if (dato.getNodeName().equals("word")) {
                            coleccionPalabras.get(i).setWord(dato.getTextContent());
                        }
                    }
                }
            }
        }
        return arrayPalabras;
    }

    // Escribir XML (usando DOM)

    /**
     * Edita los ficheros .xml
     *
     * @param ruta         Ruta del fichero a escribir
     * @param nuevaPalabra Palabra introducida por el usuario
     * @throws TransformerException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static void addPalabraXML(String ruta, String nuevaPalabra) throws TransformerException, ParserConfigurationException, IOException, SAXException {

        ArrayList<PalabraXML> coleccionPalabras = new ArrayList<PalabraXML>();

        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();

        DocumentBuilder creadorDocumento = fabrica.newDocumentBuilder();

        Document documento = creadorDocumento.parse(ruta);

        Element raiz = documento.getDocumentElement();

        NodeList listaPalabras = raiz.getElementsByTagName("palabra");


        for (int i = 0; i < listaPalabras.getLength(); i++) {
            Node palabra = listaPalabras.item(i);

            coleccionPalabras.add(new PalabraXML());

            NodeList datosPalabra = palabra.getChildNodes();

            for (int j = 0; j < datosPalabra.getLength(); j++) {
                Node dato = datosPalabra.item(j);

                if (dato.getNodeType() == Node.ELEMENT_NODE) {

                    Node datoContenido = dato.getFirstChild();

                    if (datoContenido != null && datoContenido.getNodeType() == Node.TEXT_NODE) {

                        if (dato.getNodeName().equals("word")) {
                            coleccionPalabras.get(i).setWord(dato.getTextContent());
                        }
                    }
                } else {
                    Element e = (Element) palabra;
                    String word = e.getElementsByTagName("word").toString();

                    coleccionPalabras.get(i).setWord(word);
                }
            }
            System.out.println();

        }

        Element ePalabra = documento.createElement("palabra");
        raiz.appendChild(ePalabra);

        Element eWord = documento.createElement("word");
        ePalabra.appendChild(eWord);
        eWord.appendChild(documento.createTextNode(nuevaPalabra));

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
        DOMSource domSource = new DOMSource(documento);
        StreamResult resultado = new StreamResult((new File(ruta)));
        transformer.transform(domSource, resultado);
    }


    // Leer JSON

    /**
     * Lee los ficheros .json
     *
     * @param ruta Ruta del fichero a leer
     * @return ArrayList de strings con los datos del fichero leído
     * @throws FileNotFoundException
     */
    public static ArrayList<Palabra> leerJSON(String ruta) throws FileNotFoundException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        FileReader fr = new FileReader(ruta);
        Palabra[] palabras = gson.fromJson(fr, Palabra[].class);
        return new ArrayList<Palabra>(Arrays.asList(palabras));
    }

    // Escribir JSON

    /**
     * Edita los ficheros .json
     *
     * @param listaPalabras Lista de palabras recogidas al leer un JSON
     * @return String con los datos del fichero leído
     * @throws FileNotFoundException
     */
    public static String escribirJSON(ArrayList<Palabra> listaPalabras) throws FileNotFoundException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
        String json = gson.toJson(listaPalabras);
        return json;
    }

    // Enviar palabra JSON

    /**
     * El bot envía una palabra de un fichero .json
     *
     * @param arrayList    Lista de palabras recogidas al leer un JSON
     * @param destinatario ID del usuario que está interactuando con el bot
     * @throws IOException
     */
    public static void enviarPalabra(ArrayList<Palabra> arrayList, long destinatario) throws IOException {
        Envio envio = new Envio();
        envio.setChat_id(destinatario);
        int random = (int) Math.floor(Math.random() * ((arrayList.size() - 1) - 0 + 1) + 0);
        envio.setText(arrayList.get(random).getPalabra());
        enviarMensaje(envio);
    }

    // Añadir palabra nueva JSON

    /**
     * Añade una palabra a un fichero .json
     *
     * @param ruta         Ruta del fichero a leer
     * @param nuevaPalabra Palabra introducida por el usuario
     * @throws IOException
     */
    public static void addPalabra(String ruta, String nuevaPalabra) throws IOException {

        ArrayList<Palabra> listaPalabras = leerJSON(ruta);
        listaPalabras.add(new Palabra(nuevaPalabra));

        File ficheroNew = new File(ruta);
        ficheroNew.createNewFile();

        FileWriter fw = new FileWriter(ficheroNew);
        fw.write(escribirJSON(listaPalabras));
        fw.close();
    }
}