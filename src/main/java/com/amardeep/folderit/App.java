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

/**
 * @author Amardeep Bhowmick
 *
 *This application organizes files in a directory according to their file type into directories.
 *
 */
public class App 
{
	private final String scriptFileName;
	private final String jarFileName;
	private App(){
		jarFileName=new java.io.File(App.class.getProtectionDomain()
  			  .getCodeSource()
  			  .getLocation()
  			  .getPath())
  			  .getName();
		String osName=System.getProperty("os.name");
		if((osName).contains("Windows")){
    		scriptFileName=jarFileName.substring(0,jarFileName.lastIndexOf("."))+".cmd";
    	}else{
    		scriptFileName=jarFileName.substring(0,jarFileName.lastIndexOf("."))+".sh";
    	}
		System.out.println("OS detected: "+osName+ " Ignoring script file: "+scriptFileName);
	}
	/**
	 * @param args
	 * 
	 * Entry point to the application.
	 */
    public static void main( String[] args )
    {
    	String currentDirectory=System.getProperty("user.dir");
    	String pattern="";
    	String[] directoryNames=null;
    	if( args.length >=2 && args[0].equalsIgnoreCase( "p" )  ) {
    		pattern=args[1];
    		directoryNames=pattern.split(",");
    		System.out.println("Creating directories for cmd arguments if filename is available: "+args[1]);
        }else if(args.length>0 && !args[0].equalsIgnoreCase("p")){
        	System.out.println("Incorrect params entered, application exiting!");
        	System.out.println("Example usage: java -jar folderit-x.x.x p <String,String,..>");
        	System.exit(1);
        }
    	App thisApp=new App();
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
    	//Not proceessing directories
    	files.removeAll(directories);
    	Set<String> fileNames=null;
    	List<File> fileListFilteredByName=null;
    	//If cmd arguments are present
    	if(directoryNames!=null && directoryNames.length>0){
    		System.out.println("Creating directories using cmd options...");
    		final List<String> directoryInputList=Arrays.asList(directoryNames);
    		fileListFilteredByName=files.stream()
    					   .filter(file->{
    						   String fileName=file.getName();
    						   return thisApp.matchInList(directoryInputList,fileName);
    					   })
    					   .collect(Collectors.toList());
    		System.out.println(fileListFilteredByName.size());
    		Map<String,File> dirPath=thisApp.createDirectoriesUsingName(directoryInputList, currentDirectory);
    		thisApp.moveFilesUsingName(fileListFilteredByName, dirPath);
    		
    		
    	}else{
    		System.out.println("Creating directories using file types...");
    		fileNames=files.stream().map(File::getName).collect(Collectors.toSet());
    		Map<String,List<String>> mapOfFiles = thisApp.getFileMapping(fileNames);
        	Map<String,File> dirPathsCreated=thisApp.createDirectoriesUsingType(mapOfFiles,currentDirectory);
        	thisApp.moveFiles(files,dirPathsCreated);
    	}
    	
    	
    }
    /**
     * @param mapOfFiles
     * @param currentDirectory
     * @return java.util.Map
     * @author Amardeep Bhowmick
     * 
     * This method creates directories from the mapping file containing the extensions and files.
     */
    private Map<String,File> createDirectoriesUsingType(Map<String,List<String>> mapOfFiles,final String currentDirectory){
    	final Map<String,File> dirPaths=new HashMap<>();
    	mapOfFiles.forEach((extn,files)->{
    		File directory=new File(currentDirectory+File.separator+extn);
    		directory.mkdirs();
    		dirPaths.put(extn, directory);
    	});
    	return dirPaths;
    }
    /**
     * 
     * @param directoryInputList
     * @param currentDirectory
     * @return java.util.Map
     * @author Amardeep Bhowmick
     * 
     * This method creates directories using the cmd options given.
     */
    private Map<String,File> createDirectoriesUsingName(List<String> directoryInputList,final String currentDirectory){
    	final Map<String,File> dirPaths=new HashMap<>();
    	directoryInputList.stream().forEach(dirName->{
    		File directoryToCreate=new File(currentDirectory+File.separator+dirName);
    		directoryToCreate.mkdir();
    		dirPaths.put(dirName,directoryToCreate);
    	});
    	return dirPaths;
    }
    /**
     * 
     * @param sourceFiles
     * @param dirPathsCreated
     * @author Amardeep Bhowmick
     * 
     * This method moves the files from the current directory to the directory created by this application.
     */
    private void moveFiles(final List<File> sourceFiles,Map<String,File> dirPathsCreated){
		sourceFiles.stream().filter(file -> {
			return !(scriptFileName.equalsIgnoreCase(file.getName())
					|| jarFileName.equalsIgnoreCase(file.getName()));
		}).forEach(file -> {
			String extn = file.getName().substring(file.getName().lastIndexOf(".") + 1);
			File destinationDir = dirPathsCreated.get(extn);
			try {
					Files.copy(file.toPath(),
							new File(destinationDir.getAbsolutePath() + File.separator + file.getName()).toPath(),
							StandardCopyOption.REPLACE_EXISTING);
					file.deleteOnExit();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
    	
    }
    /**
     * 
     * @param sourceFiles
     * @param dirPathsCreated
     * @author Amardeep Bhowmick
     * 
     * This method moves the files to the directories created from the cmd options.
     */
    private void moveFilesUsingName(final List<File> sourceFiles,Map<String,File> dirPathsCreated){
    	sourceFiles.stream().filter(file -> {
			return !(scriptFileName.equalsIgnoreCase(file.getName())
					|| jarFileName.equalsIgnoreCase(file.getName()));
		}).forEach(file -> {
			try {
				File dirToCopy=null;
				for(Map.Entry<String,File> entry:dirPathsCreated.entrySet()){
					if(file.getName().toLowerCase().contains(entry.getKey().toLowerCase())){
						dirToCopy=entry.getValue();
					}
				}
				
				Files.copy(file.toPath(),
						new File(dirToCopy.getAbsolutePath() + File.separator + file.getName()).toPath(),
						StandardCopyOption.REPLACE_EXISTING);
				file.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
		});
    }
    /**
     * 
     * @param fileNames
     * @return java.util.Map
     * @author Amardeep Bhowmick
     *  
     * This method creates a mapping of file extension with the actual files.
     */
    private Map<String,List<String>> getFileMapping(Set<String> fileNames){
    	return fileNames.stream()
    			.filter(file->{
    				return !(scriptFileName.equalsIgnoreCase(file)
    						|| jarFileName.equalsIgnoreCase(file));
    			})
    			.collect(Collectors.groupingBy((name)->{
    		String fileName=name.toString();
    		return fileName.substring(fileName.lastIndexOf(".")+1);
    	}));
    }
    /**
     * @param directoryInputList
     * @param fileName
     * @return boolean
     * @author Amardeep Bhowmick
     * 
     * This method matches the keyword in the filename.
     */
    private boolean matchInList(List<String> directoryInputList,String fileName){
    	for(String name:directoryInputList){
    		if(fileName.toLowerCase().contains(name.toLowerCase())){
    			System.out.println(name+fileName);
    			return true;
    		}
    	}
    	return false;
    }
}
