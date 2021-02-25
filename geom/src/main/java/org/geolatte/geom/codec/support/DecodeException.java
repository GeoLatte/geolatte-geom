package org.geolatte.geom.codec.support;

public class DecodeException extends RuntimeException{

    public DecodeException(String msg){
        super(msg);
    }

    public DecodeException(String msg, Throwable t){
        super(msg, t);
    }
}
