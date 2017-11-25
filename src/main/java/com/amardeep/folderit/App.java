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
 */
public class App 
{
	private final String scriptFileName;
	private final String jarFileName;
	App(){
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
    public static void main( String[] args )
    {
    	String currentDirectory=System.getProperty("user.dir");
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
    	Set<String> fileNames=files.stream().map(File::getName).collect(Collectors.toSet());
    	Map<String,List<String>> mapOfFiles = thisApp.getFileMapping(fileNames);
    	Map<String,File> dirPathsCreated=thisApp.createDirectories(mapOfFiles,currentDirectory);
    	thisApp.moveFiles(files,dirPathsCreated);
    }
    private Map<String,File> createDirectories(Map<String,List<String>> mapOfFiles,final String dirPath){
    	final Map<String,File> dirPaths=new HashMap<>();
    	mapOfFiles.forEach((extn,files)->{
    		File directory=new File(dirPath+File.separator+extn);
    		directory.mkdirs();
    		dirPaths.put(extn, directory);
    	});
    	return dirPaths;
    }
    private void moveFiles(final List<File> sourceFiles,Map<String,File> dirPathsCreated){
		sourceFiles.stream().filter(file -> {
			return !(scriptFileName.equalsIgnoreCase(file.getName())
					|| jarFileName.equalsIgnoreCase(file.getName()));
		}).forEach(file -> {
			String key = file.getName().substring(file.getName().lastIndexOf(".") + 1);
			File destinationDir = dirPathsCreated.get(key);
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
}
