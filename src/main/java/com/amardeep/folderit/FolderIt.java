/**
 *    Copyright (C) 2017 Amardeep Bhowmick <amardeep.bhowmick@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.amardeep.folderit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipException;
import org.zeroturnaround.zip.ZipUtil;

/**
 * @author Amardeep Bhowmick
 *
 *This application organizes files in a directory according to their file type into directories.
 *
 */
public class FolderIt 
{
	private final String jarFileName;
	static Logger LOGGER = LoggerFactory.getLogger(FolderIt.class);
	
	private FolderIt(){
		jarFileName=new java.io.File(FolderIt.class.getProtectionDomain()
  			  .getCodeSource()
  			  .getLocation()
  			  .getPath())
  			  .getName();
		String osName=System.getProperty("os.name");
		LOGGER.info("OS detected: "+osName);
	}
	/**
	 * Entry point to the application.
	 * 
	 * @param args
	 * 
	 */
    public static void main( String[] args )
    {
    	String currentDirectory=System.getProperty("user.dir");
    	String pattern="";
    	String[] directoryNames=null;
    	List<String> dirAliasNames=new ArrayList<>();
    	if( args.length >=2 && "-p".equalsIgnoreCase( args[0] )  ) {
    		pattern=args[1];
    		directoryNames=pattern.split(",");
    		if(args.length>=4 && args[2]!=null && "-a".equalsIgnoreCase(args[2])){
    			if("zip".equalsIgnoreCase(args[3].toLowerCase())){
    				LOGGER.info("Incorrect arguments application exiting!");
    	        	LOGGER.info("Example usage: java -jar folderit-x.x.x -p [String,String,..] -a [String,String] zip");
    	        	System.exit(1);
    			}
    			dirAliasNames=Arrays.asList(args[3].split(","));
    		}else if(args.length>=4 && args[2]!=null && !"-a".equalsIgnoreCase(args[2])){
    			LOGGER.info("Incorrect arguments application exiting!");
	        	LOGGER.info("Example usage: java -jar folderit-x.x.x -p [String,String,..] -a [String,String] zip");
	        	System.exit(1);
    		}
    		LOGGER.info("Creating directories from cmd arguments if filename is matched in keyword: "+args[1]);
        }else if(args.length>0 && !"-p".equalsIgnoreCase(args[0])){
        	LOGGER.info("Incorrect arguments application exiting!");
        	LOGGER.info("Example usage: java -jar folderit-x.x.x -p [String,String,..] -a [String,String] zip");
        	System.exit(1);
        }
    	FolderIt folderIt=new FolderIt();
    	File folder = new File(currentDirectory);
    	//Get list of all files
    	List<File> files=new ArrayList<>(Arrays.asList(folder.listFiles()));
    	//Get list of directories
    	List<File> directories=new ArrayList<>();
    	for(File file:files){
    		if(file.isDirectory()){
    			directories.add(file);
    		}
    	}
    	//Not processing directories
    	files.removeAll(directories);
    	Set<String> fileNames=null;
    	List<File> fileListFilteredByName=null;
    	//If cmd arguments are present
    	if(directoryNames!=null && directoryNames.length>0){
    		LOGGER.info("Creating directories using cmd options...");
    		final List<String> directoryInputList=Arrays.asList(directoryNames);
    		//Getting list of matched files
    		fileListFilteredByName=files.stream()
    					   .filter(file->{
    						   String fileName=file.getName();
    						   return folderIt.matchInList(directoryInputList,fileName);
    					   })
    					   .collect(Collectors.toList());
    		LOGGER.info("Files matched: "+fileListFilteredByName.size());
    		//Exiting if no matching file found
    		if(fileListFilteredByName!=null && fileListFilteredByName.isEmpty()){
    			LOGGER.info("No matching files found, application exiting!");
    			System.exit(1);
    		}
    		Map<String,File> dirPath=folderIt.createDirectoriesUsingName(directoryInputList, dirAliasNames,currentDirectory);
    		folderIt.moveFilesUsingName(fileListFilteredByName,dirAliasNames, dirPath);
    		if(args[args.length-1]!=null && "zip".equalsIgnoreCase(args[args.length-1])){
    			dirPath.forEach((dirName,dir)->{
    				folderIt.zipIt(dirName,dir,currentDirectory);
    			});
    		}
    		
    		
    	}else{
    		LOGGER.info("Creating directories using file types...");
    		fileNames=files.stream().map(File::getName).collect(Collectors.toSet());
    		Map<String,List<String>> mapOfFiles = folderIt.getFileMapping(fileNames);
        	Map<String,File> dirPathsCreated=folderIt.createDirectoriesUsingType(mapOfFiles,currentDirectory);
        	folderIt.moveFiles(files,dirPathsCreated);
    	}
    	
    	
    }
    /**
     * This method creates directories from the mapping file containing the extensions and files.
     *  
     * @param mapOfFiles
     * @param currentDirectory
     * @return java.util.Map
     * @author Amardeep Bhowmick
     * 
     */
    private Map<String,File> createDirectoriesUsingType(Map<String,List<String>> mapOfFiles,final String currentDirectory){
    	final Map<String,File> dirPaths=new HashMap<>();
    	mapOfFiles.forEach((extn,files)->{
    		String pathToDir=currentDirectory+File.separator+extn;
    		File directory=new File(pathToDir);
    		if (Files.exists(directory.toPath())) {
    			directory.delete();
    		} 
    		directory.mkdirs();
    		dirPaths.put(extn, directory);
    	});
    	return dirPaths;
    }
    /**
     * 
     * This method creates directories using the cmd options given.
     * 
     * @param directoryInputList
     * @param currentDirectory
     * @return java.util.Map
     * @author Amardeep Bhowmick
     * 
     */
    private Map<String,File> createDirectoriesUsingName(List<String> directoryInputList,List<String> dirAliasNames,final String currentDirectory){
		final Map<String, File> dirPaths = new HashMap<>();
		if (dirAliasNames != null && !dirAliasNames.isEmpty()) {
			IntStream.range(0, directoryInputList.size())
					 .forEach(idx->{
						 String dirName=(dirAliasNames.size()-1)>=idx?dirAliasNames.get(idx):directoryInputList.get(idx);
						 String dirNameActual=directoryInputList.get(idx);
						 String pathToDir = currentDirectory + File.separator + dirName;
						 File directoryToCreate = new File(pathToDir);
							if (Files.exists(directoryToCreate.toPath())) {
								directoryToCreate.delete();
							}
							directoryToCreate.mkdir();
							dirPaths.put(dirNameActual+":"+dirName, directoryToCreate);
					 });
		} else {
			directoryInputList.stream().forEach(dirName -> {
				String pathToDir = currentDirectory + File.separator + dirName;
				File directoryToCreate = new File(pathToDir);
				if (Files.exists(directoryToCreate.toPath())) {
					directoryToCreate.delete();
				}
				directoryToCreate.mkdir();
				dirPaths.put(dirName, directoryToCreate);
			});
		}
    	return dirPaths;
    }
    /**
     * 
     * This method moves the files from the current directory to the directory created by this application.
     * 
     * @param sourceFiles
     * @param dirPathsCreated
     * @author Amardeep Bhowmick
     * 
     */
    private void moveFiles(final List<File> sourceFiles,Map<String,File> dirPathsCreated){
		sourceFiles.stream().filter(file -> {
			return !(jarFileName.equalsIgnoreCase(file.getName()));
		}).forEach(file -> {
			String extn = file.getName().substring(file.getName().lastIndexOf(".") + 1);
			File destinationDir = dirPathsCreated.get(extn);
			try {
					Files.copy(file.toPath(),
							new File(destinationDir.getAbsolutePath() + File.separator + file.getName()).toPath(),
							StandardCopyOption.REPLACE_EXISTING);
					file.deleteOnExit();
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		});
    	
    }
    /**
     * This method moves the files to the directories created from the cmd options.
     *  
     * @param sourceFiles
     * @param dirPathsCreated
     * @author Amardeep Bhowmick
     * 
     */
    private void moveFilesUsingName(final List<File> sourceFiles,List<String> dirAliasName,Map<String,File> dirPathsCreated){
    	List<File> listOfDirsToBeCreated=sourceFiles
    									.stream()
    									.filter(file -> {
    										return !(jarFileName.equalsIgnoreCase(file.getName()));
    									})
    									.collect(Collectors.toList());
    	
    	IntStream.range(0, listOfDirsToBeCreated.size())
    		.forEach(idx -> {
    			File file=listOfDirsToBeCreated.get(idx);
    			File dirToCopy=null;
				try {
					for(Map.Entry<String,File> entry:dirPathsCreated.entrySet()){
						String namesToSearch[]=entry.getKey().toLowerCase().split(":");
						//System.out.println(namesToSearch.length==2?namesToSearch[1]+"serach "+file.getName().toLowerCase().contains(namesToSearch[1])+file.getName().toLowerCase():namesToSearch[0]+"serach "+file.getName().toLowerCase().contains(namesToSearch[0])+file.getName().toLowerCase());
						if(file.getName().toLowerCase().contains(namesToSearch[0])){
							dirToCopy=entry.getValue();
							
						}
					}
					Files.copy(file.toPath(),
							new File(dirToCopy.getAbsolutePath() + File.separator + file.getName()).toPath(),
							StandardCopyOption.REPLACE_EXISTING);
					file.deleteOnExit();
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		});
    }
    /**
     * 
     * This method creates a mapping of file extension with the actual files.
     *  
     * @param fileNames
     * @return java.util.Map
     * @author Amardeep Bhowmick
     *  
     */
    private Map<String,List<String>> getFileMapping(Set<String> fileNames){
    	return fileNames.stream()
    			.filter(file->{
    				return !(jarFileName.equalsIgnoreCase(file));
    			})
    			.collect(Collectors.groupingBy((fileName)->{
    		return fileName.substring(fileName.lastIndexOf('.')+1);
    	}));
    }
    /**
     * This method matches the keyword in the filename.
     * 
     * @param directoryInputList
     * @param fileName
     * @return boolean
     * @author Amardeep Bhowmick
     * 
     */
    private boolean matchInList(List<String> directoryInputList,String fileName){
    	for(String name:directoryInputList){
    		if(fileName.toLowerCase().contains(name.toLowerCase())){
    			return true;
    		}
    	}
    	return false;
    }
    /**
     * 
     * This method zips the directory created
     * 
     * @param directory
     */
    private void zipIt(String directoryName,File directory,String currentDirectory){
		try {
			String names[] = directoryName.split(":");
			String zipFileName = null;
			if (names.length == 2) {
				zipFileName = currentDirectory + File.separator + names[1] + ".zip";
				File zipFile = new File(zipFileName);
				if (Files.exists(zipFile.toPath())) {
					zipFile.delete();
				}
			} else {
				zipFileName = currentDirectory + File.separator + names[0] + ".zip";
				File zipFile = new File(zipFileName);
				if (Files.exists(zipFile.toPath())) {
					zipFile.delete();
				}
			}
			LOGGER.info("Zip file name: " + zipFileName);
			ZipUtil.pack(directory, new File(zipFileName));
			LOGGER.info("Zip file created!");
		}catch(ZipException ze){
			LOGGER.error("Error creating zip file!");
			System.exit(1);
    	}
    }
}
