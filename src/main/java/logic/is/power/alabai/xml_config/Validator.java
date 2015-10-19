/* Copyright (C) 2010 Alexandre Riazanov (Alexander Ryazanov)
 *
 * The copyright owner licenses this file to You under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package logic.is.power.alabai.xml_config;


import java.io.*;
import java.math.*;
import java.net.*;
import javax.xml.bind.*;
import javax.xml.bind.util.*;

import logic.is.power.alabai.jaxb.*;

import javax.xml.validation.*;

/** Simple command-line validator of Alabai config files. */
public class Validator {

    public static void main(String[] args) throws Exception {

	if (args.length == 0) 
	    {
		System.out.println("Usage: java <java options> logic.is.power.alabai.xml_config.Validator <XML Schema file> <XML file 1> .. <XML file n>");
		
		return;
	    };

	JAXBContext jc = 
	    JAXBContext.newInstance("logic.is.power.alabai.jaxb");

	Unmarshaller unmarshaller = jc.createUnmarshaller();
	
	SchemaFactory schemaFactory = 
	    SchemaFactory.
	    newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);

	URL schemaURL = 
	    ClassLoader.
	    getSystemResource("options.xsd");

	if (schemaURL == null)
	    throw new RuntimeException("Cannot get a URL for the XML Schema file options.xsd");

	
	System.out.println("Schema URL: " + schemaURL);

	Schema schema = schemaFactory.newSchema(schemaURL);
	
	unmarshaller.setSchema(schema);
	

	for (int n = 0; n < args.length; ++n)
	    {
		System.out.println("Validating file " + args[n]);
		
		AlabaiConfig conf = 
		    (AlabaiConfig)unmarshaller.unmarshal(new File(args[n]));
		System.out.println("Done.\n\n");

	    }; // for (int n = 1; n < args.length; ++n)


    } // main(String[] args)

    

} // class Validator
