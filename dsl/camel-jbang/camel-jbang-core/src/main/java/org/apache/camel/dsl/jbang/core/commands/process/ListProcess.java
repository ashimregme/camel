/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.dsl.jbang.core.commands.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import com.github.freva.asciitable.OverflowBehaviour;
import org.apache.camel.dsl.jbang.core.commands.CamelJBangMain;
import org.apache.camel.util.TimeUtils;
import org.apache.camel.util.json.JsonObject;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "ps", description = "List running Camel integrations")
public class ListProcess extends ProcessBaseCommand {

    @CommandLine.Option(names = { "--sort" },
                        description = "Sort by pid, name or age", defaultValue = "pid")
    String sort;

    public ListProcess(CamelJBangMain main) {
        super(main);
    }

    @Override
    public Integer call() throws Exception {
        List<Row> rows = new ArrayList<>();

        List<Long> pids = findPids("*");
        ProcessHandle.allProcesses()
                .filter(ph -> pids.contains(ph.pid()))
                .forEach(ph -> {
                    JsonObject root = loadStatus(ph.pid());
                    if (root != null) {
                        Row row = new Row();
                        row.name = extractName(root, ph);
                        row.pid = "" + ph.pid();
                        row.uptime = extractSince(ph);
                        row.ago = TimeUtils.printSince(row.uptime);
                        JsonObject context = (JsonObject) root.get("context");
                        row.state = context.getInteger("phase");
                        JsonObject hc = (JsonObject) root.get("healthChecks");
                        row.ready = hc != null ? hc.getString("ready") + "/" + hc.getString("total") : null;
                        rows.add(row);
                    }
                });

        // sort rows
        rows.sort(this::sortRow);

        if (!rows.isEmpty()) {
            System.out.println(AsciiTable.getTable(AsciiTable.NO_BORDERS, rows, Arrays.asList(
                    new Column().header("PID").headerAlign(HorizontalAlign.CENTER).with(r -> r.pid),
                    new Column().header("NAME").dataAlign(HorizontalAlign.LEFT).maxWidth(30, OverflowBehaviour.ELLIPSIS)
                            .with(r -> r.name),
                    new Column().header("READY").dataAlign(HorizontalAlign.CENTER).with(r -> r.ready),
                    new Column().header("STATUS").headerAlign(HorizontalAlign.CENTER)
                            .with(r -> extractState(r.state)),
                    new Column().header("AGE").headerAlign(HorizontalAlign.CENTER).with(r -> r.ago))));
        }

        return 0;
    }

    protected int sortRow(Row o1, Row o2) {
        switch (sort) {
            case "pid":
                return Long.compare(Long.parseLong(o1.pid), Long.parseLong(o2.pid));
            case "name":
                return o1.name.compareToIgnoreCase(o2.name);
            case "age":
                return Long.compare(o1.uptime, o2.uptime);
            default:
                return 0;
        }
    }

    private static class Row {
        String pid;
        String name;
        String ready;
        int state;
        String ago;
        long uptime;
    }

}
