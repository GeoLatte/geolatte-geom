package org.geolatte.geom.crs;

import org.geolatte.geom.G3D;
import org.geolatte.geom.G3DM;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class SerializableTest {

    @Test
    public void testCastToSerializable() throws IOException, ClassNotFoundException {
        CoordinateReferenceSystem<G3DM> crs = CoordinateReferenceSystems.WGS84
                .addVerticalSystem(LinearUnit.METER,G3D.class)
                .addLinearSystem(LinearUnit.METER, G3DM.class);

        Serializable ser = (Serializable) crs;
        File tempFile = Files.createTempFile("", ".ser").toFile();
        tempFile.deleteOnExit();
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tempFile))){
            out.writeObject(crs);
        };

        CoordinateReferenceSystem<G3DM> deser;
        try (ObjectInputStream ins = new ObjectInputStream(new FileInputStream(tempFile))) {
            deser = (CoordinateReferenceSystem<G3DM>) ins.readObject();
        }

        assertEquals(crs, deser);

    }


}
