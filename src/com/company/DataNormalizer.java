package com.company;

import com.company.models.Observation;

import java.util.Vector;

public class DataNormalizer {

    private Vector<Double> maxAttributesValues = new Vector<>();
    private Vector<Double> minAttributesValues = new Vector<>();

    public Vector<Double> getMaxAttributesValues() {
        return maxAttributesValues;
    }

    public Vector<Double> getMinAttributesValues() {
        return minAttributesValues;
    }

    public Vector<Observation> normalizeData(Vector<Observation> data){
        Vector<Vector<Double>> matrixData = new Vector<>();
        for (Observation observation : data)
            matrixData.add(observation.getConditionalAttributes());

        matrixData = transposeMatrix(matrixData);

        for (Vector<Double> matrixDatum : matrixData) {
            double max = matrixDatum.stream().max(Double::compareTo).get(), min = matrixDatum.stream().min(Double::compareTo).get();
            getMaxAttributesValues().add(max);
            getMinAttributesValues().add(min);
            for (int j = 0; j < matrixDatum.size(); j++) {
                matrixDatum.set(j, (matrixDatum.get(j) - min) / (max - min));
            }
        }

        matrixData = transposeMatrix(matrixData);

        for (int i=0; i<data.size(); i++)
            data.get(i).setConditionalAttributes(matrixData.get(i));

        return data;
    }

    private Vector<Vector<Double>> transposeMatrix(Vector<Vector<Double>> data){
        double[][] matrixData = new double[data.get(0).size()][data.size()];

        for (int i=0; i<data.size(); i++){
            for (int j=0; j<data.get(i).size(); j++){
                matrixData[j][i] = data.get(i).get(j);
            }
        }

        Vector<Vector<Double>> transposedMatrix = new Vector<>();

        for (double[] matrixDatum : matrixData) {
            Vector<Double> row = new Vector<>();

            for (double v : matrixDatum) row.add(v);

            transposedMatrix.add(row);
        }

        return transposedMatrix;
    }
}
