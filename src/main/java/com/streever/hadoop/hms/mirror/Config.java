package com.streever.hadoop.hms.mirror;

import com.streever.hadoop.hms.Mirror;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Config {

    private static Logger LOG = LogManager.getLogger(Mirror.class);

    private boolean dryrun = Boolean.FALSE;
    private String transferDbPrefix = "transfer_";
    private String exportBaseDirPrefix = "/apps/hive/warehouse/export_";
    private boolean overwriteTable = Boolean.TRUE;
    private Integer parallelism = 4; // Default

    private Map<Environment, Cluster> clusters = new TreeMap<Environment, Cluster>();

    public boolean isDryrun() {
        if (dryrun) {
            LOG.debug("Dry-run: ON");
        }
        return dryrun;
    }

    public void setDryrun(boolean dryrun) {
        this.dryrun = dryrun;
    }

    public String getTransferDbPrefix() {
        return transferDbPrefix;
    }

    public void setTransferDbPrefix(String transferDbPrefix) {
        this.transferDbPrefix = transferDbPrefix;
    }

    public String getExportBaseDirPrefix() {
        return exportBaseDirPrefix;
    }

    public void setExportBaseDirPrefix(String exportBaseDirPrefix) {
        this.exportBaseDirPrefix = exportBaseDirPrefix;
    }

    public boolean isOverwriteTable() {
        return overwriteTable;
    }

    public void setOverwriteTable(boolean overwriteTable) {
        this.overwriteTable = overwriteTable;
    }

    public Integer getParallelism() {
        return parallelism;
    }

    public void setParallelism(Integer parallelism) {
        this.parallelism = parallelism;
    }

    public Map<Environment, Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(Map<Environment, Cluster> clusters) {
        this.clusters = clusters;
    }

    public Cluster getCluster(Environment environment) {
        Cluster cluster = getClusters().get(environment);
        if (cluster.getEnvironment() == null) {
            cluster.setEnvironment(environment);
        }
        return cluster;
    }

    public void init() {
        // Link Cluster and it's Environment Type.
        Set<Environment> environmentSet = this.getClusters().keySet();
        for (Environment environment: environmentSet) {
            Cluster cluster = clusters.get(environment);
            cluster.setEnvironment(environment);
        }
    }
}