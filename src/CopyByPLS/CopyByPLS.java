package CopyByPLS;

import java.io.*;
//import java.nio.*;
import java.nio.file.*;
import java.util.*;

public class CopyByPLS {
	//Имена файлов
	protected static String plsFileName = "";
	protected static String DestinationFolderName = "";
	protected static String outLogFileName = "out.log";
	protected static String errorLogFileName = "err.log";
	protected static String outMessageBeforeStarting = "";
	protected static String errMessageBeforeStarting = "";
	public static boolean isQuietMode = false;
	
	//По умолчанию считаем что все кодировки работы с файлами - UTF-8
	//protected static String consoleEncodingCharset = "UTF-8";
	protected static String playlistEncodingCharset = "UTF-8";
	protected static String fileNameEncodingCharset = "UTF-8";

	public static void main(String[] args) {
		//DEBUG consoleEncodingCharset = System.getProperty("console.encoding");
		//DEBUG fileNameEncodingCharset = System.getProperty("file.encoding");
		//DEBUG		System.out.println("concoleEncodingCharset: "+consoleEncodingCharset+"\nfileNameEncodingCharset: "+fileNameEncodingCharset);

		//Проверка входных данных, подготовка к работе и копирование файлов
		if(parseArgs(args)){
			
			//Попытка изменения кодировки имен файлов на кодировку плейлиста для исключения конвертации строк
			setFileNameEncoding(playlistEncodingCharset);
			
			//Перенаправление потоков вывода сообщений и ошибок
			setLogOutputStreams(outLogFileName, errorLogFileName);
			
			//Чтение .pls-файла
			PLSFile plsFile = new PLSFile(plsFileName, playlistEncodingCharset);
			
			//Копирование списка
			FilesCopier copier = new FilesCopier(fileNameEncodingCharset);
			copier.putInOneFolder(plsFile.getFileNames(), DestinationFolderName, fileNameEncodingCharset);
			
		}else {
			//Возмущаемся по поводу не всех входных параметров
			System.out.println("Two arguments are expected to run this program:\n"
					+ "1) .pls-file name 2) destination folder\n"
					+ "(for example: \"file.pls\" \"Folder\" )");
		}
	}

	public static void setLogOutputStreams(String strOutLogFileName, String strErrorLogFileName) {
		//Перенаправление потока вывода сообщений для отладки в режиме QuietMode
		if(isQuietMode) {
			try {
				PrintStream outLogStream = new PrintStream(new FileOutputStream(strOutLogFileName));
				System.setOut(outLogStream); 
			}catch(FileNotFoundException err){
				System.err.println(err.getMessage());
			}
		}
		//Перенаправление потока вывода ошибок в файл лога для отладки
		//В данной версии по умолчанию перенаправляется в log-файл для отладки 
		try {
			PrintStream errLogStream = new PrintStream(new FileOutputStream(strErrorLogFileName));
			System.setErr(errLogStream);
		}catch(FileNotFoundException err){
			System.err.println(err.getMessage());
		}
		messagesFlush();
	}
	
