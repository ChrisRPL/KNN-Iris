package com.company.models;

import java.util.Vector;

public class Observation {
    private Vector<Double> conditionalAttributes;
    private String decisionAttribute;

    public Observation(Vector<Double> conditionalAttributes, String decisionAttribute){
        this.conditionalAttributes = conditionalAttributes;
        this.decisionAttribute = decisionAttribute;
    }

    public Vector<Double> getConditionalAttributes() {
        return conditionalAttributes;
    }

    public void setConditionalAttributes(Vector<Double> conditionalAttributes) {
        this.conditionalAttributes = conditionalAttributes;
    }

    public String getDecisionAttribute() {
        return decisionAttribute;
    }

    @Override
    public String toString() {
        return this.decisionAttribute;
    }
}
