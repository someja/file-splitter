package com.github.someja;


import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class FileSplitter {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileSplitter.class);
	private static final int SPLIT_SIZE = 1024*1024*50; // 切割大小
	private static final String SUFFIX = null; // 切割后的文件后缀
	private static final int FILE_INDEX = 1; // 文件命名起始索引
	private static final boolean IGNORE_EMPTY_LINE = true; // 默认忽略空行
	private static final boolean CLEAR_DIR_FIRST = true; // 默认清空目的文件夹

	private int splitSize; // 切割大小
	private String suffix; // 切割后的文件后缀
	private int fileIndex; // 文件命名起始索引
	private boolean ignoreEmptyLine; // 默认忽略空行
	private boolean clearDirFirst; // 默认清空目的文件夹

	private int splitFileNum = 1; // 切割生成的文件个数

	public static void main(String[] args) {
		ResourceBundle bundle;
		try{
			bundle = ResourceBundle.getBundle("message");
		}catch (MissingResourceException e){
			bundle = ResourceBundle.getBundle("message", Locale.US);
		}
		Options options = new Options();
		options.addOption(getOption("splitSize", true, bundle.getString("SPLIT_SIZE"), false));
		options.addOption(getOption("suffix", true, bundle.getString("SUFFIX"), false));
		options.addOption(getOption("fileIndex", true, bundle.getString("FILE_INDEX"), false));
		options.addOption(getOption("allowEmptyLine", false, bundle.getString("ALLOW_EMPTY_LINE"), false));
		options.addOption(getOption("notClearDirAtFirst", false, bundle.getString("NOT_CLEAR_DIR_AT_FIRST"), false));
		options.addOption(getOption("s", "source", true, bundle.getString("SOURCE"), true));
		options.addOption(getOption("d", "destinationDir", true, bundle.getString("DESTINATION"), true));

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
			int splitSize = parseSplitSize(cmd.getOptionValue("splitSize"));
			String suffix = cmd.getOptionValue("suffix", SUFFIX);
			int fileIndex = Integer.parseInt(cmd.getOptionValue("fileIndex", Integer.toString(FILE_INDEX)));
			boolean ignoreEmptyLine = cmd.hasOption("allowEmptyLine") ? false : IGNORE_EMPTY_LINE;
			boolean clearDirFirst = cmd.hasOption("notClearDirAtFirst") ? false : CLEAR_DIR_FIRST;
			String sourceFile = cmd.getOptionValue("s");
			String destDir = cmd.getOptionValue("d");

			FileSplitter splitter = new FileSplitter(splitSize, suffix, fileIndex, ignoreEmptyLine, clearDirFirst);
			splitter.start(sourceFile, destDir);
		}catch (MissingOptionException e) {
			LOGGER.error("missing required option: {}", e.getMissingOptions());
			formatter.printHelp("FileSplitter", options);
		}  catch (UnrecognizedOptionException e) {
			LOGGER.error("unrecognized option: {}", e.getOption());
			formatter.printHelp("FileSplitter", options);
		} catch (ParseException e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage());
			formatter.printHelp("FileSplitter", options);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

	private static int parseSplitSize(String splitSize) {
		if (splitSize == null || splitSize.isEmpty()) {
			return SPLIT_SIZE;
		}

		char last = splitSize.charAt(splitSize.length()-1);
		if (Character.isLetter(last)) {
			int val = Integer.parseInt(splitSize.substring(0, splitSize.length() - 1));

			last = Character.toLowerCase(last);
			int s;
			if (last == 'k') {
				s = val * 1024;
			}else if (last == 'm') {
				s = val * 1024 * 1024;
			}else if (last == 'g') {
				s = val * 1024 * 1024 * 1024;
			}else {
				throw new RuntimeException("not supported size unit: "+last);
			}

			return s;
		} else {
			return Integer.parseInt(splitSize);
		}
	}

	/**
	 * 开始切割文件, 生成到指定目录
	 * @param sourceFile 源文件,要分割的文件
	 * @param destDir 切割后文件的目录
	 * @throws IOException
	 */
	public void start(String sourceFile, String destDir) throws IOException {
		long t0 = System.currentTimeMillis();
		LOGGER.info("sourceFile: "+sourceFile);
		LOGGER.info("destDir: "+destDir);
		LOGGER.info("splitSize: "+splitSize);
		LOGGER.info("split file start.");

		File dest = new File(destDir);
		if (!dest.exists()) {
			dest.mkdirs();
		}else {
			if (dest.isFile()) {
				LOGGER.error("{} can not be file!", destDir);
				return;
			}

			if (this.clearDirFirst) {
				clearDir(dest);
			}
		}

		File src = new File(sourceFile);
		if (!src.exists()) {
			LOGGER.error("Can not find file: {}", sourceFile);
			return;
		}

		if (src.isDirectory()) {
			LOGGER.error(sourceFile + " can not be directory!");
			return;
		}

		final String lineSeparator = System.getProperty("line.separator");
		if (lineSeparator == null || lineSeparator.isEmpty()) {
			LOGGER.warn("Can not get system lineSeparator, use \n.");
		}


		final String srcSuffix;
		if (this.suffix == null) {
			srcSuffix = resolveSuffix(src.getAbsolutePath());
		} else {
			srcSuffix = this.suffix;
		}

		String row;

		File f = new File(getSplitFileName(destDir, this.fileIndex, srcSuffix));
		FileWriter fw = new FileWriter(f);

		FileReader read = new FileReader(src);
		BufferedReader br = new BufferedReader(read);

		while ((row = br.readLine()) != null) {
			if(this.ignoreEmptyLine && row.isEmpty()){
				continue;
			}
			fw.append(row + lineSeparator);
			if(f.length() > this.splitSize){
				fw.flush();
				fw.close();
				LOGGER.info("{} complete.", f.getAbsolutePath());
				this.fileIndex++;
				f = new File(getSplitFileName(destDir, this.fileIndex, srcSuffix));
				this.splitFileNum++;
				fw = new FileWriter(f);
			}
		}
		fw.close();
		if (f.length() == 0) {
			f.delete();
		} else {
			LOGGER.info(f.getAbsolutePath()+" complete.");
		}
		br.close();
		LOGGER.info("split file finished, generate {} files, elapsed {}ms", this.splitFileNum, (System.currentTimeMillis()-t0));
	}

	private String resolveSuffix(String filepath) {
		int dotPos = filepath.lastIndexOf('.');
		if (dotPos == -1) {
			return "";
		} else {
			return filepath.substring(dotPos);
		}
	}

	private void clearDir(File dest) throws IOException {
		Files.walkFileTree(dest.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (file.toFile().isFile()) {
					Files.delete(file);
				}
				return FileVisitResult.CONTINUE;
			}

		});
	}

	public FileSplitter(int splitSize, String suffix, int fileIndex, boolean ignoreEmptyLine, boolean clearDirFirst) {
		this.splitSize = splitSize;
		this.suffix = suffix;
		this.fileIndex = fileIndex;
		this.ignoreEmptyLine = ignoreEmptyLine;
		this.clearDirFirst = clearDirFirst;
	}

	public FileSplitter() {
		this(SPLIT_SIZE, SUFFIX, FILE_INDEX, IGNORE_EMPTY_LINE, CLEAR_DIR_FIRST);
	}

	private static Option getOption(String opt, boolean hasArg, String description, boolean required){
		Option option = new Option(opt, null, hasArg, description);
		option.setRequired(required);
		return option;
	}

	private static Option getOption(String opt, String longOpt, boolean hasArg, String description, boolean required){
		Option option = new Option(opt, longOpt, hasArg, description);
		option.setRequired(required);
		return option;
	}

	private static String getSplitFileName(String splitPath, int file_index, String srcSuffix) {
		return String.format("%s%s%s%s", splitPath, File.separator, file_index, srcSuffix);
	}

}
