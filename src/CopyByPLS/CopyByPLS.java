package CopyByPLS;

/*
ЭВМбз-22-1
Plotnikov Aleksey
*/

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

	public static void main(String[] args) {
		//Подготовка к работе
		if(parseArgs(args)){
			//Перенаправление потоков вывода сообщений и ошибок
			setLogOutputStreams(outLogFileName, errorLogFileName);
			PLSFile plsFile = new PLSFile(plsFileName);
			String[] fileNames = plsFile.getFileNames();
			FilesCopier copier = new FilesCopier();
			copier.putInOneFolder(fileNames, DestinationFolderName);
		}else {
			System.out.println("Two arguments are expected to run this program:\n"
					+ "1) pls-file name 2) destination folder\n"
					+ "(for example: \"file.pls\" \"Folder\" )");
		}
	}

	public static void setLogOutputStreams(String strOutLogFileName, String strErrorLogFileName) {
		if(isQuietMode) {
			//Перенаправление потока вывода сообщений для отладки в режиме QuietMode
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
			if(new File(args[0]).exists()) {
				plsFileName = args[0];
				outMessage("Input source of copied file names: "+plsFileName);
				isKnowedPLS = true;
			}
			if(new File(args[1]).isDirectory()) {
				DestinationFolderName = args[1];
				outMessage("Destination folder: "+DestinationFolderName);
				isKnowedDirectory = true;
			}
			if(args.length > 2) {
				for(int i=2; i<args.length; i++) {
					if(args[i].startsWith("/") || args[i].startsWith("-")) {
						switch(args[i].substring(1, args[i].length())) {
							//В данной версии проверяется один единственный дополнительный параметр - "/quiet" -> QuietMode
							case "quiet" : {
								isQuietMode = true;
								break;
							}
							default :{} //В данной версии по умолчанию ничего не делаем
						}
					}
				}
			}
		}
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
	protected PLSFileEntry[] entries = null;
	protected ArrayList<PLSFileEntry> plsData = new ArrayList<PLSFileEntry>();
	
	public PLSFile() {
	}
	
	public PLSFile(String fileName) {
		this.entries =  this.readPlsFile(fileName);
	}
	
	public PLSFileEntry[] readPlsFile(String fileName){
		//Подготовка буффера для чтения
		BufferedInputStream inStream = null;
		byte buffer[] = new byte[0];
		String stringBuffer = new String();
		//Чтение и ловля ошибок при чтении
		try{
			inStream = new BufferedInputStream(new FileInputStream(fileName));
			buffer = new byte[(int)(new File(fileName)).length()];
			inStream.read( buffer, 0, buffer.length);
			stringBuffer = new String(buffer);
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
		return parsePlaylist(stringBuffer);
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
						default -> {} //В данной версии по умолчанию ничего не делаем
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
	public String SUBFOLDER_NAME = "Folder"; ////В данной версии имя подпапок не выбирается
	public String FILECOUNTER_PREFFIX = "_";
	
	public FilesCopier(){
	}
	
	public void putInOneFolder(String[] sourceNameFiles, String destinationNameFolder) {
		//Path sourcePath = null;
		Path destinationPath = null;
		String[] destinationNameFiles = new String[sourceNameFiles.length];
		int folderIndex=1;
		int fileCounter=1;
		for(int i=0; i<sourceNameFiles.length; i++) {
			try{
				//sourcePath=Paths.get(folderIndex+"-"+fileCounter+1+"-"+Paths.get(sourceNameFiles[i]).getFileName().toString() );
				destinationPath = Paths.get(destinationNameFolder).resolve(Paths.get(this.SUBFOLDER_NAME+folderIndex, fileCounter+this.FILECOUNTER_PREFFIX+Paths.get(sourceNameFiles[i]).getFileName().toString()));
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
		this.copyManyFiles(sourceNameFiles, destinationNameFiles);
	}
	
	public void copyManyFiles(String[] sourceNameFiles, String[] destinationNameFiles) {
		for(int i=0; i<sourceNameFiles.length && i<destinationNameFiles.length; i++) {
			if(sourceNameFiles[i] != null && destinationNameFiles[i] != null) {
				this.copyFile(sourceNameFiles[i], destinationNameFiles[i]);
			}
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
			outStream = new BufferedOutputStream(new FileOutputStream(new File(destinationNameFile)));
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
		return "Title: "+entryTitle+"\nFileName: "+fileName+"\nLength: "+entryLength;
	}
}


