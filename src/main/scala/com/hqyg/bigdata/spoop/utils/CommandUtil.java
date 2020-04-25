package com.hqyg.bigdata.spoop.utils;


import org.apache.commons.cli.*;

import java.util.ListIterator;

public class CommandUtil {
    public static class GnuParserImpl extends GnuParser {
        private final boolean ignoreUnrecognizedOption;

        public GnuParserImpl(final boolean ignoreUnrecognizedOption) {
            this.ignoreUnrecognizedOption = ignoreUnrecognizedOption;
        }

        @Override
        protected void processOption(String arg, ListIterator iter) throws ParseException {
            boolean hasOption = getOptions().hasOption(arg);

            // this allows us to parse the options for command and then parse again
            // based on command
            if (hasOption || !ignoreUnrecognizedOption) {
                super.processOption(arg, iter);
            }
        }
    }

    public static CommandLine parse(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("m")
                .withDescription("import or export").hasArg().isRequired()
                .withArgName("mode").create());
        options.addOption(OptionBuilder.withLongOpt("db_num")
                .withDescription("database db_num in configuration").hasArg().isRequired()
                .withArgName("db_num").create());
        options.addOption(OptionBuilder.withLongOpt("tables").withDescription("jdbc tables,from_table").hasArgs()
                .isRequired().withArgName("tables").withValueSeparator(',').create());
        options.addOption(OptionBuilder.withLongOpt("date").withDescription("data date").hasArg()
                .isRequired().withArgName("date").create());

//        options.addOption(OptionBuilder.withLongOpt("jdbcString")
//                .withDescription("jdbc connection string").hasArg().isRequired()
//                .withArgName("jdbcConnectionString").create());
//
//        options.addOption(OptionBuilder.withLongOpt("u").withDescription("jdbc username").hasArg()
//                .isRequired().withArgName("username").create());
//
//        options.addOption(OptionBuilder.withLongOpt("p").withDescription("jdbc password").hasArg()
//                .withArgName("password").create());
//        options.addOption(OptionBuilder.withLongOpt("partitionColumn")
//                .withDescription("jdbc table parition column").hasArg().withArgName("pc").create());
//
//        options.addOption(OptionBuilder.withLongOpt("prefix").withDescription("hiveTableName= prefix + tableName + suffix")
//                .hasArg().isRequired().withArgName("prefix").create());
//
//        options.addOption(OptionBuilder.withLongOpt("suffix").withDescription("hiveTableName= prefix + tableName + suffix")
//                .hasArg().isRequired().withArgName("suffix").create());
        CommandLineParser commandLineParser = new GnuParserImpl(true);
        CommandLine commandLine = commandLineParser.parse(options, args, false);
        return commandLine;
    }


    public static void main(String[] args) {
        CommandLine parse = null;
        try {
            parse = parse(args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(parse.toString());
    }
}