	public static boolean parseArgs(String args[]) {
		boolean isKnowedPLS = false;
		boolean isKnowedDirectory = false;
		
		if(args.length >= 2) { //По умолчанию предполагается минимум два аргумента - имя pls-файла и папка назначения
			//Проверка на наличие плейлиста
			if(new File(args[0]).exists()) {
				plsFileName = args[0];
				outMessage("Input source of copied file names: "+plsFileName);
				isKnowedPLS = true;
			}else {
				outMessage("Can't find file: "+args[0]);
			}
			//Проверка на соответвствие аргумента (д.б. указана выходная папка)
			if(new File(args[1]).isDirectory()) {
				DestinationFolderName = args[1];
				outMessage("Destination folder: "+DestinationFolderName);
				isKnowedDirectory = true;
			}else {
				outMessage("\""+args[1]+"\" is not a Folder");				
			}
			//Проверка на указание дополнительных параметров
			if(args.length > 2) {
				int index = 0;
				int argStringLength = 0;
				//Чтение дополнительных параметров
				for(int i=2; i<args.length; i++) {
					if(args[i].startsWith("/") || args[i].startsWith("-")) {
						argStringLength = args[i].length();
						//Проверка на наличие знака присвоения
						index = args[i].indexOf("=");
						index = index!=-1? index : argStringLength;
						//Распознание дополнительных параметров
						switch(args[i].substring(1, index)) {
							//Дополнительный параметр - "/quiet" -> QuietMode
							case "quiet" : {
								isQuietMode = true;
								outMessage("QuietMode: "+isQuietMode);
								break;
							}
							//Дополнительный параметр - "/charset=XYZ" -> Playlist Charset
							case "charset" : {
								if( index+1 < argStringLength ) {
									playlistEncodingCharset = args[i].substring(index+1, argStringLength);
								}
								outMessage("Playlist encoding Charset: "+playlistEncodingCharset);
								break;
							}
							//В данной версии по умолчанию ничего не делаем
							default :{}
						}
					}
				}
			}
		}
		//Сообщаем об успешности распознания параметров
		return isKnowedPLS && isKnowedDirectory;
	}
	
	protected static void outMessage(String message) {
		outMessageBeforeStarting = outMessageBeforeStarting+message+"\n";		
	}

	protected static void errMessage(String message) {
		errMessageBeforeStarting = errMessageBeforeStarting+message+"\n";		
	}

	protected static void messagesFlush() {
		System.out.println(outMessageBeforeStarting);
		System.err.println(errMessageBeforeStarting);
		outMessageBeforeStarting = "";
		errMessageBeforeStarting = "";		
	}

	protected static boolean setFileNameEncoding(String encodingCharset) {
		boolean isSuccessful = false;
		String outMessage = "";
		String errMessage = "";
		//Чтение кодировки имен файлов по умолчанию
		try {
			fileNameEncodingCharset = System.getProperty("file.encoding");
		}catch(SecurityException err) {
			errMessage = err.getMessage();
		}catch(NullPointerException err) {
			errMessage = err.getMessage();		
		}catch(IllegalArgumentException err) {
			errMessage = err.getMessage();			
		}
		//Сравнение кодировок по умолчанию с требуемой и попытка изменения
		if( encodingCharset.equalsIgnoreCase( fileNameEncodingCharset ) ) {
			//Указанная кодировка совпадает с системным значением
			isSuccessful = true;
			outMessage = "Charset is "+fileNameEncodingCharset;
		}else {
			//Попытка изменения кодировки
			try{
				System.setProperty("file.encoding", encodingCharset);
				isSuccessful = true;
				//Перестраховка и чтение измененной кодировки для дальнейшей работы
				fileNameEncodingCharset = System.getProperty("file.encoding");
			}catch(SecurityException err) {
				errMessage = err.getMessage();
			}catch(NullPointerException err) {
				errMessage = err.getMessage();			
			}catch(IllegalArgumentException err) {
				errMessage = err.getMessage();			
			}
			//Сообщаем об успешности изменения кодировки
			outMessage = isSuccessful? "Charset is changed to "+fileNameEncodingCharset : "Error when changing charset to "+encodingCharset;			
		}
		outMessage(outMessage);
		errMessage(errMessage);
		return isSuccessful;
	}
	
}

class PLSFile{
	//Подготовка к разбору файла
	protected final String PLAYLIST_HEAD = "[playlist]";
	protected final String NUMBER_OF_ENTRIES = "numberofentries";
	protected final String FILE_NAME = "file";
	protected final String TRACK_TITLE = "title";
	protected final String TRACK_lENGTH = "length";
	protected final String EQUAL_SIGN = "=";
	protected final String EOL = "\n";
	protected enum PLSItemType{
		PLAYLIST_HEAD,
		NUMBER_OF_ENTRIES,
		FILE_NAME,
		TRACK_TITLE,
		TRACK_lENGTH,
		EQUAL_SIGN,
		EOL,
		EMPTY
	}
	private PLSFileEntry[] entries = null; //Массив записей плейлиста
	
