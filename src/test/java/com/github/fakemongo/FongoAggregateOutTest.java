package com.github.fakemongo;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.AggregationOutput;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


/**
 * User: gdepourtales
 * 2015/06/15
 */
public class FongoAggregateOutTest {

    public final FongoRule fongoRule = new FongoRule(false);

    public final ExpectedException exception = ExpectedException.none();

    @Rule
    public TestRule rules = RuleChain.outerRule(exception).around(fongoRule);

    /**
     * See http://docs.mongodb.org/manual/reference/operator/aggregation/out/#pipe._S_out
     */
    @Test
    public void testOut() {
        DBCollection coll = fongoRule.newCollection();
        DBCollection secondCollection = fongoRule.newCollection();
        String data = "[{ _id: 1, sec: \"dessert\", category: \"pie\", type: \"apple\" },\n" +
                "{ _id: 2, sec: \"dessert\", category: \"pie\", type: \"cherry\" },\n" +
                "{ _id: 3, sec: \"main\", category: \"pie\", type: \"shepherd's\" },\n" +
                "{ _id: 4, sec: \"main\", category: \"pie\", type: \"chicken pot\" }]";
        fongoRule.insertJSON(coll, data);

        DBObject project = fongoRule.parseDBObject(
                "{ $out: \"" + secondCollection.getName() + "\"}"
            );

        AggregationOutput output = coll.aggregate(Arrays.asList(project));
        assertTrue(output.getCommandResult().ok());

        List<DBObject> result = (List<DBObject>) output.getCommandResult().get("result");
        assertNotNull(result);
        assertEquals(fongoRule.parse(data), result);
        assertEquals(4, secondCollection.count());
        assertEquals("apple", secondCollection.find(fongoRule.parseDBObject("{_id:1}")).next().get("type"));
        assertEquals("pie", secondCollection.find(fongoRule.parseDBObject("{_id:1}")).next().get("category"));
        assertEquals("chicken pot", secondCollection.find(fongoRule.parseDBObject("{_id:4}")).next().get("type"));

    }


    /**
     * See http://docs.mongodb.org/manual/reference/operator/aggregation/out/#pipe._S_out
     */
    @Test
    public void testOutWithEmptyCollection() {
        DBCollection coll = fongoRule.newCollection();
        DBCollection secondCollection = fongoRule.newCollection();
        String data = "[]";
        fongoRule.insertJSON(coll, data);

        DBObject project = fongoRule.parseDBObject(
                "{ $out: \"" + secondCollection.getName() + "\"}"
        );

        AggregationOutput output = coll.aggregate(Arrays.asList(project));
        assertTrue(output.getCommandResult().ok());

        List<DBObject> result = (List<DBObject>) output.getCommandResult().get("result");
        assertNotNull(result);
        assertEquals(fongoRule.parse(data), result);
        assertEquals(0, secondCollection.count());

    }

    /**
     * See http://docs.mongodb.org/manual/reference/operator/aggregation/out/#pipe._S_out
     */
    @Test
    public void testOutWithNonExistentCollection() {
        DBCollection coll = fongoRule.newCollection();
        String data = "[{ _id: 1, sec: \"dessert\", category: \"pie\", type: \"apple\" },\n" +
                "{ _id: 2, sec: \"dessert\", category: \"pie\", type: \"cherry\" },\n" +
                "{ _id: 3, sec: \"main\", category: \"pie\", type: \"shepherd's\" } ,\n" +
                "{ _id: 4, sec: \"main\", category: \"pie\", type: \"chicken pot\" }]";
        fongoRule.insertJSON(coll, data);
        String newCollectionName = "new_collection";

        DBObject project = fongoRule.parseDBObject(
                "{ $out: \"" + newCollectionName + "\"}"
        );

        AggregationOutput output = coll.aggregate(Arrays.asList(project));
        assertTrue(output.getCommandResult().ok());

        List<DBObject> result = (List<DBObject>) output.getCommandResult().get("result");
        assertNotNull(result);
        assertEquals(fongoRule.parse(data), result);
        DBCollection newCollection = fongoRule.getDB().getCollection("new_collection");
        assertEquals(4, newCollection.count());
        assertEquals("apple", newCollection.find(fongoRule.parseDBObject("{_id:1}")).next().get("type"));
        assertEquals("pie", newCollection.find(fongoRule.parseDBObject("{_id:1}")).next().get("category"));
        assertEquals("chicken pot", newCollection.find(fongoRule.parseDBObject("{_id:4}")).next().get("type"));

    }
}
