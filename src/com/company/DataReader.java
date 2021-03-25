package com.company;

import com.company.models.Observation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class DataReader {
    private String trainingFilePath;
    private String testFilePath;

    public DataReader(String trainingFilePath,String testFilePath){
        this.trainingFilePath = trainingFilePath;
        this.testFilePath = testFilePath;
    }

    public Vector<Observation> getModelData(DataReaderType type) throws IOException {
        List<String> fileData = new ArrayList<>();

        if (type == DataReaderType.TRAINING_DATA_TYPE)
            fileData = Files.lines(Paths.get(trainingFilePath)).collect(Collectors.toList());
        else if (type == DataReaderType.TEST_DATA_TYPE)
            fileData = Files.lines(Paths.get(testFilePath)).collect(Collectors.toList());

        List<List<String>> splitData = new ArrayList<>();
        fileData.forEach(e->splitData.add(List.of(e.replaceAll("\\s{2,}", "|").replaceAll("\\s", "").replaceAll(",", ".").split("\\|"))));

        Vector<Observation> data = new Vector<>();

        for (List<String> list : splitData){
            Vector<Double> conditionalAttributes = new Vector<>();
            String decisionAttribute = null;
            for (int i=0; i<list.size(); i++){
                if (i == list.size()-1)
                    decisionAttribute = list.get(i);
                else
                    conditionalAttributes.add(Double.parseDouble(list.get(i)));
            }
            data.add(new Observation(conditionalAttributes, decisionAttribute));
        }

        return data;
    }
}
