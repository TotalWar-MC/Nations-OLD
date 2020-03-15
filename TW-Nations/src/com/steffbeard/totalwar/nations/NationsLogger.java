package com.steffbeard.totalwar.nations;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.steffbeard.totalwar.nations.config.Settings;
import com.steffbeard.totalwar.nations.economy.EconomyAccount;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author Lukas Mansour (Articdive)
 */
public class NationsLogger {
	private static final NationsLogger instance = new NationsLogger();
	private static final Logger LOGGER_MONEY = LogManager.getLogger("com.palmergames.bukkit.nations.money");
	
	private NationsLogger() {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		// Get log location.
		String logFolderName = NationsUniverse.getInstance().getRootFolder() + File.separator + "logs";
		
		Appender nationsMainAppender = FileAppender.newBuilder()
			.withFileName(logFolderName + File.separator + "nations.log")
			.withName("nations-Main-Log")
			.withAppend(Settings.isAppendingToLog())
			.withIgnoreExceptions(false)
			.withBufferedIo(false)
			.withBufferSize(0)
			.setConfiguration(config)
			.withLayout(PatternLayout.newBuilder()
				.withCharset(StandardCharsets.UTF_8)
				.withPattern("%d [%t]: %m%n")
				.withConfiguration(config)
				.build())
			.build();
		Appender nationsMoneyAppender = FileAppender.newBuilder()
			.withFileName(logFolderName + File.separator + "money.csv")
			.withName("Nations-Money")
			.withAppend(Settings.isAppendingToLog())
			.withIgnoreExceptions(false)
			.withBufferedIo(false)
			.withBufferSize(0)
			.setConfiguration(config)
			.withLayout(PatternLayout.newBuilder()
				// The comma after the date is to seperate it in CSV, this is a really nice workaround
				// And avoids having to use apache-csv to make it work with Log4J
				.withCharset(StandardCharsets.UTF_8)
				.withPattern("%d{dd MMM yyyy HH:mm:ss},%m%n")
				.withConfiguration(config)
				.build())
			.build();
		Appender nationsDebugAppender = FileAppender.newBuilder()
			.withFileName(logFolderName + File.separator + "debug.log")
			.withName("nations-Debug")
			.withAppend(Settings.isAppendingToLog())
			.withIgnoreExceptions(false)
			.withBufferedIo(false)
			.withBufferSize(0)
			.setConfiguration(config)
			.withLayout(PatternLayout.newBuilder()
				.withCharset(StandardCharsets.UTF_8)
				.withPattern("%d [%t]: %m%n")
				.withConfiguration(config)
				.build())
			.build();
		Appender nationsDatabaseAppender = FileAppender.newBuilder()
			.withFileName(logFolderName + File.separator + "database.log")
			.withName("Nations-Database")
			.withAppend(Settings.isAppendingToLog())
			.withIgnoreExceptions(false)
			.withBufferedIo(false)
			.withBufferSize(0)
			.setConfiguration(config)
			.withLayout(PatternLayout.newBuilder()
				.withCharset(StandardCharsets.UTF_8)
				.withPattern("%d [%t]: %m%n")
				.withConfiguration(config)
				.build())
			.build();
		
		nationsMainAppender.start();
		nationsMoneyAppender.start();
		nationsDebugAppender.start();
		nationsDatabaseAppender.start();
		
		// nations Main
		LoggerConfig nationsMainConfig = LoggerConfig.createLogger(true, Level.ALL, "Nations", null, new AppenderRef[0], null, config, null);
		nationsMainConfig.addAppender(nationsMainAppender, Level.ALL, null);
		config.addLogger(Main.class.getName(), nationsMainConfig);
		
		// Debug
		LoggerConfig nationsDebugConfig = LoggerConfig.createLogger(Settings.getDebug(), Level.ALL, "nations-Debug", null, new AppenderRef[0], null, config, null);
		nationsDebugConfig.addAppender(nationsDebugAppender, Level.ALL, null);
		config.addLogger("com.palmergames.bukkit.nations.debug", nationsDebugConfig);
		
		// Money
		LoggerConfig nationsMoneyConfig = LoggerConfig.createLogger(false, Level.ALL, "Nations-Money", null, new AppenderRef[0], null, config, null);
		nationsMoneyConfig.addAppender(nationsMoneyAppender, Level.ALL, null);
		config.addLogger("com.palmergames.bukkit.nations.money", nationsMoneyConfig);
		
		// Database
		LoggerConfig nationsDatabaseConfig = LoggerConfig.createLogger(false, Level.ALL, "Nations-Database", null, new AppenderRef[0], null, config, null);
		nationsDatabaseConfig.addAppender(nationsDatabaseAppender, Level.ALL, null);
		
		ctx.updateLoggers();
	}
	
	public void refreshDebugLogger() {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig nationsDebugConfig = config.getLoggerConfig("com.steffbeard.totalwar.nations.debug");
		nationsDebugConfig.setAdditive(Settings.getDebug());
		ctx.updateLoggers();
	}
	
	public void logMoneyTransaction(EconomyAccount a, double amount, EconomyAccount b, String reason) {
		
		String sender;
		String receiver;
		
		if (a == null) {
			sender = "None";
		} else {
			sender = a.getName();
		}
		
		if (b == null) {
			receiver = "None";
		} else {
			receiver = b.getName();
		}
		
		if (reason == null) {
			LOGGER_MONEY.info(String.format("%s,%s,%s,%s", "Unknown Reason", sender, amount, receiver));
		} else {
			LOGGER_MONEY.info(String.format("%s,%s,%s,%s", reason, sender, amount, receiver));
		}
	}
	
	public void logMoneyTransaction(String a, double amount, String b, String reason) {
		if (reason == null) {
			LOGGER_MONEY.info(String.format("%s,%s,%s,%s", "Unknown Reason", a, amount, b));
		} else {
			LOGGER_MONEY.info(String.format("%s,%s,%s,%s", reason, a, amount, b));
		}
	}

	public static NationsLogger getInstance() {
		return instance;
	}
}