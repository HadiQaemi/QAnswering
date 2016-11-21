/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package qclassifier;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;
/**
 *
 * @author haqa
 */
public class sample {
    public static void main(String[] args) {
        FileManager.get().addLocatorClassLoader(sample.class.getClassLoader());
        Model model = FileManager.get().loadModel("models/testFile.nt");

//        String queryString = 
//        		"SELECT ?person ?name WHERE { " +
//        		"    ?person ?p ?n . " +
//        		"    ?person ?s ?name . " +
//        		" FILTER (regex(str(?n),\"newtons\") || regex(str(?n),\"newton\") ) " +
//        		" FILTER regex(str(?s),\"AM-\")" +
//        		" FILTER regex(str(?p),\"A1\")"
//                + "}";
        String queryString = 
        		"SELECT ?r WHERE { "
                + "?q ?e ?N . "
                + "?q ?w ?V . "
                + "?q ?c ?r . "
                + "FILTER ( regex(str(?N),'newton') || regex(str(?N),'Sir Isaac Newton')) "
                + "FILTER ( regex(str(?w),'V') ) "
                + "FILTER ( regex(str(?V),'born') || regex(str(?V),'Max born'))"
//                + "FILTER ( regex(str(?c),\"LOC\") ) "
                + " } ";
        String ss = "SELECT ?r WHERE { "
                + "?q ?e ?N . "
                + "?q ?w ?V . "
                + "?q ?c ?r . "
                + "FILTER ( regex(str(?N),'newton') || regex(str(?N),'Isaac Newton') || regex(str(?N),'Sir Isaac Newton')) "
                + "FILTER (regex(str(?w),'V')) "
                + "FILTER ( regex(str(?V),'born') || regex(str(?V),'Max Born')) }  ";
        //SELECT ?q ?r ?c WHERE { ?q ?e ?N .?q ?w ?V .?q ?c ?r FILTER ( regex(str(?N),Newton) || regex(str(?N),Isaac Newton) || regex(str(?N),Sir Isaac Newton)) FILTER (regex(str(?w),v)) FILTER ( regex(str(?V),Born) || regex(str(?V),Max Born)) } 
        //SELECT ?r WHERE { ?q ?e ?N .
        //"?q ?w ?V ."
        //"?q ?c ?r ."
        //"FILTER ( regex(str(?N),"Newton") || regex(str(?N),"Isaac Newton") || regex(str(?N),"Sir Isaac Newton")) "
        //"FILTER (regex(str(?w),\"))
        //"FILTER ( regex(str(?V),"Born") || regex(str(?V),"Max Born")) } 
        Query query = QueryFactory.create(ss);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        try {
            ResultSet results = qexec.execSelect();
            while ( results.hasNext() ) {
                QuerySolution soln = results.nextSolution();
                Literal name = soln.getLiteral("r");
                System.out.println(name);
            }
        } finally {
            qexec.close();
        }

    }
}
