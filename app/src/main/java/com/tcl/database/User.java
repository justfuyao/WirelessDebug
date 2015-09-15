package com.tcl.database;

import com.tcl.bean.UserExtraContent;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS
// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END

/**
 * Entity mapped to table "USER".
 */
public class User {

    private Long id;
    private String _Name;
    private String _IpAddress;
    /**
     * Not-null value.
     */
    private String _UID;

    /**
     * Used to resolve relations
     */
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    private transient UserDao myDao;


    // KEEP FIELDS - put your custom fields here
    public static final int USER_STATUS_ONLINE = 1;
    public static final int USER_STATUS_OFFLINE = 2;

    public UserExtraContent mUserExtraContent = new UserExtraContent();

    // KEEP FIELDS END

    public User() {
    }

    public User(Long id, String _UID) {
        this.id = id;
        this._UID = _UID;
    }

    public User(Long id, String _Name, String _IpAddress, String _UID) {
        this.id = id;
        this._Name = _Name;
        this._IpAddress = _IpAddress;
        this._UID = _UID;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getUserDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String get_Name() {
        return _Name;
    }

    public void set_Name(String _Name) {
        this._Name = _Name;
    }

    public String get_IpAddress() {
        return _IpAddress;
    }

    public void set_IpAddress(String _IpAddress) {
        this._IpAddress = _IpAddress;
    }

    /**
     * Not-null value.
     */
    public String get_UID() {
        return _UID;
    }

    /**
     * Not-null value; ensure this value is available before it is saved to the database.
     */
    public void set_UID(String _UID) {
        this._UID = _UID;
    }

    /**
     * Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context.
     */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context.
     */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context.
     */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    // KEEP METHODS - put your custom methods here

//    public User(Parcel parcel) {
//        this.id = parcel.readLong();
//        this._Name = parcel.readString();
//        this._IpAddress = parcel.readString();
//        this._UID = parcel.readString();
//    }
//
//    public void copyInfo(User srcUser) {
//        this._IpAddress = srcUser._IpAddress;
//        this._Name = srcUser._Name;
//    }
//
//    public final static Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
//
//        @Override
//        public User createFromParcel(Parcel parcel) {
//            return new User(parcel);
//        }
//
//        @Override
//        public User[] newArray(int size) {
//            return new User[size];
//        }
//    };
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeLong(id);
//        dest.writeString(_Name);
//        dest.writeString(_IpAddress);
//        dest.writeString(_UID);
//    }

    public int getUserStatus() {
        return mUserExtraContent.getStatus();
    }

    public void setUserStatus(int s) {
        mUserExtraContent.setStatus(s);
    }

    public static String coverStatus2String(int status) {
        String ret = "unkown";
        switch (status) {
            case USER_STATUS_ONLINE:
                ret = "Online";
                break;
            case USER_STATUS_OFFLINE:
                ret = "Offline";
                break;
            default:
                break;
        }
        return ret;
    }

    @Override
    public String toString() {
        return "Name:" + _Name + " ip:" + _IpAddress + " uid:" + _UID + " status:" + coverStatus2String(getUserStatus());
    }
    // KEEP METHODS END

}
