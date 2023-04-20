/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gt.edu.umg.automatas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author Desarrollo
 */
public class AnalizadorHTML {

    private JTextArea txtArea;
    private Integer index;
    private File file;
    private List<StringBuilder> lines;
    //private StringBuilder stream;
    private boolean existDOCTYPEtag;
    private boolean existHTMLtag;
    private boolean existHEADtag;
    private boolean existBODYtag;
    private DocumentObjectModel dom;
    private enum CASE_ERR {
           SINTAXIS, CIERRE, INVALID_TAG
    }

    public AnalizadorHTML(File file) {
        this.file = file;
        this.lines = new ArrayList<>();
        //this.stream = new StringBuilder();
        String text = "";
        try {
            FileReader fileReader = new FileReader(file);
            try (BufferedReader bufferRead = new BufferedReader(fileReader)) {
                while ((text = bufferRead.readLine()) != null) {
                    //lines.add(text);
                    text = text.replaceAll("\t", "").replaceAll("\n", "");
                    text = parseLine(text, 0);
                    //stream.append(text);
                    lines.add(new StringBuilder(text));

                }
            }
            fileReader.close();
            fileReader = null;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AnalizadorHTML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AnalizadorHTML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean analizar(JTextArea txtArea) {
        this.txtArea = txtArea;
        this.txtArea.setText("");
        for (int i = 0; i < lines.size(); i++) {
            StringBuilder stream = lines.get(i);
            for (index = 0; index < stream.length();) {
                String token = "";
                if (stream.charAt(index) == '<') {
                    index++;
                    char caracter = stream.charAt(index);
                    switch (caracter) {
                        case '!':
                            index++;
                            token = getToken(stream);
                            if (isDOCTYPETag(token)) {
                                if (existDOCTYPEtag) {
                                    escribirError(token, i, CASE_ERR.INVALID_TAG);
                                    return false;
                                }
                                if (!validaTag(stream)) {
                                    escribirError(token, i, CASE_ERR.SINTAXIS);
                                    return false;
                                }
                                existDOCTYPEtag = true;
                            }
                            break;
                        case '/':
                            index++;
                            token = getToken(stream);
                            if (!isToken(token)) {
                                escribirError(token, i, CASE_ERR.INVALID_TAG);
                                return false;
                            }
                            if (!validaTag(stream)) {
                                escribirError(token, i, CASE_ERR.INVALID_TAG);
                                return false;
                            }
                            if (!isMETADATATag(token) && !eliminarDOM(token)) {
                                escribirError(token, i, CASE_ERR.CIERRE);
                                return false;
                            }
                            break;
                        case ' ':
                            escribirError(token, i, CASE_ERR.SINTAXIS);
                            return false;
                        default:
                            if (!Character.isAlphabetic(caracter)) {
                                escribirError(token, i, CASE_ERR.SINTAXIS);
                                return false;
                            }

                            token = getToken(stream);
                            if (isHTMLTag(token)) {
                                if (existHTMLtag) {
                                    escribirError(token, i, CASE_ERR.INVALID_TAG);
                                    return false;
                                }
                                if (!validaTag(stream)) {
                                    escribirError(token, i, CASE_ERR.SINTAXIS);
                                    return false;
                                }
                                existHTMLtag = true;
                            } else if (isHEADTag(token)) {
                                if (existHEADtag) {
                                    escribirError(token, i, CASE_ERR.INVALID_TAG);
                                    return false;
                                }
                                if (!validaTag(stream)) {
                                    escribirError(token, i, CASE_ERR.SINTAXIS);
                                    return false;
                                }
                                existHEADtag = true;
                            } else if (isBODYTag(token)) {
                                if (existBODYtag) {
                                    escribirError(token, i, CASE_ERR.INVALID_TAG);
                                    return false;
                                }
                                if (!validaTag(stream)) {
                                    escribirError(token, i, CASE_ERR.SINTAXIS);
                                    return false;
                                }
                                existBODYtag = true;
                            } else if (isMETADATATag(token)) {
                                if (!validaTag(stream)) {
                                    escribirError(token, i, CASE_ERR.SINTAXIS);
                                    return false;
                                }
                            } else if (!isToken(token)) {
                                escribirError(token, i, CASE_ERR.INVALID_TAG);
                                return false;
                            }

                            if (!validaTag(stream)) {
                                escribirError(token, i, CASE_ERR.SINTAXIS);
                                return false;
                            }

                            if (!isDOCTYPETag(token) && !isMETADATATag(token)) {
                                //agregar nodo
                                agregarDOM(token);
                            }
                    }
                }
                index++;
            }
        }

        return true;
    }

    private void escribirError(String token, int index, CASE_ERR cerr) {
        String text = "\n----------INICIO----------\n\nEl archivo presenta el siguiente error, con las siguientes etiquetas: \n";                

        switch(cerr) {
            case CIERRE:
                text+="Valor Esperado: </"+dom.getValue()+"> \n";
                text+="Valor Encontrado: </"+token+"> \n";
                break;
            case INVALID_TAG:
                text+="Valor Encontrado TAG NO EXISTE O YA SE ENCUENTRA EN EL DOCUMENTO: "+token+" \n";
                break;
            case SINTAXIS:
                text+="Valor Encontrado TAG INVALIDO (CARACTERES NO PERMITIDOS): "+token+" \n";
                break;                
        }
        text+="\nError en la linea ("+(index+1)+"): "+lines.get(index)+"\n ";        
        text += "\n----------  FIN  ----------";
        txtArea.setText(text);
    }

    private void agregarDOM(String value) {
        if (dom == null) {
            dom = new DocumentObjectModel(value);
        } else {
            DocumentObjectModel child = new DocumentObjectModel(value);
            child.setParent(dom);
            dom.setChild(child);
            dom = child;
        }
    }

    private boolean eliminarDOM(String value) {
        if (dom.getValue().equalsIgnoreCase(value)) {
            dom = dom.getParent();
            return true;
        }

        return false;
    }

    private boolean validaTag(StringBuilder stream) {
        if (index < stream.length()) {
            while (stream.charAt(index) != '>') {
                if (index < stream.length()) {
                    char current = stream.charAt(index);
                    if (!Character.isAlphabetic(current)) {
                        boolean encontrado = false;
                        for (int i = 0; i < VALID_CHR.length; i++) {
                            char chrt = VALID_CHR[i];
                            if (chrt == current) {
                                encontrado = true;
                                break;
                            }
                        }

                        if (!encontrado) {
                            return false;
                        }
                    }
                    index++;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    private String getToken(StringBuilder stream) {
        String token = "";

        while (index < stream.length() && stream.charAt(index) != ' ' && stream.charAt(index) != '>') {
            token += stream.charAt(index);
            index++;
        }

        return token.trim();
    }

    private boolean isToken(String line) {
        for (String token : TAGS_HTML) {
            if (line.equalsIgnoreCase(token)) {
                return true;
            }
        }

        return false;
    }

    private boolean isDOCTYPETag(String line) {
        return line.equalsIgnoreCase(DOCTYPE);
    }

    private boolean isHTMLTag(String line) {
        return line.equalsIgnoreCase(MAIN_ROOT);
    }

    private boolean isBODYTag(String line) {
        return line.equalsIgnoreCase(BODY);
    }

    private boolean isHEADTag(String line) {
        return line.equalsIgnoreCase(HEAD);
    }

    private boolean isMETADATATag(String line) {
        for (String METADATA1 : METADATA) {
            if (METADATA1.equalsIgnoreCase(line)) {
                return true;
            }
        }

        return false;
    }

    private String parseLine(String line, int start) {
        StringBuilder text = new StringBuilder(line);
        text = new StringBuilder(trimLeft(text, start));
        text = new StringBuilder(trimRight(text, start));

        return text.toString();
    }

    private String trimLeft(StringBuilder text, int start) {

        int index = text.indexOf(LESS_THAN, start);

        if (index != -1) {
            for (int i = (index - 1); i >= 0;) {
                if (text.charAt(i) == ' ') {
                    text.deleteCharAt(i);
                }
                i--;

                if (i >= 0 && text.charAt(i) != ' ') {
                    break;
                }
            }
        }

        index = text.indexOf(LESS_THAN, start);
        index++;
        if (index < text.length() && text.indexOf(LESS_THAN, index) != -1) {
            return trimLeft(text, index);
        }

        return text.toString();
    }

    private String trimRight(StringBuilder text, int start) {

        int index1 = text.indexOf(MORE_THAN, start);

        if (index1 != -1) {
            for (int i = (index1 + 1); i < text.length();) {
                if (text.charAt(i) == ' ') {
                    text.deleteCharAt(i);
                }

                if (i < text.length() && text.charAt(i) != ' ') {
                    break;
                }
            }
        }

        index1 = text.indexOf(MORE_THAN, start);
        index1++;
        if (index1 < text.length() && text.indexOf(MORE_THAN, index1) != -1) {
            return trimRight(text, index1);
        }

        return text.toString();
    }

    private final String SLASH = "/";
    private final String MORE_THAN = ">";
    private final String LESS_THAN = "<";
    private final String DOCTYPE = "DOCTYPE";
    private final String MAIN_ROOT = "html";
    private final String HEAD = "head";
    private final String BODY = "body";
    private final String[] METADATA = {"link", "meta", "style", "title", "script", "br", "hr"};
    private final String[] TAGS_HTML = {"html", "body", "head", "address", "title",
        "article", "aside", "footer", "h1", "h2", "h3", "h4", "h5", "h6", "header", "hgroup", "nav", "sectioning", "blockquote",
        "dd", "dl", "div", "dl", "dt", "figcaption", "figure", "li", "main", "ol", "p", "pre", "ul", "a", "abbr", "b", "bdi",
        "bdo", "cite", "code", "data", "dfn", "em", "i", "kbd", "mark", "q", "rp", "rt", "small", "span", "strong", "sub", "sup",
        "u", "var", "area", "audio", "img", "map", "video", "script", "caption", "col", "colgroup", "table", "tbody", "td", "tfoot",
        "th", "thead", "tr", "button", "datalist", "fieldset", "form", "input", "label", "legend", "meter", "option", "output", "progress",
        "select", "textarea", "frame"};
    private final char[] VALID_CHR = {'<', '>', '/', '=', '"', '\'', ' ', '-', ',', '.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
}
