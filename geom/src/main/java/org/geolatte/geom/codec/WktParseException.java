package org.geolatte.geom.codec;

public class WktParseException extends RuntimeException{

    public WktParseException(String msg){
        super(msg);
    }

    public WktParseException(String msg, Throwable t) {
        super(msg, t);
    }
}
