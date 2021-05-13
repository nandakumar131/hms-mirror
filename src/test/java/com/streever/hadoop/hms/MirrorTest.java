package com.streever.hadoop.hms;

import com.streever.hadoop.hms.mirror.ReportingConf;
import org.junit.Before;
import org.junit.Test;

import javax.validation.constraints.AssertTrue;

import static org.junit.Assert.*;

public class MirrorTest {

    private String homedir = null;

    @Before
    public void setUp() throws Exception {
        homedir = System.getProperty("user.home");
    }

    @Test
    public void test_so_ro() {
        String outputDir = homedir + System.getProperty("file.separator") + "hms-mirror-reports/so_ro";
        String[] args = new String[]{"-db", "tpcds_bin_partitioned_orc_10", "-ro", "-sql",  "-o", outputDir};

        Mirror mirror = new Mirror();
        mirror.init(args);
        mirror.doit();
    }

    @Test
    public void test_so_ro_sync() {
        String outputDir = homedir + System.getProperty("file.separator") + "hms-mirror-reports/so_ro_sync";
        String[] args = new String[]{"-db", "tpcds_bin_partitioned_orc_10", "-ro", "-sync", "-sql", "-o", outputDir};

        Mirror mirror = new Mirror();
        mirror.init(args);
        mirror.doit();
    }

    @Test
    public void test_dump() {
        String outputDir = homedir + System.getProperty("file.separator") + "hms-mirror-reports/dump";
        String[] args = new String[]{"-d", "DUMP", "-db", "tpcds_bin_partitioned_orc_10", "-sql", "-o", outputDir};

        Mirror mirror = new Mirror();
        mirror.init(args);
        mirror.doit();
    }

    @Test
    public void test_linked() {
        String outputDir = homedir + System.getProperty("file.separator") + "hms-mirror-reports/linked";
        String[] args = new String[]{"-d", "LINKED", "-db", "tpcds_bin_partitioned_orc_10", "-sql", "-o", outputDir};

        Mirror mirror = new Mirror();
        mirror.init(args);
        mirror.doit();
    }

    @Test
    public void test_common() {
        String outputDir = homedir + System.getProperty("file.separator") + "hms-mirror-reports/common";
        String[] args = new String[]{"-d", "COMMON", "-db", "tpcds_bin_partitioned_orc_10", "-sql", "-o", outputDir};

        Mirror mirror = new Mirror();
        mirror.init(args);
        mirror.doit();
    }

    @Test
    public void test_sql() {
        String outputDir = homedir + System.getProperty("file.separator") + "hms-mirror-reports/sql";
        String[] args = new String[]{"-d", "SQL", "-db", "tpcds_bin_partitioned_orc_10", "-sql", "-o", outputDir};

        Mirror mirror = new Mirror();
        mirror.init(args);
        mirror.doit();
    }

    @Test
    public void test_hybrid() {
        String outputDir = homedir + System.getProperty("file.separator") + "hms-mirror-reports/hybrid";
        String[] args = new String[]{"-d", "HYBRID", "-db", "tpcds_bin_partitioned_orc_10", "-sql", "-o", outputDir};

        Mirror mirror = new Mirror();
        mirror.init(args);
        mirror.doit();
    }

    @Test
    public void test_exp_imp() {
        String outputDir = homedir + System.getProperty("file.separator") + "hms-mirror-reports/exp_imp";
        String[] args = new String[]{"-d", "EXPORT_IMPORT", "-db", "tpcds_bin_partitioned_orc_10", "-sql", "-o", outputDir};

        Mirror mirror = new Mirror();
        mirror.init(args);
        mirror.doit();
    }

}