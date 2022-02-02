package org.geolatte.geom;

public class LLAPositionVisitors {

    static public <P extends Position> LLAPositionVisitor mkCombiningVisitor(PositionSequenceBuilder<P> builder){
        return new CombiningVisitor<>(builder);
    }
}

class CombiningVisitor<P extends Position> implements LLAPositionVisitor {

    final PositionSequenceBuilder<P> builder;

    CombiningVisitor(PositionSequenceBuilder<P> builder) {
        this.builder = builder;
    }

    /**
     * The visit method that is executed for each coordinate.
     *
     * @param coordinate the visited coordinate in array representation
     */
    @Override
    public void visit(double[] coordinate) {
        this.builder.add(coordinate);
    }

    PositionSequence<P> result() {
        return builder.toPositionSequence();
    }
}