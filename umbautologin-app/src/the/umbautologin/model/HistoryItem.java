package the.umbautologin.model;

import java.util.Date;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class HistoryItem {
    private int id;
    private Date date;
    private boolean success;
    private String message;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
