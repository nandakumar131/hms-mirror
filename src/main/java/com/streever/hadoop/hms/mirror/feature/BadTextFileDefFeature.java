package com.streever.hadoop.hms.mirror.feature;

import com.streever.hadoop.hms.mirror.EnvironmentTable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.regex.Pattern;

public class BadTextFileDefFeature extends BaseFeature implements Feature {
    private final String ROW_FORMAT_DELIMITED = "ROW FORMAT DELIMITED";
    private final Pattern FIELDS_TERMINATED_BY = Pattern.compile("FIELDS TERMINATED BY (.*)");
    private final Pattern LINES_TERMINATED_BY = Pattern.compile("LINES TERMINATED BY (.*)");
    private final String WITH_SERDEPROPERTIES = "WITH SERDEPROPERTIES";

    private final String ROW_FORMAT_SERDE = "ROW FORMAT SERDE";
    private final String LAZY_SERDE = "'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe'";

    private static Logger LOG = LogManager.getLogger(BadTextFileDefFeature.class);

    @Override
    public Boolean applicable(EnvironmentTable envTable) {
        return applicable(envTable.getDefinition());
    }

    @Override
    public Boolean applicable(List<String> schema) {
        Boolean rtn = Boolean.FALSE;

        if (contains(ROW_FORMAT_DELIMITED, schema) && contains(WITH_SERDEPROPERTIES, schema)) {
            rtn = Boolean.TRUE;
        }

        return rtn;
    }


    @Override
    public EnvironmentTable fixSchema(EnvironmentTable envTable) {
        List<String> fixed = fixSchema(envTable.getDefinition());
        envTable.setDefinition(fixed);
        return envTable;
    }

    @Override
    /**
     ROW FORMAT DELIMITED
     FIELDS TERMINATED BY '|'
     LINES TERMINATED BY '\n'
     WITH SERDEPROPERTIES (
     'escape.delim'='\\')
     STORED AS INPUTFORMAT
     'org.apache.hadoop.mapred.TextInputFormat'
     OUTPUTFORMAT
     'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
     */
    public List<String> fixSchema(List<String> schema) {
        List<String> rtn = addEscaped(schema);
        LOG.debug("Checking if table has OLD TEXTFILE definition");
        // find the index of the ROW_FORMAT_DELIMITED
        if (contains(ROW_FORMAT_DELIMITED, rtn) && contains(WITH_SERDEPROPERTIES, rtn)) {
            // Bad Definition.
            // Get the value for FIELDS_TERMINATED_BY
            String ftb = getGroupFor(FIELDS_TERMINATED_BY, rtn);
            // Get the value for LINES_TERMINATED_BY
            String ltb = getGroupFor(LINES_TERMINATED_BY, rtn);
            // Remove bad elements
            int RFD = indexOf(rtn, ROW_FORMAT_DELIMITED);
            int WS = indexOf(rtn, WITH_SERDEPROPERTIES);
            removeRange(RFD, WS, rtn);

            rtn.add(RFD++, ROW_FORMAT_SERDE);
            rtn.add(RFD++, LAZY_SERDE);
            RFD++;
            if (ftb != null) {
                rtn.add(RFD++, "'field.delim'=" + ftb + ",");
            }
            if (ltb != null) {
                rtn.add(RFD++, "'line.delim'=" + ltb + ",");
            }
        }
        return rtn;
    }

}
