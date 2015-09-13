package com.database.utils;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

public class DatabaseGenerator {

    private static final int VERSION_NUM = 1;

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(VERSION_NUM, "com.tcl.database");
        schema.enableKeepSectionsByDefault();
        schema.enableActiveEntitiesByDefault();
        addUsers(schema);
        addMsgs(schema);

        new DaoGenerator().generateAll(schema, "./app/src/main/java");
    }

    private static void addUsers(Schema schema) {
        Entity user = schema.addEntity("User");
        user.addIdProperty().autoincrement();
        user.addStringProperty("name");
        user.addStringProperty("ip_address");
        user.addStringProperty("uid");
    }

    private static void addMsgs(Schema schema) {
        Entity msg = schema.addEntity("Msgs");
        msg.addIdProperty().autoincrement();
        msg.addIntProperty("user_src_id").notNull();
        msg.addIntProperty("user_dst_id").notNull();
        msg.addDateProperty("data").notNull();
        msg.addStringProperty("content");
        msg.addIntProperty("type");
        msg.addByteArrayProperty("bytes");
    }

    private static void addNote(Schema schema) {
        Entity note = schema.addEntity("Note");
        note.addIdProperty();
        note.addStringProperty("text").notNull();
        note.addStringProperty("comment");
        note.addDateProperty("date");
    }

    private static void addCustomerOrder(Schema schema) {
        Entity customer = schema.addEntity("Customer");
        customer.addIdProperty();
        customer.addStringProperty("name").notNull();

        Entity order = schema.addEntity("Order");
        order.setTableName("ORDERS"); // "ORDER" is a reserved keyword
        order.addIdProperty();
        Property orderDate = order.addDateProperty("date").getProperty();
        Property customerId = order.addLongProperty("customerId").notNull().getProperty();
        order.addToOne(customer, customerId);

        ToMany customerToOrders = customer.addToMany(order, customerId);
        customerToOrders.setName("orders");
        customerToOrders.orderAsc(orderDate);
    }

}
