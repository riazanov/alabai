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


package logic.is.power.alabai.schematic_answer_xml;


import java.io.*;
import java.math.*;
import java.util.*;
import java.net.*;
import javax.xml.bind.*;
import javax.xml.bind.util.*;

import javax.xml.validation.*;

import logic.is.power.alabai.jaxb.*;



/** Renders logic.is.power.alabai.Clause in the XML accepted by the QueryManager demo
 *  program for incremental query rewriting.
 */
public class Renderer {

    
    public Renderer() throws java.lang.Exception {
	
	JAXBContext jc = 
	    JAXBContext.newInstance("logic.is.power.alabai.jaxb");
	_marshaller = jc.createMarshaller();
	
	SchemaFactory schemaFactory = 
	    SchemaFactory.
	    newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);

	URL schemaURL = 
	    ClassLoader.
	    getSystemResource("schematic_answer.xsd");
	

	if (schemaURL == null)
	    throw new RuntimeException("Cannot get a URL for the XML Schema file schematic_answer.xsd");

	Schema schema = schemaFactory.newSchema(schemaURL);

	_marshaller.setSchema(schema);

	// To skip the XML document header:
	_marshaller.setProperty(Marshaller.JAXB_FRAGMENT,true);

    } // Renderer()


    
    public void render(logic.is.power.alabai.Clause cl,OutputStream str) 
	throws java.lang.Exception {
	
	GenericAnswer ga = convert(cl);

	_marshaller.marshal(ga,str);
	
    } // render(logic.is.power.alabai.Clause cl,OutputStream str)

    
    private GenericAnswer convert(logic.is.power.alabai.Clause cl) {
	
	return new GenericAnswer();

    } // convert(logic.is.power.alabai.Clause cl)
    

    private Marshaller _marshaller;

} // class Renderer



