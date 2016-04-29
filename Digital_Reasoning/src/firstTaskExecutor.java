
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class firstTaskExecutor {
	
	static HashSet<String> nouns = new HashSet<String>();
	
	public static void main(String[] args) throws IOException, JAXBException, ParserConfigurationException, TransformerException {
		readFile("./NLP_test/NER.txt");
		new InputDocument(nouns,"./NLP_test/nlp_data.txt");

	}
	
	private static void readFile(String fin) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fin));
	 
		String line = null;
		while ((line = br.readLine()) != null) {
			if(line.trim().length()>0)
				nouns.addAll(Arrays.asList(line.trim().split(" ")));
		}
	 
		br.close();
	}

}
