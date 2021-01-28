package org.geolatte.geom.codec.support;

//TODO -- this is now a checked exception because in the GeoJson module
// it will need to be adapted to JsonProcessingException
public class DecodeException extends Exception{

    public DecodeException(String msg){
        super(msg);
    }

    public DecodeException(String msg, Throwable t){
        super(msg, t);
    }
}