	//!!! NEED UPDATE! Код будет оптимизирован, plsData будет перемещена !!! 
	private ArrayList<PLSFileEntry> plsData = new ArrayList<PLSFileEntry>();
	
	private String playlistEncodingCharset = "UTF-8"; //По умолчанию - UTF-8
	private String fileNameEncodingCharset = "UTF-8"; //По умолчанию - UTF-8
	
	//Пустой конструктор со значениями по умолчанию
	public PLSFile() {
		//В данной версии ничего не делаем
	}
	
	//Конструктор с именем файла
	public PLSFile(String fileName) {
		this.entries =  this.readPlsFile(fileName);
	}

	//Конструктор с именем файла и кодировкой содержиомого файла
	public PLSFile(String fileName, String playlistEncoding) {
		this.entries =  this.readPlsFile(fileName);
		this.playlistEncodingCharset = playlistEncoding;
	}
	
	//Конструктор с именем файла и кодировкой содержиомого файла и кодировкой имени файла	
	public PLSFile(String fileName, String playlistEncoding, String fileNameEncoding) {
		this.entries =  this.readPlsFile(fileName);
		this.playlistEncodingCharset = playlistEncoding;
		this.fileNameEncodingCharset = fileNameEncoding;
	}

	public PLSFileEntry[] readPlsFile(String fileName){
		return this.readPlsFile(fileName, this.playlistEncodingCharset, this.fileNameEncodingCharset);
	}

	public PLSFileEntry[] readPlsFile(String fileName, String playlistEncoding, String fileNameEncoding){
		return this.readPlsFile(new StringCharsetConverter().convertStringCharset(fileName, fileNameEncoding), playlistEncoding);
	}	
	
	public PLSFileEntry[] readPlsFile(String fileName, String playlistEncoding){
		//Подготовка буффера для чтения
		BufferedInputStream inStream = null;
		byte buffer[] = new byte[0];
		String stringBuffer = new String();
		//Чтение и ловля ошибок при чтении
		try{
			inStream = new BufferedInputStream(new FileInputStream(fileName));
			buffer = new byte[(int)(new File(fileName)).length()];
			inStream.read( buffer, 0, buffer.length);
			stringBuffer = new String(buffer, playlistEncoding); //Расшифровка массива buffer с использованием заданной кодировки charset (playlistEncoding)
			inStream.close();	
		}catch(FileNotFoundException err){
			System.err.println(err.getMessage());
		}catch(IOException err){
			System.err.println(err.getMessage());
		}catch(Exception err){
			System.err.println(err.getMessage());
		}finally{
			buffer = null;
		}
		return this.parsePlaylist(stringBuffer);
	}

