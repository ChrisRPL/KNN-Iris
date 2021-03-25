package com.company;

import java.io.IOException;
public class Main {

    public static void main(String[] args) throws IOException {
        DataReader dataReader = new DataReader(args[0], args[1]);
        DataNormalizer dataNormalizer = new DataNormalizer();
        KNN knn = new KNN(dataReader, dataNormalizer, args[2]);

        knn.run();
        knn.predictObservations(dataNormalizer.getMaxAttributesValues(), dataNormalizer.getMinAttributesValues());
    }
}
