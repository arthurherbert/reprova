package br.ufmg.engsoft.reprova.database;


import com.mongodb.client.MongoCollection;
import org.bson.Document;
import br.ufmg.engsoft.reprova.model.Question;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.result.UpdateResult;
import java.util.Collection;
import java.util.List;
import org.bson.conversions.Bson;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import com.mongodb.client.FindIterable;
import java.util.ArrayList;

import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Environments;
import java.util.Map;
import java.util.Map;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.fields;

public class QuestionsDAOCollection {
	
	/**
     * Logger instance.
     */
    protected static final Logger logger = LoggerFactory.getLogger(QuestionsDAO.class);

    /**
     * Json formatter.
     */
    protected final Json json;
	
	private final MongoCollection<Document> collection;

	public QuestionsDAOCollection(MongoCollection<Document> getCollection, Json json) {
		this.collection = getCollection;
		 if (json == null) {
            throw new IllegalArgumentException("json mustn't be null");
        }

        this.json = json;
	}

	/**
	* Get the question with the given id.
	* @param id  the question's id in the database.
	* @return  The question, or null if no such question.
	* @throws IllegalArgumentException  if any parameter is null
	*/
	public Question get(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id mustn't be null");
		}
		var question = this.collection.find(eq(new ObjectId(id))).map(this::parseDoc).first();
		if (question == null) {
			QuestionsDAO.logger.info("No such question " + id);
		}
		return question;
	}

	/**
	* Remove the question with the given id from the collection.
	* @param id  the question id
	* @return  Whether the given question was removed.
	* @throws IllegalArgumentException  if any parameter is null
	*/
	public boolean remove(String id) {
		if (id == null)
			throw new IllegalArgumentException("id mustn't be null");
		var result = this.collection.deleteOne(eq(new ObjectId(id))).wasAcknowledged();
		if (result) {
			QuestionsDAO.logger.info("Deleted question " + id);
		} else {
			QuestionsDAO.logger.warn("Failed to delete question " + id);
		}
		return result;
	}

	public boolean upsertCollection(Question question, Document doc) {
		var id = question.id;
		if (id != null) {
			var result = this.collection.replaceOne(eq(new ObjectId(id)), doc);
			if (!result.wasAcknowledged()) {
				QuestionsDAO.logger.warn("Failed to replace question " + id);
				return false;
			}
		} else {
			this.collection.insertOne(doc);
		}
		QuestionsDAO.logger.info("Stored question " + doc.get("_id"));
		return true;
	}

	/**
	* List all the questions that match the given non-null parameters. The question's statement is ommited.
	* @param theme  the expected theme, or null
	* @param pvt    the expected privacy, or null
	* @return  The questions in the collection that match the given parameters, possibly empty.
	* @throws IllegalArgumentException  if there is an invalid Question
	*/
	public Collection<Question> list(String theme, Boolean pvt) {
		var filters = Arrays.asList(theme == null ? null : eq("theme", theme), pvt == null ? null : eq("pvt", pvt))
				.stream().filter(Objects::nonNull).collect(Collectors.toList());
		var doc = filters.isEmpty() ? this.collection.find() : this.collection.find(and(filters));
		var result = new ArrayList<Question>();
		doc.projection(fields(exclude("statement"))).map(this::parseDoc).into(result);
		if (Environments.getInstance().getEnableQuestionStatistics()) {
			for (var question : result) {
				question.getStatistics();
			}
		}
		return result;
	}

	/**
	* Adds or updates the given question in the database. If the given question has an id, update, otherwise add.
	* @param question  the question to be stored
	* @return  Whether the question was successfully added.
	* @throws IllegalArgumentException  if any parameter is null
	*/
	public boolean add(Question question) {
		if (question == null) {
			throw new IllegalArgumentException("question mustn't be null");
		}
		Document doc = this.createDoc(question);
		return this.upsertCollection(question, doc);
	}

	public Document createDoc(Question question) {
		question.calculateDifficulty();
		Map<String, Object> record = null;
		if (question.record != null) {
			record = question.record.entrySet().stream()
					.collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
		}
		Document doc = new Document().append("theme", question.theme).append("description", question.description)
				.append("statement", question.statement).append("record", record == null ? null : new Document(record))
				.append("pvt", question.pvt);
		if (Environments.getInstance().getEnableEstimatedTime()) {
			doc = doc.append("estimatedTime", question.estimatedTime);
		}
		if (Environments.getInstance().getDifficultyGroup() != 0) {
			doc = doc.append("difficulty", question.difficulty);
		}
		if (Environments.getInstance().getEnableMultipleChoice()) {
			doc = doc.append("choices", question.getChoices());
		}
		if (Environments.getInstance().getEnableQuestionStatistics()) {
			doc = doc.append("statistics", question.getStatistics());
		}
		return doc;
	}
	
    /**
     * Parse the given document.
     *
     * @param document the question document, mustn't be null
     * @throws IllegalArgumentException if any parameter is null
     * @throws IllegalArgumentException if the given document is an invalid Question
     */
    protected Question parseDoc(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("document mustn't be null");
        }

        var doc = document.toJson();

        logger.info("Fetched question: " + doc);

        try {
            var question = json.parse(doc, Question.Builder.class).build();

            return question;
        } catch (Exception e) {
            logger.error("Invalid document in database!", e);
            throw new IllegalArgumentException(e);
        }
    }
}