	protected PLSFileEntry[] parsePlaylist(String playlist){
		//Подготовка к разбору
		boolean isDone = false;
		boolean isPlsFile = false;
		int index = -1;
		int lastIndex = -1;
		int length = playlist.length();
		String record = null;
		//Разбор входящих данных
		while(!isDone){
			//Определение строки
			lastIndex = index;
			index = playlist.indexOf(this.EOL, lastIndex+1);
			if (index != -1){
				record = playlist.substring(lastIndex + 1,  index - 1);
			}else{
				record = playlist.substring(lastIndex + 1,  length);
			}
			//Разбор конкретной строки записи
			if(isPlsFile){
				//Разбор записи и её значения
				int subindex = record.indexOf(this.EQUAL_SIGN);
				String itemName = "";
				int itemNameLength = 0;
				int itemNumber = 0;
				String itemValue = "";
				PLSItemType itemType = PLSItemType.EMPTY;
				if(subindex > 0) {
					itemName = record.substring(0, subindex);
					itemNameLength = itemName.length();
					//Проверка типа записи
					if(itemName.startsWith(this.FILE_NAME)) {
						itemNameLength = this.FILE_NAME.length();
						itemType = PLSItemType.FILE_NAME;
					}
					if(itemName.startsWith(this.TRACK_TITLE)) {
						itemNameLength = this.TRACK_TITLE.length();
						itemType = PLSItemType.TRACK_TITLE;
					}
					if(itemName.startsWith(this.TRACK_lENGTH)) {
						itemNameLength = this.TRACK_lENGTH.length();
						itemType = PLSItemType.TRACK_lENGTH;
					}
					if(itemName.startsWith(this.NUMBER_OF_ENTRIES)) {
						itemNameLength = this.NUMBER_OF_ENTRIES.length();
						itemType = PLSItemType.NUMBER_OF_ENTRIES;
					}
					//Чтение имени параметра с учетом его длины
					itemName = itemName.substring(0, itemNameLength);
					if(subindex - itemNameLength > 0) {
						//Попытка чтения номера 
						try {
							itemNumber = Integer.parseInt(record.substring(itemNameLength, subindex));
						}catch(NumberFormatException err){
							System.err.println(err.getMessage());
						}
					}
					itemValue = record.substring(subindex+this.EQUAL_SIGN.length(), record.length());
					switch(itemType) {
						case FILE_NAME, TRACK_TITLE, TRACK_lENGTH -> {
							itemNumber = itemNumber-1; //В связи с тем, что счет в файле начинается с 1
							//Проверка соответствия номера записи размеру уже имеющегося архива, при необходимости - увеличение его емкости
							if(itemNumber > this.plsData.size()-1) {
								PLSFileEntry empty = new PLSFileEntry();
								for(int i=this.plsData.size()-1; i < itemNumber; i++) {
									this.plsData.add(empty);
								}
								empty = null;
							}
							try {
								PLSFileEntry plsItem= this.plsData.get(itemNumber);
								switch(itemType) {
									case FILE_NAME:
										plsItem.setFileName(itemValue);
										break;
									case TRACK_TITLE:
										plsItem.setEntryTitle(itemValue);
										break;
									case TRACK_lENGTH:
										plsItem.setEntryLength(itemValue);
										break;
									default :{}
								}
								this.plsData.set(itemNumber, plsItem);
							}catch(IndexOutOfBoundsException err) {
								System.err.println(err.getMessage());							
							}
							break;
						}
						case NUMBER_OF_ENTRIES -> {
							//Попытка гарантировать необходимый размер архива
							try{
								this.plsData.ensureCapacity(Integer.parseInt(itemValue));
							}catch(NumberFormatException err) {
								System.err.println(err.getMessage());								
							}
							break;
						}
						 //В данной версии по умолчанию ничего не делаем
						default -> {}
					}
				}
			}
			//Проверка на соответсвие типу содержимого 
			if(index>0 && record.startsWith(this.PLAYLIST_HEAD)) isPlsFile = true;
			//Условие выхода из цикла
			if(index +1 == length || index == -1) isDone = true;
		}
		return this.plsData.toArray(new PLSFileEntry[this.plsData.size()]);
	}	
	
	public PLSFileEntry[] getPLSFileEntries(){
		return this.entries;
	}
	
	public String[] getFileNames(){
		String[] fileNames=new String[this.entries.length];
		for(int i=0; i<this.entries.length; i++) {
			fileNames[i]=this.entries[i].getFileName();
		}
		return fileNames;
	}
}

class FilesCopier{
	public int FILES_AMOUNT_LIMIT = 255;
	public String SUBFOLDER_NAME = "Folder"; //В данной версии имя подпапок не выбирается. В дальнейшем - планируется через параметры с консоли
	public String FILECOUNTER_PREFFIX = "_";
	public String defaultEncodingCharset = "UTF-8"; //По умолчанию - UTF-8 
	public String fileNameEncodingCharset = "UTF-8"; //По умолчанию - UTF-8
	
