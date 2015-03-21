/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at

 *  http://www.apache.org/licenses/LICENSE-2.0

 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.stratos.cli.commands;

import org.apache.commons.cli.*;
import org.apache.stratos.cli.Command;
import org.apache.stratos.cli.RestCommandLineService;
import org.apache.stratos.cli.StratosCommandContext;
import org.apache.stratos.cli.exception.CommandException;
import org.apache.stratos.cli.utils.CliConstants;
import org.apache.stratos.cli.utils.CliUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AddApplicationPolicyCommand implements Command<StratosCommandContext> {
    private static final Logger log = LoggerFactory.getLogger(AddApplicationPolicyCommand.class);

    private final Options options;

    public AddApplicationPolicyCommand(){
        options = constructOptions();
    }

    private Options constructOptions() {
        final Options options = new Options();

        Option resourcePath = new Option(CliConstants.RESOURCE_PATH, CliConstants.RESOURCE_PATH_LONG_OPTION, true,
                "Application policy resource path");
        resourcePath.setArgName("resource path");
        options.addOption(resourcePath);

        return options;
    }

    public String getName() {
        return "add-application-policy";
    }

    public String getDescription() {
        return "Add application policy deployment";
    }

    public String getArgumentSyntax() {
        return null;
    }

    public int execute(StratosCommandContext context, String[] args,Option[] alreadyParsedOpts) throws CommandException {
        if (log.isDebugEnabled()) {
            log.debug("Executing {} command...", getName());
        }

        if (args != null && args.length > 0) {
            String resourcePath = null;
            String resourceFileContent = null;

            final CommandLineParser parser = new GnuParser();
            CommandLine commandLine;

            try {
                commandLine = parser.parse(options, args);

                if (log.isDebugEnabled()) {
                    log.debug("Application policy deployment");
                }

                if (commandLine.hasOption(CliConstants.RESOURCE_PATH)) {
                    if (log.isTraceEnabled()) {
                        log.trace("Resource path option is passed");
                    }
                    resourcePath = commandLine.getOptionValue(CliConstants.RESOURCE_PATH);
                    resourceFileContent = CliUtils.readResource(resourcePath);
                }

                if (resourcePath == null) {
                    context.getStratosApplication().printUsage(getName());
                    return CliConstants.COMMAND_FAILED;
                }

                RestCommandLineService.getInstance().addApplicationPolicy(resourceFileContent);
                return CliConstants.COMMAND_SUCCESSFULL;

            } catch (ParseException e) {
                if (log.isErrorEnabled()) {
                    log.error("Error parsing arguments", e);
                }
                System.out.println(e.getMessage());
                return CliConstants.COMMAND_FAILED;
            } catch (IOException e) {
                System.out.println("Invalid resource path");
                return CliConstants.COMMAND_FAILED;
            }
        } else {
            context.getStratosApplication().printUsage(getName());
            return CliConstants.COMMAND_FAILED;
        }
    }

    public Options getOptions() {
        return options;
    }
}