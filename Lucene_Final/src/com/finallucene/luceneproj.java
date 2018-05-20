//    CS 7800 Information Retrieval Assignment 1
//   Group Members : Raakesh Chandrasekaran (U00832495)
//                   Shruti Kar (U00830293)


package com.finallucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.document.*;


import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import org.apache.lucene.search.Query;

import org.apache.lucene.queryparser.classic.QueryParser;


import org.apache.lucene.search.IndexSearcher;


public class luceneproj{
	
	
	private static StandardAnalyzer analyzer= new StandardAnalyzer();   //initializing the standard analyzer globally 
	
	private static IndexWriter writer; //initializing IndexWriter globally
	
	public luceneproj(String indexLoc) throws IOException {
		// TODO Auto-generated constructor stub
   
	    IndexWriterConfig iwc=new IndexWriterConfig(analyzer); 	// IndexWriterConfig stores the settings of IndexWriter
	  
		FSDirectory dir= FSDirectory.open(Paths.get(indexLoc));	// creates a directory on disk where the indexed files can be stored
		
		
		/* 
		 * IndexWriterConfiguration is set to CREATE mode since if there 
		 * exists already indexed files in the directory, it overwrites them
		*/
		iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);		
		
		
		writer =new IndexWriter(dir,iwc);	//IndexWriter creates and maintains indexes
		writer.commit();					
		
	}
	
	
	
	public static void main(String[] args) throws IOException{
		
		// Declaration of path of directory where Indexed files can be stored
		String dsLoc=null;
		String Idi="c:\\IndexCran";
		File IDX=new File(Idi);
		String IndexLoc=IDX.getAbsolutePath(); 
		
		
		//starts timer before starting to index
		Date strt=new Date();
		
			/*
			 * 
			 * 
			 * INDEXING
			 * 
			 * 
			 */
			
		luceneproj constr=null; 
		
		try{
			dsLoc=IndexLoc;
			constr = new luceneproj(IndexLoc); 
				
		}
		catch(Exception e){
		
			System.out.println("Index could not be created"+e.getMessage());
		}
		
		//location where the separated files are stored
		String LOC="CranfieldDataset";
		File dsl=new File(LOC);
		String DataSetLoc=dsl.getAbsolutePath();
		
		splitfunc(DataSetLoc); // reads .all file and separates the contents into new files 
		
				
		/*
		 * Reading each file content into a document doc
		 * with set of fields like title, abstract and path
		 * 
		 */ 
		putFiles(new File(DataSetLoc)); // creates and stores the list of files read to a list
		
		for(File f:lst){ 		// for each file f in list
			FileReader freader = null;
			try{
				Document docindex=new Document(); // a new document is created for each file
				freader=new FileReader(f);		
				
				String titlestrg=null;
				Scanner scnr=new Scanner(f);
				while(scnr.hasNextLine()){
					String line = scnr.nextLine();
					Pattern p = Pattern.compile("(\\s\\.)(\\D)");
					Matcher m = p.matcher(line);

					StringBuffer patternresult = new StringBuffer();
					if (m.find()) {
						m.appendReplacement(patternresult, m.group(1) + " . ");
					}
					m.appendTail(patternresult);
			
					titlestrg= patternresult.substring(0, patternresult.indexOf(" . "));
					
				}
				
				

				docindex.add(new StringField("title",titlestrg,Field.Store.YES)); // title field for each document
				docindex.add(new TextField("abstract",freader));					// abstract field for each document
				docindex.add(new StringField("path",f.getPath(),Field.Store.YES));	// path field for each document
				docindex.add(new StringField("filename",f.getName(),Field.Store.YES));// file name for each document
				
				writer.addDocument(docindex); // Indexes are written over each document
				System.out.println(f.getName()+" is Indexed"); 
			}
			catch(Exception excptn){
				System.out.println("could not add  " + f);
			}
			freader.close();
			
		}
		
		//Stop timer after finishing indexing
		Date end=new Date();
		
		lst.clear();
		System.out.println("total time taken to index the documents :" + (end.getTime() - strt.getTime()));
		
		writer.close(); // IndexWriter is closed
		
		/*
		 * 
		 * 
		 * SEARCHING
		 * 
		 * 
		 * 
		 */
		
		//Indexes created in dsLoc are accessed using IndexReader
		IndexReader rdr=DirectoryReader.open(FSDirectory.open(Paths.get(dsLoc))); 
		
		IndexSearcher schr=new IndexSearcher(rdr); //searches over IndexReader

		String str=null;
		while(str!="stop"){
			Date strtquery=new Date();
			
			try{
			System.out.println("Type your Query here or 'stop' : ");
			BufferedReader querybr= new BufferedReader(new InputStreamReader(System.in));
			str=querybr.readLine();
			if(str.equals("stop")){
				break;
			}
			
		
			QueryParser parser =null;
			parser= new QueryParser("abstract", analyzer); // parses the contents of the file
			Query qry=null;
		
			qry=parser.parse(str);	// parses the query
		
			System.out.println();
			System.out.println("Searching for: " + qry.toString("abstract"));
				
			TopDocs cltr=schr.search(qry, 1000);  //upper bound to get the actual number of hits
			
			ScoreDoc[] hits = cltr.scoreDocs; 		//stores the hits of documents in an array
			int ttlhits=cltr.totalHits;				// gets the total number of hits
			//minimum the total number of hits or a maximum of 20 hits are displayed to the user
			int last=Math.min(ttlhits,20);			
			
			
			int hitsl=hits.length;
			Date endquery=new Date();
			System.out.println("A Total of "+hitsl +" hits found");
			System.out.println("Time taken to querying is "+(endquery.getTime()-strtquery.getTime()));
			//printing the documents hit
			for(int i=0;i<last;i++){
				Document prevdc=schr.doc(hits[i].doc); 
				String path=prevdc.get("path");
				String tt=prevdc.get("title");
				String fname=prevdc.get("filename");
				if(path!=null){
					System.out.println((i+1) + ".  Score (Ranking)= " +hits[i].score);
					System.out.println("\t The File found is: " + fname  );	
					//System.out.println("\t TITLE: " + tt  );
					//System.out.println("\t "+path);
					//System.out.println();
					System.out.println();
				}
				else{
					System.out.println("path could not be found");
				}
				
			}
			}
			catch(Exception exp){
				System.out.println(" Could not parse the query..." + exp.getMessage() );
			}
			
			
		}
	
rdr.close();
		}
	

	private static void splitfunc(String SampleLoc) throws IOException {
		// TODO Auto-generated method stub
		
		//reads .all file line by line
		String inputFile="CranfieldDataset\\cran.all"; 
        File contfile=new File (inputFile);
        String contx=contfile.getAbsolutePath();
		
		BufferedReader splitbr = new BufferedReader(new FileReader(new File(contx)));
       
		String line=null;
        StringBuilder strbld = new StringBuilder(); // stores character sequence
        int count=1;
       try {
    	   // each line of the .all line file is read
           while((line = splitbr.readLine()) != null){
        	 
               // the lines starting with .I are deleted
               if(line.startsWith(".I"))
            	   
               { 
         
                   if(strbld.length()!=0){
                	   
                	   File file = new File("C:\\cran_"+count+".txt");
                       PrintWriter contwriter = new PrintWriter(file, "UTF-8");
                       contwriter.println(strbld.toString());
                       contwriter.close();
                       strbld.delete(0, strbld.length());
                       count++;
                   }
                   continue;
                   
               }
               //the line starting with .T until the line starting with .W are omitted
               else if(line.startsWith(".T")){
            	     
             	   while(!line.startsWith(".A")){
             		   
             		   line=splitbr.readLine();
             	   }
             	  while(!line.startsWith(".B")){
            		   
            		   line=splitbr.readLine();
            	   }
             	 while(!line.startsWith(".W")){
          		   
          		   line=splitbr.readLine();
          	   }
                    continue;
                }
               // lines starting with .W is skipped but the succeeding lines are the contents to the new file
               		else if(line.startsWith(".W")){
     
                 	   if(strbld.length()!=0){
                 		  File file = new File("C:\\cran_"+count+".txt");
                            PrintWriter contwriter = new PrintWriter(file, "UTF-8");
                            contwriter.println(strbld.toString());
                            contwriter.close();
                            strbld.delete(0, strbld.length());
                            count++;
                        }
                        continue;
                    }
               strbld.append(line); //appends string to character sequence
           }

          } catch (Exception exp) {
            exp.printStackTrace();
          }
          finally {
                 splitbr.close();

             }
   }

	//List is a dynamic array in JAVA which does not require a capacity while initializing
	private static ArrayList<File> lst = new ArrayList<File>();
	private static void putFiles(File file) throws IOException{
		// TODO Auto-generated method stub

		/* if there are multiple files in a directory, and a directory is provided
		 * we call each file and add it to the array list 
		 */		
	
		if(file.isDirectory()){

			for(File f:file.listFiles()){
				putFiles(f);			// traverses through the folders to reach the files.
			}
		}
		
		
		else{
			String txt=file.getName().toLowerCase();
			int tf=0;
			tf=txt.compareTo("readme");
			
			//since among the list of files ending with .txt, even README.txt is included
			//excludes README file and adds only files ending with .txt 
			if(txt.endsWith(".txt")&&tf<0 ){
				
				lst.add(file);		// adds file to the list
				
			}
			
		}
		
	}
	


}

