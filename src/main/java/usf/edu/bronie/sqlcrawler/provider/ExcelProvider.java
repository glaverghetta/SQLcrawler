package usf.edu.bronie.sqlcrawler.provider;

import java.util.ArrayList;

import com.google.gson.Gson;

import usf.edu.bronie.sqlcrawler.constants.UrlConstants;
import usf.edu.bronie.sqlcrawler.io.HttpConnection;
import usf.edu.bronie.sqlcrawler.model.GithubData;
import usf.edu.bronie.sqlcrawler.model.RootGitObject;
import usf.edu.bronie.sqlcrawler.model.SearchData;
import java.io.*;  
import java.util.Scanner;  

/** 
 * This code pulls links from a .csv file in the format name, url
 * And stores them not-persistently to be used by the crawler
 * @author bian
 * TODO: Want to be able to pull links from differing places, i.e. github links, bitbucket links
 */
public class ExcelProvider implements SourceCodeProvider{
	private ArrayList<GithubData> mGithubData;
    private int mIndex = 0;
    private String fileName;
    
	@Override
	public boolean hasNext() {
		return mIndex < mGithubData.size();
	}

	@Override
	public void pollData() {
        System.out.println("\rFetching data...");
        File file = new File(fileName);
        
        Scanner sc;
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.out.println("Invalid file name: " + fileName);
			System.exit(-1);
			return;
		}  
        sc.useDelimiter(",");   // CSV delimiter pattern
        // Reading from a .CSV file
        mGithubData = new ArrayList<GithubData>();
        while (sc.hasNext())   {
        	String name = sc.next();
        	if(sc.hasNext()) {
        		String url = sc.next();
        		mGithubData.add(new GithubData(name, url));
        	}
        }
         
        sc.close();   
        System.out.println(" ");
        System.out.println(" -------------------------------------- ");
        System.out.println("Total number of files to analyze: " + mGithubData.size());
        System.out.println(" -------------------------------------- ");
    }  

    public SearchData receiveNextData() {
        GithubData githubData = mGithubData.get(mIndex);
        String encode = githubData.getUrl().replaceAll(" ", "%20");
        String code = HttpConnection.get(encode);
        SearchData searchData = new SearchData(githubData.getUrl(), githubData.getName());
        searchData.setCode(code);
        mIndex++;
        return searchData;
    }
    
    public void setFile(String fileName) {
    	this.fileName = fileName;
    }
    
    public void printCurrentArray() {
    	 System.out.println(" -------------------------------------- ");
         System.out.println("Printing current provider data");
         System.out.println(" -------------------------------------- ");
    	for(int i = 0; i < mGithubData.size(); i++) {
    		System.out.print(mGithubData.get(i).getName());
    		System.out.print(" ");
    		System.out.println(mGithubData.get(i).getUrl());
    	}
    	System.out.println(" -------------------------------------- ");
        System.out.println("Total number of files: " + mGithubData.size());
        System.out.println(" -------------------------------------- ");
    }


}
