package com.streever.hadoop.hms.mirror.feature;

import com.streever.hadoop.hms.mirror.EnvironmentTable;

import java.util.List;

public interface Feature {
    Boolean applicable(EnvironmentTable envTable);
    Boolean applicable(List<String> schema);
    Boolean fixSchema(List<String> schema);
    Boolean fixSchema(EnvironmentTable envTable);
//    List<String> addEscaped(List<String> definition);
    String getDescription();
}
