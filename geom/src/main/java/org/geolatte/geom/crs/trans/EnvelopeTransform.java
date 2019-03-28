package org.geolatte.geom.crs.trans;

import org.geolatte.geom.Envelope;
import org.geolatte.geom.Position;
import org.geolatte.geom.crs.CoordinateReferenceSystem;

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-03-28.
 */
public class EnvelopeTransform<P extends Position, Q extends Position> {

    final private TransformOperation<P,Q> operation;

    public EnvelopeTransform(TransformOperation<P, Q> operation) {
        this.operation = operation;

    }

    public Envelope<Q> forward(Envelope<P> envelope) {
        P ll = envelope.lowerLeft();
        P ur = envelope.upperRight();
        Q tll = operation.forward(ll);
        Q tur = operation.forward(ur);
        return new Envelope<>(tll, tur, operation.getTarget());
    }

    public Envelope<P> reverse(Envelope<Q> envelope) {
        Q ll = envelope.lowerLeft();
        Q ur = envelope.upperRight();
        P tll = operation.reverse(ll);
        P tur = operation.reverse(ur);
        return new Envelope<>(tll, tur, operation.getSource());
    }

}