	//Конструктор без параметров
	public FilesCopier(){
		//Чтение кодировки имен файлов по умолчанию
		try {
			defaultEncodingCharset = System.getProperty("file.encoding");
		}catch(SecurityException err) {
			System.err.println(err.getMessage());
		}catch(NullPointerException err) {
			System.err.println(err.getMessage());		
		}catch(IllegalArgumentException err) {
			System.err.println(err.getMessage());			
		}
	}
	
	//Конструктор с указанием кодировки имен файлов
	public FilesCopier(String encoding){
		//Запуск конструктора без параметров
		this();
		this.fileNameEncodingCharset = encoding;
	}
	
	public void putInOneFolder(String[] sourceNameFiles, String destinationNameFolder) {
		this.putInOneFolder(sourceNameFiles, destinationNameFolder, this.SUBFOLDER_NAME, this.FILECOUNTER_PREFFIX, this.fileNameEncodingCharset);
	}
	
	public void putInOneFolder(String[] sourceNameFiles, String destinationNameFolder, String encoding) {
		this.putInOneFolder(sourceNameFiles, destinationNameFolder, this.SUBFOLDER_NAME, this.FILECOUNTER_PREFFIX, encoding );
	}
	
	
	public void putInOneFolder(String[] sourceNameFiles, String destinationNameFolder, String subFolderName, String FileCounterPreffix, String encoding) {
		Path destinationPath = null;
		String[] destinationNameFiles = new String[sourceNameFiles.length];
		int folderIndex=1;
		int fileCounter=1;
		for(int i=0; i<sourceNameFiles.length; i++) {
			try{
				destinationPath = Paths.get(destinationNameFolder).resolve(Paths.get(subFolderName+folderIndex, fileCounter+FileCounterPreffix+Paths.get(sourceNameFiles[i]).getFileName().toString()));
				//Распределение по папкам
				if(fileCounter < this.FILES_AMOUNT_LIMIT) {
					fileCounter++;
				}else {
					fileCounter=1;
					folderIndex++;
				}
				//Составление списка назначения
				destinationNameFiles[i]=destinationPath.toAbsolutePath().toString();
			}catch(InvalidPathException err) {
				System.err.println(err.getMessage());
				sourceNameFiles[i] = null;
				destinationNameFiles[i] = null;
			}
		}
		this.copyManyFiles(sourceNameFiles, destinationNameFiles, encoding);
	}
	
	public void copyManyFiles(String[] sourceNameFiles, String[] destinationNameFiles, String encoding) {
		//Проверка на соответствие указанной кодировки и кодировки по умолчанию 
		if( encoding.equalsIgnoreCase(this.defaultEncodingCharset) ) {
			this.copyManyFiles(sourceNameFiles, destinationNameFiles);
		}else {
			//Конвертация строк в указанную кодировку
			StringCharsetConverter converter = new StringCharsetConverter(encoding);
			this.copyManyFiles(converter.convertStringsCharset(sourceNameFiles), converter.convertStringsCharset(destinationNameFiles));			
		}
	}
	
	public void copyManyFiles(String[] sourceNameFiles, String[] destinationNameFiles) {
		for(int i=0; i<sourceNameFiles.length && i<destinationNameFiles.length; i++) {
			if(sourceNameFiles[i] != null && destinationNameFiles[i] != null) {
				this.copyFile(sourceNameFiles[i], destinationNameFiles[i]);
			}
		}		
	}

	public void copyFile(String sourceNameFile, String destinationNameFile, String encoding) {
		//Проверка на соответствие указанной кодировки и кодировки по умолчанию 
		if( encoding.equalsIgnoreCase(this.defaultEncodingCharset) ) {
			this.copyFile(sourceNameFile, destinationNameFile);
		}else {
			//Конвертация строк в указанную кодировку
			StringCharsetConverter converter = new StringCharsetConverter(encoding);
			this.copyFile(converter.convertStringCharset(sourceNameFile), converter.convertStringCharset(destinationNameFile));
		}
	}
	
