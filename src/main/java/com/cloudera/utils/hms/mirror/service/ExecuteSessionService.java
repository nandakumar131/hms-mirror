/*
 * Copyright (c) 2024. Cloudera, Inc. All Rights Reserved
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.cloudera.utils.hms.mirror.service;

import com.cloudera.utils.hadoop.cli.CliEnvironment;
import com.cloudera.utils.hms.mirror.domain.HmsMirrorConfig;
import com.cloudera.utils.hms.mirror.domain.support.Conversion;
import com.cloudera.utils.hms.mirror.domain.support.ExecuteSession;
import com.cloudera.utils.hms.mirror.domain.support.RunStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@Slf4j
@Getter
public class ExecuteSessionService {

    public static final String DEFAULT = "default.yaml";

    private CliEnvironment cliEnvironment;
    private ConnectionPoolService connectionPoolService;

//    /*
//    This should be an immutable 'running' or 'ran' version of a session.
//     */
//    @Setter
////    private ExecuteSession activeSession;

    /*
    This is the current session that can be modified (but not running yet).  This is where
    configurations can be changed before running.  Once the session is kicked off, the object
    should be cloned and the original session moved to the 'activeSession' field and added
    to the 'executeSessionQueue'.
     */
    @Setter
    private ExecuteSession loadedSession;

    private ExecuteSession activeSession;

    private String reportOutputDirectory;

    private boolean amendSessionIdToReportDir = Boolean.TRUE;

    /*
    Used to limit the number of sessions that are retained in memory.
     */
//    private int maxRetainedSessions = 5;
    private final Map<String, ExecuteSession> executeSessionMap = new TreeMap<>();
    private final Map<String, ExecuteSession> sessions = new HashMap<>();

    @Autowired
    public void setCliEnvironment(CliEnvironment cliEnvironment) {
        this.cliEnvironment = cliEnvironment;
    }

    @Autowired
    public void setConnectionPoolService(ConnectionPoolService connectionPoolService) {
        this.connectionPoolService = connectionPoolService;
    }

    public void setReportOutputDirectory(String reportOutputDirectory, boolean amendSessionIdToReportDir) {
        this.amendSessionIdToReportDir = amendSessionIdToReportDir;
        this.reportOutputDirectory = reportOutputDirectory;
    }

    public ExecuteSession createSession(String sessionId, HmsMirrorConfig hmsMirrorConfig) {
        String sessionName = sessionId != null? sessionId : DEFAULT;

        ExecuteSession session;
        if (sessions.containsKey(sessionName)) {
            session = sessions.get(sessionName);
            session.setOrigConfig(hmsMirrorConfig);
        } else {
            session = new ExecuteSession();
            session.setSessionId(sessionName);
            session.setOrigConfig(hmsMirrorConfig);
            sessions.put(sessionName, session);
        }
        return session;
    }

    public void clearActiveSession() {
        if (activeSession != null) {
           if (activeSession.getRunning().get()) {
               throw new RuntimeException("Session is still running.  Cannot clear active session.");
           } else {
            activeSession = null;
           };
        }
    }

    public ExecuteSession getActiveSession() {
        if (activeSession == null) {
            log.warn("No active session.  Transitioning loaded session to active.");
            transitionLoadedSessionToActive();
//            throw new RuntimeException("No active session. Try configuring a session first.");
        }
        return activeSession;
    }

    /*
    If sessionId is null, then pull the 'current' session.
    If sessionId is NOT null, look for it in the session map and return it.
    When not found, throw exception.
     */
    public ExecuteSession getSession(String sessionId) {
        if (sessionId == null) {
            if (loadedSession == null) {
                throw new RuntimeException("No session loaded.");
            }
            return loadedSession;
        } else {
            if (sessions.containsKey(sessionId)) {
                return sessions.get(sessionId);
            } else {
                throw new RuntimeException("Session not found: " + sessionId);
            }
        }
    }

    /*
      Look at the 'activeSession' and if it is not null, check that it is not running.
        If it is not running, then clone the currentSession and add it to the 'executeSessionQueue'.
        Set the 'activeSession' to null.  The 'getActiveSession' will then return the last session
        placed in the queue and set 'activeSession' to that session.

        This allow us to keep the current and active sessions separate.  The active session is the
        one that will be referenced during the run.
     */
    public ExecuteSession transitionLoadedSessionToActive() {
        if (activeSession != null && activeSession.getRunning().get()) {
            throw new RuntimeException("Session is still running.  Cannot transition to active.");
        }

        // This should get the loaded session and clone it.
        if (loadedSession == null) {
            throw new RuntimeException("No session loaded.");
        }
//        ExecuteSession loadedSession = getSession(null);
        ExecuteSession session = loadedSession.clone();
        HmsMirrorConfig resolvedConfig = loadedSession.getResolvedConfig();
        session.setResolvedConfig(resolvedConfig);

        // Connection Service should be set to the resolved config.
        connectionPoolService.close();
        connectionPoolService.setHmsMirrorConfig(resolvedConfig);
        connectionPoolService.setExecuteSession(session);
        try {
            connectionPoolService.init();
        } catch (Exception e) {
            log.error("Error initializing connection pool.", e);
            throw new RuntimeException("Error initializing connection pool.", e);
        }

        // Set the active session id to the current date and time.
        DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        session.setSessionId(dtf.format(new Date()));

        String sessionReportDir = null;
        if (amendSessionIdToReportDir) {
            sessionReportDir = reportOutputDirectory + File.separator + session.getSessionId();
        } else {
            sessionReportDir = reportOutputDirectory;
        }
        resolvedConfig.setOutputDirectory(sessionReportDir);

        // Create the RunStatus and Conversion objects.
        RunStatus runStatus = new RunStatus();
        // Link the RunStatus to the session so users know what session details to retrieve.
        runStatus.setSessionId(session.getSessionId());
        session.setRunStatus(runStatus);
        session.setConversion(new Conversion());
        // Clear all previous run states from sessions to keep memory usage down.
        for (Map.Entry<String, ExecuteSession> entry : executeSessionMap.entrySet()) {
            entry.getValue().setConversion(null);
            entry.getValue().setRunStatus(null);
        }
        activeSession = session;
        executeSessionMap.put(session.getSessionId(), session);
        return session;
    }

}
