/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searcher;

/**
 *
 * @author Soumita
 */

import java.util.Arrays;
import java.util.List;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import java.util.ArrayList;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ParallelScanOptions;
import com.mongodb.ServerAddress;
import java.util.HashMap;
import java.util.StringTokenizer;

    
public class Searcher {
    
    private MongoClient mongoClient;
    private DB db;
    //private DBCollection geneMap;
    private DBCollection entrezMap;
    private DBCollection aliasEntrezMap;
    private DBCollection entrezAliasMap;
    private DBCollection geneMap2;
    private DBCollection goMap2;
    private DBCollection pathwayMap;
    private DBCollection refseqMap;
    private DBCollection ensemblGeneMap;
    private DBCollection ensemblTranscriptMap;
    private DBCollection ensemblProteinMap;
    private DBCollection uniprotMap;
    private String species;
    public boolean isConnected;
    
    public Searcher(String species){
        this.species = species;
        connectToMongoDB();
    }
    
    public final void connectToMongoDB () {        
        try {
            
            mongoClient = new MongoClient (new ServerAddress("localhost" , 27017), Arrays.asList());
            
            if(species.equals("human")){            // human
            // Connect to databases
                db = mongoClient.getDB("geneVocab_HomoSapiens"); 
                
                entrezMap = db.getCollection("HS_EntrezMap");
                aliasEntrezMap = db.getCollection("HS_aliasEntrezMap");
                entrezAliasMap = db.getCollection("HS_entrezAliasMap");
                geneMap2 = db.getCollection("HS_geneMap2");
                goMap2 = db.getCollection("HS_goMap2");
                pathwayMap = db.getCollection("HS_pathwayMap");
                refseqMap = db.getCollection("HS_refseqEntrezMap");
                ensemblGeneMap = db.getCollection("HS_ensemblEntrezMap");
                ensemblTranscriptMap = db.getCollection("HS_ensembltranscriptEntrezMap");
                ensemblProteinMap = db.getCollection("HS_ensemblprotEntrezMap");
                uniprotMap = db.getCollection("HS_uniprotEntrezMap");
                                
                isConnected = true;
                
            } else if (species.equals("mouse")){    // mouse 
                db = mongoClient.getDB("geneVocab_MusMusculus");
                
                entrezMap = db.getCollection("MM_EntrezMap");
                aliasEntrezMap = db.getCollection("MM_aliasEntrezMap");
                entrezAliasMap = db.getCollection("MM_entrezAliasMap");
                geneMap2 = db.getCollection("MM_geneMap2");
                goMap2 = db.getCollection("MM_goMap2");
                pathwayMap = db.getCollection("MM_pathwayMap");
                refseqMap = db.getCollection("MM_refseqEntrezMap");
                ensemblGeneMap = db.getCollection("MM_ensemblEntrezMap");
                ensemblTranscriptMap = db.getCollection("MM_ensembltranscriptEntrezMap");
                ensemblProteinMap = db.getCollection("MM_ensemblprotEntrezMap");
                uniprotMap = db.getCollection("MM_uniprotEntrezMap");
                
                isConnected = true;
            }
            
        } catch(Exception e){
            
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            isConnected = false;
            
        }
    }
    
    public void closeMongoDBConnection () {
        
        try {            
            mongoClient.close();
            isConnected = false;            
        } catch(Exception e){            
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            isConnected = false;
            
        }
        
    }
    
    /*public ArrayList <ArrayList<String>> processRequest (String queryString, String queryType, String searchType) {
        StringTokenizer st = new StringTokenizer(queryString, ",");
        ArrayList <ArrayList<String>> fullList = new ArrayList <ArrayList<String>> ();
        int count = 0;
        while(st.hasMoreTokens()) {
            ArrayList <ArrayList<String>> part_lists = processSingleRequest(st.nextToken(), queryType, searchType);
            
            for(int i = 0; i < part_lists.size(); i++){
                if (count == 0){
                    fullList.add(part_lists.get(i));
                } else {
                    fullList.get(i).addAll(part_lists.get(i));
                }
            }
            count++;
        }
        return fullList;
    } */
    
