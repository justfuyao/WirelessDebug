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
        addUsersAndMsgs(schema);
        //addCustomerOrder(schema);
        new DaoGenerator().generateAll(schema, "./app/src/main/java");
    }

    private static void addUsersAndMsgs(Schema schema) {
        Entity user = schema.addEntity("User");
        user.addIdProperty().autoincrement();
        user.addStringProperty("_Name");
        user.addStringProperty("_IpAddress");
        user.addStringProperty("_UID").notNull().primaryKey();


        Entity msg = schema.addEntity("Msg");
        msg.addIdProperty().autoincrement();
        msg.addStringProperty("_UserUID").notNull();
        msg.addLongProperty("_Timestamps").notNull();
        msg.addIntProperty("_Type").notNull();
        msg.addIntProperty("_SendType");
        msg.addIntProperty("_CRC8");
        msg.addByteArrayProperty("_Bytes").primaryKey();
        msg.addIntProperty("_Length");
        msg.addIntProperty("_SendTime");

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
