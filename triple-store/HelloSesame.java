package org.infointeg;

import info.aduna.iteration.Iterations;
import java.io.*;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class HelloSesame {

	public static double haversine(double lat1, double lon1, double lat2,
			double lon2) {
		final double R = 6372.8; // In kilometers
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
				* Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return R * c;
		// * 0.62137;
	}

	public static void main(String[] args) throws RepositoryException,
			RDFHandlerException, RDFParseException, IOException,
			MalformedQueryException, QueryEvaluationException {

		String SOURCE = "http://dbpedia.org/sparql";
		String NS = SOURCE + "#";
		// create a model using reasoner
		OntModel model1 = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		// create a model which doesn't use a reasoner
		OntModel model2 = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_MEM);

		// read the RDF/XML file
		model1.read(SOURCE, "RDF/XML");
		model2.read(SOURCE, "RDF/XML");
		// prints out the RDF/XML structure

		System.out.println(" ");

		// Create a new query
		String queryString = "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>\n"
				+ "PREFIX dbpedia: <http://dbpedia.org/resource/>\n"
				+ "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "SELECT DISTINCT ?University ?City ?lat ?long\n"
				+ "WHERE { { ?univ\n"
				+ "dbpedia-owl:type dbpedia:Private_university;\n"
				+ "dbpedia-owl:city ?ci;\n"
				+ "rdfs:label ?University;\n"
				+ "dbpedia-owl:state dbpedia:California.\n"
				+ "?ci rdfs:label ?City.\n"
				+ "FILTER(LANGMATCHES(LANG(?University), \"en\"))\n"
				+ "FILTER(LANGMATCHES(LANG(?City), \"en\"))\n"
				+ "OPTIONAL {?univ geo:lat ?lat }\n"
				+ "OPTIONAL {?univ geo:long ?long } } }";
		Query query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.sparqlService(
				"http://dbpedia.org/sparql", query);
		ResultSet results = qe.execSelect();

		// System.out.println("Query -------------\n");
		// System.out.println(queryString);

		PrintStream out = new PrintStream(new FileOutputStream(
				"data/output.txt"));
		ResultSetFormatter.out(out, results, query);
		qe.close();

		String queryString1 = "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>\n"
				+ "PREFIX dbpedia: <http://dbpedia.org/resource/>\n"
				+ "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "CONSTRUCT {\n"
				+ "?univ <https://schema.org/name> ?University.\n"
				+ "?univ <https://schema.org/addressLocality> ?City.\n"
				+ "?univ <https://schema.org/latitude> ?lat.\n"
				+ "?univ <https://schema.org/longitude> ?long.\n"
				+ "}\n"
				+ "WHERE { {?univ\n"
				+ "dbpedia-owl:type dbpedia:Private_university;\n"
				+ "dbpedia-owl:city ?ci;\n"
				+ "rdfs:label ?University;\n"
				+ "dbpedia-owl:state dbpedia:California.\n"
				+ "?ci rdfs:label ?City.\n"
				+ "FILTER(LANGMATCHES(LANG(?University), \"en\"))\n"
				+ "FILTER(LANGMATCHES(LANG(?City), \"en\"))\n"
				+ "OPTIONAL {?univ geo:lat ?lat }\n"
				+ "OPTIONAL {?univ geo:long ?long } } }";

		// System.out.println("Query -------------\n");
		// System.out.println(queryString1);
		Query query1 = QueryFactory.create(queryString1);
		// Execute the query and obtain results
		QueryExecution qe1 = QueryExecutionFactory.sparqlService(
				"http://dbpedia.org/sparql", query1);

		com.hp.hpl.jena.rdf.model.Model results1 = qe1.execConstruct();
		// preserve namespace
		String MA_NS = "https://schema.org/";
		results1.removeNsPrefix(results1.getNsURIPrefix(MA_NS));
		results1.setNsPrefix("schema", MA_NS);
		// results1.write(System.out, "TURTLE");

		PrintStream out1 = new PrintStream(new FileOutputStream(
				"data/queryOutput.ttl"));
		results1.write(out1, "TURTLE");
		qe1.close();

		Repository rep = new SailRepository(new MemoryStore());
		rep.initialize();

		String namespace = "https://schema.org/";
		RepositoryConnection conn = rep.getConnection();

		File file = new File("data/queryOutput.ttl");
		String baseURI = "https://schema.org";
		conn.add(file, baseURI, RDFFormat.TURTLE);

		RepositoryResult<Statement> statements = conn.getStatements(null, null,
				null, true);

		Model model = Iterations.addAll(statements, new LinkedHashModel());
		model.setNamespace("rdf", RDF.NAMESPACE);
		model.setNamespace("rdfs", RDFS.NAMESPACE);
		model.setNamespace("xsd", XMLSchema.NAMESPACE);
		model.setNamespace("dbpedia", "http://dbpedia.org/resource/");
		model.setNamespace("schema", namespace);

		// Querying the repository
		System.out
				.println("Querying The Repository for list of all the universities within 200 miles of UCLA:\n");

		String repositoryQueryString = "PREFIX schema: <https://schema.org/>\n"
				+ "SELECT ?x ?y ?z ?nam WHERE { ?x schema:latitude ?y;\n"
				+ "		schema:longitude ?z;\n" + "		schema:name ?nam }";
		TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL,
				repositoryQueryString);

		TupleQueryResult repoQueryResult = tupleQuery.evaluate();
		try {
			while (repoQueryResult.hasNext()) { // iterate over the result
				BindingSet bindingSet = repoQueryResult.next();
				Value valueOfX = bindingSet.getValue("x");
				Value valueOfY = bindingSet.getValue("y"); // latitude
				Value valueOfZ = bindingSet.getValue("z"); // longitude
				Value valueOfNam = bindingSet.getValue("nam"); // name of
																// university

				String latStr = valueOfY.toString();
				String longStr = valueOfZ.toString();
				String uname = valueOfNam.toString();

				uname = uname.replace("\"", "");
				uname = uname.substring(0, uname.indexOf("@"));
				latStr = latStr.substring(0, latStr.indexOf("^"));
				latStr = latStr.replace("\"", "");
				longStr = longStr.substring(0, longStr.indexOf("^"));
				longStr = longStr.replace("\"", "");

				double lati = Double.parseDouble(latStr);
				double longi = Double.parseDouble(longStr);

				// 34.07222222222222 -118.44409722222223 Latitude and Longitude
				// of UCLA from dbpedia
				// http://dbpedia.org/page/University_of_California,_Los_Angeles
				double latUCLA = 34.07222222222222;
				double longUCLA = -118.44409722222223;

				double dist = haversine(latUCLA, longUCLA, lati, longi) * 0.62137;
				if (dist <= 200.0)
					System.out.println("- " + uname);
				// + " is " + dist + " miles from UCLA");
			}
		} finally {
			repoQueryResult.close();
		}

	}

}
