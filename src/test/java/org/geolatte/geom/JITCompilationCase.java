package org.geolatte.geom;

/**
 * @author Karel Maesen, Geovise BVBA, 2012
 */
public class JITCompilationCase {

    public static void main(String[] args){

        PointVisitor visitor = new PointVisitor() {
                double len = 0;

                @Override
                public void visit(double[] coordinates) {
                    len += Math.hypot(coordinates[0], coordinates[1]);
                }

                public double report() {
                    return len;
                }
            };

        for (int i = 0; i < 3000; i++ ){
            PointSequence pointSequence = buildPointSequence();
            pointSequence.accept(visitor);
        }
        try {Thread.sleep(2000);} catch(InterruptedException e) {}
        System.out.println("Sleeping (accept compiled?)....");
        for (int i = 0; i < 1000; i++ ){
            PointSequence pointSequence = buildPointSequence();
            pointSequence.accept(visitor);
        }

    }

    public static PointSequence buildPointSequence(){
        PointSequenceBuilder builder = PointSequenceBuilders.fixedSized(10, DimensionalFlag.XY);
        for (int i = 0; i < 10; i++) {
            builder.add(Math.random(), Math.random());
        }
        return builder.toPointSequence();
    }

}
