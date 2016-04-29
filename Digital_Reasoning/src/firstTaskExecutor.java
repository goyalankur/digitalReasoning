import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.xml.bind.JAXBException;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class firstTaskExecutor {

	static HashSet<String> nouns = new HashSet<String>();
	static String testFilePath = "./NLP_test/nlp_data/";
	static String[] files = { "d01.txt", "d02.txt", "d03.txt", "d04.txt",
			"d05.txt", "d06.txt", "d07.txt", "d08.txt", "d09.txt", "d10.txt" };

	static Document doc;
	static Element rootElement;

	public static void main(String[] args) throws IOException, JAXBException,
			ParserConfigurationException, TransformerException {
		readFile("./NLP_test/NER.txt");
		ArrayList<FileProcessor> processors = new ArrayList<FileProcessor>();
		ScheduledThreadPoolExecutor executer = new ScheduledThreadPoolExecutor(
				files.length);
		for (String file : files) {
			FileProcessor f = new FileProcessor(nouns, testFilePath + file);
			processors.add(f);
		}
		try {
			executer.invokeAll(processors);
			executer.shutdown();
			printDocument(processors);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void printDocument(ArrayList<FileProcessor> processors)
			throws Exception {
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		Element rootElement = document.createElement("inputFiles");
		document.appendChild(rootElement);
		for (FileProcessor f : processors) {
			Element fileElement = document.createElement("inputFile");
			rootElement.appendChild(fileElement);
			Attr attr = document.createAttribute("name");
			attr.setValue(f.doc.fileName);
			fileElement.setAttributeNode(attr);
			for (Sentence s : f.doc.sentences) {
				Element sentenceElement = document.createElement("sentence");
				fileElement.appendChild(sentenceElement);
				for (String token : s.tokensArray) { // loop through
														// SentenceItems
					if (s.wordsArray.contains(token)) { // add Words
						String eleName = (nouns.contains(token)) ? "namedEntity"
								: "word";
						Element wordElement = document.createElement(eleName);
						wordElement.appendChild(document.createTextNode(token));
						sentenceElement.appendChild(wordElement);
					} else if (s.punctuationArray.contains(token)) {
						Element punctuationElement = document
								.createElement("punctuation");
						punctuationElement.appendChild(document
								.createTextNode(token));
						sentenceElement.appendChild(punctuationElement);
					}
				}
				s.tokensArray.clear();
				s.wordsArray.clear();
				s.punctuationArray.clear();
			}
		}

		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "4");
		DOMSource source = new DOMSource(document);
		File file = new File("./outputTask3.xml");
		StreamResult result = new StreamResult(file);
		transformer.transform(source, result);
	}


	private static void readFile(String fin) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fin));

		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.trim().length() > 0)
				nouns.addAll(Arrays.asList(line.trim().split(" ")));
		}

		br.close();
	}

}

class FileProcessor implements Callable<FileProcessor> {
	String fileName;
	HashSet<String> nouns;
	InputDocument doc;

	public FileProcessor(HashSet<String> nouns, String fileName) {
		this.fileName = fileName;
		this.nouns = nouns;
	}

	@Override
	public FileProcessor call() {
		try {
			doc = new InputDocument(nouns, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}