import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Document;


public class InputDocument {

	ArrayList<String> wordsArray = new ArrayList<String>();
	ArrayList<String> punctuationArray = new ArrayList<String>();
	ArrayList<String> sentenceArray = new ArrayList<String>(); 
	ArrayList<String> nounsArray= new ArrayList<String>();
	
	int sentenceIndex=0;
	
	String regexSentenceIdentifier="[.?!]";
    String regexPunctuation = "\\W"; 
    
    HashSet<String> nouns;
    
    public InputDocument(HashSet<String> nouns, String fileName) throws IOException, ParserConfigurationException, TransformerException
    {
    	this.nouns = nouns;
    	BufferedReader br = null;
		String sCurrentLine;
		String content = "";

		br = new BufferedReader(new FileReader(fileName));

		while ((sCurrentLine = br.readLine()) != null) {
			content= content+sCurrentLine;
		}
		br.close();
		
		convertDocumentToSentences(content, fileName);
    }
	
	public void convertDocumentToSentences(String document, String fileName) throws ParserConfigurationException, IOException, TransformerException
	{
		if (!document.equals(""))
		{
			
			int start = 0;
			Matcher matcher = Pattern.compile(regexSentenceIdentifier).matcher(document);
			
			while (matcher.find())
			{
				String newSentence = document.substring(start, matcher.end()).trim();
				if(newSentence.startsWith(".")|| Character.isDigit(newSentence.charAt(0))||Character.isLowerCase(newSentence.charAt(0)))
				{
					String temp = sentenceArray.get(sentenceIndex-1);
					sentenceArray.remove(sentenceIndex-1);
					sentenceArray.add(sentenceIndex-1, temp+newSentence);
				}
				else
				{	
					sentenceArray.add(sentenceIndex++,newSentence);
				}
				start = matcher.end();
				
			}
			
	    }
		convertSentenceIntoWordsAndPunctuation(sentenceArray, fileName);
	}
	
	public void convertSentenceIntoWordsAndPunctuation(ArrayList<String> sentenceArray, String fileName) throws ParserConfigurationException, IOException, TransformerException
	{	
		ArrayList<String> tokensArray = new ArrayList<String>();
		Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
		
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		Element rootElement = document.createElement("inputFile");
		document.appendChild(rootElement);
		Attr attr = document.createAttribute("name");
        attr.setValue(fileName);
        rootElement.setAttributeNode(attr);
		//FileWriter fileWritter = new FileWriter(file.getName(),true);
		for(String sentence : sentenceArray)
		{		
				
			    int start = 0;
				Matcher matcher = Pattern.compile(regexPunctuation).matcher(sentence);
				
				while (matcher.find())
				{
					String newWord = sentence.substring(start, matcher.end());
					//System.out.println(newWord);
					if(p.matcher(newWord.trim()).find())
						{
						if(newWord.trim().matches("[a-zA-Z0-9]+.")) {
							wordsArray.add(newWord.substring(0, newWord.length()-1));
						 	tokensArray.add(newWord.substring(0, newWord.length()-1));
						}
						 punctuationArray.add(newWord.substring(newWord.length()-1));
						 tokensArray.add(newWord.substring(newWord.length()-1));
						}
					else if(newWord.trim().matches("[a-zA-Z0-9]+"))
						{
						 wordsArray.add(newWord.trim());
						 tokensArray.add(newWord.trim());
						}
					start = matcher.end();
					
				}		    
				//xml
				//System.out.println(tokensArray);
				Element sentenceElement = document.createElement("sentence");
				rootElement.appendChild(sentenceElement);
				convertIntoXmlFile(tokensArray, document, sentenceElement);
				tokensArray.clear();
		}
	}
	
	private void convertIntoXmlFile(ArrayList<String> tokenizedSentence, Document document, Element sentenceElement) throws ParserConfigurationException, IOException, TransformerException 
	{
					for (String token : tokenizedSentence)
					{ //loop through SentenceItems
						if (wordsArray.contains(token)) { // add Words
							String eleName = (nouns.contains(token))?"name":"word";
							Element wordElement = document.createElement(eleName);
							wordElement.appendChild(document.createTextNode(token));
							sentenceElement.appendChild(wordElement);
						}
						else if (punctuationArray.contains(token)) { // add Punctuations
						
							Element punctuationElement = document.createElement("punctuation");
							punctuationElement.appendChild(document.createTextNode(token));
							sentenceElement.appendChild(punctuationElement);
						}
					}
					printDocument(document, System.out);
					
	}
	
	public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	    
	    DOMSource source = new DOMSource(doc);
	    //transformer.transform(source, 
	      //   new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	    
		File file = new File("./outputTask1.xml");
	    StreamResult result = new StreamResult(file);        
	    transformer.transform(source, result);
	}
	
}
