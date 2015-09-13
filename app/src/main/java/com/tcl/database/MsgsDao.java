package com.tcl.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.tcl.database.Msgs;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "MSGS".
*/
public class MsgsDao extends AbstractDao<Msgs, Long> {

    public static final String TABLENAME = "MSGS";

    /**
     * Properties of entity Msgs.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property User_src_id = new Property(1, int.class, "user_src_id", false, "USER_SRC_ID");
        public final static Property User_dst_id = new Property(2, int.class, "user_dst_id", false, "USER_DST_ID");
        public final static Property Data = new Property(3, java.util.Date.class, "data", false, "DATA");
        public final static Property Content = new Property(4, String.class, "content", false, "CONTENT");
        public final static Property Type = new Property(5, Integer.class, "type", false, "TYPE");
        public final static Property Bytes = new Property(6, byte[].class, "bytes", false, "BYTES");
    };

    private DaoSession daoSession;


    public MsgsDao(DaoConfig config) {
        super(config);
    }
    
    public MsgsDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"MSGS\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"USER_SRC_ID\" INTEGER NOT NULL ," + // 1: user_src_id
                "\"USER_DST_ID\" INTEGER NOT NULL ," + // 2: user_dst_id
                "\"DATA\" INTEGER NOT NULL ," + // 3: data
                "\"CONTENT\" TEXT," + // 4: content
                "\"TYPE\" INTEGER," + // 5: type
                "\"BYTES\" BLOB);"); // 6: bytes
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"MSGS\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Msgs entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getUser_src_id());
        stmt.bindLong(3, entity.getUser_dst_id());
        stmt.bindLong(4, entity.getData().getTime());
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(5, content);
        }
 
        Integer type = entity.getType();
        if (type != null) {
            stmt.bindLong(6, type);
        }
 
        byte[] bytes = entity.getBytes();
        if (bytes != null) {
            stmt.bindBlob(7, bytes);
        }
    }

    @Override
    protected void attachEntity(Msgs entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Msgs readEntity(Cursor cursor, int offset) {
        Msgs entity = new Msgs( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // user_src_id
            cursor.getInt(offset + 2), // user_dst_id
            new java.util.Date(cursor.getLong(offset + 3)), // data
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // content
            cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5), // type
            cursor.isNull(offset + 6) ? null : cursor.getBlob(offset + 6) // bytes
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Msgs entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUser_src_id(cursor.getInt(offset + 1));
        entity.setUser_dst_id(cursor.getInt(offset + 2));
        entity.setData(new java.util.Date(cursor.getLong(offset + 3)));
        entity.setContent(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setType(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
        entity.setBytes(cursor.isNull(offset + 6) ? null : cursor.getBlob(offset + 6));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Msgs entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Msgs entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
