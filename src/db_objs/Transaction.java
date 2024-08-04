package db_objs;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

/*
    transaction entity used to store transaction data
 */
public class Transaction {
    private final int userId;
    private final  String transactionType;
    private final BigDecimal transactionAmount;
    private final Date transactionDate;
    //private final Time transactionTime;

    public Transaction(int userId, String transactionType, BigDecimal transactionAmount, Date transactionDate){
        this.userId=userId;
        this.transactionType=transactionType;
        this.transactionAmount=transactionAmount;
        this.transactionDate=transactionDate;
       // this.transactionTime=transactionTime;
    }

    public int getUserId() {
        return userId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

}