    /*public ArrayList <ArrayList<String>> processSingleRequest(String queryString, String queryType, String searchType){
        switch(queryType){
            case "entrez": 
                return processGeneQuery(queryString, searchType, "entrez");
            case "genesymbol":
                return processGeneQuery(queryString, searchType, "genesymbol");
            case "pathname":
                return processPathQuery(queryString, searchType, "pathname");
            case "pathid":
                return processPathQuery(queryString, searchType, "pathid");
            case "goid":
                return processGOQuery(queryString, searchType, "goid");
            case "goterm":
                return processGOQuery(queryString, searchType, "goterm");
            default:
                return null;
        }
    }*/
    
    public ArrayList <GeneObject> processGeneQuery (
            String queryString,
            String searchType,
            String queryType
    )   {
        
        
        
        ArrayList <GeneObject> genes_info = new ArrayList <> ();
        //ArrayList <String> entrez_ids = new ArrayList <> ();
        //ArrayList <String> gene_symbols = new ArrayList <> ();
        //ArrayList <ArrayList<String>> entrezList = new ArrayList <ArrayList<String>>();
        DBCursor cursor = null;
        
        try {

            BasicDBObject query = null;            
            
            if (searchType.equals("exact")) {
                query = new BasicDBObject("_id", queryString.trim().toLowerCase());
            } else if (searchType.equals("contains")) {
                query = new BasicDBObject("_id", java.util.regex.Pattern.compile(queryString.trim().toLowerCase()));
            }

            if (queryType.equals("entrez")) {
                cursor = entrezAliasMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    GeneObject gene_info = new GeneObject();
                    gene_info.setEntrezID((String)match.get("_id"));
                    gene_info.setAliases(match);
                    List <BasicDBObject> aliases = (List <BasicDBObject>) match.get("aliases");
                    String aliases_str = "";
                    for (int a = 0; a < aliases.size()- 1; a++) {
                        aliases_str += (String) aliases.get(a).get("alias") + " , ";
                    }
                    aliases_str += (String) aliases.get(aliases.size()- 1).get("alias");
                    //gene_symbols.add(aliases_str); 
                    gene_info.setGeneIdentifier(aliases_str, GeneObject.ENTREZ);
                    //entrez_ids.add((String)match.get("_id"));
                    genes_info.add(gene_info);
                }
                
            } else if (queryType.equals("genesymbol")) {
                cursor = aliasEntrezMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrez_ids");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){
                        //gene_symbols.add((String)match.get("_id")); 
                        //entrez_ids.add((String)match.get("entrez")); 
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.GENESYMBOL); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                    }
                }
            } else if (queryType.equals("refseq")) {
                cursor = refseqMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrezs");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){                        
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.REFSEQ_ID); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                    }
                    
                }
            } else if (queryType.equals("ensembl_gene_id")) {
                cursor = ensemblGeneMap.find(query);
                 while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrezs");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){                        
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.ENSEMBL_GENE_ID); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                    }
                    
                }
            } else if (queryType.equals("ensembl_transcript_id")) {
                cursor = ensemblTranscriptMap.find(query);
                 while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrezs");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){                        
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.ENSEMBL_TRANSCRIPT_ID); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                    }
                    
                }
            } else if (queryType.equals("ensembl_protein_id")) {
                cursor = ensemblProteinMap.find(query);
                 while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrezs");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){                        
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.ENSEMBL_PROTEIN_ID); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                    }
                    
                }
            } else if (queryType.equals("uniprot_id")) {
                cursor = uniprotMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    List <BasicDBObject> entrezs = (List <BasicDBObject>) match.get("entrezs");  
                    
                    for(int i = 0; i < entrezs.size(); i++ ){                        
                        GeneObject gene_info = new GeneObject();
                        gene_info.setGeneIdentifier((String)match.get("_id"), GeneObject.UNIPROT_ID); 
                        gene_info.setEntrezID((String)entrezs.get(i).get("entrez"));
                        genes_info.add(gene_info);
                    }
                    
                }
            }
            
            
        }catch(Exception e){
            System.out.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        
        //entrezList.add(entrez_ids);
        //entrezList.add(gene_symbols);
        return genes_info;
        
    }
    
    public ArrayList <PathwayObject> processPathQuery (
            String queryString,
            String searchType,
            String queryType
    ) {
        ArrayList <PathwayObject> path_infos = new ArrayList <PathwayObject>();
        DBCursor cursor = null;
        
        
        try {

            BasicDBObject query = null;            
            
            if (searchType.equals("exact") && queryType.equals("pathid")) {
                query = new BasicDBObject("_id", queryString.trim().toLowerCase());
            } else if (searchType.equals("contains") && queryType.equals("pathid")) {
                query = new BasicDBObject("_id", java.util.regex.Pattern.compile(queryString.trim().toLowerCase()));
            } else if (searchType.equals("exact") && queryType.equals("pathname")){
                query = new BasicDBObject("pathway", queryString.trim().toLowerCase());
            } else if (searchType.equals("contains") && queryType.equals("pathname")) {
                query = new BasicDBObject("pathway", java.util.regex.Pattern.compile(queryString.trim().toLowerCase()));
            }

            if (queryType.equals("pathname")) {
                cursor = pathwayMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    PathwayObject pathinfo = new PathwayObject();
                    pathinfo.setPathname((String)match.get("pathway")); 
                    pathinfo.setPathid((String)match.get("_id"));
                    pathinfo.setEntrezIDs(match.get("genes"));
                   
                    path_infos.add(pathinfo);
                }
            } else if (queryType.equals("pathid")) {
                cursor = pathwayMap.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    PathwayObject pathinfo = new PathwayObject();
                    pathinfo.setPathname((String)match.get("pathway")); 
                    pathinfo.setPathid((String)match.get("_id"));
                    pathinfo.setEntrezIDs(match.get("genes"));
                    
                    path_infos.add(pathinfo);
                }
            }
        }catch(Exception e){
            System.out.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        
        return path_infos;
    }
    
    public ArrayList <GoObject> processGOQuery (
            String queryString,
            String searchType,
            String queryType
    ) {
        ArrayList <GoObject> go_infos = new ArrayList <GoObject>();
        DBCursor cursor = null;
        
        try {

            BasicDBObject query = null;            
            
            if (searchType.equals("exact") && queryType.equals("goid")) {
                query = new BasicDBObject("go", queryString.trim().toLowerCase());
            } else if (searchType.equals("contains") && queryType.equals("goid")) {
                query = new BasicDBObject("go", java.util.regex.Pattern.compile(queryString.trim().toLowerCase()));
            } else if (searchType.equals("exact") && queryType.equals("goterm")){
                query = new BasicDBObject("term", queryString.trim().toLowerCase());
            } else if (searchType.equals("contains") && queryType.equals("goterm")) {
                query = new BasicDBObject("term", java.util.regex.Pattern.compile(queryString.trim().toLowerCase()));
            }

            if (queryType.equals("goterm")) {
                cursor = goMap2.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    GoObject goinfo = new GoObject();
                    goinfo.setGOIDIndex((String)match.get("_id"));
                    goinfo.setGOTerm((String)match.get("term"));
                    goinfo.setGOID((String)match.get("go"));
                    goinfo.setEntrezIDs(match.get("genes"));
                    //goinfo.setEvidences(match.get("evidences"));
                    //goinfo.setDefinition((String)match.get("definition"));
                    //goinfo.setOntology((String)match.get("ontology"));
                    //goinfo.setSynonyms(match.get("synonym"));
                    
                    go_infos.add(goinfo);
                }
            } else if (queryType.equals("goid")) {
                cursor = goMap2.find(query);
                while (cursor.hasNext()) {
                    DBObject match = (DBObject)cursor.next();
                    GoObject goinfo = new GoObject();
                    goinfo.setGOTerm((String)match.get("term"));
                    goinfo.setGOIDIndex((String)match.get("_id"));
                    goinfo.setGOID((String)match.get("go"));
                    goinfo.setEntrezIDs(match.get("genes"));
                    //goinfo.setEvidences(match.get("evidences"));
                    //goinfo.setDefinition((String)match.get("definition"));
                    //goinfo.setOntology((String)match.get("ontology"));
                    //goinfo.setSynonyms(match.get("synonym"));
                    
                    go_infos.add(goinfo);
                }
            }
        }catch(Exception e){
            System.out.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        
        return go_infos;
    }
    
    public ArrayList <GoObject> processGOid (String id) {
        ArrayList <GoObject> go_s = new ArrayList<GoObject> ();
        DBCursor cursor = null;
        
        try{
            BasicDBObject query = new BasicDBObject("go", id.trim().toLowerCase());
            cursor = goMap2.find(query);
            
            while(cursor.hasNext()){
                DBObject match = (DBObject)cursor.next();
                GoObject go = new GoObject(match);
                go_s.add(go);
            }
            
        }catch(Exception e) {
            System.out.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        
        return go_s;
    }
    
    public ArrayList <PathwayObject> processPathid (String id){
        
        ArrayList <PathwayObject> paths = new ArrayList<PathwayObject> ();
        DBCursor cursor = null;
                
        try{
            BasicDBObject query = new BasicDBObject("_id", id.trim().toLowerCase());
            cursor = pathwayMap.find(query);
            
            while(cursor.hasNext()){
                DBObject match = (DBObject)cursor.next();
                PathwayObject path = new PathwayObject(match);
                paths.add(path);
            }
        }catch(Exception e) {
            System.out.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        
        return paths;
    }
    
    public ArrayList <GeneObject> processEntrezidGeneSymbol(String entrezid){
               
        ArrayList <GeneObject> genes = new ArrayList<GeneObject> ();
        DBCursor cursor = null;
        DBCursor cursor1 = null;
        
        
        try {
            BasicDBObject query = new BasicDBObject("_id", entrezid.trim().toLowerCase());
            cursor = geneMap2.find(query);
            
            while(cursor.hasNext()){
                DBObject match = (DBObject)cursor.next();
                GeneObject gene = new GeneObject(match);
                
                BasicDBObject q1 = new BasicDBObject("_id", gene.entrez_id.trim().toLowerCase());
                cursor1 = entrezAliasMap.find(q1);
                if(cursor1.hasNext()){
                    DBObject match1 = (DBObject)cursor1.next();
                    gene.setAliases(match1); 
                }
                genes.add(gene);
            }
            
            
        } catch(Exception e) {
            System.out.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return genes;
    }
    
    public ArrayList <String> processEntrezidGOInfo(String entrezid){
        
        ArrayList <String> goinfo = new ArrayList<String> ();
        DBCursor cursor = null;
        
        try{
            BasicDBObject query = null;  
            query = new BasicDBObject("_id", entrezid.trim().toLowerCase());
            cursor = geneMap2.find(query);
            while(cursor.hasNext()){
                DBObject match = (DBObject)cursor.next();
                List <BasicDBObject> godata = (List <BasicDBObject>) match.get("goids");
                
                for(int i = 0; i < godata.size(); i++){
                    goinfo.add((String)godata.get(i).get("go"));
                    goinfo.add((String)godata.get(i).get("ontology"));
                    goinfo.add((String)godata.get(i).get("evidence"));
                    goinfo.add((String)godata.get(i).get("term"));                    
                }
            }      
            
        }catch(Exception e){
            System.out.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return goinfo;
    }
    
    public ArrayList <String> processEntrezidPathInfo(String entrezid){
        
        ArrayList <String> pathinfo = new ArrayList<String> ();
        DBCursor cursor = null;
        
        try{
            BasicDBObject query = null;  
            query = new BasicDBObject("_id", entrezid.trim().toLowerCase());
            cursor = geneMap2.find(query);
            while(cursor.hasNext()){
                DBObject match = (DBObject)cursor.next();
                List <BasicDBObject> godata = (List <BasicDBObject>) match.get("pathways");
                
                for(int i = 0; i < godata.size(); i++){
                    pathinfo.add((String)godata.get(i).get("pathway"));
                    pathinfo.add((String)godata.get(i).get("external_id"));
                    pathinfo.add((String)godata.get(i).get("source"));
                                     
                }
            }      
            
        }catch(Exception e){
            System.out.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return pathinfo;
    }
    
    public ArrayList <ArrayList <String>> getAllIdentifiersFromDB (String entrezid) {
        
        ArrayList <ArrayList <String>> db_values = new ArrayList <ArrayList <String>> ();
        DBCursor cursor = null;
        
        try {
            BasicDBObject query = null; 
            query = new BasicDBObject("_id", entrezid.trim().toLowerCase());
            cursor = entrezMap.find(query);
            
            while(cursor.hasNext()){
                
                String[] DBObjectNames = {"aliases", "refseqs", "ensembl_genes", "ensembl_transcripts", "ensembl_proteins", "uniprots"};
                String[] DBElementNames = {"alias", "refseq", "ensembl_gene", "ensembl_transcript", "ensembl_protein", "uniprot"};
                
                if(cursor.hasNext()){
                    
                    DBObject match = (DBObject)cursor.next();
                    
                    for (int t=0; t<DBObjectNames.length; t++) {
                        List <BasicDBObject> aliases = (List <BasicDBObject>) match.get(DBObjectNames[t]);
                        ArrayList <String> temp = new ArrayList <> ();
                        for(int j = 0; j < aliases.size(); j++){
                            temp.add((String)aliases.get(j).get(DBElementNames[t]));
                        }
                        db_values.add(temp);
                    }
                    
                }
                
            }
        } catch(Exception e){
            System.out.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        
        return db_values;        
    }
    
    public ArrayList <String> getIdentifiersFromDB (String entrezid, int identifier_type) {
        
        ArrayList <String> db_values = new ArrayList <> ();
        DBCursor cursor = null;
        
        try {
            BasicDBObject query = null; 
            query = new BasicDBObject("_id", entrezid.trim().toLowerCase());
            cursor = entrezMap.find(query);

            String[] DBObjectNames = {"aliases", "refseqs", "ensembl_genes", "ensembl_transcripts", "ensembl_proteins", "uniprots"};
            String[] DBElementNames = {"alias", "refseq", "ensembl_gene", "ensembl_transcript", "ensembl_protein", "uniprot"};

            if (cursor.hasNext()) {

                DBObject match = (DBObject) cursor.next();

                List<BasicDBObject> aliases = (List<BasicDBObject>) match.get(DBObjectNames[identifier_type-1]);
                for (int j = 0; j < aliases.size(); j++) {
                    db_values.add((String) aliases.get(j).get(DBElementNames[identifier_type-1]));
                }
            }

        } catch(Exception e){
            System.out.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        
        return db_values;        
    }
    
    public ArrayList <String> getEntrezFromDB (String identifier, int identifier_type) {
        
        ArrayList <String> db_entrezs = new ArrayList <> ();
        DBCursor cursor = null;
        
        String fieldname = "entrezs";

        try {
            
            BasicDBObject query = null; 
            query = new BasicDBObject("_id", identifier.trim().toLowerCase());
            
            switch (identifier_type) {
                case GeneObject.ENTREZ:
                    db_entrezs.add(identifier);
                    return db_entrezs;
                case GeneObject.GENESYMBOL:
                    //cursor = geneMap2.find(query);
                    cursor = aliasEntrezMap.find(query);
                    fieldname = "entrez_ids";
                    break;
                case GeneObject.REFSEQ_ID:
                    cursor = refseqMap.find(query);
                    break;
                case GeneObject.ENSEMBL_GENE_ID:
                    cursor = ensemblGeneMap.find(query);
                    break;
                case GeneObject.ENSEMBL_PROTEIN_ID:
                    cursor = ensemblProteinMap.find(query);
                    break;
                case GeneObject.ENSEMBL_TRANSCRIPT_ID:
                    cursor = ensemblTranscriptMap.find(query);
                    break;
                case GeneObject.UNIPROT_ID:
                    cursor = uniprotMap.find(query);
                    break;
                default:
                    break;
            }
            
            if (!(cursor == null) && cursor.hasNext()) {

                DBObject match = (DBObject) cursor.next();

                List<BasicDBObject> aliases = (List<BasicDBObject>) match.get(fieldname);
                for (int j = 0; j < aliases.size(); j++) {
                    db_entrezs.add((String) aliases.get(j).get("entrez"));
                }
            }
            
        } catch(Exception e){
            System.out.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        
        return db_entrezs;
    }
}
