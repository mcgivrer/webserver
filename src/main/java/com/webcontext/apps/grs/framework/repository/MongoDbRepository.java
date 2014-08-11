/**
 * 
 */
package com.webcontext.apps.grs.framework.repository;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import com.webcontext.apps.grs.framework.repository.exception.NullMongoDBConnection;

/**
 * This class is a lightweight implementation for a Data Repository accessing to
 * a specific Collection (<code>collectionName</code>). This database connection
 * will be the same for all implementation (shared MongoClient & DB connection).
 * 
 * @author 212391884
 *
 */
public abstract class MongoDbRepository<T> implements IMongoDbRepository<T> {

	private MongoDBConnection connection;

	/**
	 * Date formatter to be used to convert date in serialize/deserialize
	 * operations.
	 */
	protected Gson gson = new GsonBuilder()
			.setDateFormat("YYYY-mm-dd hh:MM:SS").create();

	/**
	 * Collection manipulated by this repository.
	 */
	protected String collectionName;

	/**
	 * Default constructor.
	 */
	public MongoDbRepository() {
		super();
	}

	/**
	 * Create a repository on a specific DBCollection
	 * <code>collectionName</code> .
	 * 
	 * @param collectionName
	 */
	public MongoDbRepository(String collectionName) {
		this();
		this.collectionName = collectionName;
	}

	/**
	 * create repository on the <code>connection</code>.
	 * 
	 * @param connection
	 */
	public MongoDbRepository(MongoDBConnection connection, String collectionName) {
		this();
		this.connection = connection;
		this.collectionName = collectionName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.webcontext.apps.grs.repository.IMongoDbRepository#find()
	 */
	@Override
	public List<T> find() throws NullMongoDBConnection {
		if (connection == null) {
			throw new NullMongoDBConnection("MongoDBConnection object is null");
		}
		DBCollection collection = connection.getDb().getCollection(
				collectionName);
		DBCursor cursor = collection.find();

		return buildListFromCursor(cursor);
	}

	/**
	 * Count number of entities into the corresponding collection.
	 * 
	 * @return the number of entities in the Repository specific collection.
	 */
	public long count() throws NullMongoDBConnection {
		if (connection == null) {
			throw new NullMongoDBConnection("MongoDBConnection object is null");
		}
		DBCollection collection = connection.getDb().getCollection(
				collectionName);
		DBCursor cursor = collection.find();

		return cursor.count();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.webcontext.apps.grs.repository.IMongoDbRepository#find(java.lang.
	 * String)
	 */
	@Override
	public List<T> find(String filter) throws NullMongoDBConnection {

		if (connection == null) {
			throw new NullMongoDBConnection("MongoDBConnection object is null");
		}
		DBCollection collection = connection.getDb().getCollection(
				collectionName);
		BasicDBObject searchfilter = (BasicDBObject) JSON.parse(filter);
		DBCursor cursor = collection.find(searchfilter);
		return buildListFromCursor(cursor);
	}

	/**
	 * build a <code>list</code> of data based on a <code>cursor</code>.
	 * 
	 * @param list
	 * @param cursor
	 */
	protected List<T> buildListFromCursor(DBCursor cursor) {

		List<T> list = new ArrayList<T>();
		try {
			DBObject item;
			while (cursor.hasNext()) {
				item = cursor.next();
				list.add(deserialize((BasicDBObject) item));
			}
		} finally {
			cursor.close();
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.webcontext.apps.grs.repository.IMongoDbRepository#save(T)
	 */
	@Override
	public WriteResult save(T item) throws NullMongoDBConnection {
		if (connection == null) {
			throw new NullMongoDBConnection("MongoDBConnection object is null");

		}
		DBCollection collection = connection.getDb().getCollection(
				collectionName);

		WriteResult wr = collection.save(convertFromObject(item));
		return wr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.webcontext.apps.grs.repository.IMongoDbRepository#remove(T)
	 */
	@Override
	public void remove(T item) throws NullMongoDBConnection {
		if (connection == null) {
			throw new NullMongoDBConnection("MongoDBConnection object is null");
		}
		DBCollection collection = connection.getDb().getCollection(
				collectionName);
		collection.remove(convertFromObject(item));
	}

	/**
	 * convert a T item to a BasicDBObject
	 * 
	 * @param item
	 * @return
	 */
	public BasicDBObject convertFromObject(T item) {
		BasicDBObject object = (BasicDBObject) JSON.parse(gson.toJson(item)
				.toString());
		return object;
	}

	/**
	 * Convert from <code>json</code> to entity T.
	 * 
	 * @param json
	 * @return
	 */
	public T convertToObject(String json) {
		ParameterizedType genericSuperClass = (ParameterizedType) getClass()
				.getGenericSuperclass();
		@SuppressWarnings("unchecked")
		Class<T> class1 = (Class<T>) genericSuperClass.getActualTypeArguments()[0];
		Class<T> entityClass = class1;
		return gson.fromJson(json, entityClass);
	}

	/**
	 * deserialize the BasicDBObject to an simple POJO T.
	 * 
	 * @param item
	 * @return
	 */
	public abstract T deserialize(BasicDBObject item);

	/**
	 * serialize a simple T POJO to a BasicDBObject.
	 * 
	 * @param item
	 * @return
	 */
	public abstract BasicDBObject serialize(T item);

	/**
	 * Set the connection to mongodb database.
	 */
	public void setConnection(MongoDBConnection conn) {
		this.connection = conn;
	}

}