	public void copyFile(String sourceNameFile, String destinationNameFile) {
		//Подготовка буффера для чтения
		BufferedInputStream inStream = null;
		BufferedOutputStream outStream = null;
		byte buffer[] = new byte[0];
		//Чтение и ловля ошибок при чтении
		String outMessage = "Now Coping: \""+sourceNameFile+"\"\n -> \""+destinationNameFile+"\"...";
		String errMessage = null;
		//INFORMATION_MESSAGE
		System.out.print(outMessage);
		try{
			inStream = new BufferedInputStream(new FileInputStream(sourceNameFile));
			//Создание новых папок
			Files.createDirectories(Paths.get(destinationNameFile).getParent());
			outStream = new BufferedOutputStream(new FileOutputStream(new File(destinationNameFile), false)); // Перезапись файла в случае наличия
			buffer = new byte[(int)(new File(sourceNameFile)).length()];
			inStream.read( buffer, 0, buffer.length);
			outStream.write(buffer);
			inStream.close();
			outStream.close();
			outMessage = "OK";
		}catch(FileNotFoundException err){
			outMessage = "File not found";
			errMessage = err.getMessage();
		}catch(IOException err){
			outMessage = "IO error\n";
			errMessage = err.getMessage();
		}catch(Exception err){
			outMessage = "Unexpected error\n";
			errMessage = err.getMessage();
		}//finally{}
		//INFORMATION_MESSAGE
		System.out.println(outMessage);
		if(errMessage != null) {
			System.err.println(errMessage);
		}
	}
	
}

class PLSFileEntry{
	//protected int entryNumber = 0;
	protected String fileName = "";
	protected String entryLength = "0";
	protected String entryTitle = "";
	
	public PLSFileEntry(){
		
	}
	
	/*public int getEntryNumber(){
		return this.entryNumber;
	}*/
	
	public String getFileName(){
		return this.fileName;
	}
	
	public String getEntryLength(){
		return this.entryLength;
	}

	public String getEntryTitle(){
		return this.entryTitle;
	}
	/*public void setEntryNumber(int num){
		this.entryNumber = num;
	}*/
	
	public void setFileName(String name){
		this.fileName = name;
	}

	public void setEntryLength(String length){
		this.entryLength = length;
	}

	public void setEntryTitle(String title){
		this.entryTitle = title;
	}
	
	public String toString() {
		//Данная функция написана исключительно для отладки программы
		//В будущем её использование не планируется
		return "Title: "+this.entryTitle+"\nFileName: "+this.fileName+"\nLength: "+this.entryLength;
	}
}

class StringCharsetConverter{
	private String defaultCharsetEncoding = "UTF-8"; //по умолчанию - UTF-8
	
	//Конструктор без параметров
	public StringCharsetConverter(){
		//В данной версии ничего не делаем
	}
	
	//Контсруктор с указанием конечной кодировки по умолчанию
	public StringCharsetConverter(String encoding){
		this.defaultCharsetEncoding = encoding;
	}
	
	public String[] convertStringsCharset(String[] strings){
		return this.convertStringsCharset(strings, this.defaultCharsetEncoding);
	}
	
	public String convertStringCharset(String string) {
		return this.convertStringCharset(string, this.defaultCharsetEncoding);
	}

	
	public String[] convertStringsCharset(String[] strings, String encoding){
		String[] convertedStrings = new String[strings.length];
		for(int i=0; i < strings.length ; i++) {
			if(strings[i] !=null) {
				convertedStrings[i] = this.convertStringCharset(strings[i], encoding);
			}else {
				convertedStrings[i] = strings[i];
			}
		}
		return convertedStrings;
	}
	
	public String convertStringCharset(String string, String encoding) {
		String convertedString = null;
		//Попытка перекодировать строку в новую кодировку;
		try {
			convertedString = new String( string.getBytes(encoding), encoding );
		}catch(UnsupportedEncodingException err) {
			System.err.println(err.getMessage());
			convertedString = string;
		}

		return convertedString;
	}
}

