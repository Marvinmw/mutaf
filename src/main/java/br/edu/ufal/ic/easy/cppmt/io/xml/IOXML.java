package br.edu.ufal.ic.easy.cppmt.io.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import br.edu.ufal.ic.easy.cppmt.generation.GenerationC;
import br.edu.ufal.ic.easy.cppmt.generation.GenerationXML;
import br.edu.ufal.ic.easy.cppmt.mutation.Mutation;

/**
 * Read file in C and use GenerationXML to create a XML element
 * 
 * @author Luiz Carvalho
 *
 */
public class IOXML {
	
	private GenerationXML generationXML = new GenerationXML();
	
	/**
	 * Write xml
	 * @param C file
	 * @param mutation
	 * @return xml file 
	 */
	public File write(File file, Mutation mutation) {
		String filePathC = file.getAbsolutePath();
		String filePathXML = filePathC.replace(".c", "_" + mutation.getMutationOperationName() + "_" + mutation.getId() + ".xml");
		File fileXML = new File(filePathXML);
		//if (new File(filePathXML).exists()) return write(file, mutation);
		try (BufferedWriter br = new BufferedWriter(new FileWriter(fileXML))) {
			try {
				Transformer transformer;
				transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				transformer.setOutputProperty(OutputKeys.INDENT, "not");
						
				StreamResult result = new StreamResult(new StringWriter());
				DOMSource source = new DOMSource(mutation.getDocument());
				transformer.transform(source, result);
				
				String xmlString = result.getWriter().toString();
				br.write(xmlString);
				br.flush();
				
				GenerationC generationC = new GenerationC();
				File cFile = generationC.convertsFromXML(fileXML);
				fileXML.deleteOnExit();
				mutation.setMutationFile(cFile);
				return cFile;
			} catch (TransformerConfigurationException
					| TransformerFactoryConfigurationError e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}

		  } catch (IOException e) {
			e.printStackTrace();
		  }
		return null;
	}
	
	/**
	 * Read file in C and use GenerationXML class to return a Element
	 * @param file file in C
	 * @return Document (XML root)
	 * @throws IOException 
	 */
	public Document read(File file) throws IOException {
		File xmlFile = generationXML.convertsFromC(file);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setIgnoringElementContentWhitespace(false);
		dbFactory.setNamespaceAware(true);
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			xmlFile.deleteOnExit();
			return doc;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new IOException("Sorry. We can not read the XML generated by srcML");
	}
	
	/**
	 * Remove temporary XML
	 * @param file XML file 
	 */
	public void removeXML(File file) {
		file.deleteOnExit();
	}
}
