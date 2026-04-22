package org.example.database;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import org.bson.Document;
import org.bson.conversions.Bson;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for mongodb access.
 */
public class MongoDatabaseHandler {

    private final DatabaseConfig config;
    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoDatabaseHandler(DatabaseConfig config) {
        this.config = config;
    }

    // connect to mongodb
    public void connect() {

        if (mongoClient == null) {

            try {

                String connectionString = config.getConnectionString();
                System.out.println("Connecting to MongoDB...");

                this.mongoClient = MongoClients.create(connectionString);

                this.database = mongoClient.getDatabase(config.getDatabaseName());

            } catch (Exception e) {
                System.out.println("MongoDB connection error:" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {

        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;

            System.out.println("MongoDB connection closed");
        }
    }

    // get database
    public MongoDatabase getDatabase(){

        if (database == null){

            throw new IllegalStateException("No Database connection: connect()");

        }

        return database;
    }

    // basic database operations ----------------------------------------

    // create document
    public void createDocument(String collectionName, Document document) {
        try {

            MongoCollection<Document> collection = getDatabase().getCollection(collectionName);
            collection.insertOne(document);

        } catch (Exception e){
            System.out.println("Error in creating document: " + e.getMessage());
        }
    }

    // find document
    public List<Document> findDocuments(String collectionName, Bson filter) {
        List<Document> results = new ArrayList<>();

        try {
            MongoCollection<Document> collection = getDatabase().getCollection(collectionName);
            collection.find(filter).into(results);
        } catch (Exception e){
            System.out.println("Error in finding documents: " + e.getMessage());
        }

        return results;
    }

    // update documents
    public long updateDocument(String collectionName, Bson filter, Bson update) {
        try {

            MongoCollection<Document> collection = getDatabase().getCollection(collectionName);
            UpdateResult result = collection.updateMany(filter, update);

            return result.getModifiedCount();
        } catch (Exception e){

            System.out.println("Error in updating documents: " + e.getMessage());

            return 0;
        }
    }

    // delete documents
    public long deleteDocument(String collectionName, Bson filter) {

        try {

            MongoCollection<Document> collection = getDatabase().getCollection(collectionName);
            DeleteResult result = collection.deleteMany(filter);

            return result.getDeletedCount();
        } catch (Exception e){
            System.out.println("Error in deleting documents: " + e.getMessage());
            return 0;
        }
    }

    // count documents
    public long countDocuments(String collectionName, Bson filter) {
        try {
            MongoCollection<Document> collection = getDatabase().getCollection(collectionName);
            return  collection.countDocuments(filter);
        } catch (Exception e){
            System.out.println("Error in counting documents: " + e.getMessage());
            return 0;
        }
    }

    // aggregation
    public List<Document> aggregate(String collectionName, List<Bson> pipeline) {
        List<Document> results = new ArrayList<>();

        try {
            MongoCollection<Document> collection = getDatabase().getCollection(collectionName);
            collection.aggregate(pipeline).into(results);
        } catch (Exception e){
            System.out.println("Error in aggregating documents: " + e.getMessage());

        }

        return results;
    }

    // find document with limit
    public List<Document> findDocumentWithLimit(String collectionName, Bson filter, int limit) {
        List<Document> results = new ArrayList<>();
        try {
            MongoCollection<Document> collection = getDatabase().getCollection(collectionName);
            collection.find(filter).limit(limit).into(results);
        } catch (Exception e){
            System.out.println("Error in finding documents: " + e.getMessage());

        }
        return results;
    }
}
