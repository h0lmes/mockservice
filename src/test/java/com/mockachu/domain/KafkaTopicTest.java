package com.mockachu.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KafkaTopicTest {

    @Test
    void setGroup_Null_GroupIsEmptyString() {
        assertTrue(new KafkaTopic().setGroup(null).getGroup().isEmpty());
    }

    @Test
    void setTopic_Null_TopicIsEmptyString() {
        assertTrue(new KafkaTopic().setTopic(null).getTopic().isEmpty());
    }

    @DisplayName("Test equality")
    @Test
    void equals_SameMethodPathAlt_OtherFieldsDiffer_True() {
        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        assertEquals(entity1, entity2);
    }

    @Test
    void equals_Null_False() {
        var entity = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        assertNotEquals(null, entity);
    }

    @Test
    void equals_ObjectOfOtherType_False() {
        var entity = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        assertNotEquals(entity, new Object());
    }

    @Test
    void equals_DifferentTopic_False() {
        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("AAA").setTopic("BBB").setPartition(0);
        assertNotEquals(entity1, entity2);
    }

    @Test
    void equals_DifferentPartition_False() {
        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(1);
        assertNotEquals(entity1, entity2);
    }

    @Test
    void hashCode_EqualsForEqualObjects() {
        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void compareTo_Equal() {
        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        assertEquals(0, entity1.compareTo(entity2));
    }

    @Test
    void compareTo_ByGroup() {
        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("BBB").setTopic("AAA").setPartition(0);
        assertTrue(0 > entity1.compareTo(entity2));
    }

    @Test
    void compareTo_ByTopic() {
        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("AAA").setTopic("BBB").setPartition(0);
        assertTrue(0 > entity1.compareTo(entity2));
    }

    @Test
    void compareTo_ByPartition() {
        var entity1 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(0);
        var entity2 = new KafkaTopic().setGroup("AAA").setTopic("AAA").setPartition(1);
        assertTrue(0 > entity1.compareTo(entity2));
    }
}
