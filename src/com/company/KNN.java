package com.company;

import com.company.models.Observation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class KNN {
    private Vector<Observation> trainingData;
    private Vector<Observation> testingData;
    private int bestKValue;
    private Double bestEfficiency;
    private DataReader dataReader;
    private DataNormalizer dataNormalizer;
    private String outputExcelDataPath;


    public KNN(DataReader dataReader, DataNormalizer dataNormalizer, String outputExcelDataPath) throws IOException {

        this.dataReader = dataReader;
        this.dataNormalizer = dataNormalizer;

        this.outputExcelDataPath = outputExcelDataPath;

        //IMPORT DATA
        trainingData = dataReader.getModelData(DataReaderType.TRAINING_DATA_TYPE);
        testingData = dataReader.getModelData(DataReaderType.TEST_DATA_TYPE);

        //NORMALIZE DATA
        trainingData = dataNormalizer.normalizeData(trainingData);
        testingData = dataNormalizer.normalizeData(testingData);
    }

    private Map<Observation, Double> getEuclideanDistances(Observation observation) {
        Vector<Observation> trainingData = new Vector<>(this.trainingData);

        Map<Observation, Double> euclideanDistances = new HashMap<>();
        for (Observation trainingObservation : trainingData){
            double euclideanDistance = 0.0;
            for (int i=0; i<trainingObservation.getConditionalAttributes().size(); i++){
                euclideanDistance+=Math.pow(trainingObservation.getConditionalAttributes().get(i) - observation.getConditionalAttributes().get(i), 2);
            }
            euclideanDistance = Math.sqrt(euclideanDistance);

            euclideanDistances.put(trainingObservation, euclideanDistance);
        }

        return euclideanDistances;
    }

    private Vector<Observation> getKNN(Observation observation, int k) {
        Vector<Observation> KNN = new Vector<>();

        Map<Observation, Double> euclideanDistances = getEuclideanDistances(observation).entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        List<Observation> sortedObservationsByDistance = new ArrayList<>(euclideanDistances.keySet());
        for (int i=0; i<k; i++)
            KNN.add(sortedObservationsByDistance.get(i));

        return KNN;
    }

    private Vector<Double> evaluateGoodExamplesAndEfficiency(int k) {
        double goodExamples = 0.0, efficiency;

        for (Observation observation : testingData){
            String evaluateDecisionAttribute = evaluateDecisionAttributeForVector(observation, k);

            if (evaluateDecisionAttribute.equals(observation.getDecisionAttribute()))
                goodExamples++;
        }

        efficiency = (goodExamples / testingData.size())*100;

        Vector<Double> goodExamplesAndEfficiency = new Vector<>();
        goodExamplesAndEfficiency.add(goodExamples);
        goodExamplesAndEfficiency.add(efficiency);

        return goodExamplesAndEfficiency;
    }

    private String evaluateDecisionAttributeForVector(Observation observation, int k) {
        Vector<Observation> knn = getKNN(observation, k);
        Map<String, Integer> decisionAttributes = new HashMap<>();
        for (Observation knnObservation : knn){
            if (decisionAttributes.containsKey(knnObservation.getDecisionAttribute()))
                decisionAttributes.replace(knnObservation.getDecisionAttribute(), decisionAttributes.get(knnObservation.getDecisionAttribute()) + 1);
            else
                decisionAttributes.put(knnObservation.getDecisionAttribute(), 1);
        }

        Integer maxAttribute = decisionAttributes.values().stream().max(Integer::compareTo).get();
        String evaluateDecisionAttribute = "";
        for (Map.Entry<String, Integer> attribute : decisionAttributes.entrySet()){
            if (attribute.getValue().equals(maxAttribute)) {
                evaluateDecisionAttribute = attribute.getKey();
            }
        }

        return evaluateDecisionAttribute;
    }

    private void exportExcelData() throws IOException {
        Vector<Vector<Double>> excelData = new Vector<>();
        for (int i=0; i<trainingData.size(); i++){
            excelData.add(evaluateGoodExamplesAndEfficiency(i+1));
        }

        FileWriter file = new FileWriter(this.outputExcelDataPath);
        for (int i=0; i<excelData.size(); i++){
            file.write((i+1) + "\t" + Double.toString(excelData.get(i).get(0)).replace(".", ",") + "\t" + Double.toString(excelData.get(i).get(1)/100).replace(".", ",") + "\n");
        }

        this.bestKValue = getKBestValue(excelData);
        file.close();
    }

    private int getKBestValue(Vector<Vector<Double>> data){
        int bestKValue = -1;

        Vector<Double> modelEfficiencies = new Vector<>();
        data.forEach(e->modelEfficiencies.add(e.get(1)));

        Double bestEfficiency = modelEfficiencies.stream().max(Double::compareTo).get();

        for (int i=0; i<data.size(); i++){
            if (data.get(i).get(1).equals(bestEfficiency)) {
                bestKValue = (i + 1);
                this.bestEfficiency = data.get(i).get(1);
            }
        }

        return bestKValue;
    }

    public void run() throws IOException {
        System.out.println("Specify the k-value for the KNN algorithm: ");
        Scanner scanner = new Scanner(System.in);
        int k = scanner.nextInt();
        Vector<Double> goodExamplesAndEfficiency = evaluateGoodExamplesAndEfficiency(k);
        System.out.println("Well-predicted examples: " + goodExamplesAndEfficiency.get(0) + ", algorithm correctness: " + goodExamplesAndEfficiency.get(1) + "%");
        exportExcelData();
    }

    private void predictDecisionAttributeFromInputData(Vector<Double> maxValues, Vector<Double> minValues) throws IOException {
        Vector<Double> conditionalAttributes = new Vector<>();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Specify conditional values: ");
        for (int i=0; i<trainingData.get(0).getConditionalAttributes().size(); i++){
            conditionalAttributes.add(scanner.nextDouble());
        }
        System.out.println("Specify the k-value for the model: (default k-value: " + this.bestKValue + ", efficiency: " + this.bestEfficiency + "%)");
        int kValue = scanner.nextInt();
        kValue = kValue == 0 ? this.bestKValue : kValue;

        Observation observation = new Observation(conditionalAttributes, "?");
        System.out.println(observation.getConditionalAttributes());

        this.trainingData = dataReader.getModelData(DataReaderType.TRAINING_DATA_TYPE);
        this.trainingData.add(observation);
        this.trainingData = this.dataNormalizer.normalizeData(trainingData);
        observation.setConditionalAttributes(trainingData.get(trainingData.size()-1).getConditionalAttributes());
        this.trainingData.remove(this.trainingData.size()-1);

        String decisionAttribute = evaluateDecisionAttributeForVector(observation, kValue);

        System.out.println("Predicted result by the model: " + decisionAttribute);
    }

    public void predictObservations(Vector<Double> maxValues, Vector<Double> minValues) throws IOException {
        Scanner scanner = new Scanner(System.in);
        do {
            predictDecisionAttributeFromInputData(maxValues, minValues);
            System.out.println("Do you want to make a prediction for the next observation?: ");
        } while (scanner.next().toLowerCase().equals("yes"));
        scanner.close();
    }
